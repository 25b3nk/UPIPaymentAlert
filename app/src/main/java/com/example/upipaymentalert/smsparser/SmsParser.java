package com.example.upipaymentalert.smsparser;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    TextToSpeech mTTS;

    public SmsParser(TextToSpeech mTTS) {
        this.mTTS = mTTS;
    }

    public String getAmountFromMessageBody(String body) {
        String smsParseRegex = "(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?,\\d+)?(,\\d+)?(\\.\\d{1,2})?)";
        Pattern p = Pattern.compile(smsParseRegex);
        Matcher m = p.matcher(body);
        String[] amount = new String[2];
        if (m.find()) {
            amount = Objects.requireNonNull(m.group(1)).split("\\.", 2);
        }
        Log.v("Regex", "Amount: " + amount[0] + amount[1]);
        String rupees = amount[0];
        String paisa = amount[1];
        StringBuilder textToSpeak = new StringBuilder("Received payment of ");
        if (Integer.parseInt(rupees) != 0) {
            textToSpeak.append(rupees).append(" rupees");
        }
        if (Integer.parseInt(paisa) != 0) {
            textToSpeak.append(" and ").append(paisa).append(" paisa");
        }
        return textToSpeak.toString();
    }

}
