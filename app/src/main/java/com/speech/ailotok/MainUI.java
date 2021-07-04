package com.speech.ailotok;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
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
    private View chosenTopicView;
    private Topic chosenTopic;

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MainUI(Activity activity, View mainView) {
        weakReference = new WeakReference<>(activity);
        topics.add(new Topic("Emotions", weakReference.get().getDrawable(R.drawable.ic_emoticons)));
        topics.add(new Topic("General", weakReference.get().getDrawable(R.drawable.ic_chat)));
        topics.add(new Topic("Food", weakReference.get().getDrawable(R.drawable.ic_food)));
        topics.add(new Topic("Travel", weakReference.get().getDrawable(R.drawable.ic_traveling)));

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
            DisplayMetrics displayMetrics = new DisplayMetrics();
            weakReference.get().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            gridParam.width = width / 3;
            gridParam.height = width / 2;

            inflatedLayout.setLayoutParams(gridParam);
            inflatedLayout.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onClick(View view) {
                    if (chosenTopic != null) {
                        chosenTopicView.setBackground(weakReference.get().getDrawable(R.drawable.unclicked_box));

                        if (chosenTopic.getName().equals(topic.getName()))
                            chosenTopic = null;
                        else {
                            view.setBackground(weakReference.get().getDrawable(R.drawable.clicked_topic));
                            chosenTopicView = view;
                            chosenTopic = topic;
                        }
                    } else {
                        view.setBackground(weakReference.get().getDrawable(R.drawable.clicked_topic));
                        chosenTopicView = view;
                        chosenTopic = topic;
                    }
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
