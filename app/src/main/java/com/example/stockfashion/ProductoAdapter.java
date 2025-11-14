package com.example.stockfashion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;


    public ProductoAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }


    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {

        Producto producto = listaProductos.get(position);


        holder.nombreTextView.setText(producto.getNombre());
        holder.marcaTextView.setText(producto.getMarca());
        holder.tipoTextView.setText(producto.getTipo());
        holder.tallaTextView.setText("Talla: " + producto.getTalla());
        holder.cantidadTextView.setText("Cantidad: " + producto.getCantidad());
    }


    @Override
    public int getItemCount() {
        return listaProductos.size();
    }



    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreTextView;
        public TextView marcaTextView;
        public TextView tipoTextView;
        public TextView tallaTextView;
        public TextView cantidadTextView;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.textViewNombre);
            marcaTextView = itemView.findViewById(R.id.textViewMarca);
            tipoTextView = itemView.findViewById(R.id.textViewTipo);
            tallaTextView = itemView.findViewById(R.id.textViewTalla);
            cantidadTextView = itemView.findViewById(R.id.textViewCantidad);
        }
    }
}
