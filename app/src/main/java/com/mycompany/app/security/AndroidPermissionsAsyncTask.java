package com.mycompany.app.security;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.mycompany.app.econgress.MainActivity;

public class AndroidPermissionsAsyncTask extends AsyncTask<Void, Void, Void> {

    private MainActivity econgressActivity = null;

    private AccountManager mAccountManager = null;
    private Account[] mAccounts = null;

	public AndroidPermissionsAsyncTask(MainActivity econgressActivity) {
		this.econgressActivity = econgressActivity;
 	}

	@Override
	protected Void doInBackground(Void... params) {

        econgressActivity.noGoogleAccount = false;
        econgressActivity.googleAcountNoPermissions = false;

		if (ContextCompat.checkSelfPermission(econgressActivity, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(econgressActivity, new String[]{Manifest.permission.GET_ACCOUNTS}, 0);
		}
		return null;
	}


	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

        if (econgressActivity.checkCallingOrSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            econgressActivity.googleAcountNoPermissions = true;
            econgressActivity.setNavigationViewAccount();
            return;
        }

        mAccountManager = AccountManager.get(econgressActivity);
        mAccounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

        if (mAccounts.length == 0) {
            econgressActivity.noGoogleAccount = true;
            econgressActivity.setNavigationViewAccount();
            return;
        }
        econgressActivity.googleAccount = mAccounts[0].name;
        econgressActivity.setNavigationViewAccount();

	}

}
