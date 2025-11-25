package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar la ActionBar para que se vea pantalla completa
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Tiempo de espera: 3000 milisegundos = 3 segundos
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                verificarSesion();
            }
        }, 3000);
    }

    private void verificarSesion() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Si ya hay usuario, buscamos su rol y lo mandamos al Menu directamente
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("usuarios").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String rol = "usuario"; // Valor por defecto
                        if (documentSnapshot.exists() && documentSnapshot.getString("rol") != null) {
                            rol = documentSnapshot.getString("rol");
                        }

                        // Ir al Menú
                        Intent intent = new Intent(SplashActivity.this, menu.class);
                        intent.putExtra("ROL_USUARIO", rol);
                        startActivity(intent);
                        finish(); // Matar la SplashActivity
                    })
                    .addOnFailureListener(e -> {
                        // Si falla la conexión, lo mandamos al Login por seguridad
                        irAlLogin();
                    });

        } else {
            // No hay usuario logueado, ir al Login
            irAlLogin();
        }
    }

    private void irAlLogin() {
        Intent intent = new Intent(SplashActivity.this, LogActivity.class);
        startActivity(intent);
        finish();
    }
}
