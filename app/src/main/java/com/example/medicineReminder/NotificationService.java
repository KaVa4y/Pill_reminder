package com.example.medicineReminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "MedicineReminderChannel";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String medicineName = intent.getStringExtra("MEDICINE_NAME");
        String dosage = intent.getStringExtra("MEDICINE_DOSAGE");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        createNotificationChannel();

        String dosageStr = (dosage != null && !dosage.isEmpty()) ? dosage : "Без дозировки";
        String contentText = "Примите: " + medicineName + " (" + dosageStr + ")";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)
                .setContentTitle("Напоминание о приёме лекарства")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(getResources().getColor(android.R.color.holo_blue_light));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());

        stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Напоминания о лекарствах";
            String description = "Канал для уведомлений о приёме лекарств";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}