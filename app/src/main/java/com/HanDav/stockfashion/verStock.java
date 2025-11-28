package com.HanDav.stockfashion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class verStock extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spMarca, spTipo, spTalla, spCantidad;
    private RecyclerView rvProductos;
    private ProductoAdapter adapter;

    // Listas para manejar los datos
    private final List<Producto> listaOriginal = new ArrayList<>();
    private final List<Producto> listaFiltrada = new ArrayList<>();

    // Instancia de Firebase
    private FirebaseFirestore db;

    // Variables para controlar la selección actual de los spinners
    private String currentMarca = "Todas";
    private String currentTipo = "Todos";
    private String currentTalla = "Todas";
    private String currentCantidad = "Todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verstock);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Vincular vistas con IDs del layout
        etSearch    = findViewById(R.id.etSearch);
        spMarca     = findViewById(R.id.spMarca);
        spTipo      = findViewById(R.id.spTipo);
        spTalla     = findViewById(R.id.spTalla);
        spCantidad  = findViewById(R.id.spCantidad);
        rvProductos = findViewById(R.id.rvProductos);

        rvProductos.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar el adaptador
        adapter = new ProductoAdapter(listaFiltrada);
        rvProductos.setAdapter(adapter);

        // Inicializar Spinners con valores por defecto para evitar crashes
        inicializarSpinnersVacios();

        // Cargar datos desde Firebase
        cargarProductosTiempoReal();

        // Listener búsqueda
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filtrarLista(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Listeners Spinners
        setupSpinnerListeners();
    }

    private void inicializarSpinnersVacios() {
        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Cargando..."});
        spMarca.setAdapter(emptyAdapter);
        spTipo.setAdapter(emptyAdapter);
        spTalla.setAdapter(emptyAdapter);
        spCantidad.setAdapter(emptyAdapter);
    }

    private void cargarProductosTiempoReal() {
        db.collection("productos")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error al escuchar productos", e);
                        return;
                    }

                    if (snapshots != null) {
                        listaOriginal.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Producto p = doc.toObject(Producto.class);
                            if (p != null) {
                                p.setId(doc.getId());
                                listaOriginal.add(p);
                            }
                        }
                        // Actualizar filtros y lista visual
                        configurarSpinnersDinamicos();
                        filtrarLista();
                    }
                });
    }

    private void configurarSpinnersDinamicos() {
        // 1. MARCAS (Dinámico desde DB)
        List<String> marcas = new ArrayList<>();
        marcas.add("Todas");
        marcas.addAll(obtenerValoresUnicos("marca"));
        actualizarSpinner(spMarca, marcas, currentMarca);

        // 2. TIPOS (Dinámico desde DB)
        List<String> tipos = new ArrayList<>();
        tipos.add("Todos");
        tipos.addAll(obtenerValoresUnicos("tipo"));
        actualizarSpinner(spTipo, tipos, currentTipo);

        // 3. TALLAS (Dinámico desde DB)
        List<String> tallas = new ArrayList<>();
        tallas.add("Todas");
        tallas.addAll(obtenerValoresUnicos("talla"));
        actualizarSpinner(spTalla, tallas, currentTalla);

        // 4. CANTIDAD (Estático)
        List<String> cantidades = new ArrayList<>();
        cantidades.add("Todas");
        cantidades.add("0 - 2");
        cantidades.add("3 - 5");
        cantidades.add("6+");
        actualizarSpinner(spCantidad, cantidades, currentCantidad);
    }

    // Método auxiliar para actualizar un spinner manteniendo la selección
    private void actualizarSpinner(Spinner spinner, List<String> datos, String seleccionPrevia) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, datos);
        spinner.setAdapter(adapter);
        if (datos.contains(seleccionPrevia)) {
            spinner.setSelection(datos.indexOf(seleccionPrevia));
        }
    }

    private List<String> obtenerValoresUnicos(String campo) {
        Set<String> set = new HashSet<>();
        for (Producto p : listaOriginal) {
            switch (campo) {
                case "marca": if (p.getMarca() != null) set.add(p.getMarca()); break;
                case "tipo":  if (p.getTipo() != null) set.add(p.getTipo()); break;
                case "talla": if (p.getTalla() != null) set.add(p.getTalla()); break;
            }
        }
        return new ArrayList<>(set);
    }

    private void setupSpinnerListeners() {
        spMarca.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> {
            currentMarca = spMarca.getSelectedItem().toString();
            filtrarLista();
        }));
        spTipo.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> {
            currentTipo = spTipo.getSelectedItem().toString();
            filtrarLista();
        }));
        spTalla.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> {
            currentTalla = spTalla.getSelectedItem().toString();
            filtrarLista();
        }));
        spCantidad.setOnItemSelectedListener(new SimpleItemSelectedListener(() -> {
            currentCantidad = spCantidad.getSelectedItem().toString();
            filtrarLista();
        }));
    }

    private void filtrarLista() {
        // Evitar filtrado si los spinners no están listos
        if (spMarca.getSelectedItem() == null) return;

        String textoBusqueda = etSearch.getText().toString().trim().toLowerCase();
        String selMarca = spMarca.getSelectedItem().toString();
        String selTipo = spTipo.getSelectedItem().toString();
        String selTalla = spTalla.getSelectedItem().toString();
        String selCant = spCantidad.getSelectedItem().toString();

        listaFiltrada.clear();

        for (Producto p : listaOriginal) {
            // Filtro Texto
            if (!textoBusqueda.isEmpty() && (p.getNombre() == null || !p.getNombre().toLowerCase().contains(textoBusqueda))) continue;

            // Filtros Spinners
            if (!selMarca.equals("Todas") && (p.getMarca() == null || !p.getMarca().equals(selMarca))) continue;
            if (!selTipo.equals("Todos") && (p.getTipo() == null || !p.getTipo().equals(selTipo))) continue;
            if (!selTalla.equals("Todas") && (p.getTalla() == null || !p.getTalla().equals(selTalla))) continue;

            // Filtro Cantidad
            Long cant = p.getCantidad();
            if (cant == null) continue;
            if (selCant.equals("0 - 2") && (cant < 0 || cant > 2)) continue;
            if (selCant.equals("3 - 5") && (cant < 3 || cant > 5)) continue;
            if (selCant.equals("6+") && cant < 6) continue;

            listaFiltrada.add(p);
        }
        adapter.notifyDataSetChanged();
    }

    // Clase auxiliar interna para limpiar el código de listeners
    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private final Runnable action;
        public SimpleItemSelectedListener(Runnable action) { this.action = action; }
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { action.run(); }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    }
}
