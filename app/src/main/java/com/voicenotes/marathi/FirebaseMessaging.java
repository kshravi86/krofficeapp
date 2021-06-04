package com.voicenotes.marathi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.voicenotes.marathi.activities.NotificationActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "MyAndroidFCMService";
    private PrefManager prefManager;
//    private Bitmap bitmap = null;
    //private NotificationReaderDbHelper notificationReaderDbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        prefManager = new PrefManager(FirebaseMessaging.this);
        //notificationReaderDbHelper = new NotificationReaderDbHelper(NotificationService.this);

        com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //prefManager.setBadgetsStatus(true);

//        Log.e("REMOTE MESSAGE", " " + remoteMessage.getData());
//        Log.e("REMOTE MESSAGE", " " + remoteMessage.getMessageType());
//        Log.e("REMOTE MESSAGE", " " + remoteMessage.getFrom());
//        Log.e("REMOTE MESSAGE", " " + remoteMessage.getNotification());
        //Log.e("REMOTE MESSAGE", " " + remoteMessage.getData());
        if(remoteMessage.getData().size() > 0){

            try {
                //sqLiteDatabse.open();
                Log.e(TAG, "DATA: " + remoteMessage.getData());
//                JSONObject messageObject = new JSONObject(remoteMessage.getData());
//                String date = messageObject.optString("date");
//                String url = messageObject.optString("url");
//                String content = messageObject.optString("content");
//                String notificationId = messageObject.optString("notificationId");

//                String notificationTitle = messageObject.optString("title");
//                String notificationSubject = messageObject.optString("subject");


                //  notificationReaderDbHelper.insertNotification(notificationTitle, notificationSubject);
                //  Log.e("LONG ID", " "  + notificationReaderDbHelper.insertNotification(notificationTitle, notificationSubject));
                //Bitmap bitmap = getBitmapfromUrl(remoteMessage.getData().get("url"));
                createNotificationN(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData().get("url"));
                //sqLiteDatabse.close();
            } catch (Exception e) {
                Log.e("EXCEPTION DATA", " BASE" + e);
                //e.printStackTrace();
            }

        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Bitmap bitmap = getBitmapfromUrl(String.valueOf(remoteMessage.getNotification().getImageUrl()));
            //  notificationReaderDbHelper.insertNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            createNotificationN(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), String.valueOf(remoteMessage.getNotification().getImageUrl()));
        }



    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.e("Refreshed token:", "TOKEN " + s);
        //notificationReaderDbHelper.insertNotification("Hi", "How are you?");
        prefManager.setRefreshedId(s);
    }

    private void createNotificationN(String title, String subject, String imageUri) {

        try {
            Log.e("IMAGE", "URI " + imageUri);

//            if (imageUri != null && !imageUri.isEmpty()) {
//                bitmap = getBitmapfromUrl(imageUri);
//            }

            Intent intent = new Intent(this, NotificationActivity.class);
            intent.putExtra("titleNBody", title + "@" + subject + "@" + imageUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.edit)
                            .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapfromUrl(imageUri)))
                            .setContentTitle(title)
                            .setContentText(subject)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        getString(R.string.notificationticker),
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

        } catch (Exception e) {
            Log.e("EXCPETION", "NOTIFICATION " + e);
        }
    }

//    public void createNotification(String aMessage, Context context, String messageBody, String imageUrl, Bitmap bitmap, String title_message) {
//        final int NOTIFY_ID = 0; // ID of notification
//        String id = context.getString(R.string.default_notification_channel_id); // default_channel_id
//        String title = title_message; // Default Channel
//        Intent intent;
//        PendingIntent pendingIntent;
//        NotificationCompat.Builder builder;
//        if (notifManager == null) {
//            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
//            if (mChannel == null) {
//                mChannel = new NotificationChannel(id, title, importance);
//                mChannel.enableVibration(true);
//                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//                notifManager.createNotificationChannel(mChannel);
//            }
//            builder = new NotificationCompat.Builder(context, id);
//
//            intent = new Intent(context, NotificationActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            builder.setContentTitle(aMessage)                            // required
//                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
//                    .setDefaults(Notification.DEFAULT_ALL)
//                    .setContentIntent(pendingIntent)
//                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
//                    .setContentTitle("U Speak We Pay")
//                    .setContentText(title)
//                    .setAutoCancel(true)
//                    .setStyle(new NotificationCompat.BigPictureStyle()
//                            .bigPicture(bitmap).setSummaryText(title));
//        } else {
//            builder = new NotificationCompat.Builder(context, id);
//
//            intent = new Intent(context, NotificationActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//            builder.setContentTitle(aMessage)                            // required
//                    .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
//
//                    .setDefaults(Notification.DEFAULT_ALL)
//
//                    .setContentIntent(pendingIntent)
//                    .setTicker(aMessage)
//                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
//                    .setPriority(Notification.PRIORITY_HIGH)
//                    .setContentTitle("U Speak We Pay")
//                    .setContentText(title)
//                    .setAutoCancel(true)
//                    .setStyle(new NotificationCompat.BigPictureStyle()
//                            .bigPicture(bitmap).setSummaryText(title));
//
//        }
//        Notification notification = builder.build();
//        notifManager.notify(NOTIFY_ID, notification);
//    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

}
