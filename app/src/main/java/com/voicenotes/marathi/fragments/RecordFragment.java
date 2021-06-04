package com.voicenotes.marathi.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.melnykov.fab.FloatingActionButton;
import com.voicenotes.marathi.DBHelper;
import com.voicenotes.marathi.MySpinner;
import com.voicenotes.marathi.PrefManager;
import com.voicenotes.marathi.R;


import com.voicenotes.marathi.activities.MainActivity;
import com.voicenotes.marathi.activities.StartScreenActivity;
import com.voicenotes.marathi.grpc.SpeechService;
import com.voicenotes.marathi.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.voicenotes.marathi.R.layout.spinner_item;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class RecordFragment extends Fragment {



    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    long reclen;
    private int position;
    //Recording controls
  //  private FloatingActionButton mRecordButton = null;

    //private LinearLayout mCardView;

    private TextView transcription;

  //  private CountDownTimer countDownTimer;
    private CountDownTimer threesectimer;
    private boolean counterStatus = false;
    private boolean onpause = false;
    private boolean onstoprec = false;
    //  private  TextView mTextView;

    // private TextView mRecordingPrompt;
    //private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    // private boolean mPauseRecording = true;

    NetworkInfo activeNetwork;
    ConnectivityManager cm;

   // HashMap<String,Double> marks=new HashMap<String,Double>();


    private TextView mTextField;
    //long timeWhenPaused = 0; //stores time when user clicks pause button
   // private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth auth;
   // private SpeechService mSpeechService;

  //  private DBHelper mDatabase;
    Bundle bundle;
    private String mFileName = null;
    private String mFilePath = null;
   private AdView mAdView;

    FileOutputStream os = null;
   // private FloatingActionButton mPlayButton = null;
    TableLayout mainTable;
    MediaPlayer   mMediaPlayer = new MediaPlayer();

    //TextView improve=null;




    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        //mDatabase = new DBHelper(getActivity().getApplicationContext());
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        View recordView = inflater.inflate(R.layout.fragment_record, container, false);
        mainTable=(TableLayout)recordView.findViewById(R.id.main_table);



        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);




        Log.e("Refreshed token:", "TOKEN " + new PrefManager(getContext()).getRefreshedId());
        new  DownloadInfoOfWeather().execute();

        return recordView;
    }






    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }








    @Override
    public void onStart() {
        super.onStart();




    }



    @Override
    public void onStop() {
        // Stop listening to

        // Stop Cloud Speech API


        super.onStop();


    }
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

   class DownloadInfoOfWeather extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        DownloadInfoOfWeather( ) {

            dialog = new ProgressDialog(getActivity());
        }


        @Override
        protected String doInBackground(String... params) {


            Log.e("murugan18", "calling API");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    dialog.setMessage("Please wait...");
                    dialog.setIndeterminate(true);
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);


                }
            });





            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(600, TimeUnit.SECONDS)
                        .writeTimeout(600, TimeUnit.SECONDS)
                        .readTimeout(600, TimeUnit.SECONDS)
                        .build();



                String namestr= StartScreenActivity.getDefaults("name",getActivity());
                String idstr=StartScreenActivity.getDefaults("id",getActivity());

                if(namestr==null)
                    namestr="";
                if(idstr==null)
                    idstr="";



                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)

                        .addFormDataPart("username",namestr)


                        .build();

                Request request = new Request.Builder().url("http://13.90.34.105:8108/api/getall")

                        .post(requestBody).build();

                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();


                getActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {

                        mainTable.removeAllViews();

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(jsonData.trim());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Iterator<String> keys = jsonObject.keys();
                        int count=0;

                        while(keys.hasNext()) {
                            count=count+1;
                            String key = keys.next();



                            TableRow tr=new TableRow(getActivity());


                            TextView labelName=new TextView(getActivity());
                            labelName.setTypeface(null, Typeface.BOLD);
                            labelName.setTextColor(Color.parseColor("#000000"));

                            labelName.setText("         "+key.toUpperCase());
                            labelName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr.addView(labelName);

                            TextView labelBarcode=new TextView(getActivity());

                            try {
                                String[] values=jsonObject.get(key).toString().split(",");
                                String createstring="";
                                HashMap<String,String> myhaspmap=new HashMap<>();
                                for (int k=0;k<=values.length-1;k++){
                                    String[] keyvalue=values[k].split(":");

                                    myhaspmap.put(keyvalue[0].replace("{","").replace("}","").replace("\"",""),keyvalue[1].replace("{","").replace("}","").replace("\"",""));
                                    //createstring=createstring+values[k].replace(":","  ").replace("{","").replace("}","").replace("\"","")+"\n\n";
                                }
                                createstring=createstring+"name  "+ myhaspmap.get("name")+"\n";
                                createstring=createstring+"phone  "+ myhaspmap.get("phone")+"\n";
                                createstring=createstring+"village  "+ myhaspmap.get("village")+"\n";
                                createstring=createstring+"voter-id  "+ myhaspmap.get("voter-id")+"\n";
                                createstring=createstring+"start-place  "+ myhaspmap.get("start-place")+"\n";
                                createstring=createstring+"end-place  "+ myhaspmap.get("end-place")+"\n";
                                createstring=createstring+"officeapproved  "+ myhaspmap.get("officeapproved")+"\n";
                                createstring=createstring+"member-approved  "+ myhaspmap.get("member-approved")+"\n";

                                labelBarcode.setText(createstring+"");
                                labelBarcode.setTypeface(null, Typeface.BOLD);








                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            labelBarcode.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr.addView(labelBarcode);



                            mainTable.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));


                            TableRow tr1=new TableRow(getActivity());


                            TextView labelName1=new TextView(getActivity());
                            labelName1.setTypeface(null, Typeface.BOLD);
                            labelName1.setTextColor(Color.parseColor("#000000"));

                            labelName1.setText("         ");
                            labelName1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr1.addView(labelName1);

                            TextView labelBarcode1=new TextView(getActivity());


                                labelBarcode1.setText("           ");
                                labelBarcode1.setTypeface(null, Typeface.BOLD);
                                labelBarcode1.setTextColor(Color.parseColor("#000000"));

                            labelBarcode1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr1.addView(labelBarcode1);



                            mainTable.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));


                            TableRow tr2=new TableRow(getActivity());
                            TextView labelBarcode2=new TextView(getActivity());


                            labelBarcode2.setText("           ");
                            labelBarcode2.setTypeface(null, Typeface.BOLD);
                            labelBarcode2.setTextColor(Color.parseColor("#000000"));

                            labelBarcode2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr2.addView(labelBarcode2);


                            Button labelName2=new Button(getActivity());
                            final int index = count;
                            labelName2.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    Toast.makeText(getActivity(),"Please Enter Input"+index,Toast.LENGTH_LONG).show();
                                    new DownloadInfoOfWeatherNew().execute(String.valueOf(index));


                                }
                            });
                            labelName2.setTypeface(null, Typeface.BOLD);
                            labelName2.setTextColor(Color.parseColor("#000000"));

                            labelName2.setText("Approve");
                            labelName2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr2.addView(labelName2);





                            mainTable.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));




                        }




                        dialog.dismiss();

                    }
                });


                dialog.dismiss();








            } catch (Exception e) {



            }


            return null;
        }

    }


    class DownloadInfoOfWeatherNew extends AsyncTask<String, Void, String> {

        ProgressDialog dialog;

        DownloadInfoOfWeatherNew( ) {

            dialog = new ProgressDialog(getActivity());
        }


        @Override
        protected String doInBackground(String... params) {


            Log.e("murugan18", "calling API");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    dialog.setMessage("Please wait...");
                    dialog.setIndeterminate(true);
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(false);


                }
            });





            try {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(600, TimeUnit.SECONDS)
                        .writeTimeout(600, TimeUnit.SECONDS)
                        .readTimeout(600, TimeUnit.SECONDS)
                        .build();






                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)

                        .addFormDataPart("number",params[0].toString())


                        .build();

                Request request = new Request.Builder().url("http://13.90.34.105:8108/api/update")

                        .post(requestBody).build();

                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();
                if(response.isSuccessful()){

                    Intent intent =new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }


                getActivity().runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {



                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(jsonData.trim());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        dialog.dismiss();

                    }
                });


                dialog.dismiss();








            } catch (Exception e) {



            }


            return null;
        }

    }



}