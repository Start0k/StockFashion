package com.example.stockfashion;

import com.google.firebase.firestore.Exclude;

public class Producto {


    @Exclude
    private String id;

    private String nombre;
    private String marca;
    private String tipo;
    private String talla;
    private Long cantidad;


    public Producto() {
    }


    public Producto(String nombre, String marca, String tipo, String talla, Long cantidad) {
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.talla = talla;
        this.cantidad = cantidad;
    }



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
