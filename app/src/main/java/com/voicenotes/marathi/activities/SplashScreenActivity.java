package com.voicenotes.marathi.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.voicenotes.marathi.R;
import com.voicenotes.marathi.Welcome;

public class SplashScreenActivity extends Activity {
    /*
    Class that displays splash screen on the launch of the app

    This is the first activity that is called from android manisfest.xml


     */
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your App logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your App main activity
                Intent i = new Intent(SplashScreenActivity.this, Welcome.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}

