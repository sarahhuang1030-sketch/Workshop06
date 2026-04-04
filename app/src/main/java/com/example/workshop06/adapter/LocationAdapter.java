package com.example.workshop06.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop06.R;
import com.example.workshop06.model.LocationResponse;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    public interface LocationActionListener {
        void onEdit(LocationResponse item);
        void onDelete(LocationResponse item);
    }

    private final List<LocationResponse> items;
    private final LocationActionListener listener;

    public LocationAdapter(List<LocationResponse> items, LocationActionListener listener) {
        this.items = items != null ? items : new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<LocationResponse> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationResponse item = items.get(position);

        holder.tvName.setText(item.getLocationName());
        holder.tvType.setText(item.getLocationType());

        String city = item.getCity() == null ? "" : item.getCity();
        String province = item.getProvince() == null ? "" : item.getProvince();
        String phone = item.getPhone() == null ? "" : item.getPhone();

        String cityProvince;
        if (!city.isEmpty() && !province.isEmpty()) {
            cityProvince = city + ", " + province;
        } else if (!city.isEmpty()) {
            cityProvince = city;
        } else {
            cityProvince = province;
        }

        if (!phone.isEmpty()) {
            holder.tvCity.setText(cityProvince.isEmpty() ? phone : cityProvince + " • " + phone);
        } else {
            holder.tvCity.setText(cityProvince);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvCity;
        ImageButton btnEdit, btnDelete;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvLocationName);
            tvType = itemView.findViewById(R.id.tvLocationType);
            tvCity = itemView.findViewById(R.id.tvLocationCity);
            btnEdit = itemView.findViewById(R.id.btnEditLocation);
            btnDelete = itemView.findViewById(R.id.btnDeleteLocation);
        }
    }
}