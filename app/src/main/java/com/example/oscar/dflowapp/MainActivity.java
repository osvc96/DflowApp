package com.example.oscar.dflowapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements AIListener{

    private Button listenButton;
    private Button readButton;
    private EditText editText;
    private TextView resultTextView;
    private AIService aiService;
    private AIDataService aiDataService;
    private final String CLIENT_ACCESS_TOKEN = "0e9d80a4712e4de48ba4cdf8347d9b72";
    private static final int REQUEST = 200;
    private AIError aiError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        listenButton = (Button) findViewById(R.id.listenButton);
        readButton = (Button) findViewById(R.id.readButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        validateRecordingPermission();

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    private void validateRecordingPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST);
        }
    }

    public void voiceButtonOnClick(final View view){
        aiService.startListening();
    }

    public void textButtonOnClick(final View view) {

        final AIRequest aiRequest = new AIRequest();

        String query = editText.getText().toString();

        aiRequest.setQuery(query);



        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
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
        }.execute(aiRequest);

        //aiService.startListening();



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
}
