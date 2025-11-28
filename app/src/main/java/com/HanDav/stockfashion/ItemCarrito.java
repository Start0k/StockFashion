package com.HanDav.stockfashion;

import java.io.Serializable;

public class ItemCarrito implements Serializable {

    // Identificador único para versiones de serialización (evita algunos errores de crash)
    private static final long serialVersionUID = 1L;

    private String idProducto; // ID original de Firebase
    private String nombre;
    private String marca;
    private String talla;
    private int cantidad;      // Cantidad que el cliente quiere comprar

    // Constructor vacío (necesario)
    public ItemCarrito() {
    }

    public ItemCarrito(String idProducto, String nombre, String marca, String talla, int cantidad) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.marca = marca;
        this.talla = talla;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public String getIdProducto() { return idProducto; }
    public void setIdProducto(String idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
