package com.voicenotes.marathi.listeners;

/**
 * Created by gnani.ai on 1/3/2015.
 * Listen for add/rename items in database
 */
public interface OnDatabaseChangedListener{
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRenamed();
    void onDatabaseEntryUpdated();
}