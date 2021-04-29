package com.example.kapture.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kapture.CameraPreview;
import com.example.kapture.CameraViewModel;
import com.example.kapture.LightSensor;
import com.example.kapture.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Camera extends AppCompatActivity {
    private final CameraViewModel viewModel = new CameraViewModel();
    private CameraPreview mPreview;
    FrameLayout preview;


    //for later
    /*private OrientationListener orientationListener;*/

    /*private boolean test = true;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //detect orientation change to flip fragment view
        //for later
        /*orientationListener = new OrientationListener(this) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    System.out.println("landscape");
                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    System.out.println("portrait");
                }
            }
        };
        orientationListener.enable();*/

        preview = findViewById(R.id.camera_frame_layout);
        //preview overlay
        viewModel.setControlInflater(LayoutInflater.from(getBaseContext()));
        View viewControl = viewModel.getControlInflater().inflate(R.layout.camera_overlay_layout, null);
        FrameLayout.LayoutParams layoutParamsControl
                = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
                FrameLayout.LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
        TextView workFor = findViewById(R.id.detectionWorkForTV);
        TextView startIn = findViewById(R.id.detectionStartsInTV);
        Intent intent = getIntent();
        /*prepareSensor();      flash, na razie z niego rezygnujemy   */

        Thread overlayUpdate = new Thread(() -> {
            int workForTime = intent.getIntExtra("duration", 0);
            int startInTime = intent.getIntExtra("delay", 0);

            if (startInTime > 0) {
                runOnUiThread(() -> workFor.setText("Detecting hasn't started yet!"));

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
                        startIn.setText("Starts in: " + temp);
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
                    startIn.setText("Detecting has already started!");
                });

                while (workForTime > 0) {
                    int temp = workForTime;
                    runOnUiThread(() -> {
                        workFor.setText("Detecting will be on for: " + temp);
                    });

                    workForTime--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            runOnUiThread(() -> {
                startIn.setText("Detection Finished!");
                workFor.setText("Detection Finished!");
            });


        });
        overlayUpdate.start();

        //notification creation
        createNotificationChannel();
        viewModel.setNotification(new NotificationCompat
                .Builder(this, viewModel.getChannelID())
                .setSmallIcon(R.drawable.ic_kapture_alert)
                .setContentTitle("KAPture Alert !")
                .setContentText("Movement Detected")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT));

        //getting data from pickers
        int duration = intent.getIntExtra("duration", 0);
        int delay = intent.getIntExtra("delay", 0);
        viewModel.setAlarmId(intent.getIntExtra("alarmId", 0));
        System.out.println("Duration Camera.class " + duration);
        System.out.println("Delay Camera.class " + delay);
        System.out.println("Alarm_id Camera.class " + viewModel.getAlarmId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            viewModel.setSoundPool(new SoundPool.Builder()
                    .setMaxStreams(10) //dac 1
                    .setAudioAttributes(audioAttributes)
                    .build());
        } else {
            viewModel.setSoundPool(new SoundPool(10, AudioManager.STREAM_MUSIC, 0)); //wybrac 1 oraz STREAM_ALARM
        }
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

        //taken pictures processing
        android.hardware.Camera.PictureCallback pictureCallback = ((data, camera) -> {
            viewModel.setSafeToTakePicture(false);
            camera.startPreview();
            mPreview.setSafeToTakePicture(true);
            //coordinates are flipped
            /*
            (0,max)     (0,0)
                 ********
                 ********
                 *screen*
                 ********
                 ********
            (max,max)   (max,0)
             */
            Thread processing = new Thread(() -> processPictureTask(data));
            processing.start();
            try {
                processing.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            viewModel.setSafeToTakePicture(true);
        });


        //monitoring cycle
        viewModel.setMonitoring(new Thread(() -> {

            //wait for camera to load
            try {
                viewModel.getStartMonitoring().acquire();
            } catch (InterruptedException e) {
                return;
            }
            //opóźnienie
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                return;
            }
            for (int seconds = 0; seconds < duration; seconds++) {
                /*if (viewModel.getCamera() != null) { //do flasha, ale na razie z niego rezygnujemy
                    System.out.println("Light:" + viewModel.getSensor().getLight());
                    if (viewModel.getSensor().getLight() < 4 && viewModel.getCamera().getParameters().getFlashMode().equals("off")) {
                        android.hardware.Camera.Parameters x = viewModel.getCamera().getParameters();
                        x.setFlashMode("on");
                        viewModel.getCamera().setParameters(x);
                    } else {
                        android.hardware.Camera.Parameters x = viewModel.getCamera().getParameters();
                        x.setFlashMode("off");
                        viewModel.getCamera().setParameters(x);
                    }
                }*/
                try {
                    TimeUnit.MILLISECONDS.sleep(700);
                } catch (InterruptedException e) {
                    break;
                }
                if (viewModel.isBreakMonitoring())
                    break;
                if (viewModel.isSafeToTakePicture()) {
                    if (mPreview != null && mPreview.isSafeToTakePicture()) {
                        viewModel.getCamera().takePicture(null, null, pictureCallback);
                        mPreview.setSafeToTakePicture(false);
                    }
                }
            }
        }));
        viewModel.getMonitoring().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getSoundPool().release();
        viewModel.setSoundPool(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //for later
        /*orientationListener.disable();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == viewModel.getPERMISSIONS_REQUEST_CODE()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        } else {
            System.exit(0);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setSafeToTakePicture(false);
        preview.removeAllViews();
        //viewModel.getSensor().getLightSensorManager().unregisterListener(viewModel.getSensor());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //viewModel.getSensor().getLightSensorManager().registerListener(viewModel.getSensor(), viewModel.getSensor().getLightSensor(), SensorManager.SENSOR_DELAY_NORMAL);
        ArrayList<String> permissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.CAMERA);
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);*/
        if (permissionsList.size() > 0) {
            String[] permissionsArray = new String[permissionsList.size()];
            for (int i = 0; i < permissionsList.size(); i++)
                permissionsArray[i] = permissionsList.get(i);
            ActivityCompat.requestPermissions(this, permissionsArray, viewModel.getPERMISSIONS_REQUEST_CODE());
        } else {
            startCamera();
        }
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, viewModel.getPERMISSIONS_REQUEST_CODE());
        } else {
            startCamera();
        }*/
    }

    @Override
    public void onBackPressed() {
        viewModel.setBreakMonitoring(true);
        viewModel.getMonitoring().interrupt();
        try {
            viewModel.getMonitoring().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void calculateTiles(ArrayList<int[]> tiles) {
        int x, y;
        for (x = 0; x + viewModel.getTileSize() <= viewModel.getCameraBMP().getWidth(); x += viewModel.getTileSize()) {
            for (y = 0; y + viewModel.getTileSize() <= viewModel.getCameraBMP().getHeight(); y += viewModel.getTileSize()) {
                int r = 0;
                int g = 0;
                int b = 0;
                int pixelAmount = 0;
                int colour;
                for (int i = x; i < viewModel.getTileSize() + x; i++) {
                    for (int ii = y; ii < viewModel.getTileSize() + y; ii++) {
                        colour = viewModel.getCameraBMP().getPixel(i, ii);
                        r += (colour >> 16) & 0xff;
                        g += (colour >> 8) & 0xff;
                        b += colour & 0xff;
                        pixelAmount++;
                    }
                }
                tiles.add(new int[]{
                        r / pixelAmount,
                        g / pixelAmount,
                        b / pixelAmount
                });
            }
        }
    }

    private void startCamera() {
        //set camera and preview
        viewModel.setCamera(getCameraInstance());
        mPreview = new CameraPreview(this, viewModel.getCamera());
        preview.addView(mPreview);

        //ustawienie rozdzielczości wyświetlanego obrazu
        android.hardware.Camera.Parameters parameters = viewModel.getCamera().getParameters();
        for (android.hardware.Camera.Size x : parameters.getSupportedPreviewSizes()) {
            if (((float) x.width / x.height) == 16f / 9f) {
                //parameters.setPreviewSize(176,144);
                parameters.setPreviewSize(x.width, x.height);
                //System.out.println("Ustawiono: "+ x.width + " x " + x.height);
                break;
            }

        }

        //ustawienie typu focusa
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED))
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_FIXED);

        //System.out.println("Scene modes: " + parameters.getSupportedSceneModes());
        //parameters.setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_HDR);

        viewModel.getCamera().setParameters(parameters);

        //set camera orientation
        setOrientation();

        //unlock taking pictures
        viewModel.setSafeToTakePicture(true);

        //start monitoring
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

    private void setOrientation() {
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

    public void sendNotification(int not_id, NotificationCompat.Builder notification_builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(not_id, notification_builder.build());
    }

    public void prepareSensor() {
        viewModel.setSensor(new LightSensor((SensorManager) getSystemService(SENSOR_SERVICE)));

    }

    private void sendSMSNotification() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        viewModel.getPERMISSIONS_REQUEST_SMS());
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            String telephoneNumber = "783513584";
            smsManager.sendTextMessage(telephoneNumber, null, "Alert", null, null);
        }
    }


    private void processPictureTask(byte[] data) {
        viewModel.setCameraBMP(BitmapFactory.decodeByteArray(data, 0, data.length));
        viewModel.setCameraBMP(Bitmap.createScaledBitmap(viewModel.getCameraBMP(), 400, 400, false));
        /*if (test){
            MediaStore.Images.Media.insertImage(getContentResolver(), viewModel.getCameraBMP(),
                    "image", null);
            test = false;
        }*/
        if (viewModel.getCameraTiles() == null) {
            viewModel.setCameraTiles(new ArrayList<>());
            calculateTiles(viewModel.getCameraTiles());
        } else {
            ArrayList<int[]> currentCameraTiles = new ArrayList<>();
            calculateTiles(currentCameraTiles);
            ArrayList<int[]> colourDifferences = new ArrayList<>();
            for (int i = 0; i < viewModel.getCameraTiles().size(); i++) {
                int redDifference = Math.abs(viewModel.getCameraTiles().get(i)[0] - currentCameraTiles.get(i)[0]);
                int greenDifference = Math.abs(viewModel.getCameraTiles().get(i)[1] - currentCameraTiles.get(i)[1]);
                int blueDifference = Math.abs(viewModel.getCameraTiles().get(i)[2] - currentCameraTiles.get(i)[2]);
                colourDifferences.add(new int[]{redDifference, greenDifference, blueDifference});
            }
            int mutualRedDiff = colourDifferences.get(0)[0];
            int mutualGreenDiff = colourDifferences.get(0)[1];
            int mutualBlueDiff = colourDifferences.get(0)[2];
            for (int i = 1; i < viewModel.getCameraTiles().size(); i++) {
                if (mutualRedDiff > colourDifferences.get(i)[0])
                    mutualRedDiff = colourDifferences.get(i)[0];
                if (mutualGreenDiff > colourDifferences.get(i)[1])
                    mutualGreenDiff = colourDifferences.get(i)[1];
                if (mutualBlueDiff > colourDifferences.get(i)[2])
                    mutualBlueDiff = colourDifferences.get(i)[2];
            }
            System.out.println(mutualRedDiff + " " + mutualGreenDiff + " " + mutualBlueDiff);
            for (int[] colourDiff : colourDifferences) {
                colourDiff[0] -= mutualRedDiff;
                colourDiff[1] -= mutualGreenDiff;
                colourDiff[2] -= mutualBlueDiff;
                if (colourDiff[0] > viewModel.getMovementTolerance() ||
                        colourDiff[1] > viewModel.getMovementTolerance() ||
                        colourDiff[2] > viewModel.getMovementTolerance()) {
                    Log.d("monitoring", "movement detected");
                    if (viewModel.isSendSMS())
                        sendSMSNotification();
                    sendNotification(viewModel.getNotificationId(), viewModel.getNotification());
                    viewModel.getSoundPool().play(viewModel.getAlarmId(), 1, 1, 0, 0, 1);
                    break;
                }
            }
            viewModel.setCameraTiles(currentCameraTiles);
        }
    }
}