package com.example.kapture.fragments;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.number.Scale;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.view.CameraView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kapture.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private EditText durationEditText, delayEditText, chooseAlarmSoundText;
    EditText choosePhoneNumber;
    SwitchCompat phoneNumberSwitch;
    SwitchCompat takingPicturesSwitch;

    public static final String SHARED_PREFS = "SettingsFragment";
    public static final String DELAY = "delayName";
    public static final String DURATION = "durationName";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String IS_PHONE_NUMBER = "isPhoneNumber";
    public static final String IS_TAKING_PICTURES = "isTakingPictures";
    public static final String ALARM_ID = "alarmId";
    public static final String ALARM_TEXT = "alarmName";

    private int delay;
    private int duration;
    private String alarmName;
    private String phoneNumber;
    private boolean isPhoneNumber;
    private boolean isTakingPictures;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        durationEditText = view.findViewById(R.id.durationEditText);
        delayEditText = view.findViewById(R.id.delayEditText);
        chooseAlarmSoundText = view.findViewById(R.id.chooseAlarmSoundText);
        choosePhoneNumber = view.findViewById(R.id.choosePhoneNumber);
        phoneNumberSwitch = view.findViewById(R.id.phoneNumberSwitch);
        takingPicturesSwitch = view.findViewById(R.id.takingPicturesSwitch);

        durationEditText.setInputType(InputType.TYPE_NULL);
        delayEditText.setInputType(InputType.TYPE_NULL);
        chooseAlarmSoundText.setInputType(InputType.TYPE_NULL);

        durationEditText.setOnClickListener(v -> showDurationDialog(durationEditText));

        delayEditText.setOnClickListener(v -> showDelayDialog(delayEditText));

        chooseAlarmSoundText.setOnClickListener(v -> showChooseAlarmDialog());

        choosePhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mListener.setPhoneNumber(s.toString());
            }
        });

        phoneNumberSwitch.setOnCheckedChangeListener((v, isChecked) ->
                mListener.setIsPhoneNumber(isChecked));

        takingPicturesSwitch.setOnCheckedChangeListener((v, isChecked) ->
                mListener.setIsTakingPictures(isChecked));

        // Inflate the layout for this fragment

        loadData();
        updateViews();
        return view;
    }

    private void showDurationDialog(final EditText durationEditText) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);


        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int minute, int second) {
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);

                int duration = minute * 60 + second;
                mListener.setDuration(duration);

                durationEditText.setText(getString(R.string.duration) + ' ' + duration + " sec");


            }
        };
        new TimePickerDialog(getActivity(), timeSetListener, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true).show();
    }

    private void showDelayDialog(final EditText delayEditText) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int minute, int second) {
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, second);

                int delay = minute * 60 + second;
                mListener.setDelay(delay);

                delayEditText.setText(getString(R.string.delay) + ' ' + delay + " sec");

            }
        };
        new TimePickerDialog(getActivity(), timeSetListener, calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true).show();
    }

    private void showChooseAlarmDialog() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        final String[] alarms = {"None", "Alert robery", "Bank robery",
                "Buzzer", "Camera snap", "Chicken",
                "Military alarm", "Police", "Punch",
                "School bell", "Whistle"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getActivity());
        mBuilder.setTitle("Choose sound alarm");
        mBuilder.setSingleChoiceItems(alarms, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SharedPreferences.Editor editor = sharedPreferences.edit();


                if (i == 0) {
                    chooseAlarmSoundText.setText(alarms[0]);
                    editor.putInt(ALARM_ID, 0);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 1) {
                    chooseAlarmSoundText.setText(alarms[1]);
                    editor.putInt(ALARM_ID, 1);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 2) {
                    chooseAlarmSoundText.setText(alarms[2]);
                    editor.putInt(ALARM_ID, 2);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 3) {
                    chooseAlarmSoundText.setText(alarms[3]);
                    editor.putInt(ALARM_ID, 3);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 4) {
                    chooseAlarmSoundText.setText(alarms[4]);
                    editor.putInt(ALARM_ID, 4);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 5) {
                    chooseAlarmSoundText.setText(alarms[5]);
                    editor.putInt(ALARM_ID, 5);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 6) {
                    chooseAlarmSoundText.setText(alarms[6]);
                    editor.putInt(ALARM_ID, 6);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 7) {
                    chooseAlarmSoundText.setText(alarms[7]);
                    editor.putInt(ALARM_ID, 7);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 8) {
                    chooseAlarmSoundText.setText(alarms[8]);
                    editor.putInt(ALARM_ID, 8);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 9) {
                    chooseAlarmSoundText.setText(alarms[9]);
                    editor.putInt(ALARM_ID, 9);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                } else if (i == 10) {
                    chooseAlarmSoundText.setText(alarms[10]);
                    editor.putInt(ALARM_ID, 10);
                    editor.putString(ALARM_TEXT, chooseAlarmSoundText.getText().toString());
                    editor.apply();
                }
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();

        mDialog.show();
    }

    //passing data to MainActivity.class
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof IListener) {
            mListener = (IListener) context;
        } else {
            throw new RuntimeException((context.toString() + " must implement IListener"));
        }
    }

    IListener mListener;

    public interface IListener {
        void setDuration(int duration);

        void setDelay(int delay);

        void setPhoneNumber(String phoneNumber);

        void setIsPhoneNumber(boolean isPhoneNumber);

        void setIsTakingPictures(boolean isTakingPictures);
    }

    public void loadData() {
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        delay = sharedPreferences.getInt(DELAY, 3);
        duration = sharedPreferences.getInt(DURATION, 7);
        alarmName = sharedPreferences.getString(ALARM_TEXT, "None");
        phoneNumber = sharedPreferences.getString(PHONE_NUMBER, "");
        isPhoneNumber = sharedPreferences.getBoolean(IS_PHONE_NUMBER, false);
        isTakingPictures = sharedPreferences.getBoolean(IS_TAKING_PICTURES, false);
    }

    public void updateViews() {
        delayEditText.setText(getString(R.string.delay) + ' ' + delay + " sec");
        durationEditText.setText(getString(R.string.duration) + ' ' + duration + " sec");
        chooseAlarmSoundText.setText(alarmName);
        choosePhoneNumber.setText(phoneNumber);
        phoneNumberSwitch.setChecked(isPhoneNumber);
        takingPicturesSwitch.setChecked(isTakingPictures);
    }
}