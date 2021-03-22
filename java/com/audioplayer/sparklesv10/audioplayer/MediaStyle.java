package com.audioplayer.sparklesv10.audioplayer;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;

public class MediaStyle {

    public static final String NOTIFICATION_ID = "sparkles";

    public static PendingIntent getActionIntent(Context context, String action) {
        ComponentName componentName = new ComponentName(context, AudioService.class);
        Intent intent = new Intent(action);
        intent.setComponent(componentName);

        return PendingIntent.getService(context, 0, intent, 0);
    }

    public static NotificationCompat.Builder from(Context context, MediaSessionCompat mediaSessionCompat) {
        MediaControllerCompat controllerCompat = mediaSessionCompat.getController();
        MediaMetadataCompat mediaMetadataCompat = controllerCompat.getMetadata();
        MediaDescriptionCompat mediaDescriptionCompat = mediaMetadataCompat.getDescription();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_ID);
        builder.setContentTitle(mediaDescriptionCompat.getTitle())
                .setContentText(mediaDescriptionCompat.getSubtitle())
                .setSubText(mediaDescriptionCompat.getDescription())
                .setLargeIcon(mediaDescriptionCompat.getIconBitmap())
                .setContentIntent(controllerCompat.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return  builder;
    }
}
