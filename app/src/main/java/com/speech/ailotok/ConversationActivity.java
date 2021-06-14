package com.speech.ailotok;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.speech.ailotok.model.Topic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


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
    private List<String> pastUserInputs = new ArrayList<>();
    private List<String> generatedResponses = new ArrayList<>();
    private String input = "";
    private TextToSpeech textToSpeech;
    private ProgressBar searchProgress;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        userName = getIntent().getStringExtra("name");
        pastUserInputs.add("Hello my name is " + userName + ".");
        input = "Hello " + userName + " how are you doing?";
        generatedResponses.add(input);
        mic = findViewById(R.id.mic);
        returnedText = findViewById(R.id.text_displayed);
        searchProgress = findViewById(R.id.searching);
        returnedText.setText("Hello " + userName + " how are you doing?");
//        Vector<FlowNode> flow = FlowParser.createFlow(userName, getIntent().getStringExtra("topic"));
        initiateStt();
        avatarSpeech = new AvatarSpeech(this, input, new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                onRmsChanged(0.56f);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onDone(String s) {
                if (s.equals(QUESTION)) {
                    runOnUiThread(() -> {
                        mic.setVisibility(View.VISIBLE);
                        searchProgress.setVisibility(View.INVISIBLE);
                        mic.setBackground(getDrawable(R.drawable.ic_mic));

                    });
                    returnedText.post(new Runnable() {
                        @Override
                        public void run() {
                            speech.startListening(recognizerIntent);
                        }
                    });
                } else {
//                    avatarSpeech.speak(true);
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

    //
//    }
//
    private void initiateStt() {
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
        speech.stopListening();
        Log.i(LOG_TAG, "onResults");
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
                    getAIResponse(matches.get(0));
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            returnedText.setText(e.getMessage());
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
//                } catch (JSONException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            returnedText.setText(e.getMessage());
//                        }
//                    });
//                    e.printStackTrace();
    }
//                avatarSpeech.speak(matches.get(0).length() > 6);
//                huffSpeech.answerQuestion(matches.get(0));
//            }
//        });

//    }

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
                    String text = ((EditText) findViewById(R.id.written_text)).getText().toString();
                    getAIResponse(text);

//                    getSuggestion(text);

//                    sendHuffingpostRequest(text);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            returnedText.setText(e.getMessage());
                        }
                    });
                }
            }
        });
        thread.start();

    }

    private void getAIResponse(String text) throws JSONException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                returnedText.setText(text);
                mic.setVisibility(View.INVISIBLE);
                searchProgress.setVisibility(View.VISIBLE);
            }
        });
        Log.e("bug 98", input + " human:" + text);
        String returned = sendToGrammarCheck("AI: " + input + "\nHuman: " + text + "\n");
        Log.e("bug 98", "returned text is " + returned);
        if (returned != null)
            input = returned;
        if (returned != null) {
            generatedResponses.add(input);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    avatarSpeech.speak(input);
                }
            }).start();
            runOnUiThread(new Runnable() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void run() {
                    mic.setVisibility(View.VISIBLE);
                    searchProgress.setVisibility(View.INVISIBLE);
                    returnedText.setText(input.substring(3));
                    mic.setBackground(getDrawable(R.drawable.ic_mic_gray));

                }
            });
        }
    }

    private String sendToGrammarCheck(String text) throws JSONException {
//        sendPost();
//         Create an instance of CognitoCachingCredentialsProvider
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
                this.getApplicationContext(), "us-east-1:165bf270-2865-4a1d-8836-487be260eabb", Regions.US_EAST_1);

// Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
        LambdaInvokerFactory factory = LambdaInvokerFactory.builder().credentialsProvider(cognitoProvider).region(Regions.US_EAST_2)
                .context(this.getApplicationContext()).build();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("inputs", text);
        jsonObject.put("generated_responses", generatedResponses);
        System.out.println("this is the json object" + jsonObject.toString());
        Log.e("bug 98", jsonObject.toString());


        final MyInterface myInterface = factory.build(MyInterface.class);
        try {
            return myInterface.grammarCheck(
                    jsonObject);

        } catch (LambdaFunctionException lfe) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    returnedText.setText(lfe.getDetails());
                }
            });
            Log.e("Tag", "Failed to invoke echo", lfe);

        }
        return null;
    }
//
    private void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(" https://xt7j95brm7.execute-api.eu-central-1.amazonaws.com/dev/ask");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

//                    JSONObject jsonParam = new JSONObject();
//                    jsonParam.put("context", "We introduce a new language representation model called BERT, which stands for idirectional Encoder Representations from Transformers. Unlike recent language epresentation models (Peters et al., 2018a; Radford et al., 2018), BERT is designed to pretrain deep bidirectional representations from unlabeled text by jointly conditioning on both left and right context in all layers. As a result, the pre-trained BERT model can be finetuned with just one additional output layer to create state-of-the-art models for a wide range of tasks, such as question answering and language inference, without substantial taskspecific architecture modifications. BERT is conceptually simple and empirically powerful. It obtains new state-of-the-art results on eleven natural language processing tasks, including pushing the GLUE score to 80.5% (7.7% point absolute improvement), MultiNLI accuracy to 86.7% (4.6% absolute improvement), SQuAD v1.1 question answering Test F1 to 93.2 (1.5 point absolute improvement) and SQuAD v2.0 Test F1 to 83.1 (5.1 point absolute improvement).");
//
//                    jsonParam.put("question", "What is BERTs best score on Squadv2 ?");
                    String inputString = "{\n" +
                            "\t\"context\": \"We introduce a new language representation model called BERT, which stands for idirectional Encoder Representations from Transformers. Unlike recent language epresentation models (Peters et al., 2018a; Radford et al., 2018), BERT is designed to pretrain deep bidirectional representations from unlabeled text by jointly conditioning on both left and right context in all layers. As a result, the pre-trained BERT model can be finetuned with just one additional output layer to create state-of-the-art models for a wide range of tasks, such as question answering and language inference, without substantial taskspecific architecture modifications. BERT is conceptually simple and empirically powerful. It obtains new state-of-the-art results on eleven natural language processing tasks, including pushing the GLUE score to 80.5% (7.7% point absolute improvement), MultiNLI accuracy to 86.7% (4.6% absolute improvement), SQuAD v1.1 question answering Test F1 to 93.2 (1.5 point absolute improvement) and SQuAD v2.0 Test F1 to 83.1 (5.1 point absolute improvement).\",\n" +
                            "\t\"question\": \"What is BERTs best score on Squadv2 ?\"\n" +
                            "}";
                    OutputStream os = conn.getOutputStream();
                    byte[] input = inputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
//                        suggestions.addAll(responseLine["suggestions"])''
//            JSONObject r = new JSONObject(responseLine);
//            JSONArray s = r.getJSONArray("suggestions");
                        response.append(responseLine);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            returnedText.setText(response.toString());
                        }
                    });

//                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
//                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG", conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }
}