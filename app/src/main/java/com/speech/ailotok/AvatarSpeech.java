package com.speech.ailotok;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AvatarSpeech {

    private static final String COMPUTER_GENERATED = "computerGenerated";
    private static final String USER_GENERATED = "user";
    private final UtteranceProgressListener utteranceProgressListener;
    //    private final Vector<FlowNode> flow;
    private final DisplaySpeechOnUIListener listener;
    private TextToSpeech mTTS;
    private EndListener endListener;

    public AvatarSpeech(Context context, String conversationInit, UtteranceProgressListener utteranceProgressListener, DisplaySpeechOnUIListener listener) {
        this.utteranceProgressListener = utteranceProgressListener;
//        this.flow = flow;
        this.listener = listener;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);
//                    if (result == TextToSpeech.LANG_MISSING_DATA
//                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Log.e("TTS", "Language not supported");
//                    } else {
//                        if (assignCurrentNode())
//                            speak(currentNode.getQuestion(), QUESTION);
//                    }
                    speak(conversationInit, false);
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    public void subscribeEndListener(EndListener endListener) {
        this.endListener = endListener;
    }

////    private boolean assignCurrentNode() {
//        if (flow.size() == 0) {
//            close();
//            return false;
//        } else {
//            currentNode = flow.firstElement();
//            flow.removeElementAt(0);
//            return true;
//        }
//    }


    public void speak(String phrase, boolean translation) {
        listener.display(phrase);
        HashMap<String, String> map = new HashMap<>();
        if (translation)
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, COMPUTER_GENERATED);
        else
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, USER_GENERATED);
//        String text = "what is your name";
        float pitch = 0.8f;
        float speed = 1f;
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

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void speak(String response) {
//        speak(response);
////        if (!currentNode.isNodeFinished()) {
////            speak(longResponse ? currentNode.getPositiveResponse() : currentNode.getNegativeResponse(), RESPONSE);
////        } else {
////            final Handler handler = new Handler(Looper.getMainLooper());
////            handler.postDelayed(new Runnable() {
////                @Override
////                public void run() {
////                    if (assignCurrentNode())
////                        speak(currentNode.getQuestion(), QUESTION);
////                    else
////
////
////
////                     endListener.end();
////                }
////            }, 500);
//    }


    public interface DisplaySpeechOnUIListener {
        void display(String text);
    }

    public interface EndListener {
        void end();
    }
}
