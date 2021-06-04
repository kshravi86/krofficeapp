package com.voicenotes.marathi.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.voicenotes.marathi.DBHelper;
import com.voicenotes.marathi.R;
import com.voicenotes.marathi.adapters.FileViewerAdapter;

import java.io.File;

/**
 * Created by gnani.ai on 12/23/2014.
 */
public class FileViewerFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";
    private com.melnykov.fab.FloatingActionButton del;
    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    private DBHelper db;
    private AdView mAdView;

    /**
     * @param position integer to show the fragment depends on the value of integer
     * @return
     */
    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        del = v.findViewById(R.id.delall);
        db = new DBHelper(getContext());
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.delete_all_files_message))
                        .setPositiveButton(getString(R.string.dialog_action_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.deleteAll();
                                File folder = new File(Environment.getExternalStorageDirectory() + getString(R.string.path_mara));
                                deletefiles(folder);
                            }
                        })
                        .setNegativeButton(getString(R.string.no_negative_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create();
                builder.show();
            }
        });
        RecyclerView mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);


        int numberOfColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplication(), numberOfColumns));


        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFileViewerAdapter = new FileViewerAdapter(getActivity());
        mRecyclerView.setAdapter(mFileViewerAdapter);

        /*
        code to display ads on screen
         */

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                Log.e("STATUS", " " + initializationStatus);
            }
        });

        mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("E20966CF9E9D032FCD3697C6644A61C3")
                .build();
        mAdView.loadAd(adRequest);

        return v;
    }

    /**
     * @param file file object to perform delete operation on the file
     */
    public static void deletefiles(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                deletefiles(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
        Log.d("Deleted file/folder: ", file.getAbsolutePath());
    }

    /*

    File observer to observe the events like DELETE, CREATE, MODIFY
     */

    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/SoundRecorder") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE) {
                        // user deletes a recording file out of the App

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}




