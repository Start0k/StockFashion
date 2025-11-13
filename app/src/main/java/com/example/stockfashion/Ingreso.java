package com.example.stockfashion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingreso);

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

        // Configurar el listener del botón
        btnAgregar.setOnClickListener(v -> crearProducto());

        // Cargar los datos iniciales y escuchar cambios en tiempo real
        escucharCambiosProductos();

        // Código de insets (sin cambios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void escucharCambiosProductos() {
        db.collection("productos")
                .orderBy("nombre", Query.Direction.ASCENDING) // Opcional: ordenar por nombre
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar cambios.", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                // Convertir el documento a un objeto Producto
                                Producto producto = dc.getDocument().toObject(Producto.class);
                                producto.setId(dc.getDocument().getId()); // Guardar el ID
                                listaProductos.add(producto);
                                break;
                            // Puedes manejar MODIFIED y REMOVED si necesitas actualizar/eliminar en tiempo real
                        }
                    }
                    // Notificar al adaptador que los datos han cambiado
                    adapter.notifyDataSetChanged();
                });
    }


    private void crearProducto() {
        String nombre = etNombre.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String tipo = etTipo.getText().toString().trim();
        String talla = etTalla.getText().toString().trim();
        String cantidadStr = etCantidad.getText().toString().trim();

        if (nombre.isEmpty() || marca.isEmpty() || tipo.isEmpty() || talla.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("marca", marca);
        producto.put("tipo", tipo);
        producto.put("talla", talla);

        try {
            producto.put("cantidad", Long.parseLong(cantidadStr)); // Usar Long para consistencia
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("productos")
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Ingreso.this, "Producto guardado", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                    // No necesitas añadirlo a la lista manualmente, el listener 'escucharCambiosProductos' lo hará.
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Ingreso.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error al crear el producto", e);
                });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etMarca.setText("");
        etTipo.setText("");
        etTalla.setText("");
        etCantidad.setText("");
        etNombre.requestFocus();
    }
}
