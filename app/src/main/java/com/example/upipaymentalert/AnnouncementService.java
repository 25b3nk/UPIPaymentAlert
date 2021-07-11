package com.example.upipaymentalert;


import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AnnounceSMS implements Runnable {
    private boolean pauseFlag = false;
    private boolean stopFlag = false;
    TextToSpeech mTTS;
    Cursor mCursor;

    public AnnounceSMS(Cursor cursorInput, TextToSpeech inTTS) {
        mTTS = inTTS;
        mCursor = cursorInput;
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

    private void readSMS() {
        // Done : Move check permission to MainActivity
        // Done : Start the service only when SMS permission is available
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
            Log.v("SMS service", "Before TTS");
            mTTS.speak("Checking SMS", TextToSpeech.QUEUE_FLUSH, null, "1");
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

    public void run() {
        while (true) {
            if (pauseFlag) {
                continue;
            }
            if (stopFlag) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readSMS();
        }
    }

    public void stop() {
//        mTTS.shutdown();
        stopFlag = true;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class AnnouncementService extends Service {
    Cursor mCursor;
    AnnounceSMS smsThread;
    TextToSpeech mTTS;
    private MediaPlayer player;

//    public AnnouncementService(Cursor inCursor) {
//        mCursor = inCursor;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                }
            }
        });
        smsThread = new AnnounceSMS(mCursor, mTTS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        smsThread.run();
//        player = MediaPlayer.create( this, Settings.System.DEFAULT_RINGTONE_URI );
//        player.setLooping( true );
//        player.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        smsThread.stop();
        mTTS.shutdown();
//        player.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
