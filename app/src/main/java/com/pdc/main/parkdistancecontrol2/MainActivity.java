package com.pdc.main.parkdistancecontrol2;

import android.app.Dialog;
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

    // try it an delete it
    private final boolean alternative = true;

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
                Log.i("menu", "Settings selected!!!");

                if(alternative) {
                    Dialog dialog = new Dialog(this);
                    dialog.setTitle(R.string.time_slider_dialog_title);
                    dialog.setContentView(R.layout.time_slider_dialog);
                    final Slider slider = dialog.findViewById(R.id.time_slider_slider);
                    slider.setValue(ParkSensorBackgroundService.LOOP_DELAY_IN_MS);
                    ((TextView)dialog.findViewById(R.id.sliderTextId))
                            .setText(String.format(getString(R.string.time_slider_dialog_message), ParkSensorBackgroundService.LOOP_DELAY_IN_MS));

                    final Button ok = dialog.findViewById(R.id.time_slider_ok);
                    ok.setOnClickListener(view -> {
                        final Slider slide = view.findViewById(R.id.time_slider_slider);
                        final float sliderValue = slide.getValue();
                        //do something with value
                        Log.i("newvalue:", String.valueOf(sliderValue));
                        ParkSensorBackgroundService.LOOP_DELAY_IN_MS = (int) sliderValue;
                    });
                    Button cancel = dialog.findViewById(R.id.time_slider_cancel);
                    cancel.setOnClickListener(view -> dialog.dismiss());
                    dialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Loop Configuration").setMessage(
                        "Measure delay in " + ParkSensorBackgroundService.LOOP_DELAY_IN_MS + "ms");
                    builder.setPositiveButton("-100 ms", (dialog, which) -> {
                        Log.i("menu", "-100ms");
                        ParkSensorBackgroundService.LOOP_DELAY_IN_MS -= 100;
                    });
                    builder.setNeutralButton("+100 ms", (dialog, which) -> {
                        Log.i("menu", "+100ms");
                        ParkSensorBackgroundService.LOOP_DELAY_IN_MS += 100;
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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