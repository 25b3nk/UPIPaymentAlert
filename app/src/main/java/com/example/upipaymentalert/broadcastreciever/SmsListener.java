package com.example.upipaymentalert.broadcastreciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.upipaymentalert.MainActivity;
import com.example.upipaymentalert.smsparser.SmsParser;

public class SmsListener extends BroadcastReceiver {

    MainActivity mainActivity;
    SmsParser smsParser;

    public SmsListener(MainActivity mainActivity, SmsParser smsParser) {
        this.smsParser = smsParser;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            StringBuilder messageBody = new StringBuilder();
            String address = "";
            if (mainActivity != null && smsParser != null) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    address = smsMessage.getOriginatingAddress();
                    messageBody.append(smsMessage.getMessageBody());
                }
                String textToDisplay = "Address: " + address + "\n\nBody: " + messageBody;
                Log.v("Reading SMS", textToDisplay);
                mainActivity.updateTextBox(textToDisplay);
                String textToRead = smsParser.getAmountFromMessageBody(messageBody.toString());
                mainActivity.readText(textToRead);
            }
        }

    }
}
