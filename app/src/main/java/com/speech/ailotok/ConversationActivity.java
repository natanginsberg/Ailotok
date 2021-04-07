package com.speech.ailotok;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.speech.ailotok.model.FlowNode;
import com.speech.ailotok.model.Topic;

import java.util.ArrayList;
import java.util.Vector;

public class ConversationActivity extends AppCompatActivity implements RecognitionListener {

    private String QUESTION = "question";
    private String RESPONSE = "response";

    private TextView returnedText;
    private ImageView mic;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private ProgressBar progressBar;
    private AvatarSpeech avatarSpeech;
    private Topic topic;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
//        topic = (Topic) getIntent().getExtras().get("topic");
        userName = getIntent().getStringExtra("name");
        mic = findViewById(R.id.mic);
        Vector<FlowNode> flow = FlowParser.createFlow(userName, getIntent().getStringExtra("topic"));
        initiateStt();
        avatarSpeech = new AvatarSpeech(this, flow, new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                onRmsChanged(0.56f);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDone(String s) {
                if (s.equals(QUESTION)) {
                    runOnUiThread(() -> mic.setBackground(getDrawable(R.drawable.ic_mic)));
                    returnedText.post(new Runnable() {
                        @Override
                        public void run() {
                            speech.startListening(recognizerIntent);
                        }
                    });
                } else {
                    avatarSpeech.speak(true);
                }
            }

            @Override
            public void onError(String s) {

            }
        }, text -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                returnedText.setText(text);
            }
        }));

    }

    private void initiateStt() {
        returnedText = (TextView) findViewById(R.id.text_displayed);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

//        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
        if (avatarSpeech != null)
            avatarSpeech.close();

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
//        progressBar.setIndeterminate(false);
//        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        findViewById(R.id.mic).setBackground(getDrawable(R.drawable.ic_mic_gray));

//        progressBar.setIndeterminate(true);
//        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
//        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

//        String text = "";
//        for (String result : matches)
//            text += result + "\n";

        returnedText.setText(matches.get(0));
        avatarSpeech.speak(matches.get(0).length() > 6);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}