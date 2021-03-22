package com.audioplayer.sparklesv10.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.audioplayer.sparklesv10.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import static com.audioplayer.sparklesv10.adapter.AlbumSonglistAdapter.albumSongList;
import static com.audioplayer.sparklesv10.adapter.ArtistSonglistAdapter.artistSongList;
import static com.audioplayer.sparklesv10.adapter.SongAdapter.songList;
import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.isPlaying;
import static com.audioplayer.sparklesv10.audioplayer.AudioPlayerService.play;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.NEXT_ACTION;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.PREVIOUS_ACTION;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.TOGGLEPAUSE_ACTION;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.needUpdate;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.playingIndex;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.resumePosition;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.seekBarHandler;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.sparklesMediaPlayer;
import static com.audioplayer.sparklesv10.audioplayer.AudioService.typePlaying;

public class ControllerFragment extends Fragment {

    private static final String TAG = ControllerFragment.class.getSimpleName();
    public static View top_container;
    public static ImageView ppButton, smallArt, ppButton2;
    private ImageView skipNext, skipPrev;
    private View view;
    public static ImageView albumArt;
    public static TextView song_title_artist, song_album, song_duration, song_elapsed_time, textView1, textView2;
    public static SeekBar seekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final SimpleDateFormat time = new SimpleDateFormat("mm:ss");

        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        this.view = view;

        ppButton = view.findViewById(R.id.playPause);
        ppButton2 = view.findViewById(R.id.controllerPlay);
        skipNext = view.findViewById(R.id.controllerNext);
        skipPrev = view.findViewById(R.id.controllerPrevious);

        song_title_artist = view.findViewById(R.id.controllerAristTitle);
        song_album = view.findViewById(R.id.controllerAlbum);
        song_duration = view.findViewById(R.id.timeDuration);
        song_elapsed_time = view.findViewById(R.id.timeElapsed);
        textView1 = view.findViewById(R.id.textView1);
        textView2 = view.findViewById(R.id.textView2);
        albumArt = view.findViewById(R.id.bigAlbumArt);
        smallArt = view.findViewById(R.id.smallArt);
        seekBar = view.findViewById(R.id.seekBar);

        top_container = view.findViewById(R.id.top_container);

        textView1.setSelected(true);
        textView2.setSelected(true);
        song_title_artist.setSelected(true);
        song_album.setSelected(true);

        ppButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sparklesMediaPlayer.isSparklesInit())
                            getActivity().sendBroadcast(new Intent(TOGGLEPAUSE_ACTION));
                        else play();
                    }
                },200);
            }
        });

        ppButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sparklesMediaPlayer.isSparklesInit())
                            getActivity().sendBroadcast(new Intent(TOGGLEPAUSE_ACTION));
                        else play();
                    }
                },200);
            }
        });

        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(NEXT_ACTION));
                    }
                },200);
            }
        });

        skipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(PREVIOUS_ACTION));
                    }
                },200);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sparklesMediaPlayer.seekTo(progress);
                    resumePosition = progress;
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    ppButton.setImageResource(R.drawable.ic_pause_circle_outline);
                    ppButton2.setImageResource(R.drawable.ic_pause_circle_outline);
                }
                else {
                    ppButton.setImageResource(R.drawable.ic_play_circle_outline);
                    ppButton2.setImageResource(R.drawable.ic_play_circle_outline);
                }
                if (needUpdate) {
                    if (typePlaying == 0) {
                        textView1.setText(songList.get(playingIndex).title + " - " + songList.get(playingIndex).artistName);
                        textView2.setText(songList.get(playingIndex).albumTitle);
                        song_title_artist.setText(songList.get(playingIndex).title + " - " + songList.get(playingIndex).artistName);
                        song_album.setText(songList.get(playingIndex).albumTitle);

                        if (getBitmap2(getContext(), songList.get(playingIndex).albumId) == null) {
                            albumArt.setImageResource(R.drawable.striangle);
                            smallArt.setImageResource(R.drawable.striangle);
                        } else {
                            albumArt.setImageBitmap(getBitmap(getContext(), songList.get(playingIndex).albumId));
                            smallArt.setImageBitmap(getBitmap(getContext(), songList.get(playingIndex).albumId));
                        }
                    } else if (typePlaying == 1) {
                        textView1.setText(albumSongList.get(playingIndex).title + " - " + albumSongList.get(playingIndex).artistName);
                        textView2.setText(albumSongList.get(playingIndex).albumTitle);
                        song_title_artist.setText(albumSongList.get(playingIndex).title + " - " + albumSongList.get(playingIndex).artistName);
                        song_album.setText(albumSongList.get(playingIndex).albumTitle);

                        if (getBitmap2(getContext(), albumSongList.get(playingIndex).albumId) == null) {
                            albumArt.setImageResource(R.drawable.striangle);
                            smallArt.setImageResource(R.drawable.striangle);
                        } else {
                            albumArt.setImageBitmap(getBitmap(getContext(), albumSongList.get(playingIndex).albumId));
                            smallArt.setImageBitmap(getBitmap(getContext(), albumSongList.get(playingIndex).albumId));
                        }
                    } else {
                        textView1.setText(artistSongList.get(playingIndex).title + " - " + artistSongList.get(playingIndex).artistName);
                        textView2.setText(artistSongList.get(playingIndex).albumTitle);
                        song_title_artist.setText(artistSongList.get(playingIndex).title + " - " + artistSongList.get(playingIndex).artistName);
                        song_album.setText(artistSongList.get(playingIndex).albumTitle);

                        if (getBitmap2(getContext(), artistSongList.get(playingIndex).albumId) == null) {
                            albumArt.setImageResource(R.drawable.striangle);
                            smallArt.setImageResource(R.drawable.striangle);
                        } else {
                            albumArt.setImageBitmap(getBitmap(getContext(), artistSongList.get(playingIndex).albumId));
                            smallArt.setImageBitmap(getBitmap(getContext(), artistSongList.get(playingIndex).albumId));
                        }
                    }
                    needUpdate = false;
                }
                seekBarHandler.postDelayed(this, 100);
            }
        });

        return view;
    }

    public Bitmap getBitmap(Context context, long id) {
        Bitmap albumArt = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);
            ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                FileDescriptor descriptor = fileDescriptor.getFileDescriptor();

                albumArt = BitmapFactory.decodeFileDescriptor(descriptor, null, options);
                fileDescriptor = null;
                descriptor = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (albumArt != null) {
            return albumArt;
        }
        else {
            return BitmapFactory.decodeResource(getResources(), R.drawable.notification);
        }
    }

    public Bitmap getBitmap2(Context context, long id) {
        Bitmap albumArt = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);
            ParcelFileDescriptor fileDescriptor = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (fileDescriptor != null) {
                FileDescriptor descriptor = fileDescriptor.getFileDescriptor();

                albumArt = BitmapFactory.decodeFileDescriptor(descriptor, null, options);
                fileDescriptor = null;
                descriptor = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (albumArt != null) {
            return albumArt;
        }
        else {
            return null;
        }
    }
}
