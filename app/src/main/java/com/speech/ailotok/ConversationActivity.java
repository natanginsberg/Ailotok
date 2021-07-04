package com.speech.ailotok;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class ConversationActivity extends AppCompatActivity implements RecognitionListener {

    private static final String COMPUTER_GENERATED = "computerGenerated";
    private static final String USER_GENERATED = "user";

    private ImageView mic;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private ProgressBar progressBar;
    private AvatarSpeech avatarSpeech;
    private String topic;
    private TextToSpeech textToSpeech;
    private ProgressBar searchProgress;
    private String conversation = "";
    private boolean writeOverride = false;
    private TextView currentUserTextView;
    private TextView translationText;
    private boolean translationOpen = false;
    private View currentChild;
    private TextView wordToTranslate;
    private boolean beingTranslated = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        String userName = getIntent().getStringExtra("name");
        topic = getIntent().getStringExtra("topic");
        mic = findViewById(R.id.mic);
        searchProgress = findViewById(R.id.searching);
        wordToTranslate = findViewById(R.id.word_to_translate);
        translationText = findViewById(R.id.translation);

        initiateStt("en");

        new Thread(new Runnable() {
            @Override
            public void run() {

//                try {
                try {
                    String startingSentence = getInitialSentence(userName);
                    if (startingSentence != null) {
                        JSONObject jsonObject = new JSONObject(startingSentence);
                        String displayText = jsonObject.getString("display");
                        conversation = jsonObject.getString("conversation");
                        startAvatarSpeech(displayText);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addAIMessage(displayText);
                            }
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addAIMessage(e.getMessage());
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addAIMessage(String displayText) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.body);
        @SuppressLint("InflateParams") View child = getLayoutInflater().inflate(R.layout.ai_message, null);
        TextView textView = (TextView) child.findViewById(R.id.message);
        textView.setText(displayText);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 24, 0, 24);
        layout.addView(child, layoutParams);
//        layout.setLayoutParams(layoutParams);
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.body_scroll));
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void startAvatarSpeech(String startingSentence) {
        avatarSpeech = new AvatarSpeech(this, startingSentence, new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                onRmsChanged(0.56f);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDone(String s) {
                if (s.equals(USER_GENERATED)) {
                    runOnUiThread(() -> {
                        mic.setVisibility(View.VISIBLE);
                        searchProgress.setVisibility(View.INVISIBLE);
                        mic.setBackground(getDrawable(R.drawable.ic_mic));

                    });
                }
                startListening();
            }

            @Override
            public void onError(String s) {
                addUserMessage(s);
            }
        }, text -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                addAIMessage(text);
            }
        }));

        avatarSpeech.subscribeEndListener(new AvatarSpeech.EndListener() {
            @Override
            public void end() {
                speech.destroy();
                finish();
            }
        });
//        TextToSpeech textToSpeech = new TextToSpeech();
        textToSpeech = new TextToSpeech(
                getApplicationContext(),
                status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        if (textToSpeech != null)
                            textToSpeech.setLanguage(Locale.US);
                    } else {
                        textToSpeech = null;
                    }
                });
    }

    private void initiateStt(String language) {
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (language.equals("iw"))
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "iw-IL");
        else
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

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
        mic.setVisibility(View.VISIBLE);
        searchProgress.setVisibility(View.INVISIBLE);
        mic.setBackground(getDrawable(R.drawable.ic_mic_gray));

//        progressBar.setIndeterminate(true);
//        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
//        returnedText.setText(errorMessage);
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
        System.out.println("bug 44 " + "The results are in " + beingTranslated);
        if (beingTranslated) {
            beingTranslated = false;
            return;
        }
        speech.stopListening();
//                buttonTimer = null;
        continueWithSTT(results);
