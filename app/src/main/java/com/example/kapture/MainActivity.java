package com.example.kapture;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SettingsFragment.IListener {

    public static final String SHARED_PREFS = "SettingsFragment";
    public static final String DELAY = "delayName";
    public static final String DURATION = "durationName";
    public static final String ALARM_ID = "alarmId";

    int delay = 5;
    int duration = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadLocale();
        setContentView(R.layout.main_activity);

        //fragment kodu odpowiadajacy za zarzadzanie fragmentami
        SettingsFragment sf = new SettingsFragment();
        HistoryFragment hf = new HistoryFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutFragment, sf).commit();

        ImageButton ic_settings = findViewById(R.id.ic_settings);
        ic_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                        .replace(R.id.frameLayoutFragment, sf)
                        .addToBackStack(null)
                        .commit();
            }
        });

        ImageButton ic_history = findViewById(R.id.ic_history);
        ic_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                        .replace(R.id.frameLayoutFragment, hf)
                        .addToBackStack(null)
                        .commit();
            }
        });

        //kod odpowiedzialny za interakcję z poszczególnymi ikonkami i napisami
        ImageButton ic_language = findViewById(R.id.ic_language);
        ic_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguageDialog();
            }
        });

        TextView languageChoiceText = findViewById(R.id.languageChoiceText);
        languageChoiceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLanguageDialog();
            }
        });

        ImageButton ic_cameraMonitoring = findViewById(R.id.ic_cameraMonitoring);
        ic_cameraMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                Intent intent = new Intent(MainActivity.this, Camera.class);
                intent.putExtra("delay",  sharedPreferences.getInt(DELAY, 2));
                intent.putExtra("duration", sharedPreferences.getInt(DURATION, 6));
                intent.putExtra("alarmId", sharedPreferences.getInt(ALARM_ID, 0));
                startActivity(intent);
                System.out.println("Delay Main.class " + sharedPreferences.getInt(DELAY, 2));
                System.out.println("Duration Main.class " + sharedPreferences.getInt(DURATION, 6));
                System.out.println("Alarm_id Main.class " + sharedPreferences.getInt(ALARM_ID, 0));

            }
        });

        TextView startMonitoringText = findViewById(R.id.startMonitoringText);
        startMonitoringText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                Intent intent = new Intent(MainActivity.this, Camera.class);
                intent.putExtra("delay",  sharedPreferences.getInt(DELAY, 2));
                intent.putExtra("duration", sharedPreferences.getInt(DURATION, 6));
                intent.putExtra("alarmId", sharedPreferences.getInt(ALARM_ID, 0));
                startActivity(intent);
                System.out.println("Delay Main.class " + sharedPreferences.getInt(DELAY, 2));
                System.out.println("Duration Main.class " + sharedPreferences.getInt(DURATION, 6));
                System.out.println("Alarm_id Main.class " + sharedPreferences.getInt(ALARM_ID, 0));
            }
        });


        //umożliwienie przesuwania się tekstu
        startMonitoringText.setSelected(true);
        languageChoiceText.setSelected(true);

        //historyText.setSelected(true);
        //settingsText.setSelected(true);

    }

    //fragment kodu odpowiedzialny za stworzenie alert dialogu z wyborem poszczególnych języków
        private void changeLanguageDialog(){
            final String[] languages = {"English", "Polish", "Deutsch"};
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Choose Language...");
            mBuilder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0){
                        setLocale("en");
                        recreate();
                    }
                    else if (i == 1){
                        setLocale("pl");
                        recreate();
                    }
                    else if (i == 2){
                        setLocale("de");
                        recreate();
                    }
                    dialogInterface.dismiss();
                }
            });

            AlertDialog mDialog = mBuilder.create();

            mDialog.show();
        }

        //fragment kodu odpowiedzialny za ustawienie i wczytywanie ustawien jezykowych
        private void setLocale(String language){
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration configuration = new Configuration();
            configuration.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("My_Lang", language);
            editor.apply();
        }

        public void loadLocale(){
            SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
            String language = preferences.getString("My_Lang", "");
            setLocale(language);
        }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
        saveData();
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
        saveData();
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(DELAY, this.delay);
        editor.putInt(DURATION, this.duration);
        editor.apply();
    }
}