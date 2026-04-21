package com.example.diet_trackerboom.models;

public class WeightEntry {
    private String id;
    private float weightKg;
    private String date;       // yyyy-MM-dd
    private long timestamp;

    public WeightEntry() {}

    public WeightEntry(float weightKg, String date) {
        this.weightKg = weightKg;
        this.date = date;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
