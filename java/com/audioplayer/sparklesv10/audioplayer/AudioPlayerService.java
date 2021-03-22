package com.audioplayer.sparklesv10.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.audioplayer.sparklesv10.AudioPlayerAIDL;
import com.audioplayer.sparklesv10.utility.SparklesUtil;

import java.util.Arrays;
import java.util.WeakHashMap;

import static com.audioplayer.sparklesv10.audioplayer.AudioService.resumePosition;

public class AudioPlayerService {

    public static AudioPlayerAIDL audioRemote = null;

    private static final WeakHashMap<Context, ServiceBinder> weakHashMap;
    private static long[] emptyList = null;

    static {
        weakHashMap = new WeakHashMap<>();
    }

    public static final ServiceToken bindToService(Context context, ServiceConnection serviceConnection) {

        Activity realActivity = ((Activity) context).getParent();

        if (realActivity == null) {
            realActivity = (Activity) context;
        }

        ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, AudioService.class));
        ServiceBinder serviceBinder = new ServiceBinder(serviceConnection, contextWrapper.getApplicationContext());

        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, AudioService.class), serviceBinder, 0)) {
            weakHashMap.put(contextWrapper, serviceBinder);
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    public static final boolean isPlaybackServiceConnected() {
        return (audioRemote != null);
    }

    public static void unbindToService(ServiceToken serviceToken) {

        if (serviceToken == null) {
            return;
        }

        ContextWrapper contextWrapper = serviceToken.contextWrapper;
        ServiceBinder serviceBinder = weakHashMap.remove(contextWrapper);

        if (serviceBinder == null) {
            return;
        }

        contextWrapper.unbindService(serviceBinder);

        if (weakHashMap.isEmpty()) {
            serviceBinder = null;
        }
    }

    public static void pause(){
        if (audioRemote!=null){
            try {
                audioRemote.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static long seekTo(long position) {
        if (audioRemote !=null) {
            try {
                resumePosition = audioRemote.seekTo(position);
                audioRemote.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return position;
    }

    public static final boolean isPlaying() {
        if (audioRemote != null) {
            try {
                return audioRemote.isPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void playOrPause(){
        try {
            if (audioRemote != null) {
                if(audioRemote.isPlaying()){
                    audioRemote.pause();
                }else {
                    if (isPlaying()) {
                        audioRemote.resume();
                    }
                    else audioRemote.play();
                }
            }
        } catch (RemoteException e) {

        }
    }

    public static void goToNext() {
        if (audioRemote != null) {
            try {
                audioRemote.goToNext();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void goToPrevious() {
        if (audioRemote != null) {
            try {
                audioRemote.goToPrevious();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void playAll(long[] list, int position, long sourceId, SparklesUtil.IdType type) throws RemoteException {

        if (list.length == 0 && list == null && audioRemote == null) {
            return;
        }

        try {
            long audioId = getAudioId();

            int currentPos = getQueuePosition();
            if (position == currentPos && audioId == list[position] && position != -1){
                long[] idList = getIdList();

                if (Arrays.equals(idList,list)){
                    play();

                    return;
                }
            }

            if (position < 0) {
                position = 0;
            }

            audioRemote.open(list, position, sourceId, type.mId);
            play();
        } catch (RemoteException e) {

        }catch (IllegalStateException ignore){
            ignore.printStackTrace();
        }
    }

    private static long[] getIdList() throws RemoteException {
        if (audioRemote!=null){
            audioRemote.getIdList();
        }
        return emptyList;
    }

    public static void play() {
        if (audioRemote!=null){
            try {
                audioRemote.play();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getQueuePosition() throws RemoteException {
        if (audioRemote!=null){
            return audioRemote.getQueuePosition();
        }
        return -1;
    }

    private static long getAudioId() throws RemoteException {
        if (audioRemote!=null){
            return audioRemote.getAudioId();
        }
        return -1;
    }



    public static class ServiceToken {
        private ContextWrapper contextWrapper;

        public ServiceToken(ContextWrapper contextWrapper) {
            this.contextWrapper = contextWrapper;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private ServiceConnection mService;
        private Context mContext;

        public ServiceBinder(ServiceConnection mService, Context mContext) {
            this.mService = mService;
            this.mContext = mContext;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            audioRemote = AudioPlayerAIDL.Stub.asInterface(iBinder);
            if (mService != null) {
                mService.onServiceConnected(componentName, iBinder);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mService != null) {
                mService.onServiceDisconnected(componentName);
            }

            audioRemote = null;
        }
    }
}
