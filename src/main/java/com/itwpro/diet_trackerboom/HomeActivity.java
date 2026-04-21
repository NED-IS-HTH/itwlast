package com.itwpro.diet_trackerboom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.itwpro.diet_trackerboom.adapters.FoodLogAdapter;
import com.itwpro.diet_trackerboom.models.FoodItem;
import com.example.diet_trackerboom.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting, tvDate, tvCaloriesConsumed, tvCaloriesTarget,
            tvCaloriesRemaining, tvCarbs, tvProtein, tvFat,
            tvWaterCount, tvStreak, tvCarbsTarget, tvProteinTarget, tvFatTarget;
    private ProgressBar progressCalories, progressCarbs, progressProtein, progressFat;
    private ImageButton btnAddWater, btnRemoveWater;
    private Button btnAddFood, btnHistory, btnProgress, btnLogout;
    private RecyclerView recyclerView;
    private ProgressBar loadingBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId, today;
    private UserProfile userProfile;
    private List<FoodItem> foodItems = new ArrayList<>();
    private FoodLogAdapter adapter;
    private int waterGlasses = 0;
    private ListenerRegistration foodListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth  = FirebaseAuth.getInstance();
        db     = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        today  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        initViews();
        loadProfile();
    }

    private void initViews() {
        tvGreeting         = findViewById(R.id.tvGreeting);
        tvDate             = findViewById(R.id.tvDate);
        tvCaloriesConsumed = findViewById(R.id.tvCaloriesConsumed);
        tvCaloriesTarget   = findViewById(R.id.tvCaloriesTarget);
        tvCaloriesRemaining= findViewById(R.id.tvCaloriesRemaining);
        tvCarbs            = findViewById(R.id.tvCarbs);
        tvProtein          = findViewById(R.id.tvProtein);
        tvFat              = findViewById(R.id.tvFat);
        tvCarbsTarget      = findViewById(R.id.tvCarbsTarget);
        tvProteinTarget    = findViewById(R.id.tvProteinTarget);
        tvFatTarget        = findViewById(R.id.tvFatTarget);
        tvWaterCount       = findViewById(R.id.tvWaterCount);
        tvStreak           = findViewById(R.id.tvStreak);
        progressCalories   = findViewById(R.id.progressCalories);
        progressCarbs      = findViewById(R.id.progressCarbs);
        progressProtein    = findViewById(R.id.progressProtein);
        progressFat        = findViewById(R.id.progressFat);
        btnAddWater        = findViewById(R.id.btnAddWater);
        btnRemoveWater     = findViewById(R.id.btnRemoveWater);
        btnAddFood         = findViewById(R.id.btnAddFood);
        btnHistory         = findViewById(R.id.btnHistory);
        btnProgress        = findViewById(R.id.btnProgress);
        btnLogout          = findViewById(R.id.btnLogout);
        recyclerView       = findViewById(R.id.recyclerFoodLog);
        loadingBar         = findViewById(R.id.loadingBar);

        tvDate.setText(today);

        adapter = new FoodLogAdapter(this, foodItems, (item, position) -> deleteFoodItem(item, position));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddFood.setOnClickListener(v ->
                startActivity(new Intent(this, AddFoodActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, MealHistoryActivity.class)));

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));

        btnAddWater.setOnClickListener(v -> updateWater(waterGlasses + 1));
        btnRemoveWater.setOnClickListener(v -> {
            if (waterGlasses > 0) updateWater(waterGlasses - 1);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadProfile() {
        loadingBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId)
                .collection("profile").document("data")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userProfile = doc.toObject(UserProfile.class);
                        tvGreeting.setText("Hello, " + userProfile.getName() + "!");
                        tvCaloriesTarget.setText("/ " + userProfile.getTargetCalories() + " kcal");
                        tvCarbsTarget.setText("/ " + userProfile.getTargetCarbs() + "g");
                        tvProteinTarget.setText("/ " + userProfile.getTargetProtein() + "g");
                        tvFatTarget.setText("/ " + userProfile.getTargetFat() + "g");
                        tvStreak.setText("🔥 " + userProfile.getStreakDays() + " day streak");
                        progressCalories.setMax(userProfile.getTargetCalories());
                        progressCarbs.setMax(userProfile.getTargetCarbs());
                        progressProtein.setMax(userProfile.getTargetProtein());
                        progressFat.setMax(userProfile.getTargetFat());
                    }
                    loadTodayLog();
                });
    }

    private void loadTodayLog() {
        // Load water first
        db.collection("users").document(userId)
                .collection("dailyLogs").document(today)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getLong("waterGlasses") != null) {
                        waterGlasses = doc.getLong("waterGlasses").intValue();
                        tvWaterCount.setText(waterGlasses + " / 8 glasses");
                    }
                });

        // Live listener for food items
        if (foodListener != null) foodListener.remove();
        foodListener = db.collection("users").document(userId)
                .collection("foodLogs").document(today)
                .collection("meals")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    loadingBar.setVisibility(View.GONE);
                    if (e != null || snapshots == null) return;

                    foodItems.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            foodItems.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateTotals();
                    updateStreak();
                });
    }

    private void updateTotals() {
        int totalCal = 0; float totalCarbs = 0, totalProtein = 0, totalFat = 0;
        for (FoodItem item : foodItems) {
            totalCal     += item.getCalories();
            totalCarbs   += item.getCarbs();
            totalProtein += item.getProtein();
            totalFat     += item.getFat();
        }

        tvCaloriesConsumed.setText(String.valueOf(totalCal));
        tvCarbs.setText(String.format("%.0fg", totalCarbs));
        tvProtein.setText(String.format("%.0fg", totalProtein));
        tvFat.setText(String.format("%.0fg", totalFat));

        if (userProfile != null) {
            int remaining = userProfile.getTargetCalories() - totalCal;
            tvCaloriesRemaining.setText(remaining + " remaining");
            progressCalories.setProgress(Math.min(totalCal, userProfile.getTargetCalories()));
            progressCarbs.setProgress(Math.min((int) totalCarbs, userProfile.getTargetCarbs()));
            progressProtein.setProgress(Math.min((int) totalProtein, userProfile.getTargetProtein()));
            progressFat.setProgress(Math.min((int) totalFat, userProfile.getTargetFat()));
        }

        // Save daily summary to Firestore
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCalories", totalCal);
        summary.put("totalCarbs", totalCarbs);
        summary.put("totalProtein", totalProtein);
        summary.put("totalFat", totalFat);
        summary.put("waterGlasses", waterGlasses);
        summary.put("date", today);
        db.collection("users").document(userId)
                .collection("dailyLogs").document(today).set(summary);
    }

    private void updateWater(int newCount) {
        waterGlasses = newCount;
        tvWaterCount.setText(waterGlasses + " / 8 glasses");
        Map<String, Object> data = new HashMap<>();
        data.put("waterGlasses", waterGlasses);
        data.put("date", today);
        db.collection("users").document(userId)
                .collection("dailyLogs").document(today)
                .update(data)
                .addOnFailureListener(e ->
                        db.collection("users").document(userId)
                                .collection("dailyLogs").document(today).set(data));
    }

    private void deleteFoodItem(FoodItem item, int position) {
        db.collection("users").document(userId)
                .collection("foodLogs").document(today)
                .collection("meals").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    foodItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateTotals();
                });
    }

    private void updateStreak() {
        if (userProfile == null || foodItems.isEmpty()) return;

        String lastDate = userProfile.getLastLogDate();
        if (today.equals(lastDate)) return; // Already counted today

        // Check if yesterday was logged
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        int newStreak;
        if (yesterday.equals(lastDate)) {
            newStreak = userProfile.getStreakDays() + 1;
        } else if (lastDate == null || lastDate.isEmpty()) {
            newStreak = 1;
        } else {
            newStreak = 1; // streak broken
        }

        userProfile.setStreakDays(newStreak);
        userProfile.setLastLogDate(today);
        tvStreak.setText("🔥 " + newStreak + " day streak");

        Map<String, Object> updates = new HashMap<>();
        updates.put("streakDays", newStreak);
        updates.put("lastLogDate", today);
        db.collection("users").document(userId)
                .collection("profile").document("data")
                .update(updates);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (foodListener != null) foodListener.remove();
    }
}
