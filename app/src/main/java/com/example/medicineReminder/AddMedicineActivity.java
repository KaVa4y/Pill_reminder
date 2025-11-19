package com.example.medicineReminder;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private TextInputEditText editTextMedicineName;
    private TextInputEditText editTextDosage;
    private TextView textViewSelectedTime;
    private Button btnSetTime;
    private Button btnSave;

    private Calendar selectedTime;
    private boolean timeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        initViews();
        setListeners();
    }

    private void initViews() {
        editTextMedicineName = findViewById(R.id.editTextMedicineName);
        editTextDosage = findViewById(R.id.editTextDosage);
        textViewSelectedTime = findViewById(R.id.textViewSelectedTime);
        btnSetTime = findViewById(R.id.btnSetTime);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setListeners() {
        btnSetTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveMedicine());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute1) -> {
                    selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute1);
                    selectedTime.set(Calendar.SECOND, 0);

                    String timeStr = String.format("%02d:%02d", hourOfDay, minute1);
                    textViewSelectedTime.setText("Время: " + timeStr);
                    timeSelected = true;
                },
                hour, minute, true);

        timePickerDialog.show();
    }

    private void saveMedicine() {
        String name = editTextMedicineName.getText().toString().trim();
        String dosage = editTextDosage.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название лекарства", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!timeSelected) {
            Toast.makeText(this, "Выберите время", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("MEDICINE_NAME", name);
        resultIntent.putExtra("MEDICINE_DOSAGE", dosage);
        resultIntent.putExtra("MEDICINE_TIME_HOUR", selectedTime.get(Calendar.HOUR_OF_DAY));
        resultIntent.putExtra("MEDICINE_TIME_MINUTE", selectedTime.get(Calendar.MINUTE));

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}