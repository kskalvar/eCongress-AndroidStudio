package com.mycompany.app.econgress;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.app.googleapi.GoogleApiJSONAsyncTask;
import com.mycompany.app.oauth.GoogleOauthAsyncTask;
import com.mycompany.app.persistence.SQLite.AddressDAO;
import com.mycompany.app.persistence.SQLite.SQLAddress;
import com.mycompany.app.security.AndroidPermissionsAsyncTask;
import com.mycompany.app.smtp.MailAsyncTask;
import com.mycompany.app.version.VersionAsyncTask;

import java.util.ArrayList;

import static android.R.drawable.ic_dialog_dialer;
import static android.R.drawable.ic_dialog_info;

/**
 * @author Kirk S. Kalvar
 * 
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private EditText subjectText;
	private EditText messageText;

	private static boolean versionDisplayed = false;

	public boolean mailSent = false;
	public String mailMessage = null;

	public boolean noGoogleAccount = false;
	public String googleAccount = null;
	public boolean googleAcountNoPermissions = false;

	public boolean legislativeNetworkError = false;
    public boolean legislativeIsParsed = false;
	public boolean legislativeParseError = false;
	public boolean legislativeInvalidZip = false;
    private static LegislatorObject legislatorObject = null;

    public String oauthToken = null;
	public boolean oauthJsonValidToken = false;
    public boolean oauthJsonParseError = false;
    public boolean oauthJsonNetworkError = false;
	public boolean oauthOnTokenAcquired = false;

	private SwipeRefreshLayout swipeContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendMail();
			}
		});

		swipeContainer = findViewById(R.id.swipeContainer);
		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				fetchTimelineAsync();
			}
		});

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        new AndroidPermissionsAsyncTask(MainActivity.this).execute();
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

        subjectText = findViewById(R.id.subjectText);
		messageText = findViewById(R.id.messageText);
	}

	@Override
	protected void onStart() {
		super.onStart();

		setActionBarTitle();
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {

				String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				if (subject != null) {
                    subjectText.setText(subject);
                } else {
                    subjectText.setText("");
                }

				String url = intent.getStringExtra(Intent.EXTRA_TEXT);
				if (url != null) {
                    messageText.setText(String.format("\n\n %s %s", url, this.getSignature()));

                } else {
                    messageText.setText(this.getSignature());
                }
			}

		} else {
			subjectText.setText("");
			messageText.setText(this.getSignature());
		}

		if (this.isOnline()) {
			if (!versionDisplayed) {
				versionDisplayed = true;
				new VersionAsyncTask(MainActivity.this).execute();
			}
			getRepresentatives();

		} else {
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
            AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
            SQLAddress mySQLAddress = myAddressDAO.getAddress();
            if (!mySQLAddress.getJSON().toLowerCase().equals("no json")) {
                getRepresentatives();
            }
		}
	}

	@Override
	protected void onResume() { super.onResume(); }

	@Override
	protected void onPause() {
        super.onPause();
	}

	@Override
	protected void onStop() {
        super.onStop();
	}

	@Override
	protected void onPostResume() {
        super.onPostResume();
 	}

	@Override
	protected void onRestart() {
        super.onRestart();
	}

	@Override
	protected void onDestroy() {
        super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

        if (!this.isOnline()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
            return false;
        }

		switch (item.getItemId()) {
		case R.id.main_manage:
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivityForResult(intent, 0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

 		// Handle navigation view item clicks here.
		int id = item.getItemId();
		if (id == R.id.nav_clear) {

            setRepresentativeTable();

			subjectText.setText("");
			messageText.setText(this.getSignature());
		}

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}

	public void fetchTimelineAsync() {

		if (!this.isOnline()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        } else {

			AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
			SQLAddress mySQLAddress = myAddressDAO.getAddress();

			Toast.makeText(getApplicationContext(), R.string.refreshing, Toast.LENGTH_SHORT).show();

			SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
			String url = sharedPref.getString(getString(R.string.url), null);
			String command = sharedPref.getString(getString(R.string.command), null);
			String key = sharedPref.getString(getString(R.string.key), null);

            // get the latest legislative information from the service not locally cache
            mySQLAddress.setJSON("no json");
            myAddressDAO.saveAddress(mySQLAddress);

            new GoogleApiJSONAsyncTask(MainActivity.this).execute(url + command + mySQLAddress.getAddressUrl() + key);
			new GoogleOauthAsyncTask(MainActivity.this).execute();
		}
		swipeContainer.setRefreshing(false);
	}

	private void getRepresentatives() {

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

		if (mySQLAddress.getZIP().length() == 0) {
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivityForResult(intent, 0);

		} else if (mySQLAddress.getZIP().length() != 0) {
			SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
			String url = sharedPref.getString(getString(R.string.url), null);
			String command = sharedPref.getString(getString(R.string.command), null);
			String key = sharedPref.getString(getString(R.string.key), null);

            new GoogleApiJSONAsyncTask(MainActivity.this).execute(url + command + mySQLAddress.getAddressUrl() + key);
		}

        new AndroidPermissionsAsyncTask(MainActivity.this).execute();
        new GoogleOauthAsyncTask(MainActivity.this).execute();

	}

	public void setRepresentatives() {

        if (legislativeParseError) {
            Toast.makeText(getApplicationContext(), R.string.legislative_parse_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (legislativeNetworkError) {
            Toast.makeText(getApplicationContext(), R.string.no_legislative_service, Toast.LENGTH_LONG).show();
            return;
        }

        if (oauthJsonParseError) {
            Toast.makeText(getApplicationContext(), R.string.oauth_parse_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (oauthJsonNetworkError) {
            Toast.makeText(getApplicationContext(), R.string.oauth_network_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (legislativeInvalidZip && isOnline()) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(), R.string.invalid_zip, Toast.LENGTH_LONG).show();
            return;
        } else if (legislativeInvalidZip && !isOnline()) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivityForResult(intent, 0);
            Toast.makeText(getApplicationContext(), "Warning: Unable to Contact Legislative Service to Validate Zip Code", Toast.LENGTH_LONG).show();
            return;
        }

        setRepresentativeTable();
    }

     private void setRepresentativeTable() {

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

        TableLayout tl = findViewById(R.id.tableLayout);
        tl.removeAllViews();

        getLegislatorObject().clearEmailSend();

        for (int memberIndex = 0; memberIndex < getLegislatorObject().getLegislatorCount(); memberIndex++ ) {
            final int finalMemberIndex = memberIndex;

            final CheckBox checkBox = new CheckBox(this);
            checkBox.setMinHeight(55);

            if (mySQLAddress.getTEST().equals("true")) {
                checkBox.setEnabled(false);
            }

            checkBox.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    if (checkBox.isChecked()) {
                        getLegislatorObject().setEmailSend(finalMemberIndex, true);
                    } else {
                        getLegislatorObject().setEmailSend(finalMemberIndex, false);
                    }
                }
            });

            Button nameButton = new Button(this);
            nameButton.setTextSize(14);
            nameButton.setText(getLegislatorObject().getLegislator(finalMemberIndex));

            ImageButton webButton = new ImageButton(this);
            webButton.setMaxHeight(55);
            webButton.setMaxWidth(55);
            webButton.setBackgroundColor(Color.WHITE);
			webButton.setImageResource(R.drawable.twitter_logo_high_res2_32x32);

            webButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    if (!getLegislatorObject().getWebsite(finalMemberIndex).equals("null") && isOnline()) {
                        Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                        myWebLink.setData(Uri.parse(getLegislatorObject().getWebsite(finalMemberIndex)));
                        startActivity(myWebLink);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getLegislatorObject().getLegislator(finalMemberIndex), Toast.LENGTH_SHORT).show();
                    }
                }

                private boolean isOnline() {

                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    assert cm != null;
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    return netInfo != null && netInfo.isConnected();
                }
            });

			ImageButton phoneButton = new ImageButton(this);
            phoneButton.setMaxHeight(55);
            phoneButton.setMaxWidth(55);
			phoneButton.setBackgroundColor(Color.WHITE);
			phoneButton.setImageResource(R.drawable.phone_logo_high_res2_32x32);

            phoneButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    if (!getLegislatorObject().getPhone(finalMemberIndex).equals("null") && isOnline()) {
                        Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
                        myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getLegislatorObject().getPhone(finalMemberIndex)));
                        startActivity(myPhoneLink);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getLegislatorObject().getLegislator(finalMemberIndex), Toast.LENGTH_SHORT).show();
                    }
                }

                private boolean isOnline() {

                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    assert cm != null;
                    NetworkInfo netInfo;
                    netInfo = cm.getActiveNetworkInfo();
                    return netInfo != null && netInfo.isConnected();
                }
            });

            TableRow tr = new TableRow(this);

            tr.addView(checkBox);
            tr.addView(nameButton);
            tr.addView(webButton);
            tr.addView(phoneButton);

            tl.addView(tr);
        }
	}

	public void setNavigationViewAccount() {

		NavigationView navigationView = findViewById(R.id.nav_view);
		View header=navigationView.getHeaderView(0);

		TextView email = header.findViewById(R.id.textView);
		if (googleAccount == null) {
            email.setText(R.string.permissions_not_set);
        } else {
            email.setText(String.format("%s %s", getString(R.string.account), googleAccount));
        }

        TextView textViewVersion = header.findViewById(R.id.textViewVersion);
        textViewVersion.setText(String.format("%s v%s", getString(R.string.econgress), getString(R.string.version)));
	}

	private void sendMail() {

		if (!this.isOnline()) {
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
			return;
		}

        new AndroidPermissionsAsyncTask(MainActivity.this).execute();
        new GoogleOauthAsyncTask(MainActivity.this).execute();

 		if (googleAcountNoPermissions) {
			Toast.makeText(getApplicationContext(), R.string.permissions_not_set, Toast.LENGTH_SHORT).show();
			return;
		} else if (noGoogleAccount) {
			Toast.makeText(getApplicationContext(), R.string.no_email_account, Toast.LENGTH_SHORT).show();
			return;
		} else if (!oauthOnTokenAcquired) {
			Toast.makeText(getApplicationContext(), R.string.oauth_token_not_acquired, Toast.LENGTH_SHORT).show();
			return;
		} else if (!oauthJsonValidToken) {
			Toast.makeText(getApplicationContext(), R.string.no_token, Toast.LENGTH_SHORT).show();
			return;
		} else  if (TextUtils.isEmpty(subjectText.getText())) {
			Toast.makeText(getApplicationContext(), R.string.no_subject, Toast.LENGTH_SHORT).show();
			return;
		} else  if (TextUtils.isEmpty(messageText.getText())) {
			Toast.makeText(getApplicationContext(), R.string.no_message, Toast.LENGTH_SHORT).show();
			return;
		}

		ArrayList<String> emailAddresses = new ArrayList<>();

        if (legislativeIsParsed && getLegislatorObject().getLegislatorCount() > 0) {
            for (int memberIndex = 0; memberIndex < getLegislatorObject().getLegislatorCount(); memberIndex++) {
                if (getLegislatorObject().getEmailSend(memberIndex)) {
                    if (!getLegislatorObject().getEmail(memberIndex).equals("null")) {
                        emailAddresses.add(getLegislatorObject().getEmail(memberIndex));
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.email_address_not_available) + getLegislatorObject().getLegislator(memberIndex), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        // Build address list or override for testing purposes.
		// Overriding will send message to your own email address

		String emailAddress = emailAddresses.toString().replace("[", "") // remove the right bracket
				.replace("]", "") // remove the left bracket
				.trim(); // remove trailing spaces if they occur

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

		if (mySQLAddress.getTEST().equals("true")) {
            emailAddress = googleAccount;
        }

		if (emailAddress.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.no_email_address, Toast.LENGTH_SHORT).show();
        } else {
			String subject = subjectText.getText().toString();
			String message = messageText.getText().toString();
			new MailAsyncTask(MainActivity.this).execute(emailAddress, googleAccount, subject, message, oauthToken);
			Toast.makeText(getApplicationContext(), R.string.sending_message, Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * If the "Test Email" box is checked on the SettingsActivity, set the ActionBar Title
	 */

	private void setActionBarTitle() {

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

		if (mySQLAddress.getTEST().toLowerCase().equals("true")) {
            setTitle(getString(R.string.test_mode));
        } else {
            setTitle(getString(R.string.normal_mode));
        }
	}

	private String getSignature() {

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

		String mySignature = mySQLAddress.getSignature();

		if (mySignature.isEmpty()) {
            return mySignature;
        } else {
            return "\n\n" + mySignature;
        }
	}

	public void mailStatusNotification() {

		if (mailSent) {

            setRepresentatives();

			subjectText.setText("");
			messageText.setText(this.getSignature());

		} else {
            Toast.makeText(getApplicationContext(), getString(R.string.email_failed) + this.mailMessage, Toast.LENGTH_LONG).show();
        }
	}

	public static LegislatorObject getLegislatorObject() {
		return legislatorObject;
	}

	public static void setLegislatorObject(LegislatorObject legislatorObject) {
		MainActivity.legislatorObject = legislatorObject;
	}

	public boolean isOnline() {

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo;
        netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
	}
}