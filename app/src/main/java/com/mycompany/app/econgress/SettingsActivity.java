package com.mycompany.app.econgress;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mycompany.app.persistence.SQLite.AddressDAO;
import com.mycompany.app.persistence.SQLite.SQLAddress;

public class SettingsActivity extends AppCompatActivity {

	private EditText edittext1; // Telephone
	private EditText edittext3; // Firstname
	private EditText edittext4; // Lastname
	private EditText edittext5; // MI
	private EditText edittext6; // Address 1
	private EditText edittext7; // Zip
	private EditText edittext8; // Zip+4
	private EditText edittext9; // Address 2
	private EditText edittext10; // Title - Mr/Mrs/Miss
	private EditText edittext11; // State
	private CheckBox checkbox1; // Test Checkbox

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_activity);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setTitle(getString(R.string.settings));
		}

		SQLAddress mySQLAddress;
		AddressDAO myAddressDAO;

		myAddressDAO = AddressDAO.getInstance(this.getApplicationContext());
		mySQLAddress = myAddressDAO.getAddress();

		edittext10 = (EditText) findViewById(R.id.editText10);
		edittext10.setText(mySQLAddress.getPREFIX());

		edittext3 = (EditText) findViewById(R.id.editText3);
		edittext3.setText(mySQLAddress.getFIRSTNAME());

		edittext5 = (EditText) findViewById(R.id.editText5);
		edittext5.setText(mySQLAddress.getMI());

		edittext4 = (EditText) findViewById(R.id.editText4);
		edittext4.setText(mySQLAddress.getLASTNAME());

		edittext6 = (EditText) findViewById(R.id.editText6);
		edittext6.setText(mySQLAddress.getADDRESS1());

		edittext9 = (EditText) findViewById(R.id.editText9);
		edittext9.setText(mySQLAddress.getADDRESS2());

		edittext7 = (EditText) findViewById(R.id.editText7);
		edittext7.setText(mySQLAddress.getZIP());

		edittext8 = (EditText) findViewById(R.id.editText8);
		edittext8.setText(mySQLAddress.getPLUS4());

		edittext11 = (EditText) findViewById(R.id.editText11);
		edittext11.setText(mySQLAddress.getSTATE());

		edittext1 = (EditText) findViewById(R.id.editText1);
		edittext1.setText(mySQLAddress.getTELEPHONE());

		checkbox1 = (CheckBox) findViewById(R.id.checkBox1);
		if (mySQLAddress.getTEST().equals("true")) {
			checkbox1.setChecked(true);
		}
	}

	private void saveAddress() {

		SQLAddress mySQLAddress = new SQLAddress();
		AddressDAO myAddressDAO;

		myAddressDAO = AddressDAO.getInstance(this.getApplicationContext());
		mySQLAddress.setPREFIX(edittext10.getText().toString());
		mySQLAddress.setTELEPHONE(edittext1.getText().toString());

		mySQLAddress.setFIRSTNAME(edittext3.getText().toString());
		mySQLAddress.setLASTNAME(edittext4.getText().toString());
		mySQLAddress.setMI(edittext5.getText().toString());
		mySQLAddress.setADDRESS1(edittext6.getText().toString());

		mySQLAddress.setZIP(edittext7.getText().toString());

		mySQLAddress.setPLUS4(edittext8.getText().toString());
		mySQLAddress.setSTATE(edittext11.getText().toString());
		mySQLAddress.setADDRESS2(edittext9.getText().toString());

		if (checkbox1.isChecked()) {
			mySQLAddress.setTEST("true");
		} else {
			mySQLAddress.setTEST("false");
		}
		mySQLAddress.setJSON("no json");
		myAddressDAO.saveAddress(mySQLAddress);
	}

	private boolean isZipCodeValid() {

	    boolean isValid = true;

		if (edittext7.getText().toString().isEmpty()) {
			edittext7.setText("");
			Toast.makeText(getApplicationContext(), R.string.zip_not_entered, Toast.LENGTH_SHORT).show();
			isValid = false;

		} else if (edittext7.getText().toString().length() < 5 || edittext7.getText().toString().length() > 5) {
			edittext7.setText("");
			Toast.makeText(getApplicationContext(), R.string.zip_length, Toast.LENGTH_SHORT).show();
            isValid = false;

		} else if (!TextUtils.isDigitsOnly(edittext7.getText().toString())) {
			edittext7.setText("");
			Toast.makeText(getApplicationContext(), R.string.zip_numeric, Toast.LENGTH_SHORT).show();
            isValid = false;
		}

		if (!edittext8.getText().toString().isEmpty()) {

			if (edittext8.getText().toString().length() < 4 || edittext8.getText().toString().length() > 4) {
				edittext8.setText("");
				Toast.makeText(getApplicationContext(), R.string.zip_4_length, Toast.LENGTH_SHORT).show();
                isValid = false;

			} else if (!TextUtils.isDigitsOnly(edittext8.getText().toString())) {
				edittext8.setText("");
				Toast.makeText(getApplicationContext(), R.string.zip_4_numeric, Toast.LENGTH_SHORT).show();
                isValid = false;
			}
		}
		return isValid;
	}
	public boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo;
		netInfo = cm.getActiveNetworkInfo();

		return netInfo != null && netInfo.isConnected();
	}

    public void onBackPressed(){
	    // capture backpress but nop

    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:

                if (this.isZipCodeValid()) {
                    if (this.isOnline()) {
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    }
                }
                this.saveAddress();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
