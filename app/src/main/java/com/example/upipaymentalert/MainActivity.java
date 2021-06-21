package com.example.upipaymentalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Cursor mCursor;
    TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                }
            }
        });

    }

    public void readSMS(View v) {
        mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (mCursor.moveToFirst()) {
            String msgData = "";
            String address = "";
            String body = "";
            for(int idx = 0; idx < mCursor.getColumnCount(); idx++)
            {
                if (mCursor.getColumnName(idx).toLowerCase().compareTo("address") == 0) {
                    Log.v("Reading SMS", mCursor.getColumnName(idx).toLowerCase());
                    address = mCursor.getString(idx);
                }
                else if (mCursor.getColumnName(idx).toLowerCase().compareTo("body") == 0) {
                    body = mCursor.getString(idx);
                }
                msgData += " " + mCursor.getColumnName(idx) + ":" + mCursor.getString(idx);
            }
            Log.v("Reading SMS", msgData);
            TextView viewSMS = findViewById(R.id.view_sms_tv);
            String textToDisplay = "Address: " + address + "\n\nBody: " + body;
            String textToSpeak = "Received text: " + body + "; from "  + address;
            viewSMS.setText(textToDisplay);
            int ret = mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "1");
            if (ret == -1) {
                Log.e("TTS", "TTS Speak gave an error");
            }
            else if (ret == 0) {
                Log.v("TTS", "Successful TTS");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }
}