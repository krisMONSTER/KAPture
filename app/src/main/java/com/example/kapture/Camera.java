package com.example.kapture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class Camera extends AppCompatActivity {
    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (ContextCompat.checkSelfPermission(Camera.this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        // PERMISSION DENIED
        {
            ActivityCompat
                    .requestPermissions(
                            Camera.this,
                            new String[]{Manifest.permission.CAMERA},
                            1);
        }
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        mCamera.setDisplayOrientation(90);

        FrameLayout preview = findViewById(R.id.camera_frame_layout);
        preview.addView(mPreview);



    }
    public static android.hardware.Camera getCameraInstance() {
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

}