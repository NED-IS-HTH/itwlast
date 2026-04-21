package com.example.diet_trackerboom.models;

public class UserProfile {
    private String uid;
    private String name;
    private float heightCm;
    private float weightKg;
    private int age;
    private String gender;       // "male" or "female"
    private String goal;         // "lose", "maintain", "gain"
    private int targetCalories;
    private int targetCarbs;
    private int targetProtein;
    private int targetFat;
    private int targetWaterGlasses;
    private int streakDays;
    private String lastLogDate;  // yyyy-MM-dd

    public UserProfile() {}

    public UserProfile(String uid, String name, float heightCm, float weightKg,
                       int age, String gender, String goal) {
        this.uid = uid;
        this.name = name;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.age = age;
        this.gender = gender;
        this.goal = goal;
        this.targetWaterGlasses = 8;
        this.streakDays = 0;
        calculateTargets();
    }

    // BMR using Mifflin-St Jeor equation, moderate activity multiplier
    public void calculateTargets() {
        float bmr;
        if ("male".equals(gender)) {
            bmr = (10 * weightKg) + (6.25f * heightCm) - (5 * age) + 5;
        } else {
            bmr = (10 * weightKg) + (6.25f * heightCm) - (5 * age) - 161;
        }
        float tdee = bmr * 1.55f; // moderate activity

        if ("lose".equals(goal))       targetCalories = (int)(tdee - 500);
        else if ("gain".equals(goal))  targetCalories = (int)(tdee + 300);
        else                            targetCalories = (int) tdee;

        // Macro split: 50% carbs, 25% protein, 25% fat
        targetCarbs   = (int)((targetCalories * 0.50f) / 4);
        targetProtein = (int)((targetCalories * 0.25f) / 4);
        targetFat     = (int)((targetCalories * 0.25f) / 9);
    }

    // Getters & setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getHeightCm() { return heightCm; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }
    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }
    public int getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(int targetCarbs) { this.targetCarbs = targetCarbs; }
    public int getTargetProtein() { return targetProtein; }
    public void setTargetProtein(int targetProtein) { this.targetProtein = targetProtein; }
    public int getTargetFat() { return targetFat; }
    public void setTargetFat(int targetFat) { this.targetFat = targetFat; }
    public int getTargetWaterGlasses() { return targetWaterGlasses; }
    public void setTargetWaterGlasses(int targetWaterGlasses) { this.targetWaterGlasses = targetWaterGlasses; }
    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }
    public String getLastLogDate() { return lastLogDate; }
    public void setLastLogDate(String lastLogDate) { this.lastLogDate = lastLogDate; }
}
