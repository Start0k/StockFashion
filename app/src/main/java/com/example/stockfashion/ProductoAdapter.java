package com.example.stockfashion;

import com.example.stockfashion.Producto;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;

    // Constructor para recibir la lista de productos
    public ProductoAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    /**
     * Crea nuevas vistas (invocado por el layout manager).
     * Aquí se "infla" el layout XML de cada item.
     */
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea una nueva vista desde nuestro layout item_producto.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    /**
     * Reemplaza el contenido de una vista (invocado por el layout manager).
     * Aquí se asignan los datos del producto a las vistas (TextViews).
     */
    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        // Obtiene el producto de la lista en la posición actual
        Producto producto = listaProductos.get(position);

        // Asigna los datos del producto a los TextViews del ViewHolder
        holder.nombreTextView.setText(producto.getNombre());
        holder.marcaTextView.setText(producto.getMarca());
        holder.tipoTextView.setText(producto.getTipo());
        holder.tallaTextView.setText("Talla: " + producto.getTalla());
        holder.cantidadTextView.setText("Cantidad: " + producto.getCantidad());
    }

    /**
     * Devuelve el tamaño de tu dataset (invocado por el layout manager).
     */
    @Override
    public int getItemCount() {
        return listaProductos.size();
    }


    /**
     * ViewHolder: Representa cada elemento de la lista y contiene las referencias a sus vistas (TextViews).
     * Esto evita hacer findViewById() repetidamente, mejorando el rendimiento.
     */
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
