package com.example.stockfashion;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class verStock extends AppCompatActivity implements GestionProductoAdapter.OnProductoInteractionListener {

    private static final String TAG = "verStockActivity";
    private RecyclerView recyclerViewStock;
    private GestionProductoAdapter adapter;
    private List<Producto> listaProductos;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ver_stock);

        db = FirebaseFirestore.getInstance();


        recyclerViewStock = findViewById(R.id.tblVerStock);
        recyclerViewStock.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        adapter = new GestionProductoAdapter(listaProductos, this);
        recyclerViewStock.setAdapter(adapter);

        cargarProductos();
    }

    private void cargarProductos() {
        db.collection("productos")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al escuchar cambios.", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Producto producto = dc.getDocument().toObject(Producto.class);
                        producto.setId(dc.getDocument().getId());

                        int index = getProductIndexById(producto.getId());

                        switch (dc.getType()) {
                            case ADDED:
                                if (index == -1) listaProductos.add(producto);
                                break;
                            case MODIFIED:
                                if (index != -1) listaProductos.set(index, producto);
                                break;
                            case REMOVED:
                                if (index != -1) listaProductos.remove(index);
                                break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private int getProductIndexById(String id) {
        for (int i = 0; i < listaProductos.size(); i++) {
            if (listaProductos.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onModificarProducto(Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar Cantidad");
        builder.setMessage("Producto: " + producto.getNombre());


        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nueva cantidad");
        input.setText(String.valueOf(producto.getCantidad())); // Muestra la cantidad actual
        builder.setView(input);


        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevaCantidadStr = input.getText().toString();
            if (!nuevaCantidadStr.isEmpty()) {
                try {
                    long nuevaCantidad = Long.parseLong(nuevaCantidadStr);

                    db.collection("productos").document(producto.getId())
                            .update("cantidad", nuevaCantidad)
                            .addOnSuccessListener(aVoid -> Toast.makeText(verStock.this, "Cantidad actualizada", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(verStock.this, "Error al actualizar", Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Por favor, ingrese un número válido", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onEliminarProducto(Producto producto, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Producto")
                .setMessage("¿Estás seguro de que quieres eliminar '" + producto.getNombre() + "'? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Eliminar", (dialog, which) -> {

                    db.collection("productos").document(producto.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(verStock.this, "Producto eliminado", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> Toast.makeText(verStock.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }
}
