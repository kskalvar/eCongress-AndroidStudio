package com.mycompany.app.smtp;

import com.mycompany.app.econgress.MainActivity;

import android.os.AsyncTask;

public class MailAsyncTask extends AsyncTask<String, Void, Void> {

	private MainActivity econgressActivity = null;


	public MailAsyncTask(MainActivity econgressActivity) {
		this.econgressActivity = econgressActivity;
	}

	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 * 
	 * params[0] emailAddress
	 * params[1] googleAccount
	 * params[2] subject
	 * params[3] message
	 * params[4] oauthToken
	 * 
	 */

	protected Void doInBackground(String... params) {

		econgressActivity.mailSent = false;
				
		GMailOauthSender gmail = new GMailOauthSender();
		econgressActivity.mailSent = gmail.sendMail(params[2], params[3], params[1], params[4], params[0]);
		econgressActivity.mailMessage = gmail.getMessage();
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		econgressActivity.mailStatusNotification();
	}
}
