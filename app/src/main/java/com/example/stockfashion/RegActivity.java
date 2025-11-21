package com.example.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegActivity extends AppCompatActivity {

    private static final String TAG = "RegActivity";

    // Vistas
    private EditText txtNombre, txtRut, txtTelefono, txtCorreo, txtPassword;
    private CheckBox cbEsAdmin;
    private Button btnCrearCuenta;
    private TextView tvIrLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        txtNombre = findViewById(R.id.txtRegNombre);
        txtRut = findViewById(R.id.txtRegRut);
        txtTelefono = findViewById(R.id.txtRegTelefono);
        txtCorreo = findViewById(R.id.txtRegCorreo);
        txtPassword = findViewById(R.id.txtRegPass);
        cbEsAdmin = findViewById(R.id.cbEsAdmin);
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        tvIrLogin = findViewById(R.id.tvIrLogin);


        btnCrearCuenta.setOnClickListener(v -> intentarCrearCuenta());
        tvIrLogin.setOnClickListener(v -> startActivity(new Intent(RegActivity.this, LogActivity.class)));
    }

    private void intentarCrearCuenta() {
        String nombre = txtNombre.getText().toString().trim();
        String rut = txtRut.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // Validaciones básicas
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(rut) || TextUtils.isEmpty(telefono) || TextUtils.isEmpty(correo) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }


        String rolSolicitado = cbEsAdmin.isChecked() ? "administrador" : "usuario";


        Map<String, Object> nuevoUsuarioData = new HashMap<>();
        nuevoUsuarioData.put("nombre", nombre);
        nuevoUsuarioData.put("rut", rut);
        nuevoUsuarioData.put("telefono", telefono);
        nuevoUsuarioData.put("email", correo);
        nuevoUsuarioData.put("rol", rolSolicitado);
        nuevoUsuarioData.put("fechaCreacion", FieldValue.serverTimestamp());


        if (cbEsAdmin.isChecked()) {
            mostrarDialogoAutorizacion(correo, password, nuevoUsuarioData);
        } else {
            crearCuentaEnFirebase(correo, password, nuevoUsuarioData);
        }
    }

    private void mostrarDialogoAutorizacion(String correoNuevoUsuario, String passNuevoUsuario, Map<String, Object> nuevoUsuarioData) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_autorizacion_admin, null);

        EditText txtAuthCorreo = dialogView.findViewById(R.id.txtAuthCorreoAdmin);
        EditText txtAuthPass = dialogView.findViewById(R.id.txtAuthPassAdmin);

        builder.setView(dialogView)
                .setTitle("Autorización Requerida")
                .setPositiveButton("Autorizar y Crear", (dialog, id) -> {
                    String adminCorreo = txtAuthCorreo.getText().toString().trim();
                    String adminPass = txtAuthPass.getText().toString().trim();

                    if (adminCorreo.isEmpty() || adminPass.isEmpty()) {
                        Toast.makeText(this, "Debe ingresar las credenciales del administrador.", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    verificarCredencialesAdmin(adminCorreo, adminPass, correoNuevoUsuario, passNuevoUsuario, nuevoUsuarioData);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void verificarCredencialesAdmin(String adminCorreo, String adminPass, String correoNuevo, String passNuevo, Map<String, Object> nuevoUsuarioData) {
        FirebaseAuth tempAuth = FirebaseAuth.getInstance();
        tempAuth.signInWithEmailAndPassword(adminCorreo, adminPass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser adminUser = Objects.requireNonNull(task.getResult()).getUser();
                        db.collection("usuarios").document(adminUser.getUid()).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists() && "administrador".equals(documentSnapshot.getString("rol"))) {

                                        Toast.makeText(RegActivity.this, "Autorización concedida.", Toast.LENGTH_SHORT).show();

                                        crearCuentaEnFirebase(correoNuevo, passNuevo, nuevoUsuarioData);
                                    } else {

                                        Toast.makeText(RegActivity.this, "Autorización denegada: El usuario no es administrador.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(RegActivity.this, "Error al verificar rol de administrador.", Toast.LENGTH_SHORT).show());
                    } else {

                        Toast.makeText(RegActivity.this, "Autorización denegada: Credenciales de administrador incorrectas.", Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void crearCuentaEnFirebase(String correo, String password, Map<String, Object> datosUsuario) {
        btnCrearCuenta.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser newUser = mAuth.getCurrentUser();
                        // Ahora guardamos los datos adicionales en Firestore
                        guardarDatosUsuarioEnFirestore(newUser, datosUsuario);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegActivity.this, "Error al crear la cuenta: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        btnCrearCuenta.setEnabled(true);
                    }
                });
    }

    private void guardarDatosUsuarioEnFirestore(FirebaseUser firebaseUser, Map<String, Object> datosUsuario) {
        if (firebaseUser == null) return;

        db.collection("usuarios").document(firebaseUser.getUid())
                .set(datosUsuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Datos del usuario guardados en Firestore con rol: " + datosUsuario.get("rol"));
                    Toast.makeText(RegActivity.this, "Cuenta creada con éxito.", Toast.LENGTH_SHORT).show();


                    mAuth.signOut();


                    Intent intent = new Intent(RegActivity.this, LogActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar datos en Firestore", e);
                    Toast.makeText(RegActivity.this, "Error al guardar la información del usuario.", Toast.LENGTH_SHORT).show();
                    btnCrearCuenta.setEnabled(true);
                });
    }
}
