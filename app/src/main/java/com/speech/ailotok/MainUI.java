package com.speech.ailotok;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.speech.ailotok.model.Topic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainUI {

    private List<Topic> topics = new ArrayList<>();

    private WeakReference<Activity> weakReference;
    private MainUIListener listener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MainUI(Activity activity, View mainView) {
        weakReference = new WeakReference<>(activity);
        topics.add(new Topic("Interview", weakReference.get().getDrawable(R.drawable.ic_headset), "$hey * how are you doing?$ $ $I’m very happy to talk to you. I might have some mistakes in understanding you, but I’m always here for you. I’ll correct your grammar mistakes, so talk as much as you would like. The more you talk the more fluent you will get. What was the highlight of your day?$Gotcha! I hope you will enjoy the rest of your day$ Thank you * for sharing it with me, you are an amazing person and you deserve so much love!$Why are you upset?$Gotcha!$Thank you so much for sharing that with me. I am confident that you will overcome every obstacle in your way. I love you!$tWhat else would you like to tell me?$thank you so much for talking to me!$That sounds fantastic$"));
        topics.add(new Topic("Feelings", weakReference.get().getDrawable(R.drawable.ic_action_name), "$hey * how are you doing?$ $ $I’m very happy to talk to you. I might have some mistakes in understanding you, but I’m always here for you. I’ll correct your grammar mistakes, so talk as much as you would like. The more you talk the more fluent you will get. What was the highlight of your day?$ $Gotcha! I hope you will enjoy the rest of your day$ Thank you * for sharing it with me, you are an amazing person and you deserve so much love!$Why are you upset?$Gotcha!$Thank you so much for sharing that with me. I am confident that you will overcome every obstacle in your way. I love you!$tWhat else would you like to tell me?$thank you so much for talking to me!$That sounds fantastic"));

        mainView.post(new Runnable() {
            @Override
            public void run() {
                createGridForActivity((GridLayout) mainView.findViewById(R.id.topics));
            }
        });
    }

    public void subscribeListener(MainUIListener mainUIListener) {
        this.listener = mainUIListener;
    }

    private void createGridForActivity(GridLayout gridLayout) {
        int total = 10;
        int row = total / 2;
        gridLayout.setRowCount(row + 1);
        for (Topic topic : topics) {
            LayoutInflater inflater = LayoutInflater.from(weakReference.get().getBaseContext());
            View view = inflater.inflate(R.layout.activity_main2, null);
            View inflatedLayout = inflater.inflate(R.layout.topic, (ViewGroup) view, false);
            ((ImageView) inflatedLayout.findViewById(R.id.topic_image)).setBackground(topic.getPicture());
            ((TextView) inflatedLayout.findViewById(R.id.topic_text)).setText(topic.getName());
            GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    inflatedLayout.getLayoutParams());
            gridParam.setGravity(Gravity.CENTER);
            gridParam.setMargins(75, 40, 75, 0);
            inflatedLayout.setLayoutParams(gridParam);
            inflatedLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.openConversation(topic);
                }
            });
            gridLayout.addView(inflatedLayout);


        }
    }

    public interface MainUIListener {
        void openConversation(Topic topic);
    }
}
