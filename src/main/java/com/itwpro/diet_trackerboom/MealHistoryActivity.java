package com.itwpro.diet_trackerboom;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.itwpro.diet_trackerboom.adapters.FoodLogAdapter;
import com.itwpro.diet_trackerboom.models.FoodItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MealHistoryActivity extends AppCompatActivity {

    private TextView tvSelectedDate, tvDaySummary;
    private ImageButton btnPrevDay, btnNextDay;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private Calendar selectedCal;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<FoodItem> foodItems = new ArrayList<>();
    private FoodLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_history);

        mAuth  = FirebaseAuth.getInstance();
        db     = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            // Redirect to LoginActivity or handle the error
            finish();
            return;
        }

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvDaySummary   = findViewById(R.id.tvDaySummary);
        btnPrevDay     = findViewById(R.id.btnPrevDay);
        btnNextDay     = findViewById(R.id.btnNextDay);
        recyclerView   = findViewById(R.id.recyclerHistory);
        progressBar    = findViewById(R.id.progressBar);
        tvEmpty        = findViewById(R.id.tvEmpty);

        selectedCal = Calendar.getInstance();
        selectedCal.add(Calendar.DAY_OF_YEAR, -1); // Start at yesterday

        adapter = new FoodLogAdapter(this, foodItems, (item, pos) -> {});
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnPrevDay.setOnClickListener(v -> {
            selectedCal.add(Calendar.DAY_OF_YEAR, -1);
            loadDay();
        });

        btnNextDay.setOnClickListener(v -> {
            // Don't go past today
            Calendar today = Calendar.getInstance();
            today.add(Calendar.DAY_OF_YEAR, -1);
            if (!selectedCal.after(today)) {
                selectedCal.add(Calendar.DAY_OF_YEAR, 1);
                loadDay();
            }
        });

        loadDay();
    }

    private void loadDay() {
        String date = sdf.format(selectedCal.getTime());
        tvSelectedDate.setText(date);
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("users").document(userId)
                .collection("foodLogs").document(date)
                .collection("meals")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    foodItems.clear();
                    int totalCal = 0; float totalCarbs = 0, totalProt = 0, totalFat = 0;

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            foodItems.add(item);
                            totalCal   += item.getCalories();
                            totalCarbs += item.getCarbs();
                            totalProt  += item.getProtein();
                            totalFat   += item.getFat();
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (foodItems.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvDaySummary.setText("No data");
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        tvDaySummary.setText(String.format(
                                "Total: %d kcal  |  C: %.0fg  P: %.0fg  F: %.0fg",
                                totalCal, totalCarbs, totalProt, totalFat));
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}
