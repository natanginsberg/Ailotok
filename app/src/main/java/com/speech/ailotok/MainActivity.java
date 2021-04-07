package com.speech.ailotok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.speech.ailotok.model.Topic;

public class MainActivity extends AppCompatActivity implements MainUI.MainUIListener {


    private static final int AUDIO_CODE = 100;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_CODE);
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
        String name = (String) ((EditText) findViewById(R.id.name)).getText().toString();
        if (name.equals("")) {
            findViewById(R.id.name).setBackground(getDrawable(R.drawable.edit_text_highlighted_background));
            findViewById(R.id.name_error).setVisibility(View.VISIBLE);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.name).setBackground(getDrawable(R.drawable.edit_text_background));
                    findViewById(R.id.name_error).setVisibility(View.INVISIBLE);
                }
            });
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("topic", topic.getFlowString());
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }
}