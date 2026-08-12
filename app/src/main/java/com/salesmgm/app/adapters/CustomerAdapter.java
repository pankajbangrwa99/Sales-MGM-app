package com.salesmgm.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salesmgm.app.R;
import com.salesmgm.app.models.Customer;

import java.util.List;
import java.util.Locale;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    public interface OnCustomerActionListener {
        void onCustomerClick(Customer customer);
        void onSendSmsClick(Customer customer);
    }

    private List<Customer> customers;
    private OnCustomerActionListener listener;

    public CustomerAdapter(List<Customer> customers, OnCustomerActionListener listener) {
        this.customers = customers;
        this.listener = listener;
    }

    public void updateList(List<Customer> newList) {
        this.customers = newList;
        notifyDataSetChanged();
    }

    public Customer getCustomerAt(int position) {
        if (customers != null && position >= 0 && position < customers.size()) {
            return customers.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer c = customers.get(position);
        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone() != null ? c.getPhone() : "No Phone");
        holder.tvAddress.setText(c.getAddress() != null ? c.getAddress() : "No Address");
        holder.tvCreditBalance.setText(String.format(Locale.getDefault(), "₹%.2f", c.getCreditBalance()));

        if (c.getCreditBalance() > 0) {
            holder.tvCreditBalance.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.warning));
            holder.btnSendSms.setVisibility(View.VISIBLE);
        } else {
            holder.tvCreditBalance.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.emerald));
            holder.btnSendSms.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCustomerClick(c);
        });

        holder.btnSendSms.setOnClickListener(v -> {
            if (listener != null) listener.onSendSmsClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return customers != null ? customers.size() : 0;
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvCreditBalance;
        Button btnSendSms;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCustomerName);
            tvPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvAddress = itemView.findViewById(R.id.tvCustomerAddress);
            tvCreditBalance = itemView.findViewById(R.id.tvCustomerCreditBalance);
            btnSendSms = itemView.findViewById(R.id.btnSendSmsReminder);
        }
    }
}
