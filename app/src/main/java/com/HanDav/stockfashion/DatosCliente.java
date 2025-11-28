package com.HanDav.stockfashion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

// Importamos la clase ItemCarrito (si está en el mismo paquete, esto es automático, si no, asegúrate de esta línea)
import com.HanDav.stockfashion.ItemCarrito;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatosCliente extends AppCompatActivity {

    private static final String TAG = "DatosCliente";

    // Vistas
    private EditText etNombre, etRut, etTelefono, etEmail, etDireccion;
    private Spinner spRegion, spComuna;
    private TextView txtCantidadProductos;
    private Button btnFinalizarOrden;
    private android.widget.LinearLayout btnVolver; // Cambiamos Button por LinearLayout

    // Firebase
    private FirebaseFirestore db;

    // Datos recibidos del carrito
    // CORRECCIÓN 1: Quitamos "Despacho." porque ahora es un archivo aparte
    private ArrayList<ItemCarrito> listaProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_datos_cliente);

        // Configuración de UI para bordes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_datos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // 1. Recibir lista de productos
        // CORRECCIÓN 2: Casteo correcto a la clase externa
        listaProductos = (ArrayList<ItemCarrito>) getIntent().getSerializableExtra("lista_carrito");

        if (listaProductos == null || listaProductos.isEmpty()) {
            Toast.makeText(this, "Error: No llegaron productos al despacho", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        inicializarVistas();
        configurarSpinners();
        mostrarResumen();

        // Listeners
        btnFinalizarOrden.setOnClickListener(v -> procesarOrden());
        btnVolver.setOnClickListener(v -> finish());
    }

    @SuppressLint("WrongViewCast")
    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombreCliente);
        etRut = findViewById(R.id.etRutCliente);
        etTelefono = findViewById(R.id.etTelefonoCliente);
        etEmail = findViewById(R.id.etEmailCliente);
        etDireccion = findViewById(R.id.etDireccionCliente);

        spRegion = findViewById(R.id.spRegion);
        spComuna = findViewById(R.id.spComuna);

        txtCantidadProductos = findViewById(R.id.txtCantidadProductos);

        btnFinalizarOrden = findViewById(R.id.btnFinalizarVenta);
        btnVolver = findViewById(R.id.btnVolver);
    }

    private void configurarSpinners() {
        String[] regiones = {"Metropolitana", "Valparaíso", "Biobío", "Araucanía", "Los Lagos", "Otra"};
        ArrayAdapter<String> adapterReg = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regiones);
        adapterReg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegion.setAdapter(adapterReg);

        String[] comunas = {"Santiago", "Providencia", "Maipú", "Viña del Mar", "Concepción", "Temuco", "Otra"};
        ArrayAdapter<String> adapterCom = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, comunas);
        adapterCom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComuna.setAdapter(adapterCom);
    }

    private void mostrarResumen() {
        int totalItems = 0;
        // CORRECCIÓN 3: Usamos ItemCarrito y getters
        for (ItemCarrito item : listaProductos) {
            totalItems += item.getCantidad(); // Usar getCantidad() en lugar de .cantidad
        }
        txtCantidadProductos.setText(String.valueOf(totalItems));
    }

    private void procesarOrden() {
        String nombre = etNombre.getText().toString().trim();
        String rut = etRut.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        String region = (spRegion.getSelectedItem() != null) ? spRegion.getSelectedItem().toString() : "";
        String comuna = (spComuna.getSelectedItem() != null) ? spComuna.getSelectedItem().toString() : "";

        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Requerido"); return; }
        if (TextUtils.isEmpty(rut)) { etRut.setError("Requerido"); return; }
        if (TextUtils.isEmpty(direccion)) { etDireccion.setError("Requerido"); return; }
        if (TextUtils.isEmpty(telefono)) { etTelefono.setError("Requerido"); return; }

        int totalPrendas = 0;
        for (ItemCarrito item : listaProductos) {
            totalPrendas += item.getCantidad(); // Usar getCantidad()
        }

        Map<String, Object> orden = new HashMap<>();
        orden.put("fecha", new Timestamp(new Date()));
        orden.put("estado", "Despachado");
        orden.put("total_prendas", totalPrendas);

        orden.put("cliente_nombre", nombre);
        orden.put("cliente_rut", rut);
        orden.put("cliente_telefono", telefono);
        orden.put("cliente_email", email);
        orden.put("cliente_direccion", direccion);
        orden.put("cliente_region", region);
        orden.put("cliente_comuna", comuna);

        orden.put("items", listaProductos);

        guardarYDescontarStock(orden);
    }

    private void guardarYDescontarStock(Map<String, Object> orden) {
        WriteBatch batch = db.batch();

        DocumentReference refNuevaOrden = db.collection("ordenes_despacho").document();
        batch.set(refNuevaOrden, orden);

        // CORRECCIÓN 4: Usamos ItemCarrito y getters
        for (ItemCarrito item : listaProductos) {
            DocumentReference refProducto = db.collection("productos").document(item.getIdProducto()); // Usar getIdProducto()

            // Restar cantidad del stock
            batch.update(refProducto, "cantidad", FieldValue.increment(-item.getCantidad())); // Usar getCantidad()
        }

        btnFinalizarOrden.setEnabled(false);
        btnFinalizarOrden.setText("Procesando...");

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DatosCliente.this, "¡Despacho exitoso!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(DatosCliente.this, menu.class);

                    // AGREGA ESTA LÍNEA: Le decimos al menú que cargue como Administrador
                    intent.putExtra("ROL_USUARIO", "administrador");

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnFinalizarOrden.setEnabled(true);
                    btnFinalizarOrden.setText("Confirmar Despacho");
                    Toast.makeText(DatosCliente.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error en batch", e);
                });
    }
}
