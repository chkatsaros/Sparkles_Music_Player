package com.audioplayer.sparklesv10.dataloader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.audioplayer.sparklesv10.types.Song;

import java.util.ArrayList;
import java.util.List;

public class AlbumSonglistLoader {

    public static List<Song> getAllAlbumSongs(Context context, long _id) {

        List<Song> albumSongList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[] {
                "_id", //0
                "title", //1
                "album_id", //2
                "album", //3
                "artist_id", //4
                "artist", //5
                "duration", //6
                "track" //7
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, "is_music=1 and title !='' and album_id="+_id, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int trackNumber = cursor.getInt(7);
                while (trackNumber >= 1000) {
                    trackNumber -= 1000;
                }

                albumSongList.add(new Song(cursor.getLong(0), cursor.getString(1),
                        cursor.getLong(2), cursor.getString(3),
                        cursor.getLong(4), cursor.getString(5),
                        cursor.getInt(6), trackNumber));
            } while (cursor.moveToNext());

            if (cursor != null) {
                cursor.close();
            }
        }

        return albumSongList;
    }
}
