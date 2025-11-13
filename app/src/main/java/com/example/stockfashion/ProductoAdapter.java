package com.example.stockfashion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;

    // Constructor que recibe la lista de datos
    public ProductoAdapter(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    // Crea la vista para cada item (infla el layout item_producto.xml)
    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    // Vincula los datos de un producto específico a una vista (ViewHolder)
    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto);
    }

    // Retorna el número total de items en la lista
    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    // Clase interna que representa cada "fila" de la lista
    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDetalles, tvCantidad;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvDetalles = itemView.findViewById(R.id.tvDetallesProducto);
            tvCantidad = itemView.findViewById(R.id.tvCantidadProducto);
        }

        // Método para llenar los TextViews con los datos del producto
        public void bind(Producto producto) {
            tvNombre.setText(producto.getNombre());
            String detalles = String.format(Locale.getDefault(), "Marca: %s - Tipo: %s - Talla: %s",
                    producto.getMarca(),
                    producto.getTipo(),
                    producto.getTalla());
            tvDetalles.setText(detalles);

            // Se verifica que cantidad no sea null
            if (producto.getCantidad() != null) {
                tvCantidad.setText("x" + producto.getCantidad());
            } else {
                tvCantidad.setText("x0");
            }
        }
    }
}
