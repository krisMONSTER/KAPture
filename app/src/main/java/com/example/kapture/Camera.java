package com.example.kapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Camera extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int tileSize = 200;
    private final int tileTolerance = 8;
    private ArrayList<int[]> cameraTiles;
    private Bitmap cameraBMP;
    private Thread monitoring;
    private boolean breakMonitoring = false;
    private boolean safeToTakePicture = false;
    private final Semaphore startMonitoring = new Semaphore(0);
    private OrientationListener orientationListener;

    private SoundPool soundPool;
    private int sound1, sound2, sound3, sound4, sound5, sound6, sound7, sound8, sound9, sound10;

    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;
    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        preview = findViewById(R.id.camera_frame_layout);

        //detect orientation change to flip fragment view
        orientationListener = new OrientationListener(this) {
            @Override
            public void onSimpleOrientationChanged(int orientation) {
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    System.out.println("landscape");
                }else if(orientation == Configuration.ORIENTATION_PORTRAIT){
                    System.out.println("portrait");
                }
            }
        };
        orientationListener.enable();

        //getting data from pickers
        Intent intent = getIntent();
        int duration = intent.getIntExtra("duration", 0);
        int delay = intent.getIntExtra("delay", 0);
        int alarm_id = intent.getIntExtra("alarmId", 0);
        System.out.println("Duration Camera.class " + duration);
        System.out.println("Delay Camera.class " + delay);
        System.out.println("Alarm_id Camera.class " + alarm_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10) //dac 1
                    .setAudioAttributes(audioAttributes)
                    .build();
        }
        else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0); //wybrac 1 oraz STREAM_ALARM
        }
        sound1 = soundPool.load(this, R.raw.alert_robery, 1);
        sound2 = soundPool.load(this, R.raw.bank_robery, 1);
        sound3 = soundPool.load(this, R.raw.buzzer, 1);
        sound4 = soundPool.load(this, R.raw.camera_snap, 1);
        sound5 = soundPool.load(this, R.raw.chicken, 1);
        sound6 = soundPool.load(this, R.raw.military_alarm, 1);
        sound7 = soundPool.load(this, R.raw.police, 1);
        sound8 = soundPool.load(this, R.raw.punch, 1);
        sound9 = soundPool.load(this, R.raw.school_bell, 1);
        sound10 = soundPool.load(this, R.raw.whistle, 1);

        //taken pictures processing
        android.hardware.Camera.PictureCallback pictureCallback = ((data, camera) -> {
            safeToTakePicture = false;
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
            Thread processing = new Thread(() -> {
                cameraBMP = BitmapFactory.decodeByteArray(data, 0, data.length);
                cameraBMP = Bitmap.createScaledBitmap(cameraBMP, 400, 400, false);
                if (cameraTiles == null) {
                    cameraTiles = new ArrayList<>();
                    calculateTiles(cameraTiles);
                } else {
                    ArrayList<int[]> currentCameraTiles = new ArrayList<>();
                    calculateTiles(currentCameraTiles);
                    for (int i = 0; i < cameraTiles.size(); i++) {
                        int redDifference = Math.abs(cameraTiles.get(i)[0] - currentCameraTiles.get(i)[0]);
                        int greenDifference = Math.abs(cameraTiles.get(i)[1] - currentCameraTiles.get(i)[1]);
                        int blueDifference = Math.abs(cameraTiles.get(i)[2] - currentCameraTiles.get(i)[2]);
                        //Log.d("red difference", "" + redDifference);
                        //Log.d("green difference", "" + greenDifference);
                        //Log.d("blue difference", "" + blueDifference);
                        if (redDifference > tileTolerance ||
                                greenDifference > tileTolerance ||
                                blueDifference > tileTolerance) {
                            Log.d("monitoring", "movement detected");
                            soundPool.play(alarm_id, 1, 1, 0, 0, 1);
                        }
                    }
                    cameraTiles = currentCameraTiles;
                }
            });
            processing.start();
            try {
                processing.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            safeToTakePicture = true;
        });


        //monitoring cycle
        monitoring = new Thread(() -> {
            //wait for camera to load
            try {
                startMonitoring.acquire();
            } catch (InterruptedException e) {
                return;
            }
            //opóźnienie
            try {
                TimeUnit.SECONDS.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int seconds = 0; seconds < duration; seconds++) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
                if (breakMonitoring)
                    break;
                if (safeToTakePicture) {
                    if (mPreview != null && mPreview.isSafeToTakePicture()) {
                        mCamera.takePicture(null, null, pictureCallback);
                        mPreview.setSafeToTakePicture(false);
                    }
                }
            }
        });
        monitoring.start();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        soundPool.release();
        soundPool = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        orientationListener.disable();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                System.exit(0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        safeToTakePicture = false;
        preview.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onBackPressed() {
        breakMonitoring = true;
        monitoring.interrupt();
        try {
            monitoring.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void calculateTiles(ArrayList<int[]> tiles) {
        int x, y;
        for (x = 0; x + tileSize < cameraBMP.getWidth(); x += tileSize) {
            for (y = 0; y + tileSize < cameraBMP.getHeight(); y += tileSize) {
                tiles.add(calculateTile(x, y));
            }
        }
    }

    private int[] calculateTile(int x, int y) {
        int r = 0;
        int g = 0;
        int b = 0;
        int colour;
        for (int i = x; i < tileSize + x; i++) {
            for (int ii = y; ii < tileSize + y; ii++) {
                colour = cameraBMP.getPixel(i, ii);
                r += (colour >> 16) & 0xff;
                g += (colour >> 8) & 0xff;
                b += colour & 0xff;
            }
        }
        return new int[]{
                r / (tileSize * tileSize),
                g / (tileSize * tileSize),
                b / (tileSize * tileSize)
        };
    }

    private void startCamera() {
        //set camera and preview
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        //ustawienie rozdzielczości wyświetlanego obrazu
        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        for (android.hardware.Camera.Size x : parameters.getSupportedPreviewSizes()) {
            if (((float) x.width / x.height) == 16f / 9f) {
                //parameters.setPreviewSize(176,144);
                parameters.setPreviewSize(x.width, x.height);
                //System.out.println("Ustawiono: "+ x.width + " x " + x.height);
                break;
            }

        }
        //ustawienie typu focusa

        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        else if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_INFINITY))
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_INFINITY);
        else
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);



        //System.out.println("Scene modes: " + parameters.getSupportedSceneModes());
        //parameters.setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_HDR);


        mCamera.setParameters(parameters);

        //set camera orientation
        setOrientation();

        //unlock taking pictures
        safeToTakePicture = true;

        //start monitoring
        if (startMonitoring.availablePermits() == 0)
            startMonitoring.release();
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
            downPolymorphic = mCamera.getClass().getMethod("setDisplayOrientation", int.class);
            downPolymorphic.invoke(mCamera, 90);
        } catch (Exception ignored) {
        }
    }
}