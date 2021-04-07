package com.speech.ailotok;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.speech.ailotok.model.FlowNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

public class AvatarSpeech {

    private final UtteranceProgressListener utteranceProgressListener;
    private final Vector<FlowNode> flow;
    private final DisplaySpeechOnUIListener listener;
    private TextToSpeech mTTS;
    private FlowNode currentNode;
    private String QUESTION = "question";
    private String RESPONSE = "response";

    public AvatarSpeech(Context context, Vector<FlowNode> flow, UtteranceProgressListener utteranceProgressListener, DisplaySpeechOnUIListener listener) {
        this.utteranceProgressListener = utteranceProgressListener;
        this.flow = flow;
        this.listener = listener;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        assignCurrentNode();
                        speak(currentNode.getQuestion(), currentNode.getQuestion());
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    private void assignCurrentNode() {
        if (flow.size() == 0)
            close();
        else {
            currentNode = flow.firstElement();
            flow.removeElementAt(0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speak(String phrase, String id) {
        listener.display(phrase);
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
//        String text = "what is your name";
        float pitch = 1f;
        float speed = 0.8f;
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        Set<String> a = new HashSet<>();
        a.add("female");
        mTTS.setVoice(new Voice("en-us-x-sfg#female_2-local", new Locale("en", "US"), 400, 200, true, a));
        mTTS.speak(phrase, TextToSpeech.QUEUE_FLUSH, map);
        mTTS.setOnUtteranceProgressListener(utteranceProgressListener);
    }

    protected void close() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void speak(boolean longResponse) {
        if (!currentNode.isNodeFinished()) {
            speak(longResponse ? currentNode.getNegativeResponse() : currentNode.getPositiveResponse(), RESPONSE);
        } else {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    assignCurrentNode();
                    speak(currentNode.getQuestion(), QUESTION);
                }
            }, 500);
        }
    }

    public interface DisplaySpeechOnUIListener {
        void display(String text);
    }
}
