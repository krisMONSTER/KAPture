package com.example.kapture;

import com.example.kapture.activities.Camera;
import com.example.kapture.fragments.HistoryHelper.CustomAdapter;
import com.example.kapture.fragments.HistoryHelper.DataModel;
import com.example.kapture.managers.TimerManager;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void dataModelUnitTest(){
        String event = "Event";
        String date = "13.05.2021";
        String time = "00:00";
        DataModel dataModel = new DataModel(event, date, time);
        assertDoesNotThrow(() -> {
            String receivedDate = dataModel.getDate();
            String receivedTime = dataModel.getTime();
            assertEquals(date, receivedDate);
            assertEquals(time, receivedTime);
        });
    }

    @Test
    public void timerManagerUnitTest(){
        final int initialDelayTime = 5;
        final int initialDurationTime = 5;
        CameraViewModel viewModel = new CameraViewModel();
        viewModel.setDelay(initialDelayTime);
        viewModel.setDuration(initialDurationTime);
        TimerManager timerManager = new TimerManager(viewModel);
        timerManager.start();
        timerManager.resumeTimer();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timerManager.pauseTimer();
        assertNotEquals(initialDelayTime, viewModel.getDelay());
        assertTrue(viewModel.getDelay() >= 2 && viewModel.getDelay() <= 4);
        assertDoesNotThrow(timerManager::interrupt);
    }
}