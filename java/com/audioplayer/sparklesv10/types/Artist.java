package com.audioplayer.sparklesv10.types;

public class Artist {

    public final long id;
    public final String artistName;
    public final int albumCount;
    public final int songCount;

    public Artist() {
        this.id = -1;
        this.artistName = "";
        this.albumCount = -1;
        this.songCount = -1;
    }

    public Artist(long id, String artistName,  int albumCount, int songCount) {
        this.id = id;
        this.artistName = artistName;
        this.albumCount = albumCount;
        this.songCount = songCount;
    }
}
