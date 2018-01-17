package com.example.oscar.dflowapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import android.os.Bundle;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AIListener, OnInitListener{

    private EditText editText;
    private TextView resultTextView;

    private final String CLIENT_ACCESS_TOKEN = "0e9d80a4712e4de48ba4cdf8347d9b72";
    private static final int REQUEST = 200;

    private AIService aiService;
    private AIDataService aiDataService;
    private AIError aiError;
    private Button loadOfficeMain;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        listenButton = (Button) findViewById(R.id.listenButton);
        readButton = (Button) findViewById(R.id.readButton);
        loadOfficeMain = (Button) findViewById(R.id.officeMain);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        validateRecordingPermission();

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        tts = new TextToSpeech(this, this);
    }


    private void validateRecordingPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private AsyncTask<AIRequest, Void, AIResponse> sendRequestToDialogflow = new AsyncTask<AIRequest, Void, AIResponse>() {
        @Override
        protected AIResponse doInBackground(AIRequest... requests) {
            final AIRequest request = requests[0];
            try {
                final AIResponse response = aiDataService.request(requests[0]);
                return response;
            } catch (AIServiceException e) {
                aiError = new AIError(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            if (aiResponse != null) {
                onResult(aiResponse);
            }
            else{
                onError(aiError);
            }
        }
    };
    public void officeMainButtonOnClick(final View view){
        Intent myIntent = new Intent(MainActivity.this, OfficeLoad.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void voiceButtonOnClick(final View view){
        aiService.startListening();
    }

    public void textButtonOnClick(final View view) {
        final AIRequest aiRequest = new AIRequest();
        String query = editText.getText().toString();
        aiRequest.setQuery(query);
        sendRequestToDialogflow.execute(aiRequest);
    }

    public void voiceButtonOnClick(final View view){
        aiService.startListening();
    }

    @Override
    public void onResult(AIResponse result) {
        Result res = result.getResult();

        // Get parameters
        String parameterString = "";
        if (res.getParameters() != null && !res.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : res.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        String message = "Query:" + res.getResolvedQuery() +
                "\nAction: " + res.getAction() +
                "\nParameters: " + parameterString +
                "\nSpeech: " + res.getFulfillment().getSpeech();
        // Show results in TextView.
        resultTextView.setText(message);
        speakOut(res.getFulfillment().getSpeech());
    }

    @Override
    public void onError(AIError error) {
        resultTextView.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            // tts.setPitch(5); // set pitch level
            // tts.setSpeechRate(2); // set speech speed rate
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed");
        }
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

}
