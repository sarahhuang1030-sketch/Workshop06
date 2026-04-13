package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.InvoiceResponse;

import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    public interface InvoiceActionListener {
        void onView(InvoiceResponse item);
    }

    private final List<InvoiceResponse> items;
    private final InvoiceActionListener listener;

    public InvoiceAdapter(List<InvoiceResponse> items, InvoiceActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setData(List<InvoiceResponse> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice, parent, false);
        return new InvoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        InvoiceResponse item = items.get(position);

        holder.tvInvoiceNumber.setText(
                item.getInvoiceNumber() != null ? item.getInvoiceNumber() : "—");
        holder.tvCustomer.setText(
                item.getCustomerName() != null ? item.getCustomerName() : "—");
        holder.tvStatus.setText(
                item.getStatus() != null ? item.getStatus() : "—");
        holder.tvTotal.setText(item.getTotal() != null
                ? String.format(Locale.US, "$%.2f", item.getTotal()) : "$0.00");

        holder.btnView.setOnClickListener(v -> listener.onView(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoiceNumber, tvCustomer, tvStatus, tvTotal;
        ImageButton btnView;

        public InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvoiceNumber = itemView.findViewById(R.id.tvInvoiceNumber);
            tvCustomer      = itemView.findViewById(R.id.tvInvoiceCustomer);
            tvStatus        = itemView.findViewById(R.id.tvInvoiceStatus);
            tvTotal         = itemView.findViewById(R.id.tvInvoiceTotal);
            btnView         = itemView.findViewById(R.id.btnViewInvoice);
        }
    }
}