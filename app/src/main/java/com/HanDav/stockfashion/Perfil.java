package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Perfil extends AppCompatActivity {

    private TextView tvNombre, tvEmail, tvRol, tvUid;
    private Button btnCerrarSesion;
    private ImageButton btnVolver;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        tvNombre = findViewById(R.id.tvPerfilNombre);
        tvEmail = findViewById(R.id.tvPerfilEmail);
        tvRol = findViewById(R.id.tvPerfilRol);
        tvUid = findViewById(R.id.tvPerfilUid);
        btnCerrarSesion = findViewById(R.id.btnPerfilCerrarSesion);
        btnVolver = findViewById(R.id.btnVolver);

        // Cargar datos
        cargarDatosUsuario();

        // Configurar botón Volver
        btnVolver.setOnClickListener(v -> finish());

        // Configurar botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            mAuth.signOut();
            // Redirigir al Login y borrar historial de actividades
            Intent intent = new Intent(Perfil.this, LogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // 1. Mostrar datos básicos del Auth (Email y UID)
            tvEmail.setText(user.getEmail());
            tvUid.setText(user.getUid());

            // 2. Consultar Firestore para datos extra (Nombre y Rol)
            db.collection("usuarios").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String rol = documentSnapshot.getString("rol");

                            // Validar y mostrar nombre
                            if (nombre != null && !nombre.isEmpty()) {
                                tvNombre.setText(nombre);
                            } else {
                                // Si no hay nombre en DB, intentar usar el de Google o poner default
                                String nombreGoogle = user.getDisplayName();
                                tvNombre.setText(nombreGoogle != null ? nombreGoogle : "Usuario sin nombre");
                            }

                            // Validar y mostrar rol
                            if (rol != null && !rol.isEmpty()) {
                                tvRol.setText(rol.toUpperCase());
                            } else {
                                tvRol.setText("USUARIO");
                            }

                        } else {
                            tvNombre.setText("Usuario (Datos no encontrados)");
                            tvRol.setText("Desconocido");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Perfil.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                        tvNombre.setText("Error de conexión");
                    });

        } else {
            // Si por alguna razón llega aquí sin login
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
