package com.example.kapture.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kapture.R;
import com.example.kapture.fragments.HistoryFragment;
import com.example.kapture.fragments.SettingsFragment;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SettingsFragment.IListener {

    public static final String SHARED_PREFS = "SettingsFragment";
    public static final String DELAY = "delayName";
    public static final String DURATION = "durationName";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String IS_PHONE_NUMBER = "isPhoneNumber";
    public static final String IS_TAKING_PICTURES = "isTakingPictures";
    public static final String ALARM_ID = "alarmId";

    int delay = 5;
    int duration = 10;
    String phoneNumber = "69";
    boolean isPhoneNumber = false;
    boolean isTakingPictures = false;

    private AdView adView;

    public void createAdd(){
        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d("Main Activity", "Ad closed");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("Main Activity", "Ad failed to load");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d("Main Activity", "Ad opened");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("Main Activity", "Ad loaded");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d("Main Activity", "Ad clicked");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadLocale();
        setContentView(R.layout.main_activity);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        createAdd(); //obługa reklam

        //fragment kodu odpowiadajacy za zarzadzanie fragmentami
        SettingsFragment sf = new SettingsFragment();
        HistoryFragment hf = new HistoryFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutFragment, sf).commit();

        ImageButton ic_settings = findViewById(R.id.ic_settings);
        ic_settings.setOnClickListener(view -> getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
                .replace(R.id.frameLayoutFragment, sf)
                .commit());

        ImageButton ic_history = findViewById(R.id.ic_history);
        ic_history.setOnClickListener(view -> {
            /*InputMethodManager inputMethodManager =
                    (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager
                    .hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);*/
            getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                .replace(R.id.frameLayoutFragment, hf)
                .commit();
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
                intent.putExtra("phoneNumber", sharedPreferences.getString(PHONE_NUMBER, ""));
                intent.putExtra("isPhoneNumber", sharedPreferences.getBoolean(IS_PHONE_NUMBER, false));
                intent.putExtra("isTakingPictures", sharedPreferences.getBoolean(IS_TAKING_PICTURES, false));
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
                intent.putExtra("phoneNumber", sharedPreferences.getString(PHONE_NUMBER, ""));
                intent.putExtra("isPhoneNumber", sharedPreferences.getBoolean(IS_PHONE_NUMBER, false));
                intent.putExtra("isTakingPictures", sharedPreferences.getBoolean(IS_TAKING_PICTURES, false));
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

    @Override
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    @Override
    public void setIsPhoneNumber(boolean isPhoneNumber) {
        this.isPhoneNumber = isPhoneNumber;
        saveData();
    }

    @Override
    public void setIsTakingPictures(boolean isTakingPictures) {
        this.isTakingPictures = isTakingPictures;
        saveData();
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(DELAY, this.delay);
        editor.putInt(DURATION, this.duration);
        editor.putBoolean(IS_PHONE_NUMBER, this.isPhoneNumber);
        editor.putBoolean(IS_TAKING_PICTURES, this.isTakingPictures);
        editor.apply();
    }
}