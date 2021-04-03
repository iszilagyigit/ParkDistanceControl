package com.pdc.main.parkdistancecontrol2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.
    static {
      // TODO activate after compiled library is copied to rpi3
        // see https://developer.android.com/studio/projects/configure-cmake?hl=de
      //  System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         setContentView(R.layout.activity_main);
        // System.out.println("String from JNI: " + stringFromJNI());
    }

    @Override
    protected void onStart() {
       // --> findViewById(R.id.button1_3).setVisibility(View.INVISIBLE);
        super.onStart();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}