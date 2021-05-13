package com.pdc.main.parkdistancecontrol2;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

/**
 * MainActivity for the Application.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                configureLoopDelay();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Loop delay configuration used to syncr. the communication with sensors.<br>
     * The lowest working value should be chosen.
     */
    private void configureLoopDelay() {
        Log.i("menu", "Settings selected!!!");

        Dialog dialog = new Dialog(this);
        dialog.setTitle(R.string.time_slider_dialog_title);
        dialog.setContentView(R.layout.time_slider_dialog);

        final Slider slider = dialog.findViewById(R.id.time_slider_slider);
        slider.setValue(ParkSensorBackgroundService.LOOP_DELAY_IN_MS);

        final TextView header = dialog.findViewById(R.id.time_slider_header);
        header.setText(String.format(getString(R.string.time_slider_dialog_message), ParkSensorBackgroundService.LOOP_DELAY_IN_MS));

        final Button ok = dialog.findViewById(R.id.time_slider_ok);
        ok.setOnClickListener(view -> {
            final float sliderValue = slider.getValue();
            Log.i("newvalue:", String.valueOf(sliderValue));
            ParkSensorBackgroundService.LOOP_DELAY_IN_MS = (int) sliderValue;
            dialog.dismiss();
        });

        Button cancel = dialog.findViewById(R.id.time_slider_cancel);
        cancel.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
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