package com.example.stockfashion;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ingreso extends AppCompatActivity {

    private static final String TAG = "IngresoActivity";

    // Vistas del formulario
    private EditText etNombre, etMarca, etTipo, etTalla, etCantidad;
    private Button btnAgregar;

    // Componentes del RecyclerView
    private RecyclerView recyclerView;
    private ProductoAdapter adapter;
    private List<Producto> listaProductos;

    // Instancia de Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingreso);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Enlazar vistas del formulario (ajustado a tu XML)
        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etTipo = findViewById(R.id.etTipo);
        etTalla = findViewById(R.id.etTalla);
        etCantidad = findViewById(R.id.etCantidad);
        btnAgregar = findViewById(R.id.btnAgregar); // Tu botón se llama btnAgregar

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.tblIngresoProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(listaProductos);
        recyclerView.setAdapter(adapter);

        // Configurar el listener del botón para llamar a crearProducto
        btnAgregar.setOnClickListener(v -> crearProducto());

        // Cargar los datos iniciales y escuchar cambios en tiempo real desde Firestore
        escucharCambiosProductos();

        // Código para manejar los insets de la pantalla (sin cambios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Se conecta a la colección "productos" de Firestore y escucha cambios en tiempo real.
     * Añade nuevos productos a la lista y notifica al adaptador para refrescar la UI.
     */
    private void escucharCambiosProductos() {
        db.collection("productos") // Usa el nombre de colección en MINÚSCULA
                .orderBy("nombre", Query.Direction.ASCENDING) // Opcional: ordenar alfabéticamente por nombre
                .addSnapshotListener((snapshots, e) -> {
                    // Manejo de errores
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar cambios en Firestore.", e);
                        Toast.makeText(this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Procesar los cambios en los documentos
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            // Convierte el documento a un objeto Producto
                            Producto producto = dc.getDocument().toObject(Producto.class);
                            producto.setId(dc.getDocument().getId()); // Asigna manualmente el ID del documento
                            listaProductos.add(producto); // Añade el nuevo producto a la lista
                        }
                        // Aquí podrías manejar también DocumentChange.Type.MODIFIED y .REMOVED si quieres
                        // que la lista se actualice cuando un producto se edita o elimina.
                    }

                    // Notifica al adaptador que el conjunto de datos ha cambiado, para que actualice la vista
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Recoge los datos del formulario, los valida y los guarda como un nuevo
     * documento en la colección "productos" de Firestore.
     */
    private void crearProducto() {
        String nombre = etNombre.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String talla = etTalla.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();

        // Validación para asegurarse de que ningún campo esté vacío
        if (nombre.isEmpty() || marca.isEmpty() || tipo.isEmpty() || talla.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un mapa para subir los datos a Firestore
        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("marca", marca);
        producto.put("tipo", tipo);
        producto.put("talla", talla);

        // Intentar convertir la cantidad a Long (más seguro y compatible con Firestore)
        try {
            producto.put("cantidad", Long.parseLong(cantidadStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Añadir el nuevo documento a la colección "productos"
        db.collection("productos") // Usa el nombre de colección en MINÚSCULA
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Ingreso.this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    // No es necesario añadir a la lista manualmente, el listener 'escucharCambiosProductos' lo hará.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Ingreso.this, "Error al guardar el producto", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error al crear el producto en Firestore", e);
                });
    }

    /**
     * Limpia todos los campos de texto del formulario y pone el foco en el primero.
     */
    private void limpiarCampos() {
        etNombre.setText("");
        etMarca.setText("");
        etTipo.setText("");
        etTalla.setText("");
        etCantidad.setText("");
        etNombre.requestFocus(); // Pone el cursor en el campo "Nombre" para el siguiente ingreso
    }
}
