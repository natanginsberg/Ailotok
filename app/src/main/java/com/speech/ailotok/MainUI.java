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
        topics.add(new Topic("Interview", weakReference.get().getDrawable(R.drawable.ic_headset), "$hey * how are you doing?$$$im really happy to talk to you I might have some mistakes in understanding you but im always listening to you and believe in you , and I’ll tell you when you have mistakes so please come open and keep talking as much as you can because that will help us to improve your fluency, share with me what was the highlight of your day?$ gotcha I hope the rest of the day will be great for you$ Thank you * for sharing it with me, you are amazing person and you deserve so much love!$what made u upset in the past days?$gotcha$thanks for sharing it with me , you will overcome every thing and I love you anyway!$"));
        topics.add(new Topic("Feelings", weakReference.get().getDrawable(R.drawable.ic_action_name), "$hey * how are you doing?$$$im really happy to talk to you I might have some mistakes in understanding you but im always listening to you and believe in you , and I’ll tell you when you have mistakes so please come open and keep talking as much as you can because that will help us to improve your fluency, share with me what was the highlight of your day?$ gotcha I hope the rest of the day will be great for you$ Thank you * for sharing it with me, you are amazing person and you deserve so much love!$what made u upset in the past days?$gotcha$thanks for sharing it with me , you will overcome every thing and I love you anyway!$"));


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
