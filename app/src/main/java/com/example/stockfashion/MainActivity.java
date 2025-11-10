package com.example.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // tu layout de login

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = inputEmail.getText().toString().trim();
        String pass = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Simular login: 1.5s
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            progress.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            // Ir al menú
            Intent i = new Intent(MainActivity.this, menu.class);
            startActivity(i);

            // Opcional: limpiar campos
            inputEmail.setText("");
            inputPassword.setText("");
        }, 1500);
    }
}
