package com.example.stockfashion;

import com.google.firebase.firestore.Exclude;

public class Producto {
    // Variable para guardar el ID del documento de Firestore
    @Exclude // Excluye este campo de ser guardado en la base de datos
    private String id;

    // Atributos que coinciden con los campos en tu base de datos
    private String nombre;
    private String marca;
    private String tipo;
    private String talla;
    private Long cantidad; // Usamos Long para números, es más compatible con Firestore

    // Constructor vacío, REQUERIDO por Firestore para deserializar los datos
    public Producto() {}

    public Producto(String nombre, String marca, String tipo, String talla, Long cantidad) {
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    // --- Getters y Setters para todos los campos ---

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
