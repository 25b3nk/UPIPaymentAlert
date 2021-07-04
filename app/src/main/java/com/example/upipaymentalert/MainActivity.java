package com.example.upipaymentalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    Cursor mCursor;
    TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTTS = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                mTTS.setLanguage(Locale.UK);
            }
        });
    }

    private String[] getAmountFromMessageBody(String body) {
        Pattern p = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?,\\d+)?(,\\d+)?(\\.\\d{1,2})?)");
        Matcher m = p.matcher(body);
        String[] amount = new String[2];
        if (m.find()) {
            amount = m.group(1).split("\\.", 2);
        }
        Log.v("Regex", "Amount: " + amount[0] + amount[1]);
        return amount;
    }

    @SuppressLint("ShowToast")
    public void readSMS(View v) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 123);
            return;
        }
        if (permission == PackageManager.PERMISSION_GRANTED) {
            mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
            if (mCursor.moveToFirst()) {
                StringBuilder msgData = new StringBuilder();
                String address = "";
                String body = "";
                for (int idx = 0; idx < mCursor.getColumnCount(); idx++) {
                    if (mCursor.getColumnName(idx).toLowerCase().compareTo("address") == 0) {
                        Log.v("Reading SMS", mCursor.getColumnName(idx).toLowerCase());
                        address = mCursor.getString(idx);
                    } else if (mCursor.getColumnName(idx).toLowerCase().compareTo("body") == 0) {
                        body = mCursor.getString(idx);
                    }
                    msgData.append(" ").append(mCursor.getColumnName(idx)).append(":").append(mCursor.getString(idx));
                }
                Log.v("Reading SMS", msgData.toString());
                TextView viewSMS = findViewById(R.id.view_sms_tv);
                String textToDisplay = "Address: " + address + "\n\nBody: " + body;
                String[] amount = getAmountFromMessageBody(body);
                String rupees = amount[0];
                String paisa = amount[1];
                viewSMS.setText(textToDisplay);
                StringBuilder textToSpeak = new StringBuilder("Received payment of ");
                if (Integer.parseInt(rupees) != 0) {
                    textToSpeak.append(rupees).append(" rupees");
                }
                if (Integer.parseInt(paisa) != 0) {
                    textToSpeak.append(" and ").append(paisa).append(" paisa");
                }
                int ret = mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "1");
                if (ret == -1) {
                    Log.e("TTS", "TTS Speak gave an error");
                } else if (ret == 0) {
                    Log.v("TTS", "Successful TTS");
                }
            }
        } else {
            Toast.makeText(this, "Please provide read SMS permission", Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }
}
