package com.example.stockfashion;

public class Producto {
    private String nombre;
    private String marca;
    private String tipo;
    private String talla;
    private int cantidad;

    public Producto(String nombre, String marca, String tipo, String talla, int cantidad) {
        this.nombre = nombre;
        this.marca = marca;
        this.tipo = tipo;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    public Producto(){

    }

    public String getNombre() { return nombre; }
    public String getMarca()   { return marca; }
    public String getTipo()    { return tipo; }
    public String getTalla()   { return talla; }
    public int getCantidad()   { return cantidad; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setTalla(String talla) { this.talla = talla; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
