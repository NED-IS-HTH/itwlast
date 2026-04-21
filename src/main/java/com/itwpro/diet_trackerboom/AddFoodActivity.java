package com.itwpro.diet_trackerboom;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.itwpro.diet_trackerboom.models.FoodItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddFoodActivity extends AppCompatActivity {

    private EditText etFoodName, etCalories, etCarbs, etProtein, etFat;
    private Spinner spinnerMealType;
    private Button btnSave, btnCancel;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        etFoodName     = findViewById(R.id.etFoodName);
        etCalories     = findViewById(R.id.etCalories);
        etCarbs        = findViewById(R.id.etCarbs);
        etProtein      = findViewById(R.id.etProtein);
        etFat          = findViewById(R.id.etFat);
        spinnerMealType= findViewById(R.id.spinnerMealType);
        btnSave        = findViewById(R.id.btnSave);
        btnCancel      = findViewById(R.id.btnCancel);
        progressBar    = findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> mealAdapter = ArrayAdapter.createFromResource(
                this, R.array.meal_types, android.R.layout.simple_spinner_item);
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(mealAdapter);

        btnSave.setOnClickListener(v -> saveFood());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveFood() {
        String name = etFoodName.getText().toString().trim();
        String calStr  = etCalories.getText().toString().trim();
        String carbStr = etCarbs.getText().toString().trim();
        String protStr = etProtein.getText().toString().trim();
        String fatStr  = etFat.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etFoodName.setError("Required"); return; }
        if (TextUtils.isEmpty(calStr)) { etCalories.setError("Required"); return; }

        int calories  = Integer.parseInt(calStr);
        float carbs   = TextUtils.isEmpty(carbStr) ? 0 : Float.parseFloat(carbStr);
        float protein = TextUtils.isEmpty(protStr) ? 0 : Float.parseFloat(protStr);
        float fat     = TextUtils.isEmpty(fatStr)  ? 0 : Float.parseFloat(fatStr);

        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        String mealType = mealTypes[spinnerMealType.getSelectedItemPosition()];

        FoodItem item = new FoodItem(name, calories, carbs, protein, fat, mealType, today);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .collection("foodLogs").document(today)
                .collection("meals")
                .add(item)
                .addOnSuccessListener(ref -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Food added!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
