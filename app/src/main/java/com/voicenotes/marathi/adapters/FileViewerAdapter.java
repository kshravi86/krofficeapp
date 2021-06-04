package com.voicenotes.marathi.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.voicenotes.marathi.DBHelper;
import com.voicenotes.marathi.R;
import com.voicenotes.marathi.RecordingItem;
import com.voicenotes.marathi.fragments.PlaybackFragment;
import com.voicenotes.marathi.listeners.OnDatabaseChangedListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by gnani.ai on 12/29/2014.
 */
public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "FileViewerAdapter";
    private String tr;
    private DBHelper mDatabase;

    RecordingItem item;
    Context mContext;
    // LinearLayoutManager llm;

    /*
    @param context current context of an application
     */

    public FileViewerAdapter(Context context) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        //llm = linearLayoutManager;

    }

    @Override
    public void onBindViewHolder(@NotNull final RecordingsViewHolder holder, final int position) {
        item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        if (item.getTrans().length() > 10) {
            tr = item.getTrans().substring(0, Math.min(item.getTrans().length(), 10));
            holder.vTranscription.setText(tr + "....");
        } else {
            holder.vTranscription.setText(item.getTrans());
        }
        String a[] = item.getName().split(" ");
        holder.actual_trans.setText(item.getTrans());
        if (a.length == 3)
            holder.stamp.setText(a[2]);//setting time stamp
        com.melnykov.fab.FloatingActionButton button = (com.melnykov.fab.FloatingActionButton) holder.cardView.findViewById(R.id.fab_play);
        ImageButton delbut = holder.cardView.findViewById(R.id.del);

        delbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFileDialog(holder.getPosition());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    PlaybackFragment playbackFragment =
                            new PlaybackFragment().newInstance(getItem(holder.getPosition()), position);

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playbackFragment.show(transaction, "dialog_playback");

                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }


            }
        });

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlaybackFragment playbackFragment =
                            new PlaybackFragment().newInstance(getItem(holder.getPosition()), position);

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playbackFragment.show(transaction, "dialog_playback");

                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add(mContext.getString(R.string.dialog_file_share));
//                entrys.add(mContext.getString(R.string.dialog_file_rename));
//                entrys.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                // File delete confirm
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getPosition(), holder.actual_trans.getText().toString());
                        }
//                        if (item == 1)
//                        {
//                            renameFileDialog(holder.getPosition());
//                        }
//                        else if (item == 2) {
//                            deleteFileDialog(holder.getPosition());
//                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }

    @NotNull
    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected TextView vTranscription;
        protected TextView stamp;
        protected View cardView;
        protected TextView actual_trans;

        public RecordingsViewHolder(View v) {
            super(v);
            // vTranscription=(TextView) v.findViewById(R.id.transcription);
            cardView = v.findViewById(R.id.card_view);
            vTranscription = cardView.findViewById(R.id.transcription);
            stamp = cardView.findViewById(R.id.stamp);
            actual_trans = cardView.findViewById(R.id.actual_trans);
        }
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        // llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void onDatabaseEntryUpdated() {
        notifyDataSetChanged();
    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage

        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();


        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

//    public void rename(int position, String name) {
//        //rename a file
//
//        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFilePath += "/SoundRecorder/" + name;
//        File f = new File(mFilePath);
//
//        if (f.exists() && !f.isDirectory()) {
//            //file name is not unique, cannot rename file.
//
//
//        } else {
//            //file name is unique, rename file
//            File oldFilePath = new File(getItem(position).getFilePath());
//            oldFilePath.renameTo(f);
//            mDatabase.renameItem(getItem(position), name, mFilePath);
//            notifyItemChanged(position);
//        }
//    }

    public void shareFileDialog(int position, String transcription) {


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        Toast.makeText(mContext.getApplicationContext(),transcription.toString(), Toast.LENGTH_SHORT).show();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);

        shareIntent.putExtra(Intent.EXTRA_TEXT, transcription + mContext.getString(R.string.powered_by));
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp4");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

//    public void renameFileDialog (final int position) {
//        // File rename dialog
//        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);
//
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        View view = inflater.inflate(R.layout.dialog_rename_file, null);
//
//        final EditText input = (EditText) view.findViewById(R.id.new_name);
//
//        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
//        renameFileBuilder.setCancelable(true);
//        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        try {
//                            String value = input.getText().toString().trim() + ".mp4";
//                            rename(position, value);
//
//                        } catch (Exception e) {
//                            Log.e(LOG_TAG, "exception", e);
//                        }
//
//                        dialog.cancel();
//                    }
//                });
//        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//        renameFileBuilder.setView(view);
//        AlertDialog alert = renameFileBuilder.create();
//        alert.show();
//    }

    public void deleteFileDialog(final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }
}
