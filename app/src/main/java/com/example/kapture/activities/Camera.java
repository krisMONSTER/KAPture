package com.example.kapture.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kapture.CameraPreview;
import com.example.kapture.CameraViewModel;
import com.example.kapture.MonitoringCycle;
import com.example.kapture.LightSensor;
import com.example.kapture.fragments.HistoryHelper.DatabaseHelper;
import com.example.kapture.R;
import com.example.kapture.managers.TimerManager;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Camera extends AppCompatActivity {
    LocalDate localDate;
    LocalTime localTime;
    private final CameraViewModel viewModel = new CameraViewModel();
    private MonitoringCycle monitoringCycle;
    private TimerManager timerManager;
    FrameLayout preview;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        preview = findViewById(R.id.camera_frame_layout);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
            localTime = LocalTime.now();

            DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            System.out.println(localDate.format(formatterDate));

            DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
            System.out.println(localTime.format(formatterTime));
        }

        //getting data from pickers
        viewModel.setDuration(getIntent().getIntExtra("duration", 0));
        viewModel.setDelay(getIntent().getIntExtra("delay", 0));

        viewModel.setAlarmId(getIntent().getIntExtra("alarmId", 0));
        viewModel.setDatabaseHelper(new DatabaseHelper(this));
        setupPreviewOverlay();
        setupNotification();
        setupAlarmSound();

        //setup timer
        timerManager = new TimerManager(viewModel);
        timerManager.start();

        //setup motion detector
        monitoringCycle = new MonitoringCycle(this, viewModel);
        monitoringCycle.start();
    }

    public void addData(String newEntry, String date, String time){
        boolean insertData = viewModel.getDatabaseHelper().addData(newEntry, date, time);
        if (insertData) Log.d("insert data" , "Data Successsfully Inserted!" );//toastMessage("Data Successsfully Inserted!");
        else Log.d("insert data" , "Something went wrong" );//toastMessage("Something went wrong");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getSoundPool().release();
        viewModel.setSoundPool(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == viewModel.getPERMISSIONS_REQUEST_CODE()) {
            for (int grantResult : grantResults){
                if (grantResult != PackageManager.PERMISSION_GRANTED)
                    System.exit(0);
            }
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerManager.pauseTimer();
        viewModel.setSafeToTakePicture(false);
        preview.removeAllViews();
        //viewModel.getSensor().getLightSensorManager().unregisterListener(viewModel.getSensor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerManager.resumeTimer();
        //viewModel.getSensor().getLightSensorManager().registerListener(viewModel.getSensor(), viewModel.getSensor().getLightSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        ArrayList<String> permissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.CAMERA);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.SEND_SMS);
        if (permissionsList.size() > 0) {
            String[] permissionsArray = new String[permissionsList.size()];
            for (int i = 0; i < permissionsList.size(); i++)
                permissionsArray[i] = permissionsList.get(i);
            ActivityCompat.requestPermissions(this, permissionsArray, viewModel.getPERMISSIONS_REQUEST_CODE());
        }
        else {
            startCamera();
        }
    }

    @Override
    public void onBackPressed() {
        //finish work for all threads
        viewModel.setFinishAllThreads(true);
        timerManager.interrupt();
        monitoringCycle.interrupt();
        try {
            timerManager.join();
            monitoringCycle.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void startCamera() {
        //set camera and preview
        viewModel.setCamera(getCameraInstance());
        viewModel.setCameraPreview(new CameraPreview(this, viewModel.getCamera()));
        preview.addView(viewModel.getCameraPreview());

        //ustawienie rozdzielczości wyświetlanego obrazu
        android.hardware.Camera.Parameters parameters = viewModel.getCamera().getParameters();
        for (android.hardware.Camera.Size x : parameters.getSupportedPreviewSizes()) {
            if (((float) x.width / x.height) == 16f / 9f) {
                parameters.setPreviewSize(x.width, x.height);
                break;
            }
        }

        //ustawienie typu focusa
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED))
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED);

        //System.out.println("Scene modes: " + parameters.getSupportedSceneModes());
        //parameters.setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_HDR);

        viewModel.getCamera().setParameters(parameters);

        setCameraToPortraitOrientation();

        //unlock taking pictures
        viewModel.setSafeToTakePicture(true);
        if (viewModel.getStartMonitoring().availablePermits() == 0)
            viewModel.getStartMonitoring().release();
    }

    private android.hardware.Camera getCameraInstance() {
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void setCameraToPortraitOrientation() {
        Method downPolymorphic;
        try {
            downPolymorphic = viewModel.getCamera().getClass().getMethod("setDisplayOrientation", int.class);
            downPolymorphic.invoke(viewModel.getCamera(), 90);
        } catch (Exception ignored) {
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "KAPture channel";
            String description = "Channel used to post notification from KAPture app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(viewModel.getChannelID(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPreviewOverlay() {
        viewModel.setControlInflater(LayoutInflater.from(getBaseContext()));
        View viewControl = viewModel.getControlInflater().inflate(R.layout.camera_overlay_layout, null);
        FrameLayout.LayoutParams layoutParamsControl
                = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
        TextView workFor = findViewById(R.id.detectionWorkForTV);
        TextView startIn = findViewById(R.id.detectionStartsInTV);

        Thread overlayUpdate = new Thread(() -> {
            int workForTime = viewModel.getDuration();
            int startInTime = viewModel.getDelay();

            if (startInTime > 0) {
                runOnUiThread(() -> workFor.setText(R.string.detectingHasntStartedYet));

                AtomicInteger alpha = new AtomicInteger(245);
                new Thread(() -> {
                    int delta = -5;
                    while (startIn.getVisibility() == View.VISIBLE) {
                        if (alpha.get() > 40 && alpha.get() < 250) {
                            alpha.addAndGet(delta);
                            runOnUiThread(() -> startIn.setTextColor(Color.argb(alpha.get(), 255, 255, 255)));
                        } else {
                            delta = -delta;
                            alpha.addAndGet(delta);
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                while (startInTime > 0) {
                    int temp = startInTime;

                    runOnUiThread(() -> {
                        startIn.setText(getString(R.string.startsIn) + temp);
                    });
                    startInTime--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (startInTime == 0) {

                runOnUiThread(() -> {
                    startIn.setVisibility(View.INVISIBLE);
                    workFor.setVisibility(View.VISIBLE);
                    startIn.setText(getString(R.string.detectingHasAlreadyStarted));
                    addData("Start detection", LocalDate.now().toString(), LocalTime.now().toString());//!!
                });

                while (workForTime > 0) {
                    int temp = workForTime;
                    runOnUiThread(() -> workFor.setText(getString(R.string.detectingWillBeOnFor) + temp));

                    workForTime--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            runOnUiThread(() -> {
                startIn.setText(R.string.detectionFinished);
                workFor.setText(R.string.detectionFinished);
                addData("End detection", LocalDate.now().toString(), LocalTime.now().toString());//!!
            });


        });
        overlayUpdate.start();
    }

    private void setupNotification(){
        createNotificationChannel();
        viewModel.setNotification(new NotificationCompat
                .Builder(this, viewModel.getChannelID())
                .setSmallIcon(R.drawable.ic_kapture_alert)
                .setContentTitle("KAPture Alert !")
                .setContentText("Movement Detected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT));
    }

    private void setupAlarmSound(){
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        viewModel.setSoundPool(new SoundPool.Builder()
                .setMaxStreams(10) //dac 1
                .setAudioAttributes(audioAttributes)
                .build());
        viewModel.getSoundPool().load(this, R.raw.alert_robery, 1);
        viewModel.getSoundPool().load(this, R.raw.bank_robery, 1);
        viewModel.getSoundPool().load(this, R.raw.buzzer, 1);
        viewModel.getSoundPool().load(this, R.raw.camera_snap, 1);
        viewModel.getSoundPool().load(this, R.raw.chicken, 1);
        viewModel.getSoundPool().load(this, R.raw.military_alarm, 1);
        viewModel.getSoundPool().load(this, R.raw.police, 1);
        viewModel.getSoundPool().load(this, R.raw.punch, 1);
        viewModel.getSoundPool().load(this, R.raw.school_bell, 1);
        viewModel.getSoundPool().load(this, R.raw.whistle, 1);
    }

    /*public void prepareSensor() {
        viewModel.setSensor(new LightSensor((SensorManager) getSystemService(SENSOR_SERVICE)));
    }*/
}