package com.HanDav.stockfashion;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Ingreso extends AppCompatActivity {

    private static final String TAG = "IngresoActivity";

    // CAMBIO 1: Definimos los Spinners en lugar de EditText para estos campos
    private Spinner spMarca, spTipo, spTalla;

    // Estos se mantienen como EditText
    private EditText etNombre, etCantidad;

    private Button btnAgregar;

    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> listaProductos;

    // CAMBIO 2: Variables para manejar los datos de los Spinners
    private List<String> listaMarcas, listaTipos, listaTallas;
    private ArrayAdapter<String> adapterMarca, adapterTipo, adapterTalla;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingreso);

        db = FirebaseFirestore.getInstance();

        // Inicializar Vistas (Asegúrate que los IDs coincidan con tu nuevo XML)
        etNombre = findViewById(R.id.etNombre);
        etCantidad = findViewById(R.id.etCantidad);

        spMarca = findViewById(R.id.spinnerMarca);
        spTipo = findViewById(R.id.spinnerTipo);
        spTalla = findViewById(R.id.spinnerTalla);

        btnAgregar = findViewById(R.id.btnAgregar);

        // CAMBIO 3: Configurar los Adaptadores para los Spinners
        configurarSpinners();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.tblIngresoProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(listaProductos);
        recyclerView.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> crearProducto());

        // Cargar datos
        escucharCambiosProductos(); // Lista de productos abajo
        cargarDatosSpinners();      // Opciones de los spinners

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método auxiliar para inicializar listas y adaptadores vacíos
    private void configurarSpinners() {
        // Inicializar listas
        listaMarcas = new ArrayList<>();
        listaTipos = new ArrayList<>();
        listaTallas = new ArrayList<>();

        // Configurar adaptadores (diseño simple de Android)
        adapterMarca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaMarcas);
        adapterMarca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMarca.setAdapter(adapterMarca);

        adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapterTipo);

        adapterTalla = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTallas);
        adapterTalla.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTalla.setAdapter(adapterTalla);
    }

    // CAMBIO 4: Método para traer las listas desde Firebase
    // Asume que tienes una colección "configuracion" con documentos: "marcas", "tipos", "tallas"
    // y dentro de cada documento un campo array llamado "lista"
    private void cargarDatosSpinners() {
        cargarListaDesdeFirebase("marcas", listaMarcas, adapterMarca);
        cargarListaDesdeFirebase("tipos", listaTipos, adapterTipo);
        cargarListaDesdeFirebase("tallas", listaTallas, adapterTalla);
    }

    private void cargarListaDesdeFirebase(String documentoId, List<String> listaLocal, ArrayAdapter<String> adapterLocal) {
        db.collection("configuracion").document(documentoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // "lista" es el nombre del campo Array en Firebase
                        List<String> datos = (List<String>) documentSnapshot.get("lista");
                        if (datos != null) {
                            listaLocal.clear();
                            listaLocal.addAll(datos);
                            adapterLocal.notifyDataSetChanged();
                        }
                    } else {
                        Log.w(TAG, "No se encontró el documento de configuración: " + documentoId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error cargando spinner: " + documentoId, e));
    }

    private void escucharCambiosProductos() {
        db.collection("productos")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar cambios en Firestore.", e);
                        return;
                    }

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Producto producto = dc.getDocument().toObject(Producto.class);
                            producto.setId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    listaProductos.add(producto);
                                    break;
                                case MODIFIED:
                                    for (int i = 0; i < listaProductos.size(); i++) {
                                        if (listaProductos.get(i).getId().equals(producto.getId())) {
                                            listaProductos.set(i, producto);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    for (int i = 0; i < listaProductos.size(); i++) {
                                        if (listaProductos.get(i).getId().equals(producto.getId())) {
                                            listaProductos.remove(i);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void crearProducto() {
        String nombre = etNombre.getText().toString().trim();

        // Obtener valor seleccionado del Spinner
        String marca = (spMarca.getSelectedItem() != null) ? spMarca.getSelectedItem().toString() : "";
        String tipo = (spTipo.getSelectedItem() != null) ? spTipo.getSelectedItem().toString() : "";
        String talla = (spTalla.getSelectedItem() != null) ? spTalla.getSelectedItem().toString() : "";

        String cantidadStr = etCantidad.getText().toString().trim();

        if (nombre.isEmpty() || marca.isEmpty() || tipo.isEmpty() || talla.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Long cantidadIngresada;
        try {
            cantidadIngresada = Long.parseLong(cantidadStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- LÓGICA DE VERIFICACIÓN DE DUPLICADOS ---

        // 1. Buscamos si existe un producto idéntico
        db.collection("productos")
                .whereEqualTo("nombre", nombre)
                .whereEqualTo("marca", marca)
                .whereEqualTo("tipo", tipo)
                .whereEqualTo("talla", talla)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // CASO A: YA EXISTE EL PRODUCTO
                        // Obtenemos el primer documento que coincida (debería ser único)
                        var documentoExistente = queryDocumentSnapshots.getDocuments().get(0);
                        String idDocumento = documentoExistente.getId();

                        // Obtenemos la cantidad actual de la base de datos (cuidando nulos)
                        Long cantidadActual = documentoExistente.getLong("cantidad");
                        if (cantidadActual == null) cantidadActual = 0L;

                        // Sumamos
                        Long nuevaCantidadTotal = cantidadActual + cantidadIngresada;

                        // Actualizamos solo el campo cantidad
                        db.collection("productos").document(idDocumento)
                                .update("cantidad", nuevaCantidadTotal)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Ingreso.this, "Stock actualizado: " + nuevaCantidadTotal, Toast.LENGTH_SHORT).show();
                                    limpiarCampos();
                                })
                                .addOnFailureListener(e -> Toast.makeText(Ingreso.this, "Error al actualizar stock", Toast.LENGTH_SHORT).show());

                    } else {
                        // CASO B: NO EXISTE, ES NUEVO
                        Producto nuevoProducto = new Producto(nombre, marca, tipo, talla, cantidadIngresada);

                        db.collection("productos")
                                .add(nuevoProducto)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(Ingreso.this, "Producto nuevo creado", Toast.LENGTH_SHORT).show();
                                    limpiarCampos();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Ingreso.this, "Error al crear producto", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "Error al crear", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar duplicados", e);
                    Toast.makeText(this, "Error de conexión al verificar", Toast.LENGTH_SHORT).show();
                });
    }


    private void limpiarCampos() {
        etNombre.setText("");
        etCantidad.setText("");

        // Resetear Spinners a la primera posición si hay elementos
        if (spMarca.getAdapter().getCount() > 0) spMarca.setSelection(0);
        if (spTipo.getAdapter().getCount() > 0) spTipo.setSelection(0);
        if (spTalla.getAdapter().getCount() > 0) spTalla.setSelection(0);

        etNombre.requestFocus();
    }
}
