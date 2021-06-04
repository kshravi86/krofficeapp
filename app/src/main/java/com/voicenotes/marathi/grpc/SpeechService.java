/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.voicenotes.marathi.grpc;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gnani.speech.ListenerGrpc;
import com.gnani.speech.SpeechChunk;
import com.gnani.speech.TranscriptChunk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.protobuf.ByteString;
import com.voicenotes.marathi.BuildConfig;
import com.voicenotes.marathi.DBHelper;
import com.voicenotes.marathi.R;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;


public class SpeechService extends Service {

    public interface Listener {

        /**
         * Called when a new piece of text was recognized by the Speech API.
         *
         * @param transcript  actual text after speech to text conversion
         * @param asr asr
         * @param isFinal {@code true} when the API finished processing audio.
         */
        void onSpeechRecognized(String transcript, String asr, boolean isFinal);

    }

    private static final String TAG = "SpeechService";


    private final SpeechBinder mBinder = new SpeechBinder();
    private final ArrayList<Listener> mListeners = new ArrayList<>();

    private ListenerGrpc.ListenerStub mApiG;

    private volatile AccessTokenTask mAccessTokenTask;


    private ManagedChannel channelG;


    private DBHelper mDatabase;


    private String mFileName = null;
    private String mFilePath = null;

    FileOutputStream os = null;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_BPP = 16;
    private FirebaseAuth firebaseAuth;

    //final ManagedChannel channelG;

//    public void setFileNameAndPath() {
//        int count = 0;
//        File f;
//
//        do {
//            count++;
//
//            mFileName = getString(R.string.default_file_name)
//                    + "_" + (mDatabase.getCount() + count) + ".wav";
//            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            mFilePath += "/SoundRecorder/" + mFileName;
//
//            f = new File(mFilePath);
//        } while (f.exists() && !f.isDirectory());
//    }


    private final StreamObserver<TranscriptChunk> mResponseObserverG
            = new StreamObserver<TranscriptChunk>() {
        @Override
        public void onNext(TranscriptChunk response) {

            String transcript = response.getTranscript();
            String asr = response.getAsr();
            boolean isFinal=response.getIsFinal();
//            Log.e("SpeechService", "transcript " + transcript);
//            Log.e("SpeechService", "asr " + asr);
//            Log.e("SpeechService", "isFInal " + response.getIsFinal());


            if (transcript != null) {
                for (Listener listener : mListeners) {
                    listener.onSpeechRecognized(transcript, asr, isFinal);

                }
            }

        }

        @Override
        public void onError(Throwable t) {

            Log.e(TAG, "Error calling the API.", t);

        }

        @Override
        public void onCompleted() {
            Log.i(TAG, "API completed.");
        }

    };


    private StreamObserver<SpeechChunk> mRequestObserverG;

    public static SpeechService from(IBinder binder) {
        return ((SpeechBinder) binder).getService();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //mDatabase = new DBHelper(getApplicationContext());
        firebaseAuth = FirebaseAuth.getInstance();

        fetchAccessToken();


    }

    private void fetchAccessToken() {
        if (mAccessTokenTask != null) {
            return;
        }
        mAccessTokenTask = new AccessTokenTask();
        mAccessTokenTask.execute();
    }


    /**
     * Async Task to for sending headers to STT API
     */
    private class AccessTokenTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onPostExecute(Void accessToken) {
            Metadata header = new Metadata();
            Metadata.Key<String> token = Metadata.Key.of(getString(R.string.token), Metadata.ASCII_STRING_MARSHALLER);
            header.put(token, BuildConfig.TOKEN);
            Metadata.Key<String> lang = Metadata.Key.of(getString(R.string.lang), Metadata.ASCII_STRING_MARSHALLER);
            header.put(lang, getString(R.string.lang_value));
            Metadata.Key<String> akey = Metadata.Key.of(getString(R.string.access_key), Metadata.ASCII_STRING_MARSHALLER);
            header.put(akey, BuildConfig.AKEY);
            Metadata.Key<String> audioformat = Metadata.Key.of(getString(R.string.audio_format), Metadata.ASCII_STRING_MARSHALLER);
            header.put(audioformat, getString(R.string.audio_for_value));
            Metadata.Key<String> encoding = Metadata.Key.of(getString(R.string.encoding), Metadata.ASCII_STRING_MARSHALLER);
            header.put(encoding, getString(R.string.encoding_value));
            Metadata.Key<String> sad = Metadata.Key.of(getString(R.string.silence), Metadata.ASCII_STRING_MARSHALLER);
            header.put(sad, getString(R.string.yes));
            Metadata.Key<String> email = Metadata.Key.of(getString(R.string.email), Metadata.ASCII_STRING_MARSHALLER);
            header.put(email, firebaseAuth.getCurrentUser().getEmail());
            channelG = ManagedChannelBuilder.forAddress(getString(R.string.stt_api), 443).build();

