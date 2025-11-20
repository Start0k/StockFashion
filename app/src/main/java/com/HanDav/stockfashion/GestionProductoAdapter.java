package com.HanDav.stockfashion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class GestionProductoAdapter extends RecyclerView.Adapter<GestionProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private final OnProductoInteractionListener mListener;


    public interface OnProductoInteractionListener {
        void onModificarProducto(Producto producto);
        void onEliminarProducto(Producto producto, int position);
    }

    public GestionProductoAdapter(List<Producto> listaProductos, OnProductoInteractionListener listener) {
        this.listaProductos = listaProductos;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_gestion, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto, mListener);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalles, tvCantidad;
        MaterialButton btnModificar, btnEliminar;

        ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvDetalles = itemView.findViewById(R.id.tvDetallesProducto);
            tvCantidad = itemView.findViewById(R.id.tvCantidadProducto);
            btnModificar = itemView.findViewById(R.id.btnModificarCantidad);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProducto);
        }

        void bind(final Producto producto, final OnProductoInteractionListener listener) {
            tvNombre.setText(producto.getNombre());
            String detalles = String.format(Locale.getDefault(), "Marca: %s • Tipo: %s • Talla: %s",
                    producto.getMarca(), producto.getTipo(), producto.getTalla());
            tvDetalles.setText(detalles);

            if (producto.getCantidad() != null) {
                tvCantidad.setText("x" + producto.getCantidad());
            } else {
                tvCantidad.setText("x0");
            }


            btnModificar.setOnClickListener(v -> listener.onModificarProducto(producto));
            btnEliminar.setOnClickListener(v -> listener.onEliminarProducto(producto, getAdapterPosition()));
        }
    }
}
