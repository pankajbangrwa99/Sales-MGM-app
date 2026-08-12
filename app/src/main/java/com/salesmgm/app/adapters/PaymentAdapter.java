package com.salesmgm.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.Payment;

import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private List<Payment> payments;

    public PaymentAdapter(List<Payment> payments) {
        this.payments = payments;
    }

    public void updateList(List<Payment> newList) {
        this.payments = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment p = payments.get(position);
        holder.tvCustomerName.setText(p.getCustomerName());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "₹%.2f", p.getAmountPaid()));
        holder.tvMode.setText(p.getPaymentMode());
        holder.tvTimestamp.setText(p.getTimestamp());
        holder.tvNote.setText(p.getNote() != null && !p.getNote().isEmpty() ? p.getNote() : "No Note");
    }

    @Override
    public int getItemCount() {
        return payments != null ? payments.size() : 0;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvAmount, tvMode, tvTimestamp, tvNote;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvPaymentCustomerName);
            tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
            tvMode = itemView.findViewById(R.id.tvPaymentMode);
            tvTimestamp = itemView.findViewById(R.id.tvPaymentTimestamp);
            tvNote = itemView.findViewById(R.id.tvPaymentNote);
        }
    }
}
