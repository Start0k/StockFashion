package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class LogActivity extends AppCompatActivity {

    private EditText txtMainCorreo, txtMainPass;
    private Button btnLogin;

    private FirebaseAnalytics oFirebaseAnalytics;
    private FirebaseAuth oFirebaseAuth;
    private FirebaseFirestore db; // Agregamos la instancia de Firestore

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);

        // Inicializar Firebase Analytics y Auth
        oFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        oFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Inicializar Firestore

        // Evento de Analytics (opcional)
        Bundle bundle = new Bundle();
        bundle.putString("mensaje", "Entro_al_Log");
        oFirebaseAnalytics.logEvent("pantalla_de_Log", bundle);

        this.setTitle("Inicio de Sesión");

        // Vincular Vistas
        txtMainCorreo = findViewById(R.id.txtMainCorreo);
        txtMainPass = findViewById(R.id.txtMainPass);
        btnLogin = findViewById(R.id.btnLogin);

        // Configurar botón de Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correo = txtMainCorreo.getText().toString().trim();
                String contra = txtMainPass.getText().toString().trim();
                ingresar(correo, contra);
            }
        });

        // Si tienes un botón de registrarse, descoméntalo aquí:
        /*
        Button btnRegistrarse = findViewById(R.id.btnLogRegistrarse);
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogActivity.this, RegActivity.class);
                startActivity(intent);
            }
        });
        */
    }

    /**
     * Método corregido: Autentica y luego consulta Firestore para obtener el rol.
     */
    private void ingresar(String email, String password) {
        // 1. Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LogActivity.this, "Por favor, ingrese correo y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Iniciar sesión con Firebase Auth
        oFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success - Autenticación correcta.");
                            FirebaseUser user = oFirebaseAuth.getCurrentUser();

                            if (user != null) {
                                // 3. CONSULTA A FIRESTORE
                                // Buscamos en la colección "usuarios" el documento con el ID del usuario logueado
                                db.collection("usuarios").document(user.getUid())
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                // 4. Leer el campo "rol"
                                                String rol = documentSnapshot.getString("rol");

                                                // Protección por si el campo está vacío
                                                if (rol == null) {
                                                    rol = "usuario";
                                                }

                                                Log.d(TAG, "Usuario encontrado en Firestore. Rol: " + rol);

                                                // 5. Redirigir y pasar el rol a la siguiente actividad
                                                Intent intent = new Intent(LogActivity.this, menu.class);
                                                intent.putExtra("ROL_USUARIO", rol);
                                                startActivity(intent);
                                                finish();

                                            } else {
                                                // El usuario se autenticó, pero no tiene datos en Firestore
                                                Log.w(TAG, "El documento del usuario no existe en Firestore.");
                                                Toast.makeText(LogActivity.this, "Error: Usuario sin datos de perfil.", Toast.LENGTH_LONG).show();
                                                // Opcional: Mandarlo al registro o dejarlo pasar como invitado
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error al leer datos de Firestore", e);
                                            Toast.makeText(LogActivity.this, "Error de conexión con la base de datos.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Error en Auth (contraseña mal, usuario no existe, etc)
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogActivity.this, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Opcional: Verificar si el usuario ya estaba logueado.
        // NOTA: Si usas esto, asegúrate de implementar la lógica de lectura de Firestore
        // también aquí, o el usuario entrará sin que verifiques su rol actualizado.
        FirebaseUser currentUser = oFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Si quieres autologin, llama a una función que lea Firestore aquí.
            // Por ahora lo dejamos vacío para obligar a loguear y probar el rol.
            // reload();
        }
    }

    private void reload() {
        // Implementar lógica de recarga si es necesario
    }
}
