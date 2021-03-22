package com.audioplayer.sparklesv10.types;

public class Song {

    public final long id;
    public final String title;
    public final long albumId;
    public final String albumTitle;
    public final long artistId;
    public final String artistName;
    public final int duration;
    public final int trackNumber;

    public Song() {
        this.id = -1;
        this.title = "";
        this.albumId = -1;
        this.albumTitle = "";
        this.artistId = -1;
        this.artistName = "";
        this.duration = -1;
        this.trackNumber = -1;
    }

    public Song(long id, String title, long albumId, String albumTitle, long artistId, String artistName, int duration, int trackNumber) {
        this.id = id;
        this.title = title;
        this.albumId = albumId;
        this.albumTitle = albumTitle;
        this.artistId = artistId;
        this.artistName = artistName;
        this.duration = duration;
        this.trackNumber = trackNumber;
    }
}
