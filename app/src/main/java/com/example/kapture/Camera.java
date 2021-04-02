package com.example.kapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE = 1;
    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;
    private final Semaphore cameraLock = new Semaphore(0);

    FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        preview = findViewById(R.id.camera_frame_layout);

        //taken pictures processing
        android.hardware.Camera.PictureCallback pictureCallback = ((data, camera) -> {
            new Thread(() -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }).start();
            camera.startPreview();
        });

        //monitoring cycle
        new Thread(() -> {
            for (int seconds = 0; seconds < 10; seconds++){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraLock.availablePermits() > 0){
                    if (mPreview != null && mPreview.isSafeToTakePicture()) {
                        mCamera.takePicture(null, null, pictureCallback);
                    }
                }
            }
        }).start();
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
        cameraLock.tryAcquire();
        preview.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        }
        else {
            startCamera();
        }
    }

    private void startCamera(){
        //set camera and preview
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);

        //set camera orientation
        android.hardware.Camera.Parameters p = mCamera.getParameters();
        if (Integer.parseInt(Build.VERSION.SDK) >= 8)
            setDisplayOrientation(mCamera, 90);
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

        //unlock taking pictures
        cameraLock.release();
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

    private void setDisplayOrientation(android.hardware.Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", int.class);
            downPolymorphic.invoke(camera, angle);
        } catch (Exception ignored) {}
    }
}