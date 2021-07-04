package com.speech.ailotok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.speech.ailotok.model.Topic;

public class MainActivity extends AppCompatActivity implements MainUI.MainUIListener {


    private static final int AUDIO_CODE = 100;

    private Topic topicChosen = null;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promo);
        setTimerToStartMainBackground();
    }

    private void setTimerToStartMainBackground() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_main2);
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        AUDIO_CODE);
            }
        }, 1500);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_CODE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                MainUI ui = new MainUI(this, findViewById(android.R.id.content).getRootView());
                ui.subscribeListener(this);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void openConversation(Topic topic) {
        if (topicChosen != null) {
            if (topicChosen.getName().equals(topic.getName())) {
                topicChosen = null;
            }
        } else {
            topicChosen = topic;

        }
    }

    public void startConversation(View view) {
        if (topicChosen != null) {
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("topic", topicChosen.getName());
//            intent.putExtra("name", name);
            startActivity(intent);
        }
    }
}