package com.pdc.main.parkdistancecontrol2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Background service for the 4 Park sensors. <br>
 * The RPi3 (master) communicate with SPI to an Arduino pro mini (slave) and
 * the 4 sensors JSN-SR04T v2.0 are connected to Arduino.
 *
 * @author ISzilagyi
 */
public class ParkSensorBackgroundService extends JobIntentService {
    /*
    Unique ID for this JobIntentService.
     */
    private static final int JOB_ID = 1000;

    /*
    Communication mit Arduino, loop delay count.
    It gives time to sensors for new measurement.
     Min Working 700
     */
    public static int LOOP_DELAY_IN_MS = 500;

    /*
    Nr Of measurements which this JobIntentService will do.
     */
    public static final int NR_OF_MEASUREMENTS = 1000;

    /**
     * Constants to communicate with {@link ParkSensorBroadcastReceiver}
     */
    public static final String PARK_SENSOR_INTENT_NAME = "park_sens";
    public static final String PARK_SENSOR_1_KEY = "sensor1";
    public static final String PARK_SENSOR_2_KEY = "sensor2";
    public static final String PARK_SENSOR_3_KEY = "sensor3";
    public static final String PARK_SENSOR_4_KEY = "sensor4";

    //  load the 'pdc2' library on application startup.
    static {
         // see https://developer.android.com/studio/projects/configure-cmake?hl=de
        System.loadLibrary("pdc2");
    }

    /**
     * Linux native filehandler from SPI device.
     */
    private static int spiFileHandler = -1;

    /**
     * Initialize SRPi SPI device as Master.
     *
     * @return Linux SPI device file handler or -1 in case of an error.
     */
    private static native int initSPIDevice();

    /**
     * Send and receive 4 bytes to SPI device.
     * Sending bytes are not relevant for measurements, recieved bytes are
     * the distance in cm in this form 0xS1S2S3S4 where:
     *
     * S1 is the distance measured by sensor1 or 0xFB in case the sensor is offline
     * S2 is the distance measured by sensor1 or 0xFC in case the sensor is offline
     * S3 is the distance measured by sensor1 or 0xFB in case the sensor is offline
     * S4 is the distance measured by sensor1 or 0xFB in case the sensor is offline
     *
     * @param pSpiFileHandler Linux file handler
     * @return the received 4 bytes.
     */
    private static native long parkSensorSpi4Bytes(int pSpiFileHandler);

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ParkSensorBackgroundService.class, JOB_ID, work);
        if (spiFileHandler == -1) {
            spiFileHandler = initSPIDevice();
        }
        if (spiFileHandler != -1) {
            Log.i("BackgroundService", "Initialized. " + spiFileHandler);
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i("onHandleWork", "------> SPI Handler: " + ParkSensorBackgroundService.spiFileHandler);
        Log.i("onHandleWork", "------> Executing work: " + intent);

        for (int i = 0; i < NR_OF_MEASUREMENTS; i++) {
            try {
                Thread.sleep(LOOP_DELAY_IN_MS);
                Intent measureIntent = new Intent(PARK_SENSOR_INTENT_NAME);

                if (spiFileHandler != -1) {
                    long valueFromSensor = parkSensorSpi4Bytes(spiFileHandler);
                    Log.d("onHandleWork", String.format("------> Sensor: 0x%x", valueFromSensor));
                    int sensor1 = (int) ((valueFromSensor & 0xFF000000) >>> 24);
                    int sensor2 = (int) ((valueFromSensor & 0x00FF0000) >>> 16);
                    int sensor3 = (int) ((valueFromSensor & 0x0000FF00) >>> 8);
                    int sensor4 = (int) (valueFromSensor  & 0x000000FF);
                    Log.i("onHandleWork", String.format("Sensors: 0x%x 0x%x 0x%x 0x%x ", sensor1, sensor2, sensor3, sensor4));
                    if (sensor1 != 0xFB) {
                        measureIntent.putExtra(PARK_SENSOR_1_KEY, sensor1);
                    }
                    if (sensor2 != 0xFC) {
                        measureIntent.putExtra(PARK_SENSOR_2_KEY, sensor2);
                    }
                    if (sensor3 != 0xFD) {
                        measureIntent.putExtra(PARK_SENSOR_3_KEY, sensor3);
                    }
                    if (sensor4 != 0xFE) {
                        measureIntent.putExtra(PARK_SENSOR_4_KEY, sensor4);
                    }
                }else {
                    emulatorMode(i, measureIntent);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(measureIntent);
            } catch (InterruptedException e) {
                Log.e("onHandleWork", "Interrupted", e);
            }
        }
    }

    /**
     * Return decreasing constants in emulator mode.
     * @param i loop count
     * @param measureIntent Intent to place the test value.
     */
    private void emulatorMode(int i, Intent measureIntent) {
        measureIntent.putExtra(PARK_SENSOR_1_KEY, 400 - i * 10);
        measureIntent.putExtra(PARK_SENSOR_2_KEY, 398 - i * 10);
        measureIntent.putExtra(PARK_SENSOR_3_KEY, 397 - i * 10);
        // measureIntent.putExtra(PARK_SENSOR_4_KEY, 396 - i * 10);
    }
}


/*
Set Android SELinux in permisive mode:
/system/bin/setenforce 0
OR
echo 0 >/sys/fs/selinux/enforce

SELinux denies are logged but not enforced!
I/istancecontrol2: type=1400 audit(0.0:596): avc: denied { read write } for name="spidev0.0" dev="tmpfs" ino=2328 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1
I/istancecontrol2: type=1400 audit(0.0:597): avc: denied { open } for path="/dev/spidev0.0" dev="tmpfs" ino=2328 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1
    type=1400 audit(0.0:598): avc: denied { ioctl } for path="/dev/spidev0.0" dev="tmpfs" ino=2328 ioctlcmd=6b05 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1

see also:
https://source.android.com/security/selinux/device-policy
https://android.stackexchange.com/questions/207484/how-to-fix-selinux-avc-denied-errors-when-launching-dnscrypt-as-init-d-script
 content of: /sys/fs/selinux
*/