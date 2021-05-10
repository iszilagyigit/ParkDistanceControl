package com.pdc.main.parkdistancecontrol2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Receives Intents from {@link ParkSensorBackgroundService} and update the GUI.
 */
public class ParkSensorBroadcastReceiver extends BroadcastReceiver {

    /*
    Distance marks where new Bars get visible.
    */
    private static int[] DISTANCE_MARKS_IN_CM = new int[] {30, 50, 80, 100, 150, 200, 250, 300};

    private static int MAX_NR_OF_BARS = 8;

    private final Button[] sensor1Buttons = new Button[8];
    private final Button[] sensor2Buttons = new Button[8];
    private final Button[] sensor3Buttons = new Button[8];
    private final Button[] sensor4Buttons = new Button[8];

    public ParkSensorBroadcastReceiver(final View sensor1View, final  View sensor2View,
                                       final  View sensor3View, final View sensor4View) {
        sensor1Buttons[0] = sensor1View.findViewById(R.id.button1_1);
        sensor1Buttons[1] = sensor1View.findViewById(R.id.button1_2);
        sensor1Buttons[2] = sensor1View.findViewById(R.id.button1_3);
        sensor1Buttons[3] = sensor1View.findViewById(R.id.button1_4);
        sensor1Buttons[4] = sensor1View.findViewById(R.id.button1_5);
        sensor1Buttons[5] = sensor1View.findViewById(R.id.button1_6);
        sensor1Buttons[6] = sensor1View.findViewById(R.id.button1_7);
        sensor1Buttons[7] = sensor1View.findViewById(R.id.button1_8);
        sensor2Buttons[0] = sensor2View.findViewById(R.id.button2_1);
        sensor2Buttons[1] = sensor2View.findViewById(R.id.button2_2);
        sensor2Buttons[2] = sensor2View.findViewById(R.id.button2_3);
        sensor2Buttons[3] = sensor2View.findViewById(R.id.button2_4);
        sensor2Buttons[4] = sensor2View.findViewById(R.id.button2_5);
        sensor2Buttons[5] = sensor2View.findViewById(R.id.button2_6);
        sensor2Buttons[6] = sensor2View.findViewById(R.id.button2_7);
        sensor2Buttons[7] = sensor2View.findViewById(R.id.button2_8);
        sensor3Buttons[0] = sensor3View.findViewById(R.id.button3_1);
        sensor3Buttons[1] = sensor3View.findViewById(R.id.button3_2);
        sensor3Buttons[2] = sensor3View.findViewById(R.id.button3_3);
        sensor3Buttons[3] = sensor3View.findViewById(R.id.button3_4);
        sensor3Buttons[4] = sensor3View.findViewById(R.id.button3_5);
        sensor3Buttons[5] = sensor3View.findViewById(R.id.button3_6);
        sensor3Buttons[6] = sensor3View.findViewById(R.id.button3_7);
        sensor3Buttons[7] = sensor3View.findViewById(R.id.button3_8);
        sensor4Buttons[0] = sensor4View.findViewById(R.id.button4_1);
        sensor4Buttons[1] = sensor4View.findViewById(R.id.button4_2);
        sensor4Buttons[2] = sensor4View.findViewById(R.id.button4_3);
        sensor4Buttons[3] = sensor4View.findViewById(R.id.button4_4);
        sensor4Buttons[4] = sensor4View.findViewById(R.id.button4_5);
        sensor4Buttons[5] = sensor4View.findViewById(R.id.button4_6);
        sensor4Buttons[6] = sensor4View.findViewById(R.id.button4_7);
        sensor4Buttons[7] = sensor4View.findViewById(R.id.button4_8);
    }

    private int cmToNrOfBars(int distanceInCm) {
        if (distanceInCm == -1) {
            return 0;
        }
        for (int i = 0; i < DISTANCE_MARKS_IN_CM.length; i++ ) {
            if (distanceInCm <= DISTANCE_MARKS_IN_CM[i]) {
                return ParkSensorBroadcastReceiver.MAX_NR_OF_BARS - i;
            }
        }
        return 0; // default
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int sensor1Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_1_KEY, -1);
        final int sensor2Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_2_KEY, -1);
        final int sensor3Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_3_KEY, -1);
        final int sensor4Cm = intent.getIntExtra(ParkSensorBackgroundService.PARK_SENSOR_4_KEY, -1);

        Log.d("onReceive", "<--\t"
                + sensor1Cm + "\t"
                + sensor2Cm + "\t"
                + sensor3Cm + "\t"
                + sensor4Cm
        );

        int maxOfVisibleBars = 0;
        int nrOfBars;
        nrOfBars = updateDisplay(sensor1Buttons, sensor1Cm);
        if (nrOfBars > maxOfVisibleBars) {
            maxOfVisibleBars = nrOfBars;
        }
        nrOfBars = updateDisplay(sensor2Buttons, sensor2Cm);
        if (nrOfBars > maxOfVisibleBars) {
            maxOfVisibleBars = nrOfBars;
        }
        nrOfBars = updateDisplay(sensor3Buttons, sensor3Cm);
        if (nrOfBars > maxOfVisibleBars) {
            maxOfVisibleBars = nrOfBars;
        }
        nrOfBars = updateDisplay(sensor4Buttons, sensor4Cm);
        if (nrOfBars > maxOfVisibleBars) {
            maxOfVisibleBars = nrOfBars;
        }

        if (maxOfVisibleBars >= 8) {
            Log.i("onReceive", "--> play some 'beep' sound for 1 sec, max volume");
            playSound();
        }
    }

    private void playSound() {
        ToneGenerator generator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        generator.startTone(ToneGenerator.TONE_CDMA_PIP, 1000);
        generator.release();
    }

    private int updateDisplay(final Button[] viewButtons, int sensor1Cm) {
        int nrOfBars = cmToNrOfBars(sensor1Cm);
        //Log.d("nr of bars " + nrOfBars,"nr of bars " + nrOfBars);
        viewButtons[0].setVisibility(View.VISIBLE); // first button always visible.
        if (sensor1Cm == -1) {
            viewButtons[0].setText("-------");
            viewButtons[0].setBackgroundColor(Color.DKGRAY);
        }else {
            viewButtons[0].setBackgroundColor(Color.GREEN);
            viewButtons[0].setText(sensor1Cm + " cm");
        }

        for (int i = 1; i < viewButtons.length; i++) {
            viewButtons[i].setVisibility(nrOfBars <= i ? View.INVISIBLE : View.VISIBLE);
        }
        return nrOfBars;
      }
}
