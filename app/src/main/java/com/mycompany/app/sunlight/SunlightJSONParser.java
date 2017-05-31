package com.mycompany.app.sunlight;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class SunlightJSONParser {

	private HashMap<String, String> legislator = null;
	private HashMap<Integer, HashMap<String, String>> legislators = new HashMap<Integer, HashMap<String, String>>();
	private boolean networkError = false;
	private boolean parseError = false;
	private String jsonObject = "no json";

	SunlightJSONParser() {
	}

    void getJSONFromString(String json) {

        networkError = false;
		parseError = false;

        try {

            JSONObject object = new JSONObject(json);
            JSONArray members = object.getJSONArray("results");
            String count = object.getString("count");

            for (int i = 0; i < Integer.parseInt(count); i++) {

                JSONObject congressman = members.getJSONObject(i);

                legislator = new HashMap<>();
                legislator.put("chamber", congressman.getString("chamber"));
                legislator.put("last_name", congressman.getString("last_name"));
                legislator.put("phone", congressman.getString("phone"));
                legislator.put("website", congressman.getString("website"));

                // opencongress changed email domain to emailcongress.us, until api
                // is updated, manually change domain

                // 2017-01-10 oc_email appears to have been omitted
                boolean noEmail = congressman.isNull("oc_email");

                if (noEmail) {
                    legislator.put("oc_email", "null");
                } else {
                    String ocEmail = congressman.getString("oc_email");
                    ocEmail = ocEmail.replace("opencongress.org", "emailcongress.us");
                    legislator.put("oc_email", ocEmail);
                }

                legislator.put("party", congressman.getString("party"));
                legislators.put(i, legislator);
            }

            jsonObject = object.toString();

        } catch (JSONException e) {
            Log.i("SunlightJSONParser", "JSONException " + e.getMessage());
            parseError = true;
        }
    }

	void getJSONFromUrl(String url) {

		networkError = false;

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			if (con.getResponseCode() != 200) {
				Log.i("SunlightJSONParser", "HttpURLConnection " + con.getResponseCode());
				networkError = true;
				return;
			}

			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			JSONObject object = (JSONObject) new JSONTokener(rd.readLine()).nextValue();
			JSONArray members = object.getJSONArray("results");
			String count = object.getString("count");

			for (int i = 0; i < Integer.parseInt(count); i++) {

				JSONObject congressman = members.getJSONObject(i);

				legislator = new HashMap<>();
				legislator.put("chamber", congressman.getString("chamber"));
				legislator.put("last_name", congressman.getString("last_name"));
				legislator.put("phone", congressman.getString("phone"));
				legislator.put("website", congressman.getString("website"));

				// opencongress changed email domain to emailcongress.us, until api
				// is updated, manually change domain

                // 2017-01-10 oc_email appears to have been omitted

                boolean noEmail = congressman.isNull("oc_email");

                if (noEmail) {
                    legislator.put("oc_email", "null");
                } else {
                    String ocEmail = congressman.getString("oc_email");
                    ocEmail = ocEmail.replace("opencongress.org", "emailcongress.us");
                    legislator.put("oc_email", ocEmail);
                }

				legislator.put("party", congressman.getString("party"));
				legislators.put(i, legislator);
			}

            jsonObject = object.toString();

		} catch (IOException e) {
			Log.i("SunlightJSONParser", "IOException " + e.getMessage());
			networkError = true;
		} catch (JSONException e) {
			Log.i("SunlightJSONParser", "JSONException " + e.getMessage());
			parseError = true;
		}
	}

	HashMap<Integer, HashMap<String, String>> getLegislators() {
		return legislators;
		
	}
	boolean hasNetworkError() {
		return networkError;
	}
	boolean hasParseError() { return parseError; }
	String getJsonObject() {
        return jsonObject;
    }
}
