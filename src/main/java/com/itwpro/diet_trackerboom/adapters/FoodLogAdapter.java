package com.itwpro.diet_trackerboom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.itwpro.diet_trackerboom.R;
import com.itwpro.diet_trackerboom.models.FoodItem;
import java.util.List;

public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.ViewHolder> {

    private Context context;
    private List<FoodItem> items;
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDelete(FoodItem item, int position);
    }

    public FoodLogAdapter(Context context, List<FoodItem> items, OnDeleteClickListener listener) {
        this.context = context;
        this.items = items;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_food_log, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvMealType.setText(item.getMealType());
        holder.tvCalories.setText(item.getCalories() + " kcal");
        holder.tvMacros.setText(String.format("C: %.0fg  P: %.0fg  F: %.0fg",
                item.getCarbs(), item.getProtein(), item.getFat()));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item, position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMealType, tvCalories, tvMacros;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvFoodName);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvMacros   = itemView.findViewById(R.id.tvMacros);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}
