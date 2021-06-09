package com.voicenotes.marathi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.voicenotes.marathi.R;
import com.voicenotes.marathi.Welcome;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by gnani.ai on 25/11/2017.
 */

public class StartScreenActivity extends AppCompatActivity {

    /*

    This class explains the login and all permissions needed for the app
    It has username and password edit boxes and login button
    This class sends username and password to backend




     */
    private Button btnLogin;

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private boolean doubleBackToExitPressedOnce;
    private FirebaseAuth mAuth;
    private FirebaseAuth auth;

    public static final int RequestPermissionCode = 1;

    private static final String AUDIO_RECORDER_FOLDER = "WavAudioRecorderHindi";

    String userinfo;


    private static final String TAG = "StartScreenActivity";

    private FirebaseAnalytics firebaseAnalytics;


    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    EditText name,id;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_message), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    /**
     * @return true if user grants the permissions
     * false if user denies the permissions
     */
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * request the following permissions at run time
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(StartScreenActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }


    /**
     * @param requestCode  The request code passed in requestPermissions(java.lang.String[], int)
     * @param permissions  The requested permissions. Never null. This value must never be null
     * @param grantResults The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED. Never null. This value must never be null
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {


                        Toast toast = Toast.makeText(StartScreenActivity.this, "Permission Granted", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(StartScreenActivity.this, "Permission Denied", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (checkPermission()) {

            Log.d("StartScreen", "Permissons are there");
        } else {


            requestPermission();
        }


        super.onCreate(savedInstanceState);




        setContentView(R.layout.start_screen);

        String namestr=StartScreenActivity.getDefaults("name",getApplicationContext());

        if(namestr!=null){

            Intent intent=new Intent(StartScreenActivity.this,MainActivity.class);
            startActivity(intent);

        }



        btnLogin = (Button) findViewById(R.id.btn_login);
        name=(EditText)findViewById(R.id.name);
        id=(EditText)findViewById(R.id.id_hdfc);



        /*
        Start the google sign in Intent

         */

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strname=name.getText().toString();
                String strid=id.getText().toString();

                StartScreenActivity.setDefaults("name",strname,getApplicationContext());

                StartScreenActivity.setDefaults("id",strid,getApplicationContext());
                if(strname!=null && strid!=null){
                    if(strname.contains("office") && strid.contains("office")){

                Intent intent=new Intent(StartScreenActivity.this,MainActivity.class);
                startActivity(intent);


                    }
                    else{

                        Toast.makeText(StartScreenActivity.this,"Please Check Username and password",Toast.LENGTH_LONG).show();

                    }
                }
                else{

                    Toast.makeText(StartScreenActivity.this,"Please Check Username and password",Toast.LENGTH_LONG).show();

                }








            }
        });
    }


    /**
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

                //Google Sign In Event log for Firebase


            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);

                // Toast.makeText(StartScreenActivity.this, "Google sign in failed"+e,
                //       Toast.LENGTH_LONG).show();
                // [START_EXCLUDE]
                // Snackbar.make(findViewById(R.id.main_layout), "Google sign in failed", Snackbar.LENGTH_SHORT).show();

                // [END_EXCLUDE]
            }
        }
    }


    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    /**
     * @param acct GoogleSignInAccount class's object which can be used to getId(), getIdToken(), etc
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            Toast.makeText(StartScreenActivity.this, "Authentication Success",
//                                    Toast.LENGTH_LONG).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            hideProgressDialog();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("Email", auth.getCurrentUser().getEmail());
                            startActivity(i);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(StartScreenActivity.this, "Authentication Failure",
                                    Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }


}
