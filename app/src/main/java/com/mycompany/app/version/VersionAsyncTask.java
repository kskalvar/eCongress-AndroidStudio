package com.mycompany.app.version;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mycompany.app.econgress.MainActivity;
import com.mycompany.app.econgress.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by kskalvar on 9/19/2016.
 */

public class VersionAsyncTask extends AsyncTask<String, Void, Void> {

    private MainActivity econgressActivity = null;
    private String curVersion = null;
    private String storeVersion = null;
    private Document doc = null;

    public VersionAsyncTask(MainActivity econgressActivity) {
        this.econgressActivity = econgressActivity;
    }

    @Override
    protected Void doInBackground(String... params) {

        String playStoreUrl = econgressActivity.getString(R.string.storeversion_url) + econgressActivity.getPackageName();

        try {
            doc = Jsoup.connect(String.valueOf(playStoreUrl)).get();
            Elements itemprop = doc.select("itemprop");
            if (! itemprop.isEmpty()) {
                storeVersion = doc.getElementsByAttributeValue("itemprop","softwareVersion").first().text();
            }
        } catch (IOException e) {
            storeVersion = null;
            Log.i("VersionAsyncTask", e.getMessage());
        }

        try {
            curVersion = econgressActivity.getPackageManager().getPackageInfo(econgressActivity.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            curVersion = null;
            Log.i("VersionAsyncTask", e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (curVersion == null || storeVersion == null) return;

        if (isInteger(curVersion)  && isInteger(storeVersion)) {
            if (Integer.parseInt(storeVersion) > Integer.parseInt(curVersion)) {
                Toast.makeText(econgressActivity.getApplicationContext(), "Updated eCongress Version " + storeVersion + " Available in AppStore" , Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            Log.i("VersionAsyncTask", e.getMessage());
            return false;
        } catch(NullPointerException e) {
            Log.i("VersionAsyncTask", e.getMessage());
            return false;
        }
        return true;
    }
}
