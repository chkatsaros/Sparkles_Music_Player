package com.audioplayer.sparklesv10.types;

public class Album {

    public final long id;
    public final String albumName;
    public final long artistId;
    public final String artistName;
    public final int numOfSongs;
    public final int year;

    public Album() {
        this.id = -1;
        this.albumName = "";
        this.artistId = -1;
        this.artistName = "";
        this.numOfSongs = -1;
        this.year = -1;
    }

    public Album(long id, String albumName, long artistId, String artistName, int numOfSongs, int year) {
        this.id = id;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.numOfSongs = numOfSongs;
        this.year = year;
    }
}
