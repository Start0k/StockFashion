package com.HanDav.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LogActivity extends AppCompatActivity {


    private EditText txtMainCorreo, txtMainPass;
    private android.widget.ProgressBar progressBarLog;
    private Button btnLogin, btnGoogleLogin;

    private Button btnIrRegistro;
    private FirebaseAuth oFirebaseAuth;
    private FirebaseFirestore db;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LogActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        if (btnIrRegistro != null) btnIrRegistro.setOnClickListener(v -> startActivity(new Intent(LogActivity.this, RegActivity.class)));

        oFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        progressBarLog = findViewById(R.id.progressBarLog);
        txtMainCorreo = findViewById(R.id.txtMainCorreo);
        txtMainPass = findViewById(R.id.txtMainPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);






        btnLogin.setOnClickListener(view -> {
            String correo = txtMainCorreo.getText().toString().trim();
            String contra = txtMainPass.getText().toString().trim();
            ingresar(correo, contra);
        });


        btnGoogleLogin.setOnClickListener(view -> {
            signInWithGoogle();
        });
    }



    private void signInWithGoogle() {

        mostrarCarga(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed code=" + e.getStatusCode(), e);
                Toast.makeText(this, "Falló el inicio con Google. Código: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                mostrarCarga(false);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        oFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = oFirebaseAuth.getCurrentUser();
                        verificarUsuarioEnFirestore(user);
                    } else {
                        Toast.makeText(LogActivity.this, "Error de autenticación con Firebase.", Toast.LENGTH_SHORT).show();
                        mostrarCarga(false);
                    }
                });
    }



    private void ingresar(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarCarga(true);

        oFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        verificarUsuarioEnFirestore(oFirebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LogActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        mostrarCarga(false);
                    }
                });
    }


    private void verificarUsuarioEnFirestore(FirebaseUser user) {
        if (user == null) {
            mostrarCarga(false);
            return;
        }

        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String rol = documentSnapshot.getString("rol");

                        if (rol == null) rol = "usuario";

                        Log.d(TAG, "Usuario existente. Rol: " + rol);
                        redirigirSegunRol(rol);
                    } else {
                        Log.d(TAG, "Usuario nuevo. Creando registro...");
                        crearUsuarioGoogleEnFirestore(user);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar DB", e);
                    Toast.makeText(LogActivity.this, "Error al verificar usuario.", Toast.LENGTH_SHORT).show();
                    mostrarCarga(false);
                });
    }

    private void crearUsuarioGoogleEnFirestore(FirebaseUser user) {
        Map<String, Object> datosUsuario = new HashMap<>();
        String nombre = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();

        datosUsuario.put("nombre", nombre);
        datosUsuario.put("email", user.getEmail());
        datosUsuario.put("rol", "usuario");

        db.collection("usuarios").document(user.getUid()).set(datosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Usuario creado con éxito.");
                    redirigirSegunRol("usuario");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar usuario nuevo", e);
                    Toast.makeText(LogActivity.this, "Error al crear perfil de usuario.", Toast.LENGTH_SHORT).show();
                    mostrarCarga(false);
                });
    }

    private void redirigirSegunRol(String rol) {
        Intent intent = new Intent(LogActivity.this, menu.class);
        intent.putExtra("ROL_USUARIO", rol);
        startActivity(intent);
        finish();
    }


    private void mostrarCarga(boolean cargando) {
        if (cargando) {
            progressBarLog.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnGoogleLogin.setEnabled(false);
        } else {
            progressBarLog.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnGoogleLogin.setEnabled(true);
        }
    }
}
