package com.voicenotes.marathi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.melnykov.fab.FloatingActionButton;
import com.voicenotes.marathi.R;

/**
 * This activity will be triggered when user clicks on the notification. This activity will show the notification details, if any
 */
public class NotificationActivity extends AppCompatActivity {

    private TextView txtNotificationTitle, txtNotificationBody;
    private String titleNBody;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);


        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            });
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        txtNotificationTitle = findViewById(R.id.txtNotificationTitle);
        txtNotificationBody = findViewById(R.id.txtNotificationBody);


        if (getIntent().getExtras() != null) {
            titleNBody = getIntent().getExtras().getString("titleNBody");

            Log.e("TITLEBODY", "FF " + titleNBody);
            if (titleNBody != null) {
                String[] split = titleNBody.split("@");
//                Log.e("SPLIT", "2 " + split[2]);
                txtNotificationTitle.setText(split[0]);
                txtNotificationBody.setText(split[1]);

            }


        }
    }

}
