package com.HanDav.stockfashion;

import java.io.Serializable;

// NO extiende AppCompatActivity
public class Item_Producto implements Serializable {

    // 1. Variables (Deben coincidir con los campos en Firebase)
    private String id;
    private String nombre;
    private String marca;
    private String tipo;     // Ej: Pantalón, Polera
    private String talla;
    private int cantidad;
    private double precio;   // Opcional

    // 2. Constructor Vacío (OBLIGATORIO para Firebase)
    public Item_Producto() {
    }

    // 3. Constructor Completo (Para crear productos nuevos en la App)
    public Item_Producto(String id, String nombre, String marca, String tipo, String talla, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    // 4. Getters y Setters (Funciones para leer y escribir datos)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
