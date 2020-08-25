package com.test.androidmedianotification;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

public class MusicService extends Service {

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;

    @Override
    public void onCreate() {
        // Start a new MediaSession
        mSession = new MediaSessionCompat(this, "MusicService");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        try {
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
        mMediaNotificationManager.startNotification();
    }

    @Override
    public void onDestroy() {
        mSession.release();
        mMediaNotificationManager.stopNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class MediaSessionCallback extends MediaSessionCompat.Callback {

    }
}
