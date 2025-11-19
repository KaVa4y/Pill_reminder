package com.example.medicineReminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

public class AddMedicineActivity extends AppCompatActivity {

    private TextInputEditText editTextMedicineName;
    private TextInputEditText editTextDosage;
    private TextView textViewSelectedTime;
    private MaterialButton btnSetTime;
    private MaterialButton btnSave;

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
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время")
                .build();

        picker.addOnPositiveButtonClickListener(dialog -> {
            selectedTime = Calendar.getInstance();
            selectedTime.set(Calendar.HOUR_OF_DAY, picker.getHour());
            selectedTime.set(Calendar.MINUTE, picker.getMinute());
            selectedTime.set(Calendar.SECOND, 0);

            String timeStr = String.format("%02d:%02d", picker.getHour(), picker.getMinute());
            textViewSelectedTime.setText("Время: " + timeStr);
            timeSelected = true;
        });

        picker.show(getSupportFragmentManager(), "TAG_PICKER");
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

        // Android по умолчанию поддерживает ввод кириллицы
        // Никаких дополнительных настроек не требуется
        // Проверим, что строка содержит кириллицу (для демонстрации)
        // Это не обязательно, просто проверка
        if (!name.matches(".*[а-яА-ЯёЁ].*")) {
            // Toast.makeText(this, "Для лучшего опыта используйте кириллицу", Toast.LENGTH_SHORT).show();
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