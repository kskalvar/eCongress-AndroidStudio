package com.mycompany.app.oauth;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

class OauthJSONParser {

	private boolean networkError = false;
	private boolean parseError = false;
    private boolean tokenValid = false;

    OauthJSONParser() {
	}

	void getJSONFromUrl(String urlStr) {

		networkError = false;
        parseError = false;
        tokenValid = true;

        URL url;
        try {
            url = new URL(urlStr);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            SSLContext sc;
            sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());
            con.setSSLSocketFactory(sc.getSocketFactory());
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.connect();

            if (con.getResponseCode() != 200) {
                tokenValid = false;
                return;
            }

            InputStream is = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String result = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result += inputLine;
            }

            JSONObject object = new JSONObject(result);
            tokenValid = !object.isNull("access_type");

        } catch (MalformedURLException e) {
            Log.i("OauthJSONParser", "getJSONFromUrl: MalformedURLException " + e.getMessage());
            networkError = true;
        } catch (IOException e) {
            Log.i("OauthJSONParser", "getJSONFromUrl: IOException " + e.getMessage());
            networkError = true;
        } catch (NoSuchAlgorithmException e) {
            Log.i("OauthJSONParser", "getJSONFromUrl: NoSuchAlgorithmException " + e.getMessage());
        } catch (KeyManagementException e) {
            networkError = true;
            Log.i("OauthJSONParser", "getJSONFromUrl: KeyManagementException " + e.getMessage());
        } catch (JSONException e) {
            parseError = true;
            Log.i("OauthJSONParser", "getJSONFromUrl: JSONException " + e.getMessage());
        }
    }

	boolean hasNetworkError() {
		return networkError;
	}
	boolean hasParseError() {
        return parseError;
	}
	boolean hasValidToken() {
        return tokenValid;
    }
}
