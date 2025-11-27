package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Despacho extends AppCompatActivity {

    private static final String TAG = "DespachoActivity";

    // Vistas
    private Spinner spMarca, spTipo, spTalla, spNombre;
    private EditText etCantidad;
    private Button btnAgregar, btnConfirmar;
    private RecyclerView rvProductos;
    private LinearLayout layoutEmpty;
    private TextView txtTituloProductos;

    // Firebase
    private FirebaseFirestore db;

    // Datos para Spinners de Filtro
    private List<String> listaMarcas = new ArrayList<>();
    private List<String> listaTipos = new ArrayList<>();
    private List<String> listaTallas = new ArrayList<>();

    // Datos del Producto Seleccionado Actualmente
    private List<Producto> productosEncontrados = new ArrayList<>(); // Objetos completos
    private List<String> nombresProductos = new ArrayList<>(); // Solo strings para el spinner
    private Producto productoSeleccionadoActual = null;

    // Carrito de Compras
    private List<ItemCarrito> listaCarrito = new ArrayList<>();
    private CarritoAdapter adapterCarrito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_despacho);

        db = FirebaseFirestore.getInstance();

        inicializarVistas();
        configurarSpinnersFiltros();
        configurarRecyclerView();

        // Cargar catálogos (igual que en Ingreso)
        cargarCatalogos();

        // Listeners de Botones
        btnAgregar.setOnClickListener(v -> agregarAlCarrito());
        btnConfirmar.setOnClickListener(v -> irAConfirmacion());

        // Ajuste de bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarVistas() {
        spMarca = findViewById(R.id.spMarca);
        spTipo = findViewById(R.id.spTipo);
        spTalla = findViewById(R.id.spTalla);
        spNombre = findViewById(R.id.spNombre);

        // Asegúrate que este ID exista en tu XML (si no lo tienes, agrégalo como te mencioné antes)
        etCantidad = findViewById(R.id.etCantidadDespacho);

        btnAgregar = findViewById(R.id.btnAgregarProducto);
        btnConfirmar = findViewById(R.id.btnConfirmarOrden);

        rvProductos = findViewById(R.id.rvProductos);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        txtTituloProductos = findViewById(R.id.txtTituloProductos);
    }

    private void configurarSpinnersFiltros() {
        // Configurar adaptadores básicos
        configurarAdapterSpinner(spMarca, listaMarcas);
        configurarAdapterSpinner(spTipo, listaTipos);
        configurarAdapterSpinner(spTalla, listaTallas);
        configurarAdapterSpinner(spNombre, nombresProductos);

        // Listener: Cuando cambie Marca, Tipo o Talla -> Buscar productos
        AdapterView.OnItemSelectedListener listenerFiltros = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buscarProductosDisponibles();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spMarca.setOnItemSelectedListener(listenerFiltros);
        spTipo.setOnItemSelectedListener(listenerFiltros);
        spTalla.setOnItemSelectedListener(listenerFiltros);

        // Listener: Cuando selecciona un nombre específico
        spNombre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < productosEncontrados.size()) {
                    productoSeleccionadoActual = productosEncontrados.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productoSeleccionadoActual = null;
            }
        });
    }

    private void configurarAdapterSpinner(Spinner spinner, List<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lista);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // --- BÚSQUEDA DE PRODUCTOS EN FIREBASE ---
    // Reemplaza el método buscarProductosDisponibles() con este bloque:

    private void buscarProductosDisponibles() {
        // 1. Verificar que los Spinners tengan datos seleccionados
        if (spMarca.getSelectedItem() == null || spTipo.getSelectedItem() == null || spTalla.getSelectedItem() == null) {
            Log.d(TAG, "Filtros incompletos. Esperando selección del usuario...");
            return;
        }

        String marcaSeleccionada = spMarca.getSelectedItem().toString();
        String tipoSeleccionado = spTipo.getSelectedItem().toString();
        String tallaSeleccionada = spTalla.getSelectedItem().toString();

        Log.d(TAG, "Buscando productos con -> Marca: " + marcaSeleccionada + " | Tipo: " + tipoSeleccionado + " | Talla: " + tallaSeleccionada);

        // 2. Limpiar lista actual visualmente para evitar confusiones
        nombresProductos.clear();
        productosEncontrados.clear();
        ArrayAdapter<String> adapterVacio = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresProductos);
        adapterVacio.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNombre.setAdapter(adapterVacio);

        // 3. Realizar la consulta a Firestore
        db.collection("productos")
                .whereEqualTo("marca", marcaSeleccionada)
                .whereEqualTo("tipo", tipoSeleccionado)
                .whereEqualTo("talla", tallaSeleccionada)
                .whereGreaterThan("cantidad", 0) // Solo mostrar si hay stock
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "¡Éxito! Se encontraron " + queryDocumentSnapshots.size() + " productos.");

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Producto p = doc.toObject(Producto.class);
                                if (p != null) {
                                    p.setId(doc.getId()); // Guardar el ID del documento
                                    productosEncontrados.add(p);
                                    // Mostramos Nombre y Stock disponible en el Spinner
                                    nombresProductos.add(p.getNombre() + " (Stock: " + p.getCantidad() + ")");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al convertir documento: " + doc.getId(), e);
                            }
                        }

                        // 4. Actualizar el Spinner de Nombres
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresProductos);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spNombre.setAdapter(adapter);

                        // Seleccionar el primero por defecto
                        if (!productosEncontrados.isEmpty()) {
                            spNombre.setSelection(0);
                            productoSeleccionadoActual = productosEncontrados.get(0);
                        }

                    } else {
                        Log.d(TAG, "Consulta exitosa pero SIN resultados.");
                        Toast.makeText(Despacho.this, "No hay stock para " + marcaSeleccionada + " " + tipoSeleccionado + " " + tallaSeleccionada, Toast.LENGTH_SHORT).show();
                        productoSeleccionadoActual = null;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en la consulta Firestore", e);

                    // Este mensaje es CLAVE si falta el índice
                    if (e.getMessage() != null && e.getMessage().contains("index")) {
                        Toast.makeText(Despacho.this, "Falta crear índice en Firebase. Revisa el Logcat.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ENLACE PARA CREAR ÍNDICE: " + e.getMessage());
                    } else {
                        Toast.makeText(Despacho.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // --- LOGICA DEL CARRITO ---
    private void agregarAlCarrito() {
        if (productoSeleccionadoActual == null) {
            Toast.makeText(this, "Selecciona un producto válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String cantStr = etCantidad.getText().toString().trim();
        if (cantStr.isEmpty()) {
            etCantidad.setError("Ingresa cantidad");
            return;
        }

        int cantidadSolicitada;
        try {
            cantidadSolicitada = Integer.parseInt(cantStr);
        } catch (NumberFormatException e) {
            etCantidad.setError("Número inválido");
            return;
        }

        // 1. Validar Stock
        if (cantidadSolicitada > productoSeleccionadoActual.getCantidad()) {
            Toast.makeText(this, "Stock insuficiente. Solo hay " + productoSeleccionadoActual.getCantidad(), Toast.LENGTH_LONG).show();
            return;
        }

        if (cantidadSolicitada <= 0) {
            Toast.makeText(this, "Cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Crear el ItemCarrito (Clase interna definida abajo)
        ItemCarrito item = new ItemCarrito(
                productoSeleccionadoActual.getId(),
                productoSeleccionadoActual.getNombre(),
                productoSeleccionadoActual.getMarca(),
                productoSeleccionadoActual.getTalla(),
                cantidadSolicitada
        );

        listaCarrito.add(item);
        actualizarVistaCarrito();

        Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
        etCantidad.setText("");
    }

    private void configurarRecyclerView() {
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        // Inicializamos el Adapter con la lista y el listener para eliminar
        adapterCarrito = new CarritoAdapter(listaCarrito, position -> {
            listaCarrito.remove(position);
            actualizarVistaCarrito();
        });
        rvProductos.setAdapter(adapterCarrito);
    }

    private void actualizarVistaCarrito() {
        adapterCarrito.notifyDataSetChanged();

        if (listaCarrito.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvProductos.setVisibility(View.GONE);
            txtTituloProductos.setText("Productos agregados");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvProductos.setVisibility(View.VISIBLE);
            txtTituloProductos.setText("Productos (" + listaCarrito.size() + ")");
        }
    }

    private void irAConfirmacion() {
        if (listaCarrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegúrate de que la clase 'DatosCliente' (o ConfirmarDespacho) exista
        Intent intent = new Intent(Despacho.this, DatosCliente.class);
        intent.putExtra("lista_carrito", (Serializable) listaCarrito);
        startActivity(intent);
    }

    // --- CARGA DE DATOS INICIALES ---
    private void cargarCatalogos() {
        cargarListaFirebase("marcas", listaMarcas, spMarca);
        cargarListaFirebase("tipos", listaTipos, spTipo);
        cargarListaFirebase("tallas", listaTallas, spTalla);
    }

    private void cargarListaFirebase(String doc, List<String> lista, Spinner spinner) {
        db.collection("configuracion").document(doc).get().addOnSuccessListener(s -> {
            if (s.exists()) {
                List<String> datos = (List<String>) s.get("lista");
                if (datos != null) {
                    lista.clear();
                    lista.addAll(datos);
                    ((ArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }

    // ==========================================
    // CLASE INTERNA ItemCarrito
    // ==========================================
    public static class ItemCarrito implements Serializable {
        public String idProducto;
        public String nombre;
        public String marca;
        public String talla;
        public int cantidad;

        // Constructor vacío requerido para serialización
        public ItemCarrito() {}

        public ItemCarrito(String id, String n, String m, String t, int c) {
            this.idProducto = id;
            this.nombre = n;
            this.marca = m;
            this.talla = t;
            this.cantidad = c;
        }
    }

    public static class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

        private List<ItemCarrito> listaItems;
        private OnItemDeleteListener deleteListener;

        // Interfaz para comunicar el clic de eliminación hacia Despacho.java
        public interface OnItemDeleteListener {
            void onDelete(int position);
        }

        // Constructor que coincide con tu llamada en Despacho.java
        public CarritoAdapter(List<ItemCarrito> listaItems, OnItemDeleteListener deleteListener) {
            this.listaItems = listaItems;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflamos el diseño de cada fila
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
            return new CarritoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
            ItemCarrito item = listaItems.get(position);

            holder.txtNombre.setText(item.nombre);
            holder.txtDetalle.setText(item.marca + " - " + item.talla);
            holder.txtCantidad.setText("x" + item.cantidad);

            // Configurar botón eliminar
            holder.btnEliminar.setOnClickListener(v -> {
                if (deleteListener != null) {
                    // getAdapterPosition() es más seguro que usar 'position' directamente
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        deleteListener.onDelete(pos);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return listaItems.size();
        }

        // Clase interna ViewHolder para referencias las vistas
        static class CarritoViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtDetalle, txtCantidad;
            ImageButton btnEliminar;

            public CarritoViewHolder(@NonNull View itemView) {
                super(itemView);
                // Asegúrate que estos IDs existan en layout/item_carrito.xml
                txtNombre = itemView.findViewById(R.id.txtNombreItem);
                txtDetalle = itemView.findViewById(R.id.txtDetalleItem);
                txtCantidad = itemView.findViewById(R.id.txtCantidadItem);
                btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
            }
        }
    }
}
