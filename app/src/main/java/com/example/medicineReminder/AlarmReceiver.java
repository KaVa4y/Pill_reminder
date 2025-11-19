package com.example.medicineReminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Получен сигнал будильника");

        String medicineName = intent.getStringExtra("MEDICINE_NAME");
        String dosage = intent.getStringExtra("MEDICINE_DOSAGE");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.putExtra("MEDICINE_NAME", medicineName);
        serviceIntent.putExtra("MEDICINE_DOSAGE", dosage);
        serviceIntent.putExtra("NOTIFICATION_ID", notificationId);
        context.startService(serviceIntent);
    }
}