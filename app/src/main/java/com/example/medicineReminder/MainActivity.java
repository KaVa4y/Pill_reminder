package com.example.medicineReminder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_MEDICINE = 100;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM = 101;
    private static final int REQUEST_POST_NOTIFICATIONS = 102; // Для Android 13+
    private static final int REQUEST_SCHEDULE_ALARMS = 103;   // Для Android 15+

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

        // Запрашиваем разрешение на уведомления (Android 13+)
        requestNotificationPermission();

        // Загружаем сохранённые напоминания
        loadMedicines();

        btnAdd.setOnClickListener(v -> openAddMedicineActivity());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MedicineItem item = medicineList.get(position);
            Toast.makeText(this, "Удалить: " + item.getName(), Toast.LENGTH_SHORT).show();
            removeMedicine(item);
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
            }
        }
    }

    private void openAddMedicineActivity() {
        // Проверяем разрешение на точные будильники (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Если разрешение отсутствует, запрашиваем его
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_SCHEDULE_EXACT_ALARM);
                return; // Выходим, чтобы не открывать активность до получения разрешения
            }
        }

        // Проверка разрешения на будильники (Android 15+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // На Android 15+ проверка разрешения делается через системные настройки
            // Нельзя проверить программно, только открыть настройки
            // Поэтому просто открываем активность, а в MedicineScheduler будем обрабатывать
        }

        // Если разрешения есть (или не требуются), открываем активность
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

            if (name != null && !name.isEmpty() && hour != -1 && minute != -1) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);

                // Проверяем разрешение ещё раз перед установкой будильника
                boolean canScheduleExact = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    canScheduleExact = alarmManager.canScheduleExactAlarms();
                }

                if (!canScheduleExact) {
                    // Если разрешение не получено, уведомляем пользователя
                    Toast.makeText(this, "Точное время напоминания может быть смещено системой", Toast.LENGTH_LONG).show();
                }

                MedicineItem newItem = new MedicineItem(name, dosage, selectedTime, nextId++);
                medicineList.add(newItem);
                adapter.notifyDataSetChanged();

                saveMedicine(newItem);
                // scheduleAlarm теперь обрабатывает разрешение внутри себя
                MedicineScheduler.scheduleAlarm(this, name, dosage, selectedTime, newItem.getId());

                Toast.makeText(this, "Напоминание добавлено!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка при добавлении напоминания", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SCHEDULE_EXACT_ALARM) {
            // После возврата из настроек проверяем разрешение снова
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager.canScheduleExactAlarms()) {
                    // Если пользователь дал разрешение, открываем активность
                    Intent intent = new Intent(this, AddMedicineActivity.class);
                    startActivityForResult(intent, REQUEST_ADD_MEDICINE);
                } else {
                    // Если не дал, показываем сообщение
                    Toast.makeText(this, "Для точных напоминаний нужно разрешение на будильники", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение на уведомления получено
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show();
            } else {
                // Разрешение на уведомления отклонено
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
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
                // scheduleAlarm теперь обрабатывает разрешение внутри себя
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