package com.example.kapture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.reflect.Method;

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


        FrameLayout preview = findViewById(R.id.camera_frame_layout);
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


    protected void setDisplayOrientation(android.hardware.Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[]{angle});
        } catch (Exception e1) {
        }
    }
}