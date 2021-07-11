package com.example.upipaymentalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    Cursor mCursor;
    TextToSpeech mTTS;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        mIntent = new Intent(this, AnnouncementService.class);
        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                }
            }
        });
    }

    private String getAmountFromMessageBody(String body) {
        Pattern p = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)");
        Matcher m = p.matcher(body);
        String amount = "";
        if (m.find()) {
            amount = m.group(1);
        }
        Log.v("Regex", "Amount: " + amount);
        return amount;
    }

    public void readSMS(View v) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_SMS }, 123);
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED) {
            mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            if (mCursor.moveToFirst()) {
                String msgData = "";
                String address = "";
                String body = "";
                for (int idx = 0; idx < mCursor.getColumnCount(); idx++) {
                    if (mCursor.getColumnName(idx).toLowerCase().compareTo("address") == 0) {
                        Log.v("Reading SMS", mCursor.getColumnName(idx).toLowerCase());
                        address = mCursor.getString(idx);
                    } else if (mCursor.getColumnName(idx).toLowerCase().compareTo("body") == 0) {
                        body = mCursor.getString(idx);
                    }
                    msgData += " " + mCursor.getColumnName(idx) + ":" + mCursor.getString(idx);
                }
                Log.v("Reading SMS", msgData);
                String textToDisplay = "Address: " + address + "\n\nBody: " + body;
                String amount = getAmountFromMessageBody(body);
//                String textToSpeak = "Received text: " + body + "; from " + address;
                if (amount.compareTo("") != 0) {
                    String textToSpeak = "Received rupees" + amount;
                    int ret = mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "1");
                    if (ret == -1) {
                        Log.e("TTS", "TTS Speak gave an error");
                    } else if (ret == 0) {
                        Log.v("TTS", "Successful TTS");
                    }
                }
            }
        }
        else {
            Toast.makeText(this, "Please provide read SMS permission", Toast.LENGTH_LONG);
        }
    }

    public void startService(View v) {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_SMS }, 123);
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, AnnouncementService.class));
        } else {
            Toast.makeText(this, "Cannot start service without read SMS permission", Toast.LENGTH_LONG);
        }
    }


    public void stopService(View v) {
        stopService(new Intent(this, AnnouncementService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }
}
