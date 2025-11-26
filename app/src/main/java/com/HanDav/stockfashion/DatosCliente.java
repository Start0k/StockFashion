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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DatosCliente extends AppCompatActivity {

    private static final String TAG = "DatosCliente";

    // Vistas
    private EditText etNombre, etRut, etTelefono, etEmail, etDireccion;
    private Spinner spRegion, spComuna; // Eliminado spMetodoPago
    private TextView txtCantidadProductos;
    private Button btnFinalizarOrden, btnVolver;

    // Firebase
    private FirebaseFirestore db;

    // Datos recibidos del carrito
    private ArrayList<Despacho.ItemCarrito> listaProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_datos_cliente);

        // Configuración de UI para bordes (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_datos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // 1. Recibir lista de productos de la actividad anterior
        listaProductos = (ArrayList<Despacho.ItemCarrito>) getIntent().getSerializableExtra("lista_carrito");

        if (listaProductos == null || listaProductos.isEmpty()) {
            Toast.makeText(this, "Error: No llegaron productos al despacho", Toast.LENGTH_LONG).show();
            finish(); // Cierra la pantalla si no hay datos
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
        // Eliminado: spMetodoPago = findViewById(R.id.spMetodoPago);

        txtCantidadProductos = findViewById(R.id.txtCantidadProductos);

        btnFinalizarOrden = findViewById(R.id.btnFinalizarVenta);

        btnVolver = findViewById(R.id.btnVolver);
    }

    private void configurarSpinners() {
        // Llenado básico de Regiones
        String[] regiones = {"Metropolitana", "Valparaíso", "Biobío", "Araucanía", "Los Lagos", "Otra"};
        ArrayAdapter<String> adapterReg = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regiones);
        adapterReg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRegion.setAdapter(adapterReg);

        // Llenado básico de Comunas
        String[] comunas = {"Santiago", "Providencia", "Maipú", "Viña del Mar", "Concepción", "Temuco", "Otra"};
        ArrayAdapter<String> adapterCom = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, comunas);
        adapterCom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComuna.setAdapter(adapterCom);

        // Eliminada la configuración del Spinner de Pago
    }

    private void mostrarResumen() {
        int totalItems = 0;
        for (Despacho.ItemCarrito item : listaProductos) {
            totalItems += item.cantidad;
        }
        txtCantidadProductos.setText(String.valueOf(totalItems));
    }

    private void procesarOrden() {
        // 1. Obtener datos de la interfaz
        String nombre = etNombre.getText().toString().trim();
        String rut = etRut.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        String region = (spRegion.getSelectedItem() != null) ? spRegion.getSelectedItem().toString() : "";
        String comuna = (spComuna.getSelectedItem() != null) ? spComuna.getSelectedItem().toString() : "";

        // Eliminada la variable 'pago'

        // 2. Validaciones Obligatorias
        if (TextUtils.isEmpty(nombre)) { etNombre.setError("Requerido"); return; }
        if (TextUtils.isEmpty(rut)) { etRut.setError("Requerido"); return; }
        if (TextUtils.isEmpty(direccion)) { etDireccion.setError("Requerido"); return; }
        if (TextUtils.isEmpty(telefono)) { etTelefono.setError("Requerido"); return; }

        // 3. Calcular Totales para guardar en la BD
        int totalPrendas = 0;
        for (Despacho.ItemCarrito item : listaProductos) {
            totalPrendas += item.cantidad;
        }

        // 4. Preparar el Mapa de Datos (Documento JSON)
        Map<String, Object> orden = new HashMap<>();

        // Datos Generales
        orden.put("fecha", new Timestamp(new Date())); // Hora del servidor
        orden.put("estado", "Despachado");
        // Eliminado: orden.put("metodo_pago", pago);
        orden.put("total_prendas", totalPrendas);

        // Datos Cliente
        orden.put("cliente_nombre", nombre);
        orden.put("cliente_rut", rut);
        orden.put("cliente_telefono", telefono);
        orden.put("cliente_email", email);
        orden.put("cliente_direccion", direccion);
        orden.put("cliente_region", region);
        orden.put("cliente_comuna", comuna);

        // Lista de Productos
        orden.put("items", listaProductos);

        // 5. Guardar en Firebase con Batch (Lote)
        guardarYDescontarStock(orden);
    }

    private void guardarYDescontarStock(Map<String, Object> orden) {
        WriteBatch batch = db.batch();

        // A. Referencia para la nueva orden
        DocumentReference refNuevaOrden = db.collection("ordenes_despacho").document();
        batch.set(refNuevaOrden, orden);

        // B. Referencias para descontar stock de cada producto
        for (Despacho.ItemCarrito item : listaProductos) {
            DocumentReference refProducto = db.collection("productos").document(item.idProducto);

            // Increment(-cantidad) es la forma segura de restar
            batch.update(refProducto, "cantidad", FieldValue.increment(-item.cantidad));
        }

        // C. Ejecutar el lote
        btnFinalizarOrden.setEnabled(false);
        btnFinalizarOrden.setText("Procesando...");

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DatosCliente.this, "¡Despacho exitoso!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(DatosCliente.this, menu.class);
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
