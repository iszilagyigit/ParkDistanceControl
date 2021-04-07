package com.pdc.main.parkdistancecontrol2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

public class ParkSensorBroadcastReceiver extends BroadcastReceiver {
    private View sensor1View;

    public ParkSensorBroadcastReceiver(View sensor1) {
        this.sensor1View = sensor1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int sensor1Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_1_KEY, -1);
        final int sensor2Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_2_KEY, -1);
        final int sensor3Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_3_KEY, -1);
        final int sensor4Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_4_KEY, -1);

        Log.i("MainActivity - received:", "<----"
                + sensor1Cm + "\t"
                + sensor2Cm + "\t"
                + sensor3Cm + "\t"
                + sensor4Cm + "\t"
        );
        if (sensor1Cm % 2 == 0) {
            this.sensor1View.findViewById(R.id.button1_6).setVisibility(View.INVISIBLE);
        }else {
            this.sensor1View.findViewById(R.id.button1_6).setVisibility(View.VISIBLE);
        }

    }

}
