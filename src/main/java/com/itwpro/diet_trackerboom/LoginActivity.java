package com.itwpro.diet_trackerboom;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signupLink;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        emailInput   = findViewById(R.id.emailInput);
        passwordInput= findViewById(R.id.passwordInput);
        loginButton  = findViewById(R.id.loginButton);
        signupLink   = findViewById(R.id.signupLink);
        progressBar  = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> loginUser());

        signupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private void loginUser() {
        String email    = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Check if profile is set up — route accordingly
                        db.collection("users").document(uid)
                                .collection("profile").document("data")
                                .get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        startActivity(new Intent(this, HomeActivity.class));
                                    } else {
                                        startActivity(new Intent(this, ProfileSetupActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    startActivity(new Intent(this, HomeActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