            mApiG = ListenerGrpc.newStub(channelG);
            mApiG = MetadataUtils.attachHeaders(mApiG, header);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release the gRPC channel.
        if (mApiG != null) {
            //final ManagedChannel channel = (ManagedChannel) mApiG.getChannel();
            if (channelG != null && !channelG.isShutdown()) {
                try {
                    channelG.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error shutting down the gRPC channel.", e);
                }
            }
            mApiG = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addListener(@NonNull Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListeners.remove(listener);
    }

    /**
     * Starts recognizing speech audio.
     * It will call a method of proto file
     */
    public void startRecognizing() {


        mRequestObserverG = mApiG.doSpeechToText(mResponseObserverG);


      /*  setFileNameAndPath();


        try {

            String filename = FileUtils.getTempFilename();
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */


    }

    /**
     * Recognizes the speech audio. This method should be called every time a chunk of byte buffer
     * is ready.
     *
     * @param data The audio data.
     * @param size The number of elements that are actually relevant in the {@code data}.
     */
    public void recognize(byte[] data, int size) {
        if (mRequestObserverG == null) {
            return;
        }

     /*   try {
            os.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d("SpeechService", "inside recognise function");

        */
        try {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


            mRequestObserverG.onNext(SpeechChunk.newBuilder()
                    .setToken(firebaseAuth.getCurrentUser().getEmail())
                    .setContent(ByteString.copyFrom(data, 0, size))
                    .build());

        } catch (Exception e) {
            Log.d("SpeechService", "" + e);
        }
    }


//    private void copyWaveFile(String inFilename, String outFilename) {
//        FileInputStream in;
//        FileOutputStream out;
//        long totalAudioLen;
//        long totalDataLen;
//        long longSampleRate = RECORDER_SAMPLERATE;
//        int channels = 1;
//        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
//        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
//                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
//
//        byte[] data = new byte[bufferSize];
//
//        try {
//            in = new FileInputStream(inFilename);
//            out = new FileOutputStream(outFilename);
//            totalAudioLen = in.getChannel().size();
//            totalDataLen = totalAudioLen + 36;
//
//
//            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
//                    longSampleRate, channels, byteRate);
//
//            while (in.read(data) != -1) {
//                out.write(data);
//            }
//
//            in.close();
//            out.close();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeWaveFileHeader(
//            FileOutputStream out, long totalAudioLen,
//            long totalDataLen, long longSampleRate, int channels,
//            long byteRate) throws IOException {
//        byte[] header = new byte[44];
//
//        header[0] = 'R';  // RIFF/WAVE header
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//        header[4] = (byte) (totalDataLen & 0xff);
//        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//        header[12] = 'f';  // 'fmt ' chunk
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' ';
//        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        header[20] = 1;  // format = 1
//        header[21] = 0;
//        header[22] = (byte) channels;
//        header[23] = 0;
//        header[24] = (byte) (longSampleRate & 0xff);
//        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
//        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
//        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
//        header[28] = (byte) (byteRate & 0xff);
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//        header[32] = (byte) (2 * 16 / 8);  // block align
//        header[33] = 0;
//        header[34] = RECORDER_BPP;  // bits per sample
//        header[35] = 0;
//        header[36] = 'd';
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a';
//        header[40] = (byte) (totalAudioLen & 0xff);
//        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
//
//        out.write(header, 0, 44);
//    }

    /**
     * Finishes recognizing speech audio.
     */
    public void finishRecognizing() {
        if (mRequestObserverG == null) {
            return;
        }

        Log.d("SpeechService", "finished recognizing function");

/*
        copyWaveFile(FileUtils.getTempFilename(),mFilePath);

        FileUtils.deleteTempFile();
        */

        mRequestObserverG.onCompleted();
        mRequestObserverG = null;
    }


    private class SpeechBinder extends Binder {

        SpeechService getService() {
            return SpeechService.this;
        }

    }


}


