package com.salesmgm.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.BillItem;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangeListener {
        boolean onIncreaseRequested(BillItem item, int position);
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    private List<BillItem> cartItems;
    private OnCartItemChangeListener listener;

    public CartAdapter(List<BillItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        BillItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvUnitPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getUnitPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getTotalPrice()));

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                boolean allowed = listener.onIncreaseRequested(item, holder.getAdapterPosition());
                if (!allowed) return;
            }
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(holder.getAdapterPosition());
                if (listener != null) listener.onQuantityChanged();
            } else {
                int pos = holder.getAdapterPosition();
                cartItems.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) listener.onItemRemoved(pos);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            cartItems.remove(pos);
            notifyItemRemoved(pos);
            if (listener != null) listener.onItemRemoved(pos);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUnitPrice, tvQuantity, tvTotalPrice;
        ImageButton btnIncrease, btnDecrease, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvUnitPrice = itemView.findViewById(R.id.tvCartItemUnitPrice);
            tvQuantity = itemView.findViewById(R.id.tvCartItemQty);
            tvTotalPrice = itemView.findViewById(R.id.tvCartItemTotalPrice);
            btnIncrease = itemView.findViewById(R.id.btnCartIncrease);
            btnDecrease = itemView.findViewById(R.id.btnCartDecrease);
            btnRemove = itemView.findViewById(R.id.btnCartRemove);
        }
    }
}
