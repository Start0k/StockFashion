package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView; // Corregido import
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class menu extends AppCompatActivity {

    private static final String TAG = "MenuActivity";

    // Vistas
    private MaterialCardView cardIngreso, cardStock, cardModificar, cardDespacho, cardReportes;
    // Nota: cardAsistencia a veces es View o CardView según tu XML, lo dejo como View para evitar errores de cast
    private View cardAsistencia;

    private Button btnCrearUsuario;
    private Button btnCerrarSesion;
    private TextView tvTitle, tvSubtitle;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Validar usuario logueado al inicio
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Si no hay sesión, mandar al Login inmediatamente
            Intent intent = new Intent(menu.this, LogActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Enlazar Vistas
        try {
            tvTitle = findViewById(R.id.tvTitle);
            tvSubtitle = findViewById(R.id.tvSubtitle);

            // Tarjetas
            cardIngreso = findViewById(R.id.cardIngreso);
            cardStock = findViewById(R.id.cardStock);
            cardModificar = findViewById(R.id.cardModificar);
            cardDespacho = findViewById(R.id.cardDespacho);
            cardAsistencia = findViewById(R.id.cardAsistencia);
            cardReportes = findViewById(R.id.cardReportes);

            // Botones
            btnCrearUsuario = findViewById(R.id.btnCrearUsuario);
            // btnCerrarSesion = findViewById(R.id.btnCerrarSesion); // Descomentar si tienes este botón en el XML principal

            ImageButton btnPerfil = findViewById(R.id.btnPerfil);
            btnPerfil.setOnClickListener(v -> {
                // Asegúrate que tu clase Perfil se llame "perfil" o "Perfil" según tu archivo
                Intent intent = new Intent(menu.this, Perfil.class);
                startActivity(intent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al enlazar las vistas. Verifica los IDs en activity_menu.xml", e);
            Toast.makeText(this, "Error en interfaz gráfica", Toast.LENGTH_LONG).show();
        }

        // Configurar acciones de botones
        configurarListeners();

        // Ocultar botones sensibles por defecto (Seguridad visual)
        ocultarOpcionesAdmin();
        tvTitle.setText("Cargando...");
        tvSubtitle.setText("Verificando permisos...");

        // --- LÓGICA PRINCIPAL: OBTENER DATOS DE FIRESTORE ---
        verificarUsuarioYRol(user.getUid());
    }

    private void verificarUsuarioYRol(String uid) {
        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 1. Obtener Nombre
                        String nombreReal = documentSnapshot.getString("nombre");
                        if (nombreReal != null && !nombreReal.isEmpty()) {
                            tvTitle.setText("Bienvenido, " + nombreReal);
                        } else {
                            tvTitle.setText("Bienvenido, Usuario");
                        }

                        // 2. Obtener Rol (Fundamental para permisos)
                        String rolBD = documentSnapshot.getString("rol");

                        // Normalizar rol (manejar nulos)
                        if (rolBD == null) rolBD = "usuario";

                        Log.d(TAG, "Rol obtenido de BD: " + rolBD);
                        tvSubtitle.setText("Perfil: " + rolBD.toUpperCase());

                        // 3. Actualizar Interfaz
                        ajustarVisibilidadPorRol(rolBD);

                    } else {
                        Log.d(TAG, "Usuario no encontrado en BD (posiblemente borrado)");
                        tvTitle.setText("Error de Usuario");
                        // Tratamos como usuario básico si no hay registro
                        ajustarVisibilidadPorRol("usuario");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al conectar con Firestore", e);
                    tvTitle.setText("Sin conexión");
                    Toast.makeText(menu.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void ocultarOpcionesAdmin() {
        // Estado inicial seguro: ocultar todo lo que sea solo de admin
        if (cardIngreso != null) cardIngreso.setVisibility(View.GONE);
        if (cardModificar != null) cardModificar.setVisibility(View.GONE);
        if (btnCrearUsuario != null) btnCrearUsuario.setVisibility(View.GONE);
        if (cardReportes != null) cardReportes.setVisibility(View.GONE);

        // Lo que siempre se ve
        if (cardStock != null) cardStock.setVisibility(View.VISIBLE);
        if (cardAsistencia != null) cardAsistencia.setVisibility(View.VISIBLE);
    }

    private void ajustarVisibilidadPorRol(String rol) {
        // Aceptamos "admin", "administrador", "ADMIN", etc.
        boolean esAdmin = "administrador".equalsIgnoreCase(rol) || "admin".equalsIgnoreCase(rol);

        if (esAdmin) {
            Log.d(TAG, "Usuario es Administrador. Mostrando opciones.");
            if (cardIngreso != null) cardIngreso.setVisibility(View.VISIBLE);
            if (cardModificar != null) cardModificar.setVisibility(View.VISIBLE);
            if (cardDespacho != null) cardDespacho.setVisibility(View.VISIBLE);
            if (btnCrearUsuario != null) btnCrearUsuario.setVisibility(View.VISIBLE);
            if (cardReportes != null) cardReportes.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Usuario es Estándar. Ocultando admin.");
            // Aseguramos que sigan ocultos
            ocultarOpcionesAdmin();

            // El usuario normal SÍ puede ver despacho? (ajusta según tu lógica)
            // En tu código original, despacho estaba GONE para usuario.
            if (cardDespacho != null) cardDespacho.setVisibility(View.GONE);
        }

        // Asegurar visibilidad de elementos comunes
        if (cardStock != null) cardStock.setVisibility(View.VISIBLE);
        if (cardAsistencia != null) cardAsistencia.setVisibility(View.VISIBLE);
    }

    private void configurarListeners() {
        if (cardIngreso != null) cardIngreso.setOnClickListener(v -> startActivity(new Intent(menu.this, Ingreso.class)));
        if (cardStock != null) cardStock.setOnClickListener(v -> startActivity(new Intent(menu.this, verStock.class))); // Ojo: verStock en minúscula según tu proyecto
        if (btnCrearUsuario != null) btnCrearUsuario.setOnClickListener(v -> startActivity(new Intent(menu.this, RegActivity.class)));
        if (cardModificar != null) cardModificar.setOnClickListener(v -> startActivity(new Intent(menu.this, Modificar.class)));
        if (cardDespacho != null) cardDespacho.setOnClickListener(v -> startActivity(new Intent(menu.this, Despacho.class)));
        if (cardAsistencia != null) cardAsistencia.setOnClickListener(v -> startActivity(new Intent(menu.this, Asistencia.class)));
        if (cardReportes != null) cardReportes.setOnClickListener(v -> startActivity(new Intent(menu.this, ReportesAsistencia.class)));

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
}
