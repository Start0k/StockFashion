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

// Importante: Asegúrate de que ItemCarrito está en el mismo paquete o impórtalo

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
    private List<Producto> productosEncontrados = new ArrayList<>(); // Objetos completos (STOCK)
    private List<String> nombresProductos = new ArrayList<>(); // Strings para mostrar en spinner
    private Producto productoSeleccionadoActual = null;

    // Carrito de Compras
    // IMPORTANTE: Usamos la clase ItemCarrito externa, no una interna
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

        // Cargar catálogos desde Firebase
        cargarCatalogos();

        // Listeners de Botones
        btnAgregar.setOnClickListener(v -> agregarAlCarrito());
        btnConfirmar.setOnClickListener(v -> irAConfirmacion());

        // Ajuste de bordes (EdgeToEdge)
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

        etCantidad = findViewById(R.id.etCantidadDespacho); // Asegúrate que este ID exista en el XML

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

        // Listener: Cuando selecciona un nombre específico del resultado
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
    private void buscarProductosDisponibles() {
        // 1. Verificar que los Spinners tengan datos seleccionados
        if (spMarca.getSelectedItem() == null || spTipo.getSelectedItem() == null || spTalla.getSelectedItem() == null) {
            return;
        }

        String marcaSeleccionada = spMarca.getSelectedItem().toString();
        String tipoSeleccionado = spTipo.getSelectedItem().toString();
        String tallaSeleccionada = spTalla.getSelectedItem().toString();

        // 2. Limpiar lista actual visualmente
        nombresProductos.clear();
        productosEncontrados.clear();
        ((ArrayAdapter) spNombre.getAdapter()).notifyDataSetChanged();

        // 3. Realizar la consulta a Firestore
        db.collection("productos")
                .whereEqualTo("marca", marcaSeleccionada)
                .whereEqualTo("tipo", tipoSeleccionado)
                .whereEqualTo("talla", tallaSeleccionada)
                .whereGreaterThan("cantidad", 0) // Solo mostrar si hay stock > 0
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Producto p = doc.toObject(Producto.class);
                                if (p != null) {
                                    p.setId(doc.getId()); // Importante: Guardar ID
                                    productosEncontrados.add(p);
                                    nombresProductos.add(p.getNombre() + " (Stock: " + p.getCantidad() + ")");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al convertir documento: " + doc.getId(), e);
                            }
                        }

                        // 4. Actualizar el Spinner de Nombres
                        ((ArrayAdapter) spNombre.getAdapter()).notifyDataSetChanged();

                        if (!productosEncontrados.isEmpty()) {
                            spNombre.setSelection(0);
                            productoSeleccionadoActual = productosEncontrados.get(0);
                        } else {
                            productoSeleccionadoActual = null;
                        }

                    } else {
                        Toast.makeText(Despacho.this, "No hay stock disponible", Toast.LENGTH_SHORT).show();
                        productoSeleccionadoActual = null;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error en la consulta Firestore", e);
                    if (e.getMessage() != null && e.getMessage().contains("index")) {
                        Toast.makeText(Despacho.this, "Falta índice. Revisa el Logcat.", Toast.LENGTH_LONG).show();
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

        // 1. Validar Stock disponible
        if (cantidadSolicitada > productoSeleccionadoActual.getCantidad()) {
            Toast.makeText(this, "Stock insuficiente. Solo hay " + productoSeleccionadoActual.getCantidad(), Toast.LENGTH_LONG).show();
            return;
        }

        if (cantidadSolicitada <= 0) {
            Toast.makeText(this, "Cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Crear el ItemCarrito (Usando la clase externa nueva)
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
        // Inicializamos el adapter pasando la lista y un listener para borrar
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

    // --- MÉTODO PARA IR A LA CONFIRMACIÓN (SIN GUARDAR TODAVÍA) ---
    private void irAConfirmacion() {
        // 1. Validaciones
        if (listaCarrito.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Crear Intent hacia DatosCliente
        Intent intent = new Intent(Despacho.this, DatosCliente.class);

        // 3. Enviar la lista serializada
        // La clase ItemCarrito (externa) implementa Serializable
        intent.putExtra("lista_carrito", (Serializable) listaCarrito);

        // 4. Iniciar la actividad
        startActivity(intent);
    }

    // --- CARGA DE DATOS INICIALES (SPINNERS) ---
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
    // CLASE INTERNA PARA EL ADAPTER DEL RECYCLERVIEW
    // ==========================================
    public static class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

        private List<ItemCarrito> listaItems;
        private OnItemDeleteListener deleteListener;

        public interface OnItemDeleteListener {
            void onDelete(int position);
        }

        public CarritoAdapter(List<ItemCarrito> listaItems, OnItemDeleteListener deleteListener) {
            this.listaItems = listaItems;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
            return new CarritoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
            ItemCarrito item = listaItems.get(position);

            // Usamos los getters de la clase ItemCarrito
            holder.txtNombre.setText(item.getNombre());
            holder.txtDetalle.setText(item.getMarca() + " - " + item.getTalla());
            holder.txtCantidad.setText("x" + item.getCantidad());

            holder.btnEliminar.setOnClickListener(v -> {
                if (deleteListener != null) {
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

        class CarritoViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtDetalle, txtCantidad;
            ImageButton btnEliminar;

            public CarritoViewHolder(@NonNull View itemView) {
                super(itemView);
                txtNombre = itemView.findViewById(R.id.txtNombreItem);
                txtDetalle = itemView.findViewById(R.id.txtDetalleItem);
                txtCantidad = itemView.findViewById(R.id.txtCantidadItem);
                btnEliminar = itemView.findViewById(R.id.btnEliminarItem);
            }
        }
    }
}
