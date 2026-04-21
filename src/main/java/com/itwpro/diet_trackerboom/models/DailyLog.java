package com.example.diet_trackerboom.models;

public class DailyLog {
    private String date;         // yyyy-MM-dd
    private int totalCalories;
    private float totalCarbs;
    private float totalProtein;
    private float totalFat;
    private int waterGlasses;

    public DailyLog() {}

    public DailyLog(String date) {
        this.date = date;
        this.totalCalories = 0;
        this.totalCarbs = 0;
        this.totalProtein = 0;
        this.totalFat = 0;
        this.waterGlasses = 0;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
    public float getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(float totalCarbs) { this.totalCarbs = totalCarbs; }
    public float getTotalProtein() { return totalProtein; }
    public void setTotalProtein(float totalProtein) { this.totalProtein = totalProtein; }
    public float getTotalFat() { return totalFat; }
    public void setTotalFat(float totalFat) { this.totalFat = totalFat; }
    public int getWaterGlasses() { return waterGlasses; }
    public void setWaterGlasses(int waterGlasses) { this.waterGlasses = waterGlasses; }
}
