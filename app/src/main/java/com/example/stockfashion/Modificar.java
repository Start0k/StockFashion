package com.example.stockfashion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Modificar extends AppCompatActivity {

    private EditText etSearch;
    private Spinner spMarca, spTipo, spTalla, spCantidad;
    private RecyclerView rvProductos;

    private ProductoAdapter adapter;
    private final List<Producto> listaOriginal = new ArrayList<>();
    private final List<Producto> listaFiltrada = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modificar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias a la vista
        etSearch    = findViewById(R.id.etSearch);
        spMarca     = findViewById(R.id.spMarca);
        spTipo      = findViewById(R.id.spTipo);
        spTalla     = findViewById(R.id.spTalla);
        spCantidad  = findViewById(R.id.spCantidad);
        rvProductos = findViewById(R.id.rvProductos);

        rvProductos.setLayoutManager(new LinearLayoutManager(this));

        // Datos de prueba
        cargarDatosDummy();

        //  Llenar spinners dinámicamente según datos
        configurarSpinners();

        //  Configurar RecyclerView
        adapter = new ProductoAdapter(listaFiltrada);
        rvProductos.setAdapter(adapter);

        //  Listeners de búsqueda
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLista();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        //  Listeners de filtros
        spMarca.setOnItemSelectedListener(new SimpleOnItemSelectedListener(() -> filtrarLista()));
        spTipo.setOnItemSelectedListener(new SimpleOnItemSelectedListener(() -> filtrarLista()));
        spTalla.setOnItemSelectedListener(new SimpleOnItemSelectedListener(() -> filtrarLista()));
        spCantidad.setOnItemSelectedListener(new SimpleOnItemSelectedListener(() -> filtrarLista()));

        // Primera carga
        filtrarLista();
    }

    private void cargarDatosDummy() {
        // Reemplaza los números por su equivalente en Long añadiendo una 'L'
        listaOriginal.clear();
        listaOriginal.add(new Producto("Polera básica negra", "Nike",  "Polera", "M", 10L));
        listaOriginal.add(new Producto("Polera oversized blanca", "Adidas", "Polera", "L", 5L));
        listaOriginal.add(new Producto("Jeans skinny azul", "Levis", "Pantalón", "38", 3L));
        listaOriginal.add(new Producto("Vestido flores", "Zara", "Vestido", "S", 2L));
        listaOriginal.add(new Producto("Chaqueta cuero", "H&M", "Chaqueta", "M", 1L));
        listaOriginal.add(new Producto("Polera estampada", "Nike", "Polera", "S", 8L));
        listaOriginal.add(new Producto("Pantalón cargo", "Adidas", "Pantalón", "40", 6L));

        listaFiltrada.clear();
        listaFiltrada.addAll(listaOriginal);
    }

    private void configurarSpinners() {
        // Marca
        List<String> marcas = new ArrayList<>();
        marcas.add("Todas");
        marcas.addAll(obtenerValoresUnicosDeCampo("marca"));
        spMarca.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, marcas));

        // Tipo
        List<String> tipos = new ArrayList<>();
        tipos.add("Todos");
        tipos.addAll(obtenerValoresUnicosDeCampo("tipo"));
        spTipo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tipos));

        // Talla
        List<String> tallas = new ArrayList<>();
        tallas.add("Todas");
        tallas.addAll(obtenerValoresUnicosDeCampo("talla"));
        spTalla.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tallas));

        // Cantidad (aquí uso rangos como ejemplo sencillo)
        List<String> cantidades = new ArrayList<>();
        cantidades.add("Todas");
        cantidades.add("0 - 2");
        cantidades.add("3 - 5");
        cantidades.add("6+");
        spCantidad.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cantidades));
    }

    private List<String> obtenerValoresUnicosDeCampo(String campo) {
        Set<String> set = new HashSet<>();
        for (Producto p : listaOriginal) {
            switch (campo) {
                case "marca":
                    set.add(p.getMarca());
                    break;
                case "tipo":
                    set.add(p.getTipo());
                    break;
                case "talla":
                    set.add(p.getTalla());
                    break;
            }
        }
        return new ArrayList<>(set);
    }

    private void filtrarLista() {
        String textoBusqueda = etSearch.getText().toString().trim().toLowerCase();

        String marcaSel    = spMarca.getSelectedItem().toString();
        String tipoSel     = spTipo.getSelectedItem().toString();
        String tallaSel    = spTalla.getSelectedItem().toString();
        String cantSel     = spCantidad.getSelectedItem().toString();

        listaFiltrada.clear();

        for (Producto p : listaOriginal) {

            // Filtro por nombre
            if (!textoBusqueda.isEmpty() &&
                    !p.getNombre().toLowerCase().contains(textoBusqueda)) {
                continue;
            }

            // Filtro por marca
            if (!marcaSel.equals("Todas") && !p.getMarca().equals(marcaSel)) {
                continue;
            }

            // Filtro por tipo
            if (!tipoSel.equals("Todos") && !p.getTipo().equals(tipoSel)) {
                continue;
            }

            // Filtro por talla
            Long cantidadLong = p.getCantidad();
            if (cantidadLong == null) {
                continue; // Si no tiene cantidad, lo ignoramos en el filtro
            }

            // Filtro por cantidad (según rangos de ejemplo)
            int c = cantidadLong.intValue();
            if (cantSel.equals("0 - 2") && !(c >= 0 && c <= 2)) continue;
            if (cantSel.equals("3 - 5") && !(c >= 3 && c <= 5)) continue;
            if (cantSel.equals("6+")     && c < 6) continue;

            listaFiltrada.add(p);
        }

        adapter.notifyDataSetChanged();
    }
}
