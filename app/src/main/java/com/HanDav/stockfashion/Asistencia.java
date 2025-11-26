package com.HanDav.stockfashion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class Asistencia extends AppCompatActivity {

    private View btnMarcarEntrada, btnMarcarSalida;
    private TextView tvFechaHoy, tvEstado;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Biometría
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asistencia);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnMarcarEntrada = findViewById(R.id.btnMarcarEntrada);
        btnMarcarSalida = findViewById(R.id.btnMarcarSalida);
        tvFechaHoy = findViewById(R.id.tvFechaHoy);
        tvEstado = findViewById(R.id.tvEstado);

        String fechaActual = new SimpleDateFormat("EEEE, d 'de' MMMM yyyy", new Locale("es", "ES")).format(new Date());
        tvFechaHoy.setText(fechaActual);

        configurarBiometria();

        // MODIFICADO: Ahora llamamos a validar antes de lanzar el biométrico
        btnMarcarEntrada.setOnClickListener(v -> validarYMarcar("ENTRADA"));
        btnMarcarSalida.setOnClickListener(v -> validarYMarcar("SALIDA"));
    }

    // --- NUEVA LÓGICA DE VALIDACIÓN ---
    private void validarYMarcar(String tipoAccion) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        tvEstado.setText("Verificando registros de hoy...");
        btnMarcarEntrada.setEnabled(false); // Evitar doble clic
        btnMarcarSalida.setEnabled(false);

        // Obtenemos la fecha de hoy en formato simple para comparar (dd-MM-yyyy)
        String fechaHoySimple = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Consultamos si ya existe un registro de este TIPO en esta FECHA
        db.collection("registro_asistencia")
                .document(user.getUid())
                .collection("historial")
                .whereEqualTo("fecha_simple", fechaHoySimple) // Filtro 1: Hoy
                .whereEqualTo("tipo", tipoAccion)             // Filtro 2: Entrada o Salida
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Reactivar botones
                    btnMarcarEntrada.setEnabled(true);
                    btnMarcarSalida.setEnabled(true);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // YA EXISTE UN REGISTRO
                        tvEstado.setText("Ya marcaste " + tipoAccion + " el día de hoy.");
                        Toast.makeText(Asistencia.this, "Error: Ya registraste " + tipoAccion + " hoy.", Toast.LENGTH_LONG).show();
                    } else {
                        // NO EXISTE -> Permiso concedido para usar huella
                        tvEstado.setText("Validación correcta. Usa tu huella.");
                        lanzarBiometrico(tipoAccion, fechaHoySimple); // Pasamos la fecha simple para guardarla
                    }
                })
                .addOnFailureListener(e -> {
                    btnMarcarEntrada.setEnabled(true);
                    btnMarcarSalida.setEnabled(true);
                    tvEstado.setText("Error al conectar con base de datos.");
                    Log.e("Asistencia", "Error validando", e);
                });
    }

    private void configurarBiometria() {
        executor = ContextCompat.getMainExecutor(this);
        // Inicializamos el prompt vacío aquí, pero la lógica real está en lanzarBiometrico
        // para poder capturar la acción específica en el callback.
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirmar Asistencia")
                .setSubtitle("Verifica tu identidad")
                .setNegativeButtonText("Cancelar")
                .build();
    }

    // Variables temporales
    private String accionPendiente = "";
    private String fechaSimplePendiente = "";

    private void lanzarBiometrico(String tipoAccion, String fechaSimple) {
        accionPendiente = tipoAccion;
        fechaSimplePendiente = fechaSimple;

        biometricPrompt = new BiometricPrompt(Asistencia.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // AL VALIDAR HUELLA -> GUARDAR
                guardarAsistenciaEnFirestore(accionPendiente, fechaSimplePendiente);
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                tvEstado.setText("Cancelado o Error Biométrico");
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private void guardarAsistenciaEnFirestore(String tipo, String fechaSimple) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvEstado.setText("Guardando registro...");

            Map<String, Object> registro = new HashMap<>();
            registro.put("tipo", tipo);
            registro.put("timestamp", FieldValue.serverTimestamp());
            registro.put("fecha_legible", new Date().toString());

            // CAMPO CLAVE PARA LA VALIDACIÓN
            registro.put("fecha_simple", fechaSimple); // Guardamos "26-11-2025" para buscar fácil

            registro.put("uid_usuario", user.getUid());
            registro.put("email_usuario", user.getEmail());

            db.collection("registro_asistencia")
                    .document(user.getUid())
                    .collection("historial")
                    .add(registro)
                    .addOnSuccessListener(documentReference -> {
                        tvEstado.setText("¡" + tipo + " registrada exitosamente!");
                        Toast.makeText(Asistencia.this, "Listo. " + tipo + " guardada.", Toast.LENGTH_LONG).show();
                        // Opcional: finish();
                    })
                    .addOnFailureListener(e -> {
                        tvEstado.setText("Error al guardar.");
                    });
        }
    }
}
