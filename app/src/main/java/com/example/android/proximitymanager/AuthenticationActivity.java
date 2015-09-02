package com.example.android.proximitymanager;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.android.proximitymanager.api.ProximityApi;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;


public class AuthenticationActivity extends AppCompatActivity {
    private static final String TAG =
            AuthenticationActivity.class.getSimpleName();

    private static final int REQUEST_PICK_ACCOUNT = 42;
    private static final int REQUEST_ERROR_RECOVER = 43;

    private static final String SCOPE_PROXIMITY =
            "oauth2:https://www.googleapis.com/auth/userlocation.beacon.registry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                GetTokenTask task = new GetTokenTask();
                task.execute(email);
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Utils.showToast(this, "You have to select an account");
            }
        }
        // Handle the result from exceptions
        if (requestCode == REQUEST_ERROR_RECOVER && resultCode == RESULT_OK) {

        }
    }

    public void onAccountClick(View v) {
        pickUserAccount();
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, true, null, null, null, null);
        startActivityForResult(intent, REQUEST_PICK_ACCOUNT);
    }

    /* Background Token Fetch */

    private class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return fetchToken(params[0]);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                ProximityApi.getInstance(AuthenticationActivity.this)
                        .setAuthToken(result);
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private String fetchToken(String email) throws IOException {
        try {
            return GoogleAuthUtil.getToken(this, email, SCOPE_PROXIMITY);
        } catch (UserRecoverableAuthException e) {
            handleException(e);
        } catch (GoogleAuthException e) {
            Log.w(TAG, "Fatal Exception", e);
        }

        return null;
    }

    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException) e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            AuthenticationActivity.this,
                            REQUEST_ERROR_RECOVER);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_ERROR_RECOVER);
                }
            }
        });
    }
}
