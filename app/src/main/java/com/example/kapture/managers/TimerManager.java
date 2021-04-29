package com.example.kapture.managers;

import com.example.kapture.CameraViewModel;

import java.util.concurrent.TimeUnit;

public class TimerManager extends Thread {
    private final CameraViewModel viewModel;
    private boolean paused = true;

    public TimerManager(CameraViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void run() {
        long storedTime = System.currentTimeMillis();
        while (viewModel.getDelay() > 0 || viewModel.getDuration() > 0) {
            if (viewModel.isFinishAllThreads()) break;
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
            if (storedTime + 1000 < System.currentTimeMillis()) {
                storedTime += 1000;
                if (!paused) {
                    if (viewModel.getDelay() > 0)
                        viewModel.setDelay(viewModel.getDelay() - 1);
                    else
                        viewModel.setDuration(viewModel.getDuration() - 1);
                }
            }
        }
    }

    synchronized public void pauseTimer() {
        paused = true;
    }

    synchronized public void resumeTimer() {
        paused = false;
    }
}
