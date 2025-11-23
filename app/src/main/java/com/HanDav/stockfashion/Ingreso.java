package com.HanDav.stockfashion;

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


    private EditText etNombre, etMarca, etTipo, etTalla, etCantidad;
    private Button btnAgregar;


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


        etNombre = findViewById(R.id.etNombre);
        etMarca = findViewById(R.id.etMarca);
        etTipo = findViewById(R.id.etTipo);
        etTalla = findViewById(R.id.etTalla);
        etCantidad = findViewById(R.id.etCantidad);
        btnAgregar = findViewById(R.id.btnAgregar);


        recyclerView = findViewById(R.id.tblIngresoProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        adapter = new ProductoAdapter(listaProductos);
        recyclerView.setAdapter(adapter);


        btnAgregar.setOnClickListener(v -> crearProducto());


        escucharCambiosProductos();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void escucharCambiosProductos() {
        db.collection("productos")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Log.w(TAG, "Error al escuchar cambios en Firestore.", e);
                        Toast.makeText(this, "Error al cargar los productos", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            Producto producto = dc.getDocument().toObject(Producto.class);
                            producto.setId(dc.getDocument().getId());
                            listaProductos.add(producto);
                        }
                        }


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
            producto.put("cantidad", Long.parseLong(cantidadStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }


        db.collection("productos")
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Ingreso.this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
                    limpiarCampos();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Ingreso.this, "Error al guardar el producto", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error al crear el producto en Firestore", e);
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
