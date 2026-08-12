package com.salesmgm.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.Bill;

import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    public interface OnBillClickListener {
        void onBillClick(Bill bill);
    }

    private List<Bill> bills;
    private OnBillClickListener listener;

    public BillAdapter(List<Bill> bills, OnBillClickListener listener) {
        this.bills = bills;
        this.listener = listener;
    }

    public void updateList(List<Bill> newList) {
        this.bills = newList;
        notifyDataSetChanged();
    }

    public Bill getBillAt(int position) {
        if (bills != null && position >= 0 && position < bills.size()) {
            return bills.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill b = bills.get(position);
        holder.tvInvoiceNo.setText(b.getInvoiceNumber());
        holder.tvCustomerName.setText(b.getCustomerName() != null ? b.getCustomerName() : "Walk-in Customer");
        holder.tvTimestamp.setText(b.getTimestamp());
        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", b.getTotalAmount()));
        holder.tvPaymentMode.setText(b.getPaymentMode());

        if (b.getDueAmount() > 0) {
            holder.tvDueAmount.setVisibility(View.VISIBLE);
            holder.tvDueAmount.setText(String.format(Locale.getDefault(), "Due: ₹%.2f", b.getDueAmount()));
        } else {
            holder.tvDueAmount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBillClick(b);
        });
    }

    @Override
    public int getItemCount() {
        return bills != null ? bills.size() : 0;
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNo, tvCustomerName, tvTimestamp, tvTotalAmount, tvPaymentMode, tvDueAmount;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNo = itemView.findViewById(R.id.tvBillInvoiceNo);
            tvCustomerName = itemView.findViewById(R.id.tvBillCustomerName);
            tvTimestamp = itemView.findViewById(R.id.tvBillTimestamp);
            tvTotalAmount = itemView.findViewById(R.id.tvBillTotalAmount);
            tvPaymentMode = itemView.findViewById(R.id.tvBillPaymentMode);
            tvDueAmount = itemView.findViewById(R.id.tvBillDueAmount);
        }
    }
}
