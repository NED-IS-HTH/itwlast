package com.itwpro.diet_trackerboom;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diet_trackerboom.adapters.WeightLogAdapter;
import com.example.diet_trackerboom.models.WeightEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProgressActivity extends AppCompatActivity {

    private LineChart weightChart;
    private BarChart calorieChart;
    private EditText etWeight;
    private Button btnLogWeight;
    private RecyclerView recyclerWeight;
    private ProgressBar progressBar;
    private TextView tvBMI, tvCurrentWeight;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private List<WeightEntry> weightEntries = new ArrayList<>();
    private WeightLogAdapter weightAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        mAuth  = FirebaseAuth.getInstance();
        db     = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        weightChart    = findViewById(R.id.weightChart);
        calorieChart   = findViewById(R.id.calorieChart);
        etWeight       = findViewById(R.id.etWeight);
        btnLogWeight   = findViewById(R.id.btnLogWeight);
        recyclerWeight = findViewById(R.id.recyclerWeightLog);
        progressBar    = findViewById(R.id.progressBar);
        tvBMI          = findViewById(R.id.tvBMI);
        tvCurrentWeight= findViewById(R.id.tvCurrentWeight);

        weightAdapter = new WeightLogAdapter(this, weightEntries);
        recyclerWeight.setLayoutManager(new LinearLayoutManager(this));
        recyclerWeight.setAdapter(weightAdapter);

        btnLogWeight.setOnClickListener(v -> logWeight());

        loadWeightLog();
        loadWeeklyCalories();
        loadProfileForBMI();
    }

    private void logWeight() {
        String weightStr = etWeight.getText().toString().trim();
        if (weightStr.isEmpty()) { etWeight.setError("Enter weight"); return; }

        float weight = Float.parseFloat(weightStr);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WeightEntry entry = new WeightEntry(weight, today);

        progressBar.setVisibility(View.VISIBLE);
        btnLogWeight.setEnabled(false);

        db.collection("users").document(userId)
                .collection("weightLogs")
                .add(entry)
                .addOnSuccessListener(ref -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogWeight.setEnabled(true);
                    etWeight.setText("");
                    Toast.makeText(this, "Weight logged!", Toast.LENGTH_SHORT).show();

                    // Update profile weight too
                    Map<String, Object> update = new HashMap<>();
                    update.put("weightKg", weight);
                    db.collection("users").document(userId)
                            .collection("profile").document("data").update(update);

                    loadWeightLog();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogWeight.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWeightLog() {
        db.collection("users").document(userId)
                .collection("weightLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(snapshots -> {
                    weightEntries.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        WeightEntry e = doc.toObject(WeightEntry.class);
                        if (e != null) { e.setId(doc.getId()); weightEntries.add(e); }
                    }
                    weightAdapter.notifyDataSetChanged();
                    if (!weightEntries.isEmpty()) {
                        tvCurrentWeight.setText(String.format("Current: %.1f kg", weightEntries.get(0).getWeightKg()));
                        buildWeightChart();
                    }
                });
    }

    private void buildWeightChart() {
        // Reverse to show oldest → newest
        List<WeightEntry> reversed = new ArrayList<>(weightEntries);
        Collections.reverse(reversed);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < reversed.size(); i++) {
            entries.add(new Entry(i, reversed.get(i).getWeightKg()));
            String date = reversed.get(i).getDate();
            labels.add(date.substring(5)); // MM-dd
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weight (kg)");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#A5D6A7"));

        LineData lineData = new LineData(dataSet);
        weightChart.setData(lineData);
        weightChart.getDescription().setEnabled(false);
        weightChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        weightChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weightChart.getXAxis().setGranularity(1f);
        weightChart.getXAxis().setLabelRotationAngle(-45f);
        weightChart.getAxisRight().setEnabled(false);
        weightChart.animateX(1000);
        weightChart.invalidate();
    }

    private void loadWeeklyCalories() {
        List<String> last7Days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 6; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_YEAR, -i);
            last7Days.add(sdf.format(c.getTime()));
        }

        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        final int[] loaded = {0};

        for (int i = 0; i < last7Days.size(); i++) {
            String date = last7Days.get(i);
            labels.add(date.substring(5));
            final int index = i;

            db.collection("users").document(userId)
                    .collection("dailyLogs").document(date)
                    .get()
                    .addOnSuccessListener(doc -> {
                        float calories = 0;
                        if (doc.exists() && doc.getLong("totalCalories") != null) {
                            calories = doc.getLong("totalCalories").floatValue();
                        }
                        barEntries.add(new BarEntry(index, calories));
                        loaded[0]++;
                        if (loaded[0] == 7) buildCalorieChart(barEntries, labels);
                    });
        }
    }

    private void buildCalorieChart(List<BarEntry> entries, List<String> labels) {
        // Sort by x
        entries.sort((a, b) -> Float.compare(a.getX(), b.getX()));

        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        calorieChart.setData(barData);
        calorieChart.getDescription().setEnabled(false);
        calorieChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        calorieChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        calorieChart.getXAxis().setGranularity(1f);
        calorieChart.getXAxis().setLabelRotationAngle(-45f);
        calorieChart.getAxisRight().setEnabled(false);
        calorieChart.animateY(800);
        calorieChart.invalidate();
    }

    private void loadProfileForBMI() {
        db.collection("users").document(userId)
                .collection("profile").document("data")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double weight = doc.getDouble("weightKg");
                        Double height = doc.getDouble("heightCm");
                        if (weight != null && height != null && height > 0) {
                            float heightM = (float)(height / 100.0);
                            float bmi = (float)(weight / (heightM * heightM));
                            String category;
                            if (bmi < 18.5) category = "Underweight";
                            else if (bmi < 25) category = "Normal";
                            else if (bmi < 30) category = "Overweight";
                            else category = "Obese";
                            tvBMI.setText(String.format("BMI: %.1f (%s)", bmi, category));
                        }
                    }
                });
    }
}
