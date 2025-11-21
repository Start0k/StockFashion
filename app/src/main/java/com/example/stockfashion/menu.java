package com.example.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // Asegúrate de tener esta importación para el Toast de diagnóstico
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class menu extends AppCompatActivity {

    private static final String TAG = "MenuActivity";

    // Vistas
    private MaterialCardView cardIngreso, cardStock, cardModificar, cardDespacho;
    private Button btnCrearUsuario;
    private Button btnCerrarSesion;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();


        try {
            cardIngreso = findViewById(R.id.cardIngreso);
            cardStock = findViewById(R.id.cardStock);
            cardModificar = findViewById(R.id.cardModificar);
            cardDespacho = findViewById(R.id.cardDespacho);
            btnCrearUsuario = findViewById(R.id.btnCrearUsuario);
            btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        } catch (Exception e) {
            Log.e(TAG, "Error al enlazar las vistas. Verifica los IDs en activity_menu.xml", e);

        }



        configurarListeners();

        // Lógica de roles
        String rolUsuario = getIntent().getStringExtra("ROL_USUARIO");

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
            cardIngreso.setVisibility(View.VISIBLE);
            cardModificar.setVisibility(View.VISIBLE);
            btnCrearUsuario.setVisibility(View.VISIBLE);
            cardStock.setVisibility(View.VISIBLE);
            cardDespacho.setVisibility(View.VISIBLE);
        }
    }
}
