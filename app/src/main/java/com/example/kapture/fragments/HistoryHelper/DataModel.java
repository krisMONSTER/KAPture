package com.example.kapture.fragments.HistoryHelper;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.kapture.R;

public class DataModel extends AppCompatActivity {
    private final String event;
    private final String date;
    private final String time;

    public DataModel(String event, String date, String time){
        this.event = event;
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getEvent(Context context) {
        if (event.equals("Start detection")) return context.getString(R.string.startDetection);
        else if (event.equals("Motion detected")) return context.getString(R.string.motionDetected);
        else return context.getString(R.string.endDetection);
        //return event;
    }

    public String getTime() {
        return time;
    }
}
