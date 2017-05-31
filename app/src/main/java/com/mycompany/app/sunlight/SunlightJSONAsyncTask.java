package com.mycompany.app.sunlight;

import java.util.HashMap;
import com.mycompany.app.econgress.MainActivity;
import com.mycompany.app.persistence.SQLite.AddressDAO;
import com.mycompany.app.persistence.SQLite.SQLAddress;

import android.os.AsyncTask;
import android.util.Log;


public class SunlightJSONAsyncTask extends AsyncTask<String, Void, HashMap<Integer, HashMap<String, String>>> {

	private MainActivity econgressActivity = null;
	private HashMap<Integer, HashMap<String, String>> legislators = null;

	private boolean isParsed = false;
	private boolean networkError = false;
    private boolean parseError = false;

	public SunlightJSONAsyncTask(MainActivity econgressActivity) {
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
		econgressActivity.sunlightIsParsed = this.isParsed;
		econgressActivity.sunlightNetworkError = this.networkError;
        econgressActivity.sunlightParseError = this.parseError;

		econgressActivity.setRepresentatives();
	}

    private void parse(String uri) {

        legislators = null;
        isParsed = false;
        networkError = false;
        parseError = false;

        String savedJson;
        String parsedJson;

        AddressDAO myAddressDAO = AddressDAO.getInstance(econgressActivity);
        SQLAddress mySQLAddress = myAddressDAO.getAddress();
        savedJson = mySQLAddress.getJSON();

        SunlightJSONParser jParser = new SunlightJSONParser();

        if (savedJson.toLowerCase().equals("no json")) {
            jParser.getJSONFromUrl(uri);
            Log.i("SunlightJSONAsyncTask", "parse: getJSONFromUrl");
        } else {
            jParser.getJSONFromString(savedJson);
            Log.i("SunlightJSONAsyncTask", "parse: getJSONFromString");
        }

        legislators = jParser.getLegislators();
        networkError = jParser.hasNetworkError();
        parseError = jParser.hasParseError();
        parsedJson = jParser.getJsonObject();

        mySQLAddress.setJSON(parsedJson);
        myAddressDAO.saveAddress(mySQLAddress);

        if (legislators != null) {
            Log.i("SunlightJSONAsyncTask", "parse: size: " + legislators.size());
            isParsed = true;
        }
    }
}
