package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;


import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class menu extends AppCompatActivity {

    private static final String TAG = "MenuActivity";

    // Vistas
    private MaterialCardView cardIngreso, cardStock, cardModificar, cardDespacho;
    private Button btnCrearUsuario;
    private Button btnCerrarSesion;

    private View cardAsistencia;

    private android.widget.TextView tvTitle, tvSubtitle;


    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();


        try {
            tvTitle = findViewById(R.id.tvTitle);
            tvSubtitle = findViewById(R.id.tvSubtitle);
            cardIngreso = findViewById(R.id.cardIngreso);
            cardStock = findViewById(R.id.cardStock);
            cardModificar = findViewById(R.id.cardModificar);
            cardDespacho = findViewById(R.id.cardDespacho);
            btnCrearUsuario = findViewById(R.id.btnCrearUsuario);
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
            cardAsistencia = findViewById(R.id.cardAsistencia);
        } catch (Exception e) {
            Log.e(TAG, "Error al enlazar las vistas. Verifica los IDs en activity_menu.xml", e);

        }

        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(menu.this, Perfil.class);
            startActivity(intent);
        });


        // ... código anterior ...

        FirebaseUser user = mAuth.getCurrentUser();
        // Lógica de roles (recibido del Intent)
        String rolUsuario = getIntent().getStringExtra("ROL_USUARIO");

        if (user != null) {
            Log.d(TAG, "Usuario actual: " + user.getEmail());

            // INICIO DE LA MODIFICACIÓN PARA OBTENER EL NOMBRE REAL
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Referencia temporal mientras carga
            tvTitle.setText("Cargando...");
            tvSubtitle.setText("acceso tipo: " + (rolUsuario != null ? rolUsuario : "Cargando..."));

            // Consultamos la colección "usuarios" buscando el documento con la UID
            db.collection("usuarios").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Recuperamos el campo "nombre"
                            String nombreReal = documentSnapshot.getString("nombre");

                            // Validamos que no sea nulo
                            if (nombreReal != null && !nombreReal.isEmpty()) {
                                tvTitle.setText("Bienvenido, " + nombreReal);
                            } else {
                                // Si el campo "nombre" está vacío, usamos el del Auth o uno genérico
                                String nombreAuth = user.getDisplayName();
                                tvTitle.setText("Bienvenido, " + (nombreAuth != null ? nombreAuth : "Usuario"));
                            }
                        } else {
                            Log.d(TAG, "El documento del usuario no existe en Firestore");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener datos del usuario", e);
                        tvTitle.setText("Bienvenido, Usuario");
                    });
            // FIN DE LA MODIFICACIÓN

        } else {
            Log.d(TAG, "No hay usuario actual");
            tvTitle.setText("StockFashion");
            tvSubtitle.setText("Gestión de inventario");
        }

        // ... resto del código (configurarListeners, etc) ...


        configurarListeners();



        if (rolUsuario == null) {
            rolUsuario = "usuario";
            Log.w(TAG, "No se recibió el rol, se asignó 'usuario' por defecto.");
        }

        // Toast para diagnóstico
        Toast.makeText(this, "Rol recibido: " + rolUsuario, Toast.LENGTH_LONG).show();


        ajustarVisibilidadPorRol(rolUsuario);
    }

    private void configurarListeners() {

        if (cardIngreso != null) cardIngreso.setOnClickListener(v -> startActivity(new Intent(menu.this, Ingreso.class)));
        if (cardStock != null) cardStock.setOnClickListener(v -> startActivity(new Intent(menu.this, verStock.class)));
        if (btnCrearUsuario != null) btnCrearUsuario.setOnClickListener(v -> startActivity(new Intent(menu.this, RegActivity.class)));
        if (cardModificar != null) cardModificar.setOnClickListener(v -> startActivity(new Intent(menu.this, Modificar.class)));
        if (cardDespacho != null) cardDespacho.setOnClickListener(v -> startActivity(new Intent(menu.this, Despacho.class)));
        if (cardAsistencia != null) cardAsistencia.setOnClickListener(v -> startActivity(new Intent(menu.this, Asistencia.class)));



        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(menu.this, LogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void ajustarVisibilidadPorRol(String rol) {
        if ("administrador".equals(rol)) {
            Log.d(TAG, "Usuario es Administrador. Mostrando todas las opciones.");
            cardIngreso.setVisibility(View.VISIBLE);
            cardStock.setVisibility(View.VISIBLE);
            cardModificar.setVisibility(View.VISIBLE);
            cardDespacho.setVisibility(View.VISIBLE);
            btnCrearUsuario.setVisibility(View.VISIBLE);
        } else { // Rol "usuario"
            Log.d(TAG, "Usuario es estándar. Ocultando opciones de administrador.");
            cardIngreso.setVisibility(View.GONE);
            cardModificar.setVisibility(View.GONE);
            btnCrearUsuario.setVisibility(View.GONE);
            cardStock.setVisibility(View.VISIBLE);
            cardDespacho.setVisibility(View.GONE);
        }
    }
}
