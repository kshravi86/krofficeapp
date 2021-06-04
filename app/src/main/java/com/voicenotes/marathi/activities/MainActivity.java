package com.voicenotes.marathi.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.voicenotes.marathi.ForceUpdateChecker;
import com.voicenotes.marathi.R;
import com.voicenotes.marathi.Welcome;
import com.voicenotes.marathi.fragments.FileViewerFragment;
import com.voicenotes.marathi.fragments.RecordFragment;


import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener {


    /*

    Main class where recording happens and all the fragments for each of the pitch type has its own fragments

    This activity will host all the fragments for all the pitches




     */

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION_CODE = 1;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Feedback");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    //private com.melnykov.fab.FloatingActionButton shareButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        if (!checkPermission()) {
            requestPermission();
        }
        // delete all the files in below folder to delete all the older files

        deleteFiles("rm -rf /storage/emulated/0/SoundRecorder/Marathi/*.wav");

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
//        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
//        tabs.setShouldExpand(true);
//        tabs.setViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }


    }

    public void next_fragment(View view) {
        pager.setCurrentItem(pager.getCurrentItem()+1);
    }

    public void previous_fragment(View view) {
        pager.setCurrentItem(pager.getCurrentItem()-1);
    }

    public void gotonext(View view){

        Intent intent=new Intent(MainActivity.this, Welcome.class);
        startActivity(intent);



    }


    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        android.app.AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_version_title))
                .setMessage(R.string.update_app_message)
                .setPositiveButton(getString(R.string.update_positive_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton(getString(R.string.no_negative_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
        dialog.show();
    }

    public void deleteFiles(String command) {


            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(command);
            } catch (IOException e) { }


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
     * @param requestCode  The request code passed in requestPermissions(java.lang.String[], int)
     * @param permissions  The requested permissions. Never null. This value must never be null
     * @param grantResults The grant results for the corresponding permissions which is either PackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED. Never null. This value must never be null
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (storagePermission && recordPermission) {
                        Toast toast = Toast.makeText(MainActivity.this, getString(R.string.per_granted), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(MainActivity.this, getString(R.string.per_denied), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
                break;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQUEST_PERMISSION_CODE);

    }

    /**
     * Adapter to hold two fragments
     */
    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = {"Users Info"};

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return RecordFragment.newInstance(position);
                }

            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


}
