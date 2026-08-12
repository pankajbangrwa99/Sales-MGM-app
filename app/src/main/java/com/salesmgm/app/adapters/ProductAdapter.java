package com.salesmgm.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.Product;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> products;
    private OnProductClickListener listener;

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void updateList(List<Product> newList) {
        this.products = newList;
        notifyDataSetChanged();
    }

    public Product getProductAt(int position) {
        if (products != null && position >= 0 && position < products.size()) {
            return products.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = products.get(position);
        holder.tvName.setText(p.getName());
        holder.tvCategory.setText(p.getCategory() != null ? p.getCategory() : "General");
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.2f", p.getPrice()));
        holder.tvStock.setText(String.format(Locale.getDefault(), "%d %s", p.getQuantity(), p.getUnit() != null ? p.getUnit() : "Pcs"));

        if (p.isLowStock()) {
            holder.tvLowStockBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvLowStockBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, tvStock, tvLowStockBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvProductCategory);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            tvLowStockBadge = itemView.findViewById(R.id.tvLowStockBadge);
        }
    }
}
