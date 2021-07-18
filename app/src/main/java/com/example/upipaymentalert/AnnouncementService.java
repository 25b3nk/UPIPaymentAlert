package com.example.upipaymentalert;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.upipaymentalert.smsparser.SmsParser;

import java.util.Locale;


class SmsListener2 extends BroadcastReceiver {

    Context mContext;
    SmsParser smsParser;
    TextToSpeech mTTS;

    public SmsListener2(Context context, SmsParser smsParser) {
        mContext = context;
        this.smsParser = smsParser;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTTS.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        Log.v("AnnounceSMS", "Called when stopping service");
        if (mTTS != null) {
            Log.v("AnnounceSMS", "Closing TTS object in broadcast receiver");
            mTTS.stop();
            mTTS.shutdown();
        }
        super.finalize();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            StringBuilder messageBody = new StringBuilder();
            String address = "";
            if ( smsParser != null) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    address = smsMessage.getOriginatingAddress();
                    messageBody.append(smsMessage.getMessageBody());
                }
                String textToDisplay = "Address: " + address + "\n\nBody: " + messageBody;
                Log.v("Reading SMS", textToDisplay);
                String textToRead = smsParser.getAmountFromMessageBody(messageBody.toString());
                if (textToRead == null) {
                    return;
                }
                int ret = mTTS.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "1");
                if (ret == -1) {
                    Log.e("TTS", "TTS Speak gave an error");
                } else if (ret == 0) {
                    Log.v("TTS", "Successful TTS");
                }
            }
        }

    }
}

public class AnnouncementService extends Service {
    SmsListener2 mSmsListener;
    IntentFilter mIntentFilter;

    @Override
    public void onCreate() {
        super.onCreate();
        mSmsListener = new SmsListener2(this, new SmsParser());
        mIntentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("AnnounceSMS", "Service starting");
        try {
            registerReceiver(mSmsListener, mIntentFilter);
        } catch (IllegalArgumentException e) {
            Log.v("AnnounceSMS", "Receiver already registered");
        }
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.v("AnnounceSMS", "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.v("AnnounceSMS", "Stop service request");
        Toast.makeText(this, "service stopping", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        try {
            mSmsListener.finalize();
            unregisterReceiver(mSmsListener);
        } catch (IllegalArgumentException e) {
            Log.v("AnnounceSMS", "Receiver already un-registered");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
