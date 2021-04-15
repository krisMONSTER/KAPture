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
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Camera extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int tileSize = 200;
    private final int tileTolerance = 8;
    private ArrayList<int[]> cameraTiles;
    private Bitmap cameraBMP;
    private Thread monitoring;
    private boolean breakMonitoring = false;
    private boolean safeToTakePicture = false;

    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;
    AudioManager amanager;
    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        amanager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        preview = findViewById(R.id.camera_frame_layout);

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

        //getting data from pickers
        Intent intent = getIntent();
        int duration = intent.getIntExtra("duration", 0);
        int delay = intent.getIntExtra("delay", 0);
        System.out.println("Duration Camera.class " + duration);
        System.out.println("Delay Camera.class " + delay);

        //monitoring cycle
        monitoring = new Thread(() -> {
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

        //set camera orientation
        /* api level is > 24 so method below is redundant */
        /*setCameraOrientation();*/
        setOrientation();

        //unlock taking pictures
        safeToTakePicture = true;
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
        } catch (Exception ignored) {}
    }

    /*private void setCameraOrientation(){
        android.hardware.Camera.Parameters p = mCamera.getParameters();
        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setOrientation(mCamera);
        else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                p.set("orientation", "portrait");
                p.set("rotation", 90);
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                p.set("orientation", "landscape");
                p.set("rotation", 90);
            }
        }
    }*/
}