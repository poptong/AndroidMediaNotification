package com.test.androidmedianotification;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_START_FULLSCREEN =
            "com.example.android.uamp.EXTRA_START_FULLSCREEN";

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION =
            "com.example.android.uamp.CURRENT_MEDIA_DESCRIPTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(getApplicationContext(), MusicService.class);
        startService(i);

    }
}
