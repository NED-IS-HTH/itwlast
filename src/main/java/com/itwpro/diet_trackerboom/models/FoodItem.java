package com.itwpro.diet_trackerboom.models;

public class FoodItem {
    private String id;
    private String name;
    private int calories;
    private float carbs;
    private float protein;
    private float fat;
    private String mealType;   // "Breakfast", "Lunch", "Dinner", "Snack"
    private String date;       // yyyy-MM-dd
    private long timestamp;

    public FoodItem() {}

    public FoodItem(String name, int calories, float carbs, float protein,
                    float fat, String mealType, String date) {
        this.name = name;
        this.calories = calories;
        this.carbs = carbs;
        this.protein = protein;
        this.fat = fat;
        this.mealType = mealType;
        this.date = date;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public float getCarbs() { return carbs; }
    public void setCarbs(float carbs) { this.carbs = carbs; }
    public float getProtein() { return protein; }
    public void setProtein(float protein) { this.protein = protein; }
    public float getFat() { return fat; }
    public void setFat(float fat) { this.fat = fat; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
