package com.HanDav.stockfashion;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportesAsistencia extends AppCompatActivity {


    private Spinner spinnerUsuarios;
    private Button btnGenerarReporte, btnImprimirPDF;
    private ImageButton btnVolverReporte;
    private LinearLayout layoutResultados;
    private TextView tvEstadoReporte;
    private FirebaseFirestore db;
    private ArrayList<String> nombresUsuarios = new ArrayList<>();
    private ArrayList<String> uidsUsuarios = new ArrayList<>();
    private String uidSeleccionado = "";
    private String nombreSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes_asistencia);

        db = FirebaseFirestore.getInstance();


        spinnerUsuarios = findViewById(R.id.spinnerUsuarios);
        btnGenerarReporte = findViewById(R.id.btnGenerarReporte);
        btnImprimirPDF = findViewById(R.id.btnImprimirPDF);
        btnVolverReporte = findViewById(R.id.btnVolverReporte);
        layoutResultados = findViewById(R.id.layoutResultados);
        tvEstadoReporte = findViewById(R.id.tvEstadoReporte);

        cargarUsuariosEnSpinner();

        spinnerUsuarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < uidsUsuarios.size()) {
                    uidSeleccionado = uidsUsuarios.get(position);
                    nombreSeleccionado = nombresUsuarios.get(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnGenerarReporte.setOnClickListener(v -> {
            if (!uidSeleccionado.isEmpty()) {
                cargarHistorialAsistencia(uidSeleccionado);
            } else {
                Toast.makeText(this, "Seleccione un usuario primero", Toast.LENGTH_SHORT).show();
            }
        });

        btnVolverReporte.setOnClickListener(v -> finish());

        // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
        btnImprimirPDF.setOnClickListener(v -> {
            // Llamamos al método real de crear PDF
            if (layoutResultados.getChildCount() > 0) {
                generarPDF();
            } else {
                Toast.makeText(this, "No hay datos para imprimir", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ... (Tus métodos cargarUsuariosEnSpinner y cargarHistorialAsistencia se mantienen IGUAL) ...
    private void cargarUsuariosEnSpinner() {
        db.collection("usuarios").whereNotEqualTo("rol", "admin").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    nombresUsuarios.clear(); uidsUsuarios.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombre = document.getString("nombre");
                        if (nombre == null || nombre.isEmpty()) nombre = document.getString("email");
                        nombresUsuarios.add(nombre);
                        uidsUsuarios.add(document.getId());
                    }
                    if (nombresUsuarios.isEmpty()) {
                        nombresUsuarios.add("No hay empleados"); uidsUsuarios.add(""); btnGenerarReporte.setEnabled(false);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresUsuarios);
                    spinnerUsuarios.setAdapter(adapter);
                });
    }

    private void cargarHistorialAsistencia(String uid) {
        layoutResultados.removeAllViews();
        tvEstadoReporte.setVisibility(View.VISIBLE); tvEstadoReporte.setText("Cargando..."); btnImprimirPDF.setEnabled(false);

        db.collection("registro_asistencia").document(uid).collection("historial")
                .orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvEstadoReporte.setVisibility(View.GONE);
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEstadoReporte.setVisibility(View.VISIBLE); tvEstadoReporte.setText("Sin registros."); return;
                    }
                    agregarTituloReporte("Historial de: " + nombreSeleccionado);
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tipo = doc.getString("tipo");
                        Date fechaObj = doc.getTimestamp("timestamp") != null ? doc.getTimestamp("timestamp").toDate() : null;
                        String f = "Fecha desc.", h = "--:--";
                        if (fechaObj != null) {
                            f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaObj);
                            h = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(fechaObj);
                        }
                        crearFilaReporte(tipo, f, h);
                    }
                    btnImprimirPDF.setEnabled(true);
                });
    }

    private void crearFilaReporte(String tipo, String fecha, String hora) {

        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        card.setRadius(12); card.setElevation(2);

        LinearLayout layoutInterno = new LinearLayout(this);
        layoutInterno.setOrientation(LinearLayout.HORIZONTAL);
        layoutInterno.setPadding(32, 32, 32, 32);
        layoutInterno.setGravity(Gravity.CENTER_VERTICAL);

        View indicador = new View(this);
        LinearLayout.LayoutParams paramsIndicador = new LinearLayout.LayoutParams(10, 100);
        paramsIndicador.setMargins(0, 0, 24, 0);
        if ("ENTRADA".equalsIgnoreCase(tipo)) indicador.setBackgroundColor(0xFF10B981);
        else indicador.setBackgroundColor(0xFFEF4444);
        indicador.setLayoutParams(paramsIndicador);

        LinearLayout textos = new LinearLayout(this);
        textos.setOrientation(LinearLayout.VERTICAL);
        TextView tvTipo = new TextView(this);
        tvTipo.setText(tipo != null ? tipo.toUpperCase() : "REGISTRO");
        tvTipo.setTextSize(16); tvTipo.setTypeface(null, android.graphics.Typeface.BOLD); tvTipo.setTextColor(0xFF111827);
        TextView tvFecha = new TextView(this);
        tvFecha.setText(fecha + " - " + hora); tvFecha.setTextColor(0xFF6B7280);

        textos.addView(tvTipo); textos.addView(tvFecha);
        layoutInterno.addView(indicador); layoutInterno.addView(textos);
        card.addView(layoutInterno);
        layoutResultados.addView(card);
    }

    private void agregarTituloReporte(String titulo) {
        TextView tv = new TextView(this);
        tv.setText(titulo); tv.setTextSize(18); tv.setPadding(0, 0, 0, 24);
        tv.setTextColor(0xFF374151); tv.setTypeface(null, android.graphics.Typeface.BOLD);
        layoutResultados.addView(tv);
    }

    // --- MÉTODO NUEVO: GENERAR PDF ---
    private void generarPDF() {

        int width = layoutResultados.getWidth();
        int height = layoutResultados.getHeight();


        if (width == 0 || height == 0) return;


        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);


        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);


        layoutResultados.draw(canvas);

        document.finishPage(page);


        String nombreArchivo = "Reporte_" + nombreSeleccionado.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    document.writeTo(outputStream);
                    if (outputStream != null) outputStream.close();
                    Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
                }
            } else {

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo);
                document.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        document.close();
    }
}
