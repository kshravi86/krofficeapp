package com.voicenotes.marathi;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

/*
First class that will be invoked after launching an app
 */

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();


    public void onCreate() {
        /*
        Function that is executed when app is launched and firebase has to be called for force update
         */
    super.onCreate();




    }
}
