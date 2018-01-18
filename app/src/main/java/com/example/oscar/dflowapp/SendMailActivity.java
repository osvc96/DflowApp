package com.example.oscar.dflowapp;

/**
 * Created by juanjose on 1/17/18.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.JsonElement;
import java.util.Map;

/**
 * This activity handles the send mail operation of the app.
 * The app must be connected to Office 365 before this activity can send an email.
 * It also uses the MSGraphAPIController to send the message.
 */
public class SendMailActivity extends AppCompatActivity {

    // arguments for this activity
    public static final String ARG_GIVEN_NAME = "givenName";
    public static final String ARG_DISPLAY_ID = "displayableId";

    // views
    private EditText mEmailEditText;
    private ImageButton mVoiceButton;
    private ProgressBar mSendMailProgressBar;
    private String mGivenName;
    private TextView mConclusionTextView;
    private TextView mDescriptionTextView;

    private AIService aiService;
    private AIDataService aiDataService;
    private final String CLIENT_ACCESS_TOKEN = "87eebff0f9a74d628bb9fdba33a13ce1";
    private static final int REQUEST = 200;
    private AIError aiError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        // find the views
        TextView mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mEmailEditText = (EditText) findViewById(R.id.emailEditText);
        mVoiceButton = (ImageButton) findViewById(R.id.voiceButton);
        mSendMailProgressBar = (ProgressBar) findViewById(R.id.sendMailProgressBar);
        mConclusionTextView = (TextView) findViewById(R.id.conclusionTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.descriptionTextView);

        // Extract the givenName and displayableId and use it in the UI.
        mGivenName = getIntent().getStringExtra(ARG_GIVEN_NAME);
        mTitleTextView.append(mGivenName + "!");
        mEmailEditText.setText(getIntent().getStringExtra(ARG_DISPLAY_ID));

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiDataService = new AIDataService(this, config);

        aiService = AIService.getService(this, config);
        //aiService.setListener(this);
    }

    /**
     * Handler for the onclick event of the send mail button. It uses the MSGraphAPIController to
     * send an email. When the call is completed, the call will return to either the success()
     * or failure() methods in this class which will then take the next steps on the UI.
     * This method sends the email using the address stored in the mEmailEditText view.
     * The subject and body of the message is stored in the strings.xml file.
     *
     * @param v The view.
     */
    public void onVoiceButtonClick(View v) {

      final AIRequest aiRequest = new AIRequest();
      String query = mEmailEditText.getText().toString();
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

    }

    public void sendEmail() {
        resetUIForSendMail();

        //Prepare body message and insert name of sender
        String body = getString(R.string.mail_body_text);
        body = body.replace("{0}", mGivenName);

        Call<Void> result = new MSGraphAPIController(this).sendMail(
                getString(R.string.destination),
                getString(R.string.mail_subject_text),
                body);

        result.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) {
                    showSendMailSuccessUI();
                } else {
                    showSendMailErrorUI();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                showSendMailErrorUI();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disconnectMenuitem:
                AuthenticationManager.getInstance(this).disconnect();
                Intent connectIntent = new Intent(this, OfficeLoad.class);
                startActivity(connectIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetUIForSendMail() {
        mConclusionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.VISIBLE);
        mVoiceButton.setVisibility(View.GONE);
    }

    private void showSendMailSuccessUI() {
        mDescriptionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.GONE);
        mConclusionTextView.setText(R.string.conclusion_text);
        mConclusionTextView.setVisibility(View.VISIBLE);
        mVoiceButton.setVisibility(View.VISIBLE);
        Toast.makeText(
                SendMailActivity.this,
                R.string.send_mail_toast_text,
                Toast.LENGTH_SHORT).show();
    }

    private void showSendMailErrorUI() {
        mDescriptionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.GONE);
        mConclusionTextView.setText(R.string.sendmail_text_error);
        mConclusionTextView.setVisibility(View.VISIBLE);
        mVoiceButton.setVisibility(View.VISIBLE);
        Toast.makeText(
                SendMailActivity.this,
                R.string.send_mail_toast_text_error,
                Toast.LENGTH_LONG).show();
    }

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
        //mDescriptionTextView.setText(message);
        if (!res.isActionIncomplete() && !res.getAction().equals("input.unknown")){
            sendEmail();
        }else{
            mDescriptionTextView.setVisibility(View.GONE);
            mSendMailProgressBar.setVisibility(View.GONE);
            mConclusionTextView.setVisibility(View.VISIBLE);
            mConclusionTextView.setText(message);
            mVoiceButton.setVisibility(View.VISIBLE);
        }

    }

    public void onError(AIError error) {
        mDescriptionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.GONE);
        mConclusionTextView.setVisibility(View.VISIBLE);
        mConclusionTextView.setText(error.toString());
        mVoiceButton.setVisibility(View.VISIBLE);
    }


}
