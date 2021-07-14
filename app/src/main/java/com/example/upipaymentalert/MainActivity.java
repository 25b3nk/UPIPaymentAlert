package com.example.upipaymentalert;

import android.Manifest;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        checkForSmsReceivePermissions();
        SmsListener broadcastReceiver = new SmsListener();
        broadcastReceiver.setMainActivityHandler(this);
        IntentFilter callInterceptorIntentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(broadcastReceiver, callInterceptorIntentFilter);
    }

    void checkForSmsReceivePermissions() {
        // Check if App already has permissions for receiving SMS
        if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED) {
            // App has permissions to listen incoming SMS messages
            Log.d("adnan", "checkForSmsReceivePermissions: Allowed");
        } else {
            // App don't have permissions to listen incoming SMS messages
            Log.d("adnan", "checkForSmsReceivePermissions: Denied");

            // Request permissions from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 43391);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 43391) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("adnan", "Sms Receive Permissions granted");
            } else {
                Log.d("adnan", "Sms Receive Permissions denied");
            }
        }
    }

    private String[] getAmountFromMessageBody(String body) {
        Pattern p = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?,\\d+)?(,\\d+)?(\\.\\d{1,2})?)");
        Matcher m = p.matcher(body);
        String[] amount = new String[2];
        if (m.find()) {
            amount = Objects.requireNonNull(m.group(1)).split("\\.", 2);
        }
        Log.v("Regex", "Amount: " + amount[0] + amount[1]);
        return amount;
    }

    public void readSMS(SmsMessage smsMessage) {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 123);
            return;
        }
        String body = smsMessage.getMessageBody();
        String address = smsMessage.getOriginatingAddress();
        Log.v("Reading SMS", body);
        TextView viewSMS = findViewById(R.id.view_sms_tv);
        String textToDisplay = "Address: " + address + "\n\nBody: " + body;
        String[] amount = getAmountFromMessageBody(smsMessage.getMessageBody());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTTS.shutdown();
    }
}
