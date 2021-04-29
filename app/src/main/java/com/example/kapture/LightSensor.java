package com.example.kapture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class LightSensor implements SensorEventListener {
    private SensorManager lightSensorManager;
    private Sensor lightSensor;
    private float light;

    public LightSensor(SensorManager sm) {
        lightSensorManager = sm;
        lightSensor = lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public SensorManager getLightSensorManager() {
        return lightSensorManager;
    }

    public Sensor getLightSensor() {
        return lightSensor;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        light = event.values[0];
    }

    public float getLight() {
        return light;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }
}