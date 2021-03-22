package com.audioplayer.sparklesv10.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.audioplayer.sparklesv10.types.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistLoader {

    public ArrayList<Artist> getAllArtists(Context context, Cursor cursor) {

        ArrayList<Artist> list = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new Artist(cursor.getLong(0), cursor.getString(1),
                        cursor.getInt(2), cursor.getInt(3)));
            } while (cursor.moveToNext());

            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    public Artist getArtist(Context context, long id) {

        return artist(makeCursor(context, "_id=?", new String[]{String.valueOf(id)}));
    }

    private Artist artist(Cursor cursor) {

        Artist artist = new Artist();

        if (cursor != null && cursor.moveToFirst()) {
            artist = new Artist(cursor.getLong(0), cursor.getString(1),
                    cursor.getInt(2), cursor.getInt(3));
        }

        if (cursor != null) cursor.close();

        return artist;
    }

    public List<Artist> artistList(Context context) {
        return getAllArtists(context, makeCursor(context, null, null));
    }

    public static Cursor makeCursor(Context context, String selection, String[] selectionArg) {

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                "_id", //0
                "artist", //1
                "number_of_albums", //2
                "number_of_tracks", //3
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArg, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

        return cursor;
    }
}
