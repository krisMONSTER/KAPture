package com.example.kapture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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

        TextView settingsText = findViewById(R.id.settingsText);
        settingsText.setOnClickListener(new View.OnClickListener() {
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

        TextView historyText = findViewById(R.id.historyText);
        historyText.setOnClickListener(new View.OnClickListener() {
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
                Intent intent = new Intent(MainActivity.this, Camera.class);
                startActivity(intent);
            }
        });

        TextView startMonitoringText = findViewById(R.id.startMonitoringText);
        startMonitoringText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Camera.class);
                startActivity(intent);
            }
        });

        //umożliwienie przesuwania się tekstu
        startMonitoringText.setSelected(true);
        languageChoiceText.setSelected(true);
        historyText.setSelected(true);
        settingsText.setSelected(true);
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
}