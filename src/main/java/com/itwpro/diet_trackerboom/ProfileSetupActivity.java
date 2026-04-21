package com.itwpro.diet_trackerboom;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diet_trackerboom.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileSetupActivity extends AppCompatActivity {

    private EditText etName, etAge, etHeight, etWeight;
    private RadioGroup rgGender;
    private Spinner spinnerGoal;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etName     = findViewById(R.id.etName);
        etAge      = findViewById(R.id.etAge);
        etHeight   = findViewById(R.id.etHeight);
        etWeight   = findViewById(R.id.etWeight);
        rgGender   = findViewById(R.id.rgGender);
        spinnerGoal= findViewById(R.id.spinnerGoal);
        btnSave    = findViewById(R.id.btnSave);
        progressBar= findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                this, R.array.goal_options, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String ageStr    = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
        if (TextUtils.isEmpty(ageStr)) { etAge.setError("Required"); return; }
        if (TextUtils.isEmpty(heightStr)) { etHeight.setError("Required"); return; }
        if (TextUtils.isEmpty(weightStr)) { etWeight.setError("Required"); return; }
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        int age      = Integer.parseInt(ageStr);
        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);

        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "male" : "female";

        String[] goalValues = {"lose", "maintain", "gain"};
        String goal = goalValues[spinnerGoal.getSelectedItemPosition()];

        String uid = mAuth.getCurrentUser().getUid();
        UserProfile profile = new UserProfile(uid, name, height, weight, age, gender, goal);

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        db.collection("users").document(uid)
                .collection("profile").document("data")
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
