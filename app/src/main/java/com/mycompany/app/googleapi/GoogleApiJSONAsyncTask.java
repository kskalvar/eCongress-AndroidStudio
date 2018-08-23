package com.mycompany.app.googleapi;

import android.os.AsyncTask;
import android.util.Log;

import com.mycompany.app.econgress.LegislatorObject;
import com.mycompany.app.econgress.MainActivity;
import com.mycompany.app.persistence.SQLite.AddressDAO;
import com.mycompany.app.persistence.SQLite.SQLAddress;

import java.util.HashMap;


public class GoogleApiJSONAsyncTask extends AsyncTask<String, Void, HashMap<Integer, HashMap<String, String>>> {

    private MainActivity econgressActivity;
	private HashMap<Integer, HashMap<String, String>> legislators = null;

	private boolean isParsed = false;
	private boolean networkError = false;
    private boolean parseError = false;
    private boolean invalidZip = false;

	public GoogleApiJSONAsyncTask(MainActivity econgressActivity) {
		this.econgressActivity = econgressActivity;
	}

	@Override
	protected HashMap<Integer, HashMap<String, String>> doInBackground(String... params) {

		parse(params[0]);
		return legislators;
	}

	@Override
	protected void onPostExecute(HashMap<Integer, HashMap<String, String>> result) {
		
		super.onPostExecute(result);

		LegislatorObject legislatorObject = new LegislatorObject();
		legislatorObject.setLegislators(result);
		MainActivity.setLegislatorObject(legislatorObject);

		econgressActivity.legislativeIsParsed = this.isParsed;
		econgressActivity.legislativeNetworkError = this.networkError;
        econgressActivity.legislativeParseError = this.parseError;
        econgressActivity.legislativeInvalidZip = this.invalidZip;

		econgressActivity.setRepresentatives();
	}

    private void parse(String uri) {

        legislators = null;
        isParsed = false;
        networkError = false;
        parseError = false;
        invalidZip = false;

        String savedJson;
        String parsedJson;

        AddressDAO myAddressDAO = AddressDAO.getInstance(econgressActivity);
        SQLAddress mySQLAddress = myAddressDAO.getAddress();
        savedJson = mySQLAddress.getJSON();

        GoogleApiJSONParser jParser = new GoogleApiJSONParser();

        if (savedJson.toLowerCase().equals("no json")) {
            jParser.getJSONFromUrl(uri);
            Log.i("GoogleApiJSONAsyncTask", "parse: getJSONFromUrl");
        } else {
            jParser.getJSONFromString(savedJson);
            Log.i("GoogleApiJSONAsyncTask", "parse: getJSONFromString");
        }

        legislators = jParser.getLegislators();

        networkError = jParser.hasNetworkError();
        parseError = jParser.hasParseError();
        parsedJson = jParser.getJsonObject();
        invalidZip = jParser.hasInvalidZip();

        mySQLAddress.setJSON(parsedJson);
        myAddressDAO.saveAddress(mySQLAddress);

        if (legislators != null) {
            Log.i("GoogleApiJSONAsyncTask", "parse: size: " + legislators.size());
            isParsed = true;
        }
    }
}
