package com.example.diet_trackerboom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.itwpro.diet_trackerboom.R;
import com.example.diet_trackerboom.models.WeightEntry;
import java.util.List;

public class WeightLogAdapter extends RecyclerView.Adapter<WeightLogAdapter.ViewHolder> {

    private Context context;
    private List<WeightEntry> entries;

    public WeightLogAdapter(Context context, List<WeightEntry> entries) {
        this.context = context;
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_weight_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightEntry entry = entries.get(position);
        holder.tvDate.setText(entry.getDate());
        holder.tvWeight.setText(String.format("%.1f kg", entry.getWeightKg()));
    }

    @Override
    public int getItemCount() { return entries.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeight;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate   = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
        }
    }
}
