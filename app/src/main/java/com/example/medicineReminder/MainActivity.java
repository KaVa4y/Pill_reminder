package com.example.medicineReminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_MEDICINE = 100;

    private ListView listView;
    private Button btnAdd;
    private List<MedicineItem> medicineList;
    private MedicineAdapter adapter;
    private int nextId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewMedicines);
        btnAdd = findViewById(R.id.btnAddMedicine);

        medicineList = new ArrayList<>();
        adapter = new MedicineAdapter(this, medicineList);
        listView.setAdapter(adapter);

        loadMedicines();

        btnAdd.setOnClickListener(v -> openAddMedicineActivity());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MedicineItem item = medicineList.get(position);
            Toast.makeText(this, "Удалить: " + item.getName(), Toast.LENGTH_SHORT).show();
            removeMedicine(item);
        });
    }

    private void openAddMedicineActivity() {
        Intent intent = new Intent(this, AddMedicineActivity.class);
        startActivityForResult(intent, REQUEST_ADD_MEDICINE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_MEDICINE && resultCode == RESULT_OK) {
            String name = data.getStringExtra("MEDICINE_NAME");
            String dosage = data.getStringExtra("MEDICINE_DOSAGE");
            int hour = data.getIntExtra("MEDICINE_TIME_HOUR", -1);
            int minute = data.getIntExtra("MEDICINE_TIME_MINUTE", -1);

            if (hour != -1 && minute != -1) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);

                MedicineItem newItem = new MedicineItem(name, dosage, selectedTime, nextId++);
                medicineList.add(newItem);
                adapter.notifyDataSetChanged();

                saveMedicine(newItem);
                MedicineScheduler.scheduleAlarm(this, name, dosage, selectedTime, newItem.getId());

                Toast.makeText(this, "Напоминание добавлено!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeMedicine(MedicineItem item) {
        medicineList.remove(item);
        adapter.notifyDataSetChanged();
        MedicineScheduler.cancelAlarm(this, item.getId());
        deleteMedicine(item);
    }

    // --- Упрощённое хранение в SharedPreferences ---
    private void saveMedicine(MedicineItem item) {
        SharedPreferences sp = getSharedPreferences("medicines", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("medicine_name_" + item.getId(), item.getName());
        editor.putString("medicine_dosage_" + item.getId(), item.getDosage());
        editor.putLong("medicine_time_" + item.getId(), item.getTime().getTimeInMillis());
        editor.apply();
    }

    private void loadMedicines() {
        SharedPreferences sp = getSharedPreferences("medicines", Context.MODE_PRIVATE);
        for (int i = 1; i <= nextId; i++) {
            String name = sp.getString("medicine_name_" + i, null);
            String dosage = sp.getString("medicine_dosage_" + i, null);
            long timeMillis = sp.getLong("medicine_time_" + i, -1);
            if (name != null && timeMillis != -1) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(timeMillis);
                MedicineItem item = new MedicineItem(name, dosage, cal, i);
                medicineList.add(item);
                MedicineScheduler.scheduleAlarm(this, name, dosage, cal, i);
            }
        }
        nextId = medicineList.isEmpty() ? 1 : medicineList.get(medicineList.size() - 1).getId() + 1;
    }

    private void deleteMedicine(MedicineItem item) {
        SharedPreferences sp = getSharedPreferences("medicines", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("medicine_name_" + item.getId());
        editor.remove("medicine_dosage_" + item.getId());
        editor.remove("medicine_time_" + item.getId());
        editor.apply();
    }
}