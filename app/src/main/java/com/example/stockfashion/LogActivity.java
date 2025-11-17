package com.example.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;


public class LogActivity extends AppCompatActivity {

    private EditText txtMainCorreo, txtMainPass;
    private Button btnLogin;


    private FirebaseAnalytics oFirebaseAnalytics;
    private FirebaseAuth oFirebaseAuth;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);

        oFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("mensaje", "Entro_al_Log");
        oFirebaseAnalytics.logEvent("pantalla_de_Log", bundle);
        this.setTitle("Inicio de Session");
        EditText txtC = findViewById(R.id.txtMainCorreo);
        EditText txtP = findViewById(R.id.txtMainPass);

        oFirebaseAuth = FirebaseAuth.getInstance();
        Button button1 = findViewById(R.id.btnLogin);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText correo = findViewById(R.id.txtMainCorreo);
                EditText contra = findViewById(R.id.txtMainPass);
                ingresar(String.valueOf(correo.getText()), String.valueOf(contra.getText()));

            }
        });

        //Boton que redirecciona a activity registro
//        Button button2 = findViewById(R.id.btnLogRegistrarse);
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(LogActivity.this, RegActivity.class);
//                startActivity(intent);
//            }
//        });



    }


    private void ingresar(String email, String password) {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LogActivity.this, "Por favor, ingrese correo y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        oFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success - Inicio de sesión exitoso.");
                            FirebaseUser user = oFirebaseAuth.getCurrentUser();

                            if (user != null) {

                                user.getIdToken(true)
                                        .addOnCompleteListener(tokenTask -> {
                                            if (tokenTask.isSuccessful()) {

                                                String rol = "usuario"; // Rol por defecto si no se encuentra un claim.
                                                if (tokenTask.getResult().getClaims().get("rol") != null) {
                                                    rol = tokenTask.getResult().getClaims().get("rol").toString();
                                                }

                                                Log.d(TAG, "Usuario tiene el rol: " + rol);


                                                Intent intent = new Intent(LogActivity.this, menu.class);


                                                intent.putExtra("ROL_USUARIO", rol);


                                                startActivity(intent);
                                                finish();

                                            } else {
                                                Log.w(TAG, "Error al obtener el token y los claims:", tokenTask.getException());
                                                Toast.makeText(LogActivity.this, "No se pudieron verificar los permisos. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogActivity.this, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = oFirebaseAuth.getCurrentUser();
        if (currentUser != null){
            reload();
        }
    }



    private void updateUI(FirebaseUser user) {

    }



    private void reload(){

    }


}
