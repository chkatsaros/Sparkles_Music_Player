package com.audioplayer.sparklesv10.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SongDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "songs.db";
    private static final int VERSION = 4;
    private Context context;

    public static SongDatabase instance = null;

    public static SongDatabase getInstance(Context context) {

        if (instance == null) {
            instance = new SongDatabase(context.getApplicationContext());
        }

        return instance;
    }

    public SongDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        PlayingInfo.getInstance(context).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PlayingInfo.getInstance(context).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        PlayingInfo.getInstance(context).onDowngrade(db, oldVersion, newVersion);
    }
}
