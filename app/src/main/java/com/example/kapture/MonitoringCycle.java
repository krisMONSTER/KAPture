package com.example.kapture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MonitoringCycle extends Thread {
    LocalDate localDate;
    LocalTime localTime;
    private final CameraViewModel viewModel;
    private final android.hardware.Camera.PictureCallback pictureCallback;
    private final Context context;

    public MonitoringCycle(Context context, CameraViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
            localTime = LocalTime.now();

            DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            System.out.println(localDate.format(formatterDate));

            DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
            System.out.println(localTime.format(formatterTime));
        }

        pictureCallback = ((data, camera) -> {
            viewModel.setSafeToTakePicture(false);

            //resume camera preview
            camera.startPreview();
            viewModel.getCameraPreview().setSafeToTakePicture(true);

            //process for movement detection
            Thread processing = new Thread(() -> processPictureTask(data));
            processing.start();
        });
    }

    @Override
    public void run() {
        //wait for camera to load
        try {
            viewModel.getStartMonitoring().acquire();
        } catch (InterruptedException e) {
            return;
        }
        //set delay
        while (viewModel.getDelay() > 0) {
            if (viewModel.isFinishAllThreads()) break;
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                return;
            }
        }
        //monitoring cycle
        while (viewModel.getDuration() > 0) {
            if (viewModel.isFinishAllThreads()) break;
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
            if (viewModel.isSafeToTakePicture()) {
                if (viewModel.getCameraPreview() != null &&
                        viewModel.getCameraPreview().isSafeToTakePicture()) {
                    viewModel.getCameraPreview().setSafeToTakePicture(false);
                    viewModel.getCamera().takePicture(null, null, pictureCallback);
                }
            }
        }
    }

    private void processPictureTask(byte[] data) {
        viewModel.setCameraBMP(BitmapFactory.decodeByteArray(data, 0, data.length));
        viewModel.setCameraBMP(Bitmap.createScaledBitmap(viewModel.getCameraBMP(), 600, 400, false));
        if (viewModel.getCameraTiles() == null) {
            viewModel.setCameraTiles(new ArrayList<>());
            calculateTiles(viewModel.getCameraTiles());
        } else {
            ArrayList<int[]> currentCameraTiles = new ArrayList<>();
            calculateTiles(currentCameraTiles);
            ArrayList<int[]> colourDifferences = new ArrayList<>();
            for (int i = 0; i < viewModel.getCameraTiles().size(); i++){
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
            for (int[] colourDiff : colourDifferences) {
                colourDiff[0] -= mutualRedDiff;
                colourDiff[1] -= mutualGreenDiff;
                colourDiff[2] -= mutualBlueDiff;
                if (colourDiff[0] > viewModel.getMovementTolerance() ||
                        colourDiff[1] > viewModel.getMovementTolerance() ||
                        colourDiff[2] > viewModel.getMovementTolerance()) {
                    Log.d("monitoring", "movement detected");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        addData("Motion detected", LocalDate.now().toString(), LocalTime.now().toString());
                    }
                    if (viewModel.isSendSMS()) {
                        sendSMSNotification();
                        viewModel.setSendSMS(false);
                    }
                    sendNotification(viewModel.getNotificationId(), viewModel.getNotification());
                    viewModel.getSoundPool().play(viewModel.getAlarmId(), 1, 1, 0, 0, 1);
                    break;
                }
            }
            viewModel.setCameraTiles(currentCameraTiles);
        }
        viewModel.setSafeToTakePicture(true);
    }

    private void calculateTiles(ArrayList<int[]> tiles) {
        tiles.clear();
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

    private void sendNotification(int not_id, NotificationCompat.Builder notification_builder) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(not_id, notification_builder.build());
    }

    private void sendSMSNotification() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            String telephoneNumber = "783513584";
            smsManager.sendTextMessage(telephoneNumber, null, "Alert", null, null);
        }
    }

    private void addData(String newEntry, String date, String time){
        boolean insertData = viewModel.getDatabaseHelper().addData(newEntry, date, time);
        if (insertData) Log.d("insert data" , "Data Successsfully Inserted!" );//toastMessage("Data Successsfully Inserted!");
        else Log.d("insert data" , "Something went wrong" );//toastMessage("Something went wrong");
    }
}
