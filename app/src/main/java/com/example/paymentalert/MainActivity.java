package com.example.paymentalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void readSMS(View v) {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (cursor.moveToFirst()) {
            String msgData = "";
            String address = "";
            String body = "";
            for(int idx=0;idx<cursor.getColumnCount();idx++)
            {
                if (cursor.getColumnName(idx).toLowerCase().compareTo("address") == 0) {
                    Log.v("Reading SMS", cursor.getColumnName(idx).toLowerCase());
                    address = cursor.getString(idx);
                }
                else if (cursor.getColumnName(idx).toLowerCase().compareTo("body") == 0) {
                    body = cursor.getString(idx);
                }
                msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
            }
            Log.v("Reading SMS", msgData);
            TextView viewSMS = findViewById(R.id.view_sms_tv);
            viewSMS.setText("ADDRESS: " + address + "\nBody: " + body);
        }
    }
}