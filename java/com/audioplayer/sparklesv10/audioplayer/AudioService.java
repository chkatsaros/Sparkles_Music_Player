package com.audioplayer.sparklesv10.audioplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.audioplayer.sparklesv10.AudioPlayerAIDL;
import com.audioplayer.sparklesv10.MainActivity;
import com.audioplayer.sparklesv10.R;
import com.audioplayer.sparklesv10.database.PlayingInfo;
import com.audioplayer.sparklesv10.types.PlayBackTrack;
import com.audioplayer.sparklesv10.utility.SparklesUtil;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.audioplayer.sparklesv10.adapter.SongAdapter.songList;
import static com.audioplayer.sparklesv10.adapter.AlbumSonglistAdapter.albumSongList;
import static com.audioplayer.sparklesv10.adapter.ArtistSonglistAdapter.artistSongList;
import static com.audioplayer.sparklesv10.audioplayer.MediaStyle.NOTIFICATION_ID;
import static com.audioplayer.sparklesv10.audioplayer.MediaStyle.getActionIntent;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.ppButton;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.ppButton2;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.seekBar;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.song_duration;
import static com.audioplayer.sparklesv10.fragments.ControllerFragment.song_elapsed_time;

public class AudioService extends Service {

    public static final String TOGGLEPAUSE_ACTION = "com.audioplayer.sparklesv10.togglepause";
    public static final String PLAY_ACTION = "com.audioplayer.sparklesv10.play";
    public static final String PAUSE_ACTION = "com.audioplayer.sparklesv10.pause";
    public static final String STOP_ACTION = "com.audioplayer.sparklesv10.stop";
    public static final String NEXT_ACTION = "com.audioplayer.sparklesv10.next";
    public static final String PREVIOUS_ACTION = "com.audioplayer.sparklesv10.previous";

    private static final int SERVER_DIED = 10;
    private static final int FOCUS_CHANGE = 13;
    private static final int FADE_UP = 11;
    private static final int FADE_DOWN = 12;
    private static final int GO_TO_NEXT_TRACK = 22;
    private static final int GO_TO_PREVIOUS_TRACK = 23;

    private static final int NOTIFICATION_MODE_NON = 0;
    private static final int NOTIFICATION_MODE_FOREGROUND = 1;
    private static final int NOTIFICATION_MODE_BACKGROUND = 2;
    private int mNotify = NOTIFICATION_MODE_NON;
    public static long resumePosition = 0;

    private static final String TAG = "AudioService";

    private final IBinder I_BINDER = new SubStub(this);
    public static ArrayList<PlayBackTrack> playBackTrackList = new ArrayList<>(100);
    private PlayingInfo playingInfo;
    public static int playingIndex = -1;
    private SharedPreferences preference;
    public static SparklesMediaPlayer sparklesMediaPlayer;
    public static boolean isSupposedToBePlaying = false;
    private boolean sparklesPausedByTransientLossOfFocus = false;
    private AudioManager audioManager;
    private SparklesPlayerHandler sparklesPlayerHandler;
    private HandlerThread sparklesHandlerThread;

    public static Handler seekBarHandler = new Handler();
    public static boolean needUpdate = false;
    public static int songPosition = 0;
    public static int typePlaying = 0;
    private int notificationId;
    private MediaSessionCompat mSession;
    private NotificationManagerCompat mNotificationManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            commandHandler(intent);
        }
    };

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            sparklesPlayerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {

        playingInfo.saveSongInDatabase(playBackTrackList);

        if (isSupposedToBePlaying || sparklesPausedByTransientLossOfFocus) return true;

        stopSelf();
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel();
        }

        playingInfo = playingInfo.getInstance(this);
        playBackTrackList = playingInfo.getSongToDatabase();
        preference = getSharedPreferences("musicservice", 0);

        playingIndex = preference.getInt("pos", 0);
        sparklesHandlerThread = new HandlerThread("SparklesPlayerHandler", Process.THREAD_PRIORITY_BACKGROUND);
        sparklesHandlerThread.start();
        sparklesPlayerHandler = new SparklesPlayerHandler(sparklesHandlerThread.getLooper(),this);

        sparklesMediaPlayer = new SparklesMediaPlayer(this);
        sparklesMediaPlayer.setupHandler(sparklesPlayerHandler);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TOGGLEPAUSE_ACTION);
        filter.addAction(PLAY_ACTION);
        filter.addAction(PAUSE_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        filter.addAction(STOP_ACTION);

        registerReceiver(receiver, filter);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setupMediaSession();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return I_BINDER;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            commandHandler(intent);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sparklesMediaPlayer.release();
        sparklesMediaPlayer = null;

        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!isPlaying())
            System.exit(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationChannel(){

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID,"Sparkles", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
    }

    private void commandHandler(Intent intent) {

        String action = intent.getAction();

        if (TOGGLEPAUSE_ACTION.equals(action)) {
            if (isPlaying()) {
                pause();
                isSupposedToBePlaying = false;
                sparklesPausedByTransientLossOfFocus = false;
                mNotificationManager.notify(notificationId, createNotification());
                ppButton.setImageResource(R.drawable.ic_play_circle_outline);
                ppButton2.setImageResource(R.drawable.ic_play_circle_outline);
            }
            else {
                resume();
                ppButton.setImageResource(R.drawable.ic_pause_circle_outline);
                ppButton2.setImageResource(R.drawable.ic_pause_circle_outline);
            }
        }
        else if (PLAY_ACTION.equals(action)) {
            isSupposedToBePlaying = true;
            play();
            mNotificationManager.notify(notificationId, createNotification());
        }
        else if (PAUSE_ACTION.equals(action)) {
            pause();
            isSupposedToBePlaying = false;
            sparklesPausedByTransientLossOfFocus = false;
        }
        else if (NEXT_ACTION.equals(action)) {
            goToNext();
        }
        else if (PREVIOUS_ACTION.equals(action)) {
            goToPrevious();
        }
        else if (STOP_ACTION.equals(action)) {
            stop();
            isSupposedToBePlaying = false;
            sparklesPausedByTransientLossOfFocus = false;
            ppButton.setImageResource(R.drawable.ic_play_circle_outline);
            ppButton2.setImageResource(R.drawable.ic_play_circle_outline);
            mNotificationManager.notify(notificationId, createNotification());
        }

    }

    private void setupMediaSession() {
        mSession = new MediaSessionCompat(this, "Sparkles");

        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                play();
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                goToNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                goToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                stop();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                sparklesMediaPlayer.seekTo(pos);
            }
        });
    }

    private void updateMediaSession() {

        int playPauseState;
        final SimpleDateFormat time = new SimpleDateFormat("mm:ss");

        if (isPlaying()) {
            playPauseState = PlaybackStateCompat.STATE_PLAYING;
        }
        else {
            playPauseState = PlaybackStateCompat.STATE_PAUSED;
        }

        if (typePlaying == 0) {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songList.get(playingIndex).title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songList.get(playingIndex).artistName)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getBitmap(this, songList.get(playingIndex).albumId))
                    .build());
        }
        else if (typePlaying == 1) {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, albumSongList.get(playingIndex).title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, albumSongList.get(playingIndex).artistName)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getBitmap(this, albumSongList.get(playingIndex).albumId))
                    .build());
        }
        else {
            mSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, artistSongList.get(playingIndex).title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistSongList.get(playingIndex).artistName)
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getBitmap(this, artistSongList.get(playingIndex).albumId))
                    .build());
        }

        seekBar.setMax(sparklesMediaPlayer.getDuration());
        seekBar.setProgress(sparklesMediaPlayer.getCurrentPosition());

        seekBarHandler.removeCallbacks(moveSeekBarThread);
        seekBarHandler.postDelayed(moveSeekBarThread, 100);

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(playPauseState, getCurrentPosition(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP)
                .build());
    }

    Runnable moveSeekBarThread = new Runnable() {
        final SimpleDateFormat time = new SimpleDateFormat("mm:ss");

        public void run() {
            if (isPlaying()){
                int mediaPos_new = sparklesMediaPlayer.getCurrentPosition();
                int mediaMax_new = sparklesMediaPlayer.getDuration();
                seekBar.setMax(mediaMax_new);
                seekBar.setProgress(mediaPos_new);
                song_elapsed_time.setText(time.format(mediaPos_new));
                song_duration.setText(time.format(mediaMax_new));
                seekBarHandler.postDelayed(this, 100);
            }
        }
    };

    private Notification createNotification() {

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int playPauseButtton;

        if (isPlaying())
            playPauseButtton = R.drawable.ic_pause_circle_outline;
        else
            playPauseButtton = R.drawable.ic_play_circle_outline;

        NotificationCompat.Builder builder = MediaStyle.from(this, mSession);

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2, 3)
                .setMediaSession(mSession.getSessionToken()));

        builder.setSmallIcon(R.drawable.striangle)
                .setColor(getResources().getColor(R.color.colorPrimary));

        builder.setAutoCancel(true);

        builder.setContentIntent(resultPendingIntent);

        builder.addAction(R.drawable.ic_skip_previous, getString(R.string.previous), getActionIntent(this, PREVIOUS_ACTION))
                .addAction(R.drawable.ic_stop, getString(R.string.stop), getActionIntent(this, STOP_ACTION))
                .addAction(playPauseButtton, getString(R.string.togglepause), getActionIntent(this, TOGGLEPAUSE_ACTION))
                .addAction(R.drawable.ic_skip_next, getString(R.string.next), getActionIntent(this, NEXT_ACTION));

        return builder.build();
    }

    private void goToPrevious() {

        synchronized (this) {
            playingIndex--;

            if (playingIndex < 0) {
                playingIndex = playBackTrackList.size() - 1;
            }

            sparklesMediaPlayer.stop();
            play();
        }
    }


    private void goToNext() {
        synchronized (this) {
            playingIndex++;
            if (playingIndex == playBackTrackList.size()) {
                playingIndex = 0;
            }

            sparklesMediaPlayer.stop();
            play();
        }
    }

    private int getCurrentPosition() {

        if (sparklesMediaPlayer.sparklesInit) return sparklesMediaPlayer.getCurrentPosition();

        return -1;
    }

    private void open(long[] list, int position, long sourceId, SparklesUtil.IdType idType) {

        synchronized (this) {
            boolean newList = true;
            if (list.length == playBackTrackList.size()) {
                newList = false;
                for (int i=0; i<list.length; i++) {
                    if (list[i] != playBackTrackList.get(i).mId) {
                        newList = true;
                        break;
                    }
                }
            }
            if (newList) {
                addToPlayList(list, -1, sourceId, idType);
                playingInfo.saveSongInDatabase(playBackTrackList);
            }

            if (position >= 0) playingIndex = position;
        }
    }

    public void addToPlayList(long[] list, int position, long sourceId, SparklesUtil.IdType idType){

        if (position < 0) {
            playBackTrackList.clear();
            position = 0;
        }

        playBackTrackList.ensureCapacity(playBackTrackList.size() + list.length);

        if (position > 0) position = playBackTrackList.size();

        ArrayList<PlayBackTrack> myList = new ArrayList<>(list.length);

        for (int i=0; i<list.length; i++) {
            myList.add(new PlayBackTrack(list[i], sourceId, idType, i));
        }

        playBackTrackList.addAll(position, myList);
    }


    public long getAudioId() {

        PlayBackTrack playBackTrack = getCurrentTrack();

        if (playBackTrack != null) return playBackTrack.mId;

        return -1;
    }

    public PlayBackTrack getCurrentTrack() {

        return getTrack(playingIndex);
    }

    public synchronized PlayBackTrack getTrack(int playingPosition) {

        if (playingPosition != -1 && playingPosition < playBackTrackList.size()) return playBackTrackList.get(playingPosition);

        return null;
    }

    public int getQueuePosition() {

        synchronized (this) {
           return playingIndex;
        }
    }

    public long[] getIdList() {

        synchronized (this) {
            int lenght = playBackTrackList.size();
            long[] idL = new long[lenght];
            for (int i = 0; i < lenght; i++) {
                idL[i] = playBackTrackList.get(i).mId;

            }
            return idL;
        }
    }

    private boolean isPlaying() {
        synchronized (this){
            return isSupposedToBePlaying;
        }
    }

    private void pause() {

        if (isPlaying()) {
            sparklesMediaPlayer.pause();
            isSupposedToBePlaying = false;
            resumePosition = sparklesMediaPlayer.getCurrentPosition();
        }
    }

    private void resume() {
        sparklesMediaPlayer.seekTo(resumePosition);

        int status = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        mSession.setActive(true);
        sparklesMediaPlayer.start();
        sparklesPlayerHandler.removeMessages(FADE_DOWN);
        sparklesPlayerHandler.sendEmptyMessage(FADE_UP);
        isSupposedToBePlaying = true;
        sparklesPausedByTransientLossOfFocus = true;

        updateMediaSession();

        notificationId = hashCode();

        startForeground(notificationId, createNotification());
    }

    private void play() {

        songPosition = 0;

        sparklesMediaPlayer.setupDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + playBackTrackList.get(playingIndex).mId);

        int status = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        mSession.setActive(true);
        sparklesMediaPlayer.start();
        sparklesPlayerHandler.removeMessages(FADE_DOWN);
        sparklesPlayerHandler.sendEmptyMessage(FADE_UP);
        isSupposedToBePlaying = true;
        sparklesPausedByTransientLossOfFocus = true;

        needUpdate = true;
        updateMediaSession();

        notificationId = hashCode();

        startForeground(notificationId, createNotification());
    }

    private int getAudioSessionId() {
        synchronized (this) {
            return sparklesMediaPlayer.getAudioSessionId();
        }
    }

    private void stop() {

        if (sparklesMediaPlayer.sparklesInit) {
            sparklesMediaPlayer.pause();
            resumePosition = 0;
            isSupposedToBePlaying = false;
        }
    }

    private int getDuration() {
        if (sparklesMediaPlayer != null) return sparklesMediaPlayer.getDuration();

        return -1;
    }

    private long seekTo(long position) {
        if (sparklesMediaPlayer != null) return sparklesMediaPlayer.seekTo(position);

        return -1;
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

    public class SparklesMediaPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


        private WeakReference<AudioService> audioServiceWeakReference;
        private MediaPlayer sparklesMediaPlayer = new MediaPlayer();
        private boolean sparklesInit = false;

        private Handler sparklesHandler;

        private float sparklesVolume;

        public SparklesMediaPlayer(AudioService audioService) {
            this.audioServiceWeakReference =  new WeakReference<>(audioService);
        }

        public void setupDataSource(String path) {
            try {
                sparklesInit = setDataPath(sparklesMediaPlayer, path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean setDataPath(MediaPlayer sparklesMediaPlayer, String path) throws IOException {
            sparklesMediaPlayer.reset();
            sparklesMediaPlayer.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                sparklesMediaPlayer.setDataSource(audioServiceWeakReference.get(), Uri.parse(path));
            }
            else {
                sparklesMediaPlayer.setDataSource(path);
            }

            sparklesMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sparklesMediaPlayer.prepare();
            sparklesMediaPlayer.setOnErrorListener(this);
            sparklesMediaPlayer.setOnCompletionListener(this);

            return true;
        }

        public boolean isSparklesInit() {
            return sparklesInit;
        }

        public void setupHandler(Handler handler) {
            sparklesHandler = handler;
        }

        public void start() {
            sparklesMediaPlayer.start();
        }

        public void stop() {
            sparklesMediaPlayer.stop();
            sparklesInit = false;
        }

        public void pause() {
            sparklesMediaPlayer.pause();
        }

        public void release() {
            stop();
            sparklesMediaPlayer.release();
        }

        public int getDuration() {
            if (sparklesMediaPlayer!=null && isSparklesInit()) return sparklesMediaPlayer.getDuration();

            return -1;
        }

        public int getCurrentPosition() {
            if (sparklesMediaPlayer!=null && isSparklesInit()) return sparklesMediaPlayer.getCurrentPosition();

            return 0;
        }

        public void setVolume(float volume) {
            sparklesVolume = volume;
            sparklesMediaPlayer.setVolume(sparklesVolume, sparklesVolume);
        }
        public long seekTo(long position) {
            sparklesMediaPlayer.seekTo((int) position);

            return position;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp == sparklesMediaPlayer) {
                sparklesHandler.sendEmptyMessage(GO_TO_NEXT_TRACK);
            }
        }
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                sparklesInit = false;
                sparklesMediaPlayer.release();
                sparklesMediaPlayer = new MediaPlayer();
                Message message = sparklesHandler.obtainMessage(SERVER_DIED);
                sparklesHandler.sendMessageDelayed(message, 2000);
            }
            return false;
        }
        public int getAudioSessionId() {
            return sparklesMediaPlayer.getAudioSessionId();
        }
    }
    public class SparklesPlayerHandler extends Handler {


        private WeakReference<AudioService> audioService;

        private float sparklesVolume = 1.0f;
        public SparklesPlayerHandler(@NonNull Looper looper, AudioService service) {
            super(looper);
            this.audioService = new WeakReference<>(service);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {

            AudioService mService = audioService.get();
            if (mService == null) {
                return;
            }

            synchronized (mService) {
                switch (msg.what) {
                    case FADE_UP:
                        sparklesVolume += 0.1;
                        if (sparklesVolume < 0.1f )
                            sendEmptyMessageDelayed(FADE_UP, 10);
                        else sparklesVolume = 1.0f;
                        mService.sparklesMediaPlayer.setVolume(sparklesVolume);
                        break;
                    case FADE_DOWN:
                        sparklesVolume -= 0.5;
                        if (sparklesVolume < 0.2f )
                            sendEmptyMessageDelayed(FADE_DOWN, 10);
                        else sparklesVolume = 0.2f;
                        mService.sparklesMediaPlayer.setVolume(sparklesVolume);
                        break;
                    case GO_TO_NEXT_TRACK:
                        goToNext();
                        break;
                    case GO_TO_PREVIOUS_TRACK:
                        goToPrevious();
                        break;
                    case FOCUS_CHANGE:
                        switch (msg.arg1) {
                            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                                removeMessages(FADE_UP);
                                sendEmptyMessage(FADE_DOWN);
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (mService.isSupposedToBePlaying)
                                    mService.sparklesPausedByTransientLossOfFocus = msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                                mService.pause();
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!mService.isSupposedToBePlaying && mService.sparklesPausedByTransientLossOfFocus) {
                                    mService.sparklesPausedByTransientLossOfFocus = false;
                                    sparklesVolume = 0.0f;
                                    mService.sparklesMediaPlayer.setVolume(sparklesVolume);
                                    mService.play();
                                }
                                else {
                                    removeMessages(FADE_DOWN);
                                    sendEmptyMessage(FADE_UP);
                                }
                                break;
                        }
                        break;
                }
            }

            super.handleMessage(msg);
        }
    }
    public static final class SubStub extends AudioPlayerAIDL.Stub {



        private WeakReference<AudioService> audioPlayerServiceWeakReference;

        public SubStub(AudioService audioService) {
            this.audioPlayerServiceWeakReference = new WeakReference<>(audioService);
        }

        @Override
        public void open(long[] list, int position, long sourceId, int type) {
            audioPlayerServiceWeakReference.get().open(list, position, sourceId, SparklesUtil.IdType.getInstance(type));
        }

        @Override
        public void play() {
            audioPlayerServiceWeakReference.get().play();
        }

        @Override
        public void stop() {
            audioPlayerServiceWeakReference.get().stop();
        }

        @Override
        public void pause() {
            audioPlayerServiceWeakReference.get().pause();
        }

        @Override
        public void resume() {
            audioPlayerServiceWeakReference.get().resume();
        }

        @Override
        public void goToNext() {
            audioPlayerServiceWeakReference.get().goToNext();
        }

        @Override
        public void goToPrevious() {
            audioPlayerServiceWeakReference.get().goToPrevious();
        }

        @Override
        public boolean isPlaying() {
            return audioPlayerServiceWeakReference.get().isPlaying();
        }

        @Override
        public long getAudioId() {
            return audioPlayerServiceWeakReference.get().getAudioId();
        }
        @Override
        public int getCurrentPosition() {
            return audioPlayerServiceWeakReference.get().getCurrentPosition();
        }

        @Override
        public int getQueuePosition() {
            return audioPlayerServiceWeakReference.get().getQueuePosition();
        }

        @Override
        public int getDuration() {
            return audioPlayerServiceWeakReference.get().getDuration();
        }

        @Override
        public long[] getIdList() {
            return audioPlayerServiceWeakReference.get().getIdList();
        }

        @Override
        public long seekTo(long position) {
            return audioPlayerServiceWeakReference.get().seekTo(position);
        }

    }
}
