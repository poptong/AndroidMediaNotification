/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.androidmedianotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.test.androidmedianotification.utils.AlbumArtCache;
import com.test.androidmedianotification.utils.LogHelper;
import com.test.androidmedianotification.utils.ResourceHelper;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = "MediaNotificationManager";

    private static final String CHANNEL_ID = "com.example.android.uamp.MUSIC_CHANNEL_ID";

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.example.android.uamp.pause";
    public static final String ACTION_PLAY = "com.example.android.uamp.play";
    public static final String ACTION_PREV = "com.example.android.uamp.prev";
    public static final String ACTION_NEXT = "com.example.android.uamp.next";
    public static final String ACTION_STOP = "com.example.android.uamp.stop";
    public static final String ACTION_STOP_CASTING = "com.example.android.uamp.stop_cast";

    private final MusicService mService;

    private final NotificationManager mNotificationManager;

    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopIntent;

    private final PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    private boolean mStarted = false;

    public MediaNotificationManager(MusicService service) throws RemoteException {
        mService = service;

        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary,
                Color.DKGRAY);

        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!mStarted) {
            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                mService.registerReceiver(this, filter);

                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                break;
            case ACTION_PLAY:
                break;
            case ACTION_NEXT:
                break;
            case ACTION_PREV:
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra(MainActivity.EXTRA_START_FULLSCREEN, true);
        if (description != null) {
            openUI.putExtra(MainActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private Notification createNotification() {
        LogHelper.d(TAG, "updateNotificationMetadata.");

        String fetchArtUrl = null;
        Bitmap art = null;
//        if (description.getIconUri() != null) {
//            // This sample assumes the iconUri will be a valid URL formatted String, but
//            // it can actually be any valid Android Uri formatted String.
//            // async fetch the album art icon
//            String artUrl = null;
//            art = AlbumArtCache.getInstance().getBigImage(artUrl);
//            if (art == null) {
//                fetchArtUrl = artUrl;
//                // use a placeholder art while the remote art is being downloaded
//                art = BitmapFactory.decodeResource(mService.getResources(),
//                        R.drawable.ic_default_art);
//            }
//        }

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService, CHANNEL_ID);

        notificationBuilder
                .setStyle(new MediaStyle()
                        // show only play/pause in compact view
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(mStopIntent)
                       )
                .setDeleteIntent(mStopIntent)
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentTitle("title")
                .setContentText("context")
                .setLargeIcon(art);


        return notificationBuilder.build();
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            mService.getString(R.string.notification_channel),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                    mService.getString(R.string.notification_channel_description));

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
