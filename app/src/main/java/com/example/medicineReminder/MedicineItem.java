package com.example.medicineReminder;

import java.util.Calendar;

public class MedicineItem {
    private String name;
    private String dosage;
    private Calendar time;
    private int id;

    public MedicineItem(String name, String dosage, Calendar time, int id) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.id = id;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public Calendar getTime() { return time; }
    public int getId() { return id; }

    @Override
    public String toString() {
        String dosageStr = (dosage != null && !dosage.isEmpty()) ? dosage : "Без дозировки";
        return name + " - " + dosageStr + " - " + time.getTime().toString();
    }
}