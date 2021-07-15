package com.example.upipaymentalert;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.upipaymentalert.broadcastreciever.SmsListener;
import com.example.upipaymentalert.smsparser.SmsParser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
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
        manageSMSPermission();
        SmsParser smsParser = new SmsParser(mTTS);
        SmsListener broadcastReceiver = new SmsListener(this, smsParser);
        IntentFilter callInterceptorIntentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(broadcastReceiver, callInterceptorIntentFilter);
    }

    private void manageSMSPermission() {
        int readSMSPermission = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_SMS);
        int receiveSMSPermission = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECEIVE_SMS);

        if (readSMSPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 43391);
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
                Log.d("adnan", "Sms Receive Permissions granted");
            } else {
                Log.d("adnan", "Sms Receive Permissions denied");
            }
        }
    }

    public void updateTextBox(String textToDisplay) {
        TextView viewSMS = findViewById(R.id.view_sms_tv);
        viewSMS.setText(textToDisplay);
    }

    public void readText(String textToSpeak) {
        int ret = mTTS.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "1");
        if (ret == -1) {
            Log.e("TTS", "TTS Speak gave an error");
        } else if (ret == 0) {
            Log.v("TTS", "Successful TTS");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }
}
