package com.audioplayer.sparklesv10;

interface AudioPlayerAIDL {

    void open(in long[] list,int position,long sourceId,int type);
    void play();
    void stop();
    void resume();
    void pause();
    boolean isPlaying();
    long getAudioId();
    int getQueuePosition();
    int getDuration();
    int getCurrentPosition();
    void goToNext();
    void goToPrevious();
    long[] getIdList();
    long seekTo(long position);

}