//
        Log.i(LOG_TAG, "onResults");
        // waiting for the user to press the button if he wants to.


    }

    private void continueWithSTT(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

//        String text = "";
//        for (String result : matches)
//            text += result + "\n";

//        returnedText.setText(matches.get(0));
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try {
                try {
                    if (translationOpen)
                        setHebrewWord(matches.get(0));
                    else
                        getAIResponse(matches.get(0), false);
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addAIMessage(e.getMessage());
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setHebrewWord(String word) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wordToTranslate.setText(word);
            }
        });
        translateWord(word);

    }

    private void translateWord(String word) {

        translate_api translate = new translate_api();
        translate.setOnTranslationCompleteListener(new translate_api.OnTranslationCompleteListener() {
            @Override
            public void onStartTranslation() {
                // here you can perform initial work before translated the text like displaying progress bar
            }

            @Override
            public void onCompleted(String text) {
                // "text" variable will give you the translated text
                translationText.setText(text);

                new CountDownTimer(3000, 1000) {

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        usePrompt(text);
                        closeTranslationWindow();
                        initiateStt("en");
                    }
                }.start();

            }

            @Override
            public void onError(Exception e) {

            }
        });
        translate.execute(word, "he", "en");
    }

    private void usePrompt(String word) {
        avatarSpeech.speak("Now, repeat what you wanted using ." + word, false);
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

    public void getResponse(View view) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String text = ((EditText) currentChild.findViewById(R.id.written_text)).getText().toString();
                    writeOverride = true;
                    getAIResponse(text, true);

//                    getSuggestion(text);

//                    sendHuffingpostRequest(text);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addAIMessage(e.getMessage());
                        }
                    });
                }
            }
        });
        thread.start();

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void getAIResponse(String text, boolean written) throws JSONException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (written && writeOverride)
                    changeUserMessage(text);
                else
                    addUserMessage(text);
                mic.setVisibility(View.INVISIBLE);
                searchProgress.setVisibility(View.VISIBLE);
            }
        });
        String returned = sendToGrammarCheck(text);
        if (returned == null || (!written && writeOverride))
            return;
        JSONObject jsonObject = new JSONObject(returned);
        writeOverride = false;
        String display = "";
        try {
            display = jsonObject.getString("display") + "\n" + jsonObject.getString("correction");
            conversation = jsonObject.getString("conversation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ;
        String finalDisplay = display;
        new Thread(new Runnable() {
            @Override
            public void run() {
                avatarSpeech.speak(finalDisplay, false);
            }
        }).start();
        runOnUiThread(() -> {
            mic.setVisibility(View.VISIBLE);
            searchProgress.setVisibility(View.INVISIBLE);

            addAIMessage(finalDisplay);
            mic.setBackground(getDrawable(R.drawable.ic_mic_gray));

        });
    }

    private void changeUserMessage(String text) {
        currentUserTextView.setText(text);
        restoreMessage();
    }

    private void addUserMessage(String text) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.body);
        currentChild = getLayoutInflater().inflate(R.layout.user_message, null);
        currentUserTextView = currentChild.findViewById(R.id.message);
        currentUserTextView.setText(text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 24, 0, 24);
        layout.addView(currentChild, layoutParams);
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.body_scroll));
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private String sendToGrammarCheck(String text) throws JSONException {
//         Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                this.getApplicationContext(), "us-east-1:165bf270-2865-4a1d-8836-487be260eabb", Regions.US_EAST_1);

        LambdaInvokerFactory factory = LambdaInvokerFactory.builder().credentialsProvider(cognitoProvider).region(Regions.US_EAST_2)
                .context(this.getApplicationContext()).clientConfiguration(new ClientConfiguration().withSocketTimeout(600000)).build();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("input", text);
//        if (conversation.length() > 300) {
//            conversation = reduceConversation(conversation);
//        }
        jsonObject.put("conversation", conversation);
        if (conversationIsLong())
            jsonObject.put("start", 2);
        else
            jsonObject.put("start", 3);
        jsonObject.put("userName", "yossi");
        jsonObject.put("topic", topic);

        final MyInterface myInterface = factory.build(MyInterface.class);
        try {
            return myInterface.grammarCheck(
                    jsonObject);

        } catch (LambdaFunctionException lfe) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addAIMessage(lfe.getDetails());
                }
            });
        }
        return null;
    }

    private boolean conversationIsLong() {
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {

            lastIndex = conversation.indexOf("Human:", lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += 6;
            }
        }
        return count > 1;
    }

    private String reduceConversation(String conv) {
        while (conv.length() > 300) {
            conv = conv.substring(conv.indexOf("Human:", 6));
        }
        return conv;
    }

    private String getInitialSentence(String userName) throws JSONException {
//         Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                this.getApplicationContext(), "us-east-1:165bf270-2865-4a1d-8836-487be260eabb", Regions.US_EAST_1);

        LambdaInvokerFactory factory = LambdaInvokerFactory.builder().credentialsProvider(cognitoProvider).region(Regions.US_EAST_2)
                .context(this.getApplicationContext()).clientConfiguration(new ClientConfiguration().withSocketTimeout(60000)).build();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start", 1);
        jsonObject.put("userName", "yossi");
        jsonObject.put("topic", topic);

        final MyInterface myInterface = factory.build(MyInterface.class);
        try {
            return myInterface.grammarCheck(
                    jsonObject);

        } catch (LambdaFunctionException lfe) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addAIMessage(lfe.getDetails());
                }
            });
        }
        return null;
    }


    public void openTranslate(View view) {
        if (translationOpen) {
            translationOpen = false;
            findViewById(R.id.translation_window).setVisibility(View.INVISIBLE);
        } else {
            speech.stopListening();
            beingTranslated = true;
            translationOpen = true;
            initiateStt("iw");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    avatarSpeech.speak("What word do you need help with?", true);
                    findViewById(R.id.translation_window).setVisibility(View.VISIBLE);
                    beingTranslated = false;
                }
            }, 1000);

        }
    }

    private void startTranslation() {
        startListening();

    }

    private void startListening() {
        runOnUiThread(new Runnable() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void run() {
                mic.setBackground(getDrawable(R.drawable.ic_mic));
            }
        });
        searchProgress.post(new Runnable() {
            @Override
            public void run() {
                speech.startListening(recognizerIntent);
            }
        });
    }

    private void closeTranslationWindow() {
        translationOpen = false;
        findViewById(R.id.translation_window).setVisibility(View.INVISIBLE);
        translationText.setText("");
        wordToTranslate.setText("");
    }

    public void openEdit(View view) {
        writeOverride = true;
        currentChild.findViewById(R.id.enter_button).setVisibility(View.VISIBLE);
        currentChild.findViewById(R.id.message).setVisibility(View.INVISIBLE);
        currentChild.findViewById(R.id.written_text).setVisibility(View.VISIBLE);
        currentChild.findViewById(R.id.edit_icon).setVisibility(View.INVISIBLE);
        ((EditText) currentChild.findViewById(R.id.written_text)).setText(currentUserTextView.getText());
    }

    private void restoreMessage() {
        currentChild.findViewById(R.id.enter_button).setVisibility(View.INVISIBLE);
        currentChild.findViewById(R.id.message).setVisibility(View.VISIBLE);
        currentChild.findViewById(R.id.written_text).setVisibility(View.INVISIBLE);
        currentChild.findViewById(R.id.edit_icon).setVisibility(View.VISIBLE);
    }

    public void startListening(View view) {
        startListening();
    }
}