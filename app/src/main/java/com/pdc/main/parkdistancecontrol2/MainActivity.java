package com.pdc.main.parkdistancecontrol2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * MainActivity for the Application.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                new ParkSensorBroadcastReceiver(
                        findViewById(R.id.sensor1),
                        findViewById(R.id.sensor2),
                        findViewById(R.id.sensor3),
                        findViewById(R.id.sensor4)
                ),
                new IntentFilter(ParkSensorBackgroundService.PARK_SENSOR_INTENT_NAME));
        Intent serviceIntent = new Intent();
        ParkSensorBackgroundService.enqueueWork(getApplicationContext(), serviceIntent);
    }
}