package com.example.stockfashion;

import com.google.firebase.firestore.Exclude;

public class Producto {

    // 1. AÑADIR @EXCLUDE: Esto le dice a Firestore que no intente
    //    guardar o leer este campo desde la base de datos.
    @Exclude
    private String id;

    private String nombre;
    private String marca;
    private String tipo;
    private String talla;
    private Long cantidad; // 2. CAMBIO A LONG: Es más compatible y seguro con Firestore que 'int'.

    // Constructor público y vacío. ¡OBLIGATORIO para Firestore!
    public Producto() {
    }

    // 3. CONSTRUCTOR OPCIONAL: Útil si necesitas crear objetos Producto manualmente en tu código.
    //    Nota que ya no incluye el 'id'.
    public Producto(String nombre, String marca, String tipo, String talla, Long cantidad) {
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    // --- GETTERS Y SETTERS ---
    // (Estos ya los tenías bien, pero los adaptamos para Long)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }
}
