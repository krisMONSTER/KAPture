package com.example.kapture;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.number.Scale;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private EditText durationEditText, delayEditText;

    public static final String SHARED_PREFS = "SettingsFragment";
    public static final String DELAY = "delayName";
    public static final String DURATION = "durationName";

    private int delay;
    private int duration;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        durationEditText = view.findViewById(R.id.durationEditText);
        delayEditText = view.findViewById(R.id.delayEditText);

        durationEditText.setInputType(InputType.TYPE_NULL);
        delayEditText.setInputType(InputType.TYPE_NULL);

        durationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDurationDialog(durationEditText);
            }
        });

        delayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDelayDialog(delayEditText);
            }
        });
        // Inflate the layout for this fragment

        loadData();
        updateViews();
        return view;
    }

    private void showDurationDialog(final EditText durationEditText){
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int minute, int second) {
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);

                int duration = minute*60 + second;
                mListener.setDuration(duration);

                durationEditText.setText(getString(R.string.duration) + ' ' + duration + " sec");


            }
        };
        new TimePickerDialog(getActivity(), timeSetListener, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true).show();
    }

    private void showDelayDialog(final EditText delayEditText){
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int minute, int second) {
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);

                int delay = minute*60 + second;
                mListener.setDelay(delay);

                delayEditText.setText(getString(R.string.delay) + ' ' + delay + " sec");

            }
        };
        new TimePickerDialog(getActivity(), timeSetListener, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true).show();
    }

    //passing data to MainActivity.class
    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);

        if (context instanceof IListener){
            mListener = (IListener)context;
        } else {
            throw new RuntimeException((context.toString() + " must implement IListener"));
        }
    }

    IListener mListener;

    public interface IListener{
        void setDuration(int duration);
        void setDelay(int delay);
    }

    public void loadData(){
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        delay = sharedPreferences.getInt(DELAY, 3);
        duration = sharedPreferences.getInt(DURATION, 7);
    }

    public void updateViews(){
        delayEditText.setText(getString(R.string.delay) + ' ' + delay + " sec");
        durationEditText.setText(getString(R.string.duration) + ' ' + duration + " sec");
    }
}