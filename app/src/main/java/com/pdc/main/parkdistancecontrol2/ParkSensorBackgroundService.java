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

    // Used to load the 'pdc2' library on application startup.
    static {
        // TODO activate after compiled library is copied to rpi3
        // see https://developer.android.com/studio/projects/configure-cmake?hl=de
        System.loadLibrary("pdc2");
    }

    private static int spiFileHandler = -1;

    private native static int initSPIDevice();
    private native static byte sendFirstByte(int pSpiFileHandler);
    private native static long parkSensorSpi4Bytes(int pSpiFileHandler);

    private native String stringFromJNI();
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
            byte spiResponse = sendFirstByte(spiFileHandler);
            Log.i("BackgroundService", String.format("Init byte response: 0x%x",spiResponse));
        }
    }

    /*
    In permisive mode:
/system/bin/setenforce 0
# OR
echo 0 >/sys/fs/selinux/enforce
denies are logged but not enforced!

I/istancecontrol2: type=1400 audit(0.0:596): avc: denied { read write } for name="spidev0.0" dev="tmpfs" ino=2328 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1
I/istancecontrol2: type=1400 audit(0.0:597): avc: denied { open } for path="/dev/spidev0.0" dev="tmpfs" ino=2328 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1
    type=1400 audit(0.0:598): avc: denied { ioctl } for path="/dev/spidev0.0" dev="tmpfs" ino=2328 ioctlcmd=6b05 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:device:s0 tclass=chr_file permissive=1

see also:
https://android.stackexchange.com/questions/207484/how-to-fix-selinux-avc-denied-errors-when-launching-dnscrypt-as-init-d-script
https://source.android.com/security/selinux/device-policy
 content of: /sys/fs/selinux

 */

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i("BackgroundService", "------> " + stringFromJNI());
        Log.i("BackgroundService", "------> SPI Handler: " + ParkSensorBackgroundService.spiFileHandler);
        Log.i("BackgroundService", "------> Executing work: " + intent);

        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(1000);
                Intent measureIntent = new Intent(PARK_SENSOR_INTENT_NAME);

                if (spiFileHandler != -1) {
                    long valueFromSensor = parkSensorSpi4Bytes(spiFileHandler);
                    Log.i("BackgroundService", String.format("------> Sensor: 0x%x", valueFromSensor));
                    int sensor1 = (int) (valueFromSensor  & 0x000000FF);
                    int sensor2 = (int) ((valueFromSensor & 0x0000FF00) >>> 8);
                    int sensor3 = (int) ((valueFromSensor & 0x00FF0000) >>> 16);
                    int sensor4 = (int) ((valueFromSensor & 0xFF000000) >>> 24);
                    Log.i("BackgroundService", String.format("------> 0x%x 0x%x 0x%x 0x%x ", sensor1, sensor2, sensor3, sensor4));

                    measureIntent.putExtra(PARK_SENSOR_1_KEY, sensor1);
                    measureIntent.putExtra(PARK_SENSOR_2_KEY, sensor2);
                    measureIntent.putExtra(PARK_SENSOR_3_KEY, sensor3);
                    measureIntent.putExtra(PARK_SENSOR_4_KEY, sensor4);
                }else {
                    measureIntent.putExtra(PARK_SENSOR_1_KEY, 400 - i * 10);
                    measureIntent.putExtra(PARK_SENSOR_2_KEY, 398 - i * 10);
                    measureIntent.putExtra(PARK_SENSOR_3_KEY, 397 - i * 10);
                    measureIntent.putExtra(PARK_SENSOR_4_KEY, 396 - i * 10);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(measureIntent);
            } catch (InterruptedException e) {
                Log.e("BackgroundService", "Interrupted", e);
            }
        }
    }
}