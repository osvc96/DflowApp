package com.example.oscar.dflowapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;


public class OfficeLoad extends AppCompatActivity{

    private static final String TAG = "OfficeLoad";

    private Button mConnectButton;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private ProgressBar mConnectProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_load);

        // set up our views
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectProgressBar = (ProgressBar) findViewById(R.id.connectProgressBar);
        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.descriptionTextView);

        // add click listener
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectingInProgressUI();

                //check that client id and redirect have been configured
                if (!hasAzureConfiguration()) {
                    Toast.makeText(
                            OfficeLoad.this,
                            getString(R.string.warning_clientid_redirecturi_incorrect),
                            Toast.LENGTH_LONG).show();
                    resetUIForConnect();
                    return;
                }

                connect();
            }
        });

    }

    private void connect() {
        // define the post-auth callback
        AuthenticationCallback<String> callback =
                new AuthenticationCallback<String>() {

                    @Override
                    public void onSuccess(String idToken) {
                        String name = "";
                        String preferredUsername = "";
                        try {
                            // get the user info from the id token
                            IdToken claims = IdToken.parse(new GsonFactory(), idToken);
                            name = claims.getPayload().get("name").toString();
                            preferredUsername = claims.getPayload().get("preferred_username").toString();
                        } catch (IOException ioe) {
                            Log.e(TAG, ioe.getMessage());
                        } catch (NullPointerException npe) {
                            Log.e(TAG, npe.getMessage());

                        }

                        // Prepare the SendMailActivity intent
                        Intent sendMailActivity =
                                new Intent(OfficeLoad.this, SendMailActivity.class);

                        // take the user's info along
                        sendMailActivity.putExtra(SendMailActivity.ARG_GIVEN_NAME, name);
                        sendMailActivity.putExtra(SendMailActivity.ARG_DISPLAY_ID, preferredUsername);

                        // actually start the activity
                        startActivity(sendMailActivity);

                        resetUIForConnect();
                    }

                    @Override
                    public void onError(Exception exc) {
                        showConnectErrorUI();
                    }
                };

        AuthenticationManager mgr = AuthenticationManager.getInstance(this);
        mgr.connect(this, callback);
    }



    private static boolean hasAzureConfiguration() {
        try {
            UUID.fromString(Constants.CLIENT_ID);
            URI.create(Constants.REDIRECT_URI);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void resetUIForConnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectButton.setVisibility(View.VISIBLE);
                mTitleTextView.setVisibility(View.GONE);
                mDescriptionTextView.setVisibility(View.GONE);
                mConnectProgressBar.setVisibility(View.GONE);
            }
        });
    }


    private void showConnectingInProgressUI() {
        mConnectButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mConnectProgressBar.setVisibility(View.VISIBLE);
    }

    private void showConnectErrorUI() {
        mConnectButton.setVisibility(View.VISIBLE);
        mConnectProgressBar.setVisibility(View.GONE);
        mTitleTextView.setText(R.string.title_text_error);
        mTitleTextView.setVisibility(View.VISIBLE);
        mDescriptionTextView.setText(R.string.connect_text_error);
        mDescriptionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(
                OfficeLoad.this,
                R.string.connect_toast_text_error,
                Toast.LENGTH_LONG).show();
    }



}
