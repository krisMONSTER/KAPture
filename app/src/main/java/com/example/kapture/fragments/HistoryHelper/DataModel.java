package com.example.kapture.fragments.HistoryHelper;

public class DataModel {
    private String event;
    private String date;
    private String time;

    public DataModel(String event, String date, String time){
        this.event = event;
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getEvent() {
        return event;
    }

    public String getTime() {
        return time;
    }
}
