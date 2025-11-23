package com.example.medicineReminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Map;

public class MedicineScheduler {

    private static final String TAG = "MedicineScheduler";

    public static void scheduleAlarm(Context context, String medicineName, String dosage, Calendar time, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("MEDICINE_NAME", medicineName);
        intent.putExtra("MEDICINE_DOSAGE", dosage);
        intent.putExtra("NOTIFICATION_ID", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
                Log.d(TAG, "Точный будильник установлен на: " + time.getTime());
            } else {
                Log.w(TAG, "Точное разрешение на будильник отсутствует. Установка обычного будильника.");
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
            }
            Log.d(TAG, "Будильник установлен на: " + time.getTime());
        }
    }

    public static void cancelAlarm(Context context, int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Будильник отменён: ID=" + notificationId);
    }

    public static void scheduleAllAlarms(Context context) {
        Log.d(TAG, "Восстановление будильников после загрузки устройства...");
        SharedPreferences sp = context.getSharedPreferences("medicines", Context.MODE_PRIVATE);

        Map<String, ?> allEntries = (Map<String, ?>) sp.getAll();
        for (String key : allEntries.keySet()) {
            if (key.startsWith("medicine_name_")) {
                String idStr = key.replace("medicine_name_", "");
                try {
                    int id = Integer.parseInt(idStr);

                    String name = sp.getString("medicine_name_" + id, null);
                    String dosage = sp.getString("medicine_dosage_" + id, null);
                    long timeMillis = sp.getLong("medicine_time_" + id, -1);

                    if (name != null && timeMillis != -1) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(timeMillis);

                        scheduleAlarm(context, name, dosage, cal, id);
                        Log.d(TAG, "Будильник восстановлен: " + name);
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Неверный формат ID: " + idStr);
                }
            }
        }
    }
}