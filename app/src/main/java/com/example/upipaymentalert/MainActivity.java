package com.example.upipaymentalert;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    Intent mIntent;
    NotificationCompat.Builder mNotificationBuilder;
    NotificationManagerCompat mNotificationManager;
    final String CHANNEL_ID = "CHANNEL_ID";
    final int CHANNEL_ID_NUM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIntent = new Intent(this, AnnouncementService.class);
        manageSMSPermission();
        buildNotification();

    }

    private void buildNotification() {
        // Create notification channel for Android version >= Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        Intent intent2 = new Intent(this, AnnouncementService.class);
//        PendingIntent pendingIntent2 = PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("UPA")
                .setContentText("UPI Payment Alert")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true);
        mNotificationManager = NotificationManagerCompat.from(this);
    }

    private void manageSMSPermission() {
        int readSMSPermission = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS);
        int receiveSMSPermission = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECEIVE_SMS);

        if (readSMSPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 43391);
        }

        if (receiveSMSPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 43391 || requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("app permission", "Granted Permission for " + requestCode);
            } else {
                Log.d("app permission", "Permission Denied for " + requestCode);
            }
        }
    }

    public void startService(View v) {
        Log.v("SMS", "Start of service from button");
        mNotificationManager.notify(CHANNEL_ID_NUM, mNotificationBuilder.build());
        startBGService();
    }

    private void startBGService() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_SMS }, 123);
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED) {
            startService(mIntent);
        } else {
            Toast.makeText(this, "Cannot start service without read SMS permission", Toast.LENGTH_LONG).show();
        }
        Log.v("SMS", "Start of service from button done");
    }

    public void stopService(View v) {
        Log.v("SMS", "Stop service button is clicked");
        stopBGService();
    }

    public void stopBGService() {
        stopService(mIntent);
        mNotificationManager.cancel(CHANNEL_ID_NUM);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
