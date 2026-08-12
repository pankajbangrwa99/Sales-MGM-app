package com.salesmgm.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.StockLog;

import java.util.List;
import java.util.Locale;

public class StockLogAdapter extends RecyclerView.Adapter<StockLogAdapter.StockLogViewHolder> {

    public interface OnStockLogClickListener {
        void onStockLogClick(StockLog log);
    }

    private List<StockLog> logs;
    private OnStockLogClickListener listener;

    public StockLogAdapter(List<StockLog> logs, OnStockLogClickListener listener) {
        this.logs = logs;
        this.listener = listener;
    }

    public void updateList(List<StockLog> newList) {
        this.logs = newList;
        notifyDataSetChanged();
    }

    public StockLog getLogAt(int position) {
        if (logs != null && position >= 0 && position < logs.size()) {
            return logs.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public StockLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock_log, parent, false);
        return new StockLogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StockLogViewHolder holder, int position) {
        StockLog log = logs.get(position);
        holder.tvProductName.setText(log.getProductName() != null ? log.getProductName() : "Product");
        holder.tvInvoiceNo.setText(log.getInvoiceNumber() != null ? log.getInvoiceNumber() : "Stock Log");
        holder.tvTimestamp.setText(log.getTimestamp() != null ? log.getTimestamp() : "");
        holder.tvCustomer.setText("Customer: " + (log.getCustomerName() != null ? log.getCustomerName() : "Walk-in"));
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", log.getTotalAmount()));

        if ("STOCK_IN".equalsIgnoreCase(log.getMovementType())) {
            holder.tvTypeBadge.setText("📈 STOCK IN");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_emerald);
            holder.tvQty.setText("+" + log.getQuantity() + " Pcs");
            holder.tvQty.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvTypeBadge.setText("📉 STOCK OUT");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            holder.tvQty.setText("-" + log.getQuantity() + " Pcs");
            holder.tvQty.setTextColor(Color.parseColor("#EF4444"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStockLogClick(log);
        });
    }

    @Override
    public int getItemCount() {
        return logs != null ? logs.size() : 0;
    }

    static class StockLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvTypeBadge, tvInvoiceNo, tvTimestamp, tvProductName, tvQty, tvCustomer, tvTotal;

        public StockLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTypeBadge = itemView.findViewById(R.id.tvStockLogTypeBadge);
            tvInvoiceNo = itemView.findViewById(R.id.tvStockLogInvoiceNo);
            tvTimestamp = itemView.findViewById(R.id.tvStockLogTimestamp);
            tvProductName = itemView.findViewById(R.id.tvStockLogProductName);
            tvQty = itemView.findViewById(R.id.tvStockLogQty);
            tvCustomer = itemView.findViewById(R.id.tvStockLogCustomer);
            tvTotal = itemView.findViewById(R.id.tvStockLogTotal);
        }
    }
}
