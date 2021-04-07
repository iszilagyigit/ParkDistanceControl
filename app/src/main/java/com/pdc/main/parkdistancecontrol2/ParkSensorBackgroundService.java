package com.pdc.main.parkdistancecontrol2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ParkSensorBackgroundService extends JobIntentService {
    private static final int JOB_ID = 1000;
    public static final String PARK_SENSOR_INTENT_NAME = "park_sens";
    public static final String PARK_SENSOR_1_KEY = "sensor1";
    public static final String PARK_SENSOR_2_KEY = "sensor2";
    public static final String PARK_SENSOR_3_KEY = "sensor3";
    public static final String PARK_SENSOR_4_KEY = "sensor4";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ParkSensorBackgroundService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i("BackgroundService", "------> Executing work: " + intent);

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(500);
                Intent measureIntent = new Intent(PARK_SENSOR_INTENT_NAME);
                // TODO read values from RPI SPI
                measureIntent.putExtra(PARK_SENSOR_1_KEY, i + 10);
                measureIntent.putExtra(PARK_SENSOR_2_KEY, 34);
                measureIntent.putExtra(PARK_SENSOR_3_KEY, 23);
                measureIntent.putExtra(PARK_SENSOR_4_KEY, 27);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(measureIntent);
            } catch (InterruptedException e) {
                Log.e("BackgroundService", "Interrupted", e);
            }
        }
    }
}