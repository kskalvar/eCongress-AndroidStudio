package com.mycompany.app.oauth;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.mycompany.app.econgress.MainActivity;
import com.mycompany.app.econgress.R;

public class GoogleOauthAsyncTask extends AsyncTask<Void, Void, Void> {

	private MainActivity econgressActivity = null;

	private AccountManager mAccountManager = null;
	private Account[] mAccounts = null;

    private String token = null;
    private boolean jsonNetworkError = false;
    private boolean jsonValidToken = false;
    private boolean jsonParseError = false;
    private boolean onTokenAcquired = false;

    public GoogleOauthAsyncTask(MainActivity econgressActivity) {
		this.econgressActivity = econgressActivity;
        onTokenAcquired = false;
	}

	@Override
	protected Void doInBackground(Void... params) {

		mAccountManager = AccountManager.get(econgressActivity);
		mAccounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

		if (mAccounts.length == 0) {
			return null;
		}

		getToken();

		for (int i = 0; i < 10 && !onTokenAcquired ; i++ ) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!onTokenAcquired) {
            return null;
        }

        OauthJSONParser jParser = new OauthJSONParser();
		String jsonString = econgressActivity.getString(R.string.oauth_url) + token;
		jParser.getJSONFromUrl(jsonString);

        jsonNetworkError = jParser.hasNetworkError();
        jsonParseError = jParser.hasParseError();
        jsonValidToken = jParser.hasValidToken();

        return null;
	}

	private void getToken() {
		mAccountManager.getAuthToken(mAccounts[0], "oauth2:https://mail.google.com/", null, econgressActivity, new OnTokenAcquired(), null);
	}

	private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

		@Override
		public void run(AccountManagerFuture<Bundle> result) {

			try {
				Bundle bundle = result.getResult();

                token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                mAccountManager.invalidateAuthToken("com.google", token);

                mAccountManager.getAuthToken(mAccounts[0], "oauth2:https://mail.google.com/", null, econgressActivity, null, null);
                bundle = result.getResult();
                token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

                onTokenAcquired = true;
			} catch (Exception e) {
                onTokenAcquired = false;
			}
        }
	}

	@Override
	protected void onPostExecute(Void result) {

		super.onPostExecute(result);
        econgressActivity.oauthOnTokenAcquired = this.onTokenAcquired;
        econgressActivity.oauthJsonValidToken = this.jsonValidToken;
		econgressActivity.oauthJsonNetworkError = this.jsonNetworkError;
		econgressActivity.oauthJsonParseError = this.jsonParseError;
		econgressActivity.oauthToken = this.token;
	}

}
