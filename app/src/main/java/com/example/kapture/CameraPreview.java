package com.example.kapture;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean safeToTakePicture = false;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        safeToTakePicture = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        safeToTakePicture = false;
        mCamera.stopPreview();
        mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        safeToTakePicture = false;
        if (mHolder.getSurface() == null)
            return;
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        safeToTakePicture = true;
    }

    public boolean isSafeToTakePicture() {
        return safeToTakePicture;
    }

    public void setSafeToTakePicture(boolean safe) {
        this.safeToTakePicture = safe;
    }
}
