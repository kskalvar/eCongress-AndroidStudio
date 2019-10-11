package com.mycompany.app.googleapi;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class GoogleApiJSONParser {

	private HashMap<String, String> legislator = null;

	// private HashMap<Integer, HashMap<String, String>> legislators = new HashMap<>();
    private SparseArray<HashMap<String, String>> legislators = new SparseArray<>();

	private boolean networkError = false;
	private boolean parseError = false;
	private boolean invalidZip = false;
	private String jsonObject = "no json";

	GoogleApiJSONParser() {
	}

    void getJSONFromString(String json) {

		parseError = false;

        try {

            JSONObject object = new JSONObject(json);
            buildLegislatorHashmap(object);
            jsonObject = object.toString();

        } catch (JSONException e) {
            Log.i("GoogleApiJSONParser", "JSONException " + e.getMessage());
            parseError = true;
        }
    }

	void getJSONFromUrl(String url) {

        networkError = false;
        parseError = false;
        invalidZip = false;
        String line;

		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			if (con.getResponseCode() == 404) {
				Log.i("GoogleApiJSONParser", "HttpURLConnection " + con.getResponseCode());
				networkError = true;
				return;
			}

            StringBuilder sb = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            JSONObject object = new JSONObject(sb.toString());
            buildLegislatorHashmap(object);
            jsonObject = object.toString();

		} catch (IOException e) {
			Log.i("GoogleApiJSONParser", "IOException " + e.getMessage());
            invalidZip = true;
		} catch (JSONException e) {
			Log.i("GoogleApiJSONParser", "JSONException " + e.getMessage());
			parseError = true;
		}
	}

	private void buildLegislatorHashmap(JSONObject object) {

	    parseError = false;

        try {

            JSONArray offices = object.getJSONArray("offices");
            int officesLength = offices.length();

            int legislatorCount = 0;
            for (int i = 0; i < officesLength; i++) {

                String s;

                JSONObject office = offices.getJSONObject(i);
                JSONArray officesOfficialIndices = office.getJSONArray("officialIndices");

                String rank = office.getString("name");

                if (rank.equals("President of the United States")) {
                    rank = "President";
                } else if (rank.equals("Vice President of the United States")) {
                    continue;
                } else if (rank.equals("United States Senate")) {
                    rank = "US Sen";
                } else if (rank.contains("United States House of Representatives")) {
                    rank = "US Rep";
                }

                for (int j = 0; j < officesOfficialIndices.length(); j++) {

                    legislator = new HashMap<>();

                    int memberIndex = Integer.parseInt(officesOfficialIndices.getString(j));

                    JSONArray officials = object.getJSONArray("officials");
                    JSONObject member = officials.getJSONObject(memberIndex);

                    legislator.put("chamber", rank);
                    legislator.put("last_name", member.getString("name"));

                    // Google changed their structure, yeah no notice!
                    if (member.isNull("channels")) {
                        legislator.put("website", "null");
                    } else {
                        JSONArray urls = member.getJSONArray("channels");
                        JSONObject twitter = urls.getJSONObject(1);
                        legislator.put("website", "http://twitter.com/" + twitter.get("id"));
                    }

                    s = member.getString("party");
                    if ( s.contains("Democratic")) {
                        s = "(D)";
                    } else {
                        s = "(R)";
                    }
                    legislator.put("party", s);

                    legislator.put("oc_email", "null");
                    legislator.put("sendMail", "false");

                    JSONArray phones = member.getJSONArray("phones");
                    s = phones.getString(0);
                    legislator.put("phone", s);

                    legislators.put(legislatorCount, legislator);
                    legislatorCount = legislatorCount + 1;

                }
            }

        } catch (JSONException e) {
            Log.i("GoogleApiJSONParser", "JSONException " + e.getMessage());
            parseError = true;
        }
    }

	SparseArray<HashMap<String, String>> getLegislators() {
		return legislators;
		
	}
	boolean hasNetworkError() { return networkError; }
	boolean hasParseError() { return parseError; }
	String getJsonObject() { return jsonObject; }
    boolean hasInvalidZip() { return invalidZip; }
}
