package com.audioplayer.sparklesv10.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.audioplayer.sparklesv10.types.PlayBackTrack;
import com.audioplayer.sparklesv10.utility.SparklesUtil;

import java.util.ArrayList;

public class PlayingInfo {

    public static PlayingInfo instance = null;
    private SongDatabase songDatabase = null;

    public PlayingInfo(Context context) {

        songDatabase = SongDatabase.getInstance(context);
    }

    public static final synchronized PlayingInfo getInstance(Context context) {

        if (instance == null) {
            instance = new PlayingInfo(context.getApplicationContext());
        }
        
        return instance;
    }

    public void onCreate(SQLiteDatabase database) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(SongInfo.NAME);
        builder.append("(");
        builder.append(SongInfo.TRACK_ID);
        builder.append(" LONG NOT NULL,");
        builder.append(SongInfo.SOURCE_ID);
        builder.append(" LONG NOT NULL,");
        builder.append(SongInfo.SOURCE_TYPE);
        builder.append(" INT NOT NULL,");
        builder.append(SongInfo.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");
        database.execSQL(builder.toString());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2 && newVersion >= 2 ) onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SongInfo.NAME);
        onCreate(db);
    }

    public synchronized void saveSongInDatabase(ArrayList<PlayBackTrack> playBackTrackList) {

        SQLiteDatabase sqLiteDatabase = songDatabase.getWritableDatabase();
        sqLiteDatabase.beginTransaction();

        try {
            sqLiteDatabase.delete(SongInfo.NAME, null, null);
            sqLiteDatabase.setTransactionSuccessful();
        }
        finally {
            sqLiteDatabase.endTransaction();
        }

        int PROCESS_NUM = 20;
        int position = 0;

        while (position < playBackTrackList.size()) {

            sqLiteDatabase.beginTransaction();

            try {
                for (int i = 0; i < playBackTrackList.size(); i++) {
                    PlayBackTrack track = playBackTrackList.get(i);
                    ContentValues contentValues = new ContentValues(4);
                    contentValues.put(SongInfo.TRACK_ID, track.mId);
                    contentValues.put(SongInfo.SOURCE_ID, track.sourceId);
                    contentValues.put(SongInfo.SOURCE_TYPE, track.mIdType.mId);
                    contentValues.put(SongInfo.SOURCE_POSITION, track.mCurrentPosition);
                    sqLiteDatabase.insert(SongInfo.NAME, null, contentValues);
                }
                sqLiteDatabase.setTransactionSuccessful();
            }
            finally {
                sqLiteDatabase.endTransaction();
                position += PROCESS_NUM;
            }

        }
    }

    public ArrayList<PlayBackTrack> getSongToDatabase() {

        ArrayList<PlayBackTrack> list = new ArrayList<>();
        Cursor cursor = songDatabase.getReadableDatabase().query(SongInfo.NAME, null, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                list.ensureCapacity(cursor.getCount());
                do {
                    list.add(new PlayBackTrack(cursor.getLong(0), cursor.getLong(1), SparklesUtil.IdType.getInstance(cursor.getInt(2)), cursor.getInt(3)));
                } while (cursor.moveToNext());
            }
            return list;
        }
        finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

    }

    public static class SongInfo {
        public static String NAME = "playbacktrack";
        public static String TRACK_ID = "trackid";
        public static String SOURCE_ID = "sourceid";
        public static String SOURCE_TYPE = "sourcetype";
        public static String SOURCE_POSITION = "sourceposition";
    }
}
