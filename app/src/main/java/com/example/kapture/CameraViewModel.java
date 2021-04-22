package com.example.kapture;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.SoundPool;
import android.view.LayoutInflater;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class CameraViewModel extends ViewModel {
    private final Semaphore startMonitoring = new Semaphore(0);
    private final String channelID = "KAPture Alert";
    private final int notificationId = 5796;
    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int tileSize = 200;
    private final int movementTolerance = 15;
    private final int lightTolerance = 30;
    private NotificationCompat.Builder notification;
    private ArrayList<int[]> cameraTiles;
    private Bitmap cameraBMP;
    private Thread monitoring;
    private boolean breakMonitoring = false;
    private boolean safeToTakePicture = false;
    private int alarmId;
    private LayoutInflater controlInflater;
    private SoundPool soundPool;
    private android.hardware.Camera camera;

    public int getLightTolerance() {
        return lightTolerance;
    }

    public LayoutInflater getControlInflater() {
        return controlInflater;
    }

    public void setControlInflater(LayoutInflater controlInflater) {
        this.controlInflater = controlInflater;
    }

    public SoundPool getSoundPool() {
        return soundPool;
    }

    public void setSoundPool(SoundPool soundPool) {
        this.soundPool = soundPool;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public Semaphore getStartMonitoring() {
        return startMonitoring;
    }

    public String getChannelID() {
        return channelID;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public int getPERMISSIONS_REQUEST_CODE() {
        return PERMISSIONS_REQUEST_CODE;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getMovementTolerance() {
        return movementTolerance;
    }

    public NotificationCompat.Builder getNotification() {
        return notification;
    }

    public void setNotification(NotificationCompat.Builder notification) {
        this.notification = notification;
    }

    public ArrayList<int[]> getCameraTiles() {
        return cameraTiles;
    }

    public void setCameraTiles(ArrayList<int[]> cameraTiles) {
        this.cameraTiles = cameraTiles;
    }

    public Bitmap getCameraBMP() {
        return cameraBMP;
    }

    public void setCameraBMP(Bitmap cameraBMP) {
        this.cameraBMP = cameraBMP;
    }

    public Thread getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(Thread monitoring) {
        this.monitoring = monitoring;
    }

    public boolean isBreakMonitoring() {
        return breakMonitoring;
    }

    public void setBreakMonitoring(boolean breakMonitoring) {
        this.breakMonitoring = breakMonitoring;
    }

    public boolean isSafeToTakePicture() {
        return safeToTakePicture;
    }

    public void setSafeToTakePicture(boolean safeToTakePicture) {
        this.safeToTakePicture = safeToTakePicture;
    }
}
