package com.speech.ailotok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.speech.ailotok.model.Topic;
import com.amazonaws.mobileconnectors.lambdainvoker.*;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;

public class MainActivity extends AppCompatActivity implements MainUI.MainUIListener {


    private static final int AUDIO_CODE = 100;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promo);
        setTimerToStartMainBackground();

//        // Create an instance of CognitoCachingCredentialsProvider
//        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
//                this.getApplicationContext(), "identity-pool-id", Regions.US_WEST_2);
//
//// Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
//        LambdaInvokerFactory factory = new LambdaInvokerFactory(this.getApplicationContext(),
//                Regions.US_WEST_2, cognitoProvider);
//
//// Create the Lambda proxy object with a default Json data binder.
//// You can provide your own data binder by implementing
//// LambdaDataBinder.
//        final MyInterface myInterface = factory.build(MyInterface.class);

//        RequestClass request = new RequestClass("John", "Doe");
// The Lambda function invocation results in a network call.
// Make sure it is not called from the main thread.
//        new AsyncTask<RequestClass, Void, ResponseClass>() {
//            @Override
//            protected ResponseClass doInBackground(RequestClass... params) {
//                // invoke "echo" method. In case it fails, it will throw a
//                // LambdaFunctionException.
//                try {
//                    return myInterface.AndroidBackendLambdaFunction(params[0]);
//                } catch (LambdaFunctionException lfe) {
//                    Log.e("Tag", "Failed to invoke echo", lfe);
//                    return null;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(ResponseClass result) {
//                if (result == null) {
//                    return;
//                }
//
//                // Do a toast
//                Toast.makeText(MainActivity.this, result.getGreetings(), Toast.LENGTH_LONG).show();
//            }
//        }.execute(request);

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