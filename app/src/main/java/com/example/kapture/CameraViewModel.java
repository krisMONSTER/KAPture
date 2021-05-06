package com.example.kapture;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.SoundPool;
import android.view.LayoutInflater;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModel;

import com.example.kapture.fragments.HistoryHelper.DatabaseHelper;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class CameraViewModel extends ViewModel {
    private final Semaphore startMonitoring = new Semaphore(0);
    private final String channelID = "KAPture Alert";
    private final int notificationId = 5796;
    private final int PERMISSIONS_REQUEST_CODE = 1;
    private final int PERMISSIONS_REQUEST_SMS = 152;
    private final int tileSize = 100;
    private final int movementTolerance = 8;
    private NotificationCompat.Builder notification;
    private ArrayList<int[]> cameraTiles;
    private Bitmap cameraBMP;
    private boolean finishAllThreads = false;
    private boolean safeToTakePicture = false;
    private LayoutInflater controlInflater;
    private SoundPool soundPool;
    private android.hardware.Camera camera;
    private CameraPreview cameraPreview;
    private int alarmId;
    private int delay;
    private int duration;
    private String phoneNumber;
    private boolean sendSMS;
    private boolean takingPicturesEnabled;
    private LightSensor sensor;
    private DatabaseHelper databaseHelper;

    //getters and setters below


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isTakingPicturesEnabled() {
        return takingPicturesEnabled;
    }

    public void setTakingPicturesEnabled(boolean takingPicturesEnabled) {
        this.takingPicturesEnabled = takingPicturesEnabled;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public void setDatabaseHelper(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public int getPERMISSIONS_REQUEST_SMS() {
        return PERMISSIONS_REQUEST_SMS;
    }

    public boolean isSendSMS() {
        return sendSMS;
    }

    public  void setSendSMS(boolean sendSMS) {
        this.sendSMS = sendSMS;
    }

    public LightSensor getSensor() {
        return sensor;
    }

    public void setSensor(LightSensor sensor) {
        this.sensor = sensor;
    }

    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }

    public void setCameraPreview(CameraPreview cameraPreview) {
        this.cameraPreview = cameraPreview;
    }

    synchronized public int getDelay() {
        return delay;
    }

    synchronized public void setDelay(int delay) {
        this.delay = delay;
    }

    synchronized public int getDuration() {
        return duration;
    }

    synchronized public void setDuration(int duration) {
        this.duration = duration;
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

    public boolean isFinishAllThreads() {
        return finishAllThreads;
    }

    public void setFinishAllThreads(boolean finishAllThreads) {
        this.finishAllThreads = finishAllThreads;
    }

    public boolean isSafeToTakePicture() {
        return safeToTakePicture;
    }

    public void setSafeToTakePicture(boolean safeToTakePicture) {
        this.safeToTakePicture = safeToTakePicture;
    }
}
