package com.mycompany.app.econgress;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.app.oauth.GoogleOauthAsyncTask;
import com.mycompany.app.persistence.SQLite.AddressDAO;
import com.mycompany.app.persistence.SQLite.SQLAddress;
import com.mycompany.app.smtp.MailAsyncTask;
import com.mycompany.app.sunlight.LegislatorObject;
import com.mycompany.app.sunlight.SunlightJSONAsyncTask;
import com.mycompany.app.version.VersionAsyncTask;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * @author Kirk S. Kalvar
 * 
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private CheckBox checkBox1_1;
	private CheckBox checkBox1_2;
	private CheckBox checkBox1_3;
	private CheckBox checkBox1_4;
	private CheckBox checkBox1_5;

	private Button nameButton1_1;
	private Button nameButton1_2;
	private Button nameButton1_3;
	private Button nameButton1_4;
	private Button nameButton1_5;

	private ImageButton webButton1_1;
	private ImageButton webButton1_2;
	private ImageButton webButton1_3;
	private ImageButton webButton1_4;
	private ImageButton webButton1_5;

	private ImageButton phoneButton1_1;
	private ImageButton phoneButton1_2;
	private ImageButton phoneButton1_3;
	private ImageButton phoneButton1_4;
	private ImageButton phoneButton1_5;

	private EditText subjectText;
	private EditText messageText;

	private static boolean versionDisplayed = false;

	public boolean mailSent = false;
	public String mailMessage = null;

	public boolean noGoogleAccount = false;
	public String googleAccount = null;
	public boolean googleAcountNoPermissions = false;

	public boolean sunlightNetworkError = false;
    public boolean sunlightIsParsed = false;
	public boolean sunlightParseError = false;
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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendMail();
			}
		});

		swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
		swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				fetchTimelineAsync();
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		checkBox1_1 = (CheckBox) findViewById(R.id.checkBox1_1);
		checkBox1_2 = (CheckBox) findViewById(R.id.checkBox1_2);
		checkBox1_3 = (CheckBox) findViewById(R.id.checkBox1_3);
		checkBox1_4 = (CheckBox) findViewById(R.id.checkBox1_4);
		checkBox1_5 = (CheckBox) findViewById(R.id.checkBox1_5);

		nameButton1_1 = (Button) findViewById(R.id.nameButton1_1);
		nameButton1_2 = (Button) findViewById(R.id.nameButton1_2);
		nameButton1_3 = (Button) findViewById(R.id.nameButton1_3);
		nameButton1_4 = (Button) findViewById(R.id.nameButton1_4);
		nameButton1_5 = (Button) findViewById(R.id.nameButton1_5);

		webButton1_1 = (ImageButton) findViewById(R.id.webButton1_1);
		webButton1_2 = (ImageButton) findViewById(R.id.webButton1_2);
		webButton1_3 = (ImageButton) findViewById(R.id.webButton1_3);
		webButton1_4 = (ImageButton) findViewById(R.id.webButton1_4);
		webButton1_5 = (ImageButton) findViewById(R.id.webButton1_5);

		phoneButton1_1 = (ImageButton) findViewById(R.id.phoneButton1_1);
		phoneButton1_2 = (ImageButton) findViewById(R.id.phoneButton1_2);
		phoneButton1_3 = (ImageButton) findViewById(R.id.phoneButton1_3);
		phoneButton1_4 = (ImageButton) findViewById(R.id.phoneButton1_4);
		phoneButton1_5 = (ImageButton) findViewById(R.id.phoneButton1_5);

		subjectText = (EditText) findViewById(R.id.subjectText);
		messageText = (EditText) findViewById(R.id.messageText);
	}

	@Override
	protected void onStart() {
		super.onStart();

		setActionBarTitle();

		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, 0);
		}

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
					messageText.setText("\n\n" + url + this.getSignature());
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
			getRepresentatives();
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
        super.onResume();

		if (this.isOnline()) {
			new GoogleOauthAsyncTask(MainActivity.this).execute();
		}
	}

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

			checkBox1_1.setChecked(false);
			checkBox1_2.setChecked(false);
			checkBox1_3.setChecked(false);
			checkBox1_4.setChecked(false);
			checkBox1_5.setChecked(false);

			subjectText.setText("");
			messageText.setText(this.getSignature());
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

			new SunlightJSONAsyncTask(MainActivity.this).execute(url + command + mySQLAddress.getZIP() + key);
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

			new SunlightJSONAsyncTask(MainActivity.this).execute(url + command + mySQLAddress.getZIP() + key);
		}
	}

	public void setRepresentatives() {

		setRepresentativeButtonDefaults();

		if (sunlightParseError) {
			Toast.makeText(getApplicationContext(), R.string.legislative_parse_error, Toast.LENGTH_LONG).show();
			return;
		}

		if (sunlightNetworkError) {
			Toast.makeText(getApplicationContext(), R.string.no_legislative_service, Toast.LENGTH_LONG).show();
			return;
		}

        if (oauthJsonParseError) {
            Toast.makeText(getApplicationContext(), R.string.oauth_parse_error, Toast.LENGTH_LONG).show();
        }

        if (oauthJsonNetworkError) {
            Toast.makeText(getApplicationContext(), R.string.oauth_network_error, Toast.LENGTH_LONG).show();
        }

		AddressDAO myAddressDAO = AddressDAO.getInstance(getApplicationContext());
		SQLAddress mySQLAddress = myAddressDAO.getAddress();

		/* POTUS */
		nameButton1_1.setText(getString(R.string.presidentsymbol));
		if (mySQLAddress.getTEST().equals("true")) {
			checkBox1_1.setEnabled(false);
			checkBox1_1.setChecked(false);
		} else {
			checkBox1_1.setEnabled(true);
			checkBox1_1.setChecked(false);
		}

		webButton1_1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			if (isOnline()) {
				Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
				myWebLink.setData(Uri.parse(getString(R.string.presidentwebsite)));
				startActivity(myWebLink);
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getString(R.string.presidentsymbol), Toast.LENGTH_SHORT).show();
			}
			}
		});

		phoneButton1_1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
            if (isOnline()) {
                Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
                myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getString(R.string.presidentphone)));
                startActivity(myPhoneLink);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getString(R.string.presidentsymbol), Toast.LENGTH_SHORT).show();
            }
            }
		});

		/* First Legislator */
		if (sunlightIsParsed && getLegislatorObject().getLegislatorCount() > 0) {

			nameButton1_2.setText(getLegislatorObject().getLegislator(0));
			if (mySQLAddress.getTEST().equals("true")) {
				checkBox1_2.setEnabled(false);
				checkBox1_2.setChecked(false);
			} else {
				checkBox1_2.setEnabled(true);
				checkBox1_2.setChecked(false);
			}
			checkBox1_2.setVisibility(View.VISIBLE);
			nameButton1_2.setVisibility(View.VISIBLE);
			webButton1_2.setVisibility(View.VISIBLE);
			phoneButton1_2.setVisibility(View.VISIBLE);

			webButton1_2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					if (!getLegislatorObject().getWebsite(0).equals("null") && isOnline()) {
						Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
						myWebLink.setData(Uri.parse(getLegislatorObject().getWebsite(0)));
						startActivity(myWebLink);
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getLegislatorObject().getLegislator(0), Toast.LENGTH_SHORT).show();
					}
				}

				private boolean isOnline() {

					ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = cm.getActiveNetworkInfo();
					return netInfo != null && netInfo.isConnected();
				}
			});

			phoneButton1_2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					if (!getLegislatorObject().getPhone(0).equals("null") && isOnline()) {
						Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
						myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getLegislatorObject().getPhone(0)));
						startActivity(myPhoneLink);
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getLegislatorObject().getLegislator(0), Toast.LENGTH_SHORT).show();
					}
				}

				private boolean isOnline() {

					ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo netInfo = cm.getActiveNetworkInfo();
					return netInfo != null && netInfo.isConnected();
				}
			});

			/* Second Legislator */
			if (getLegislatorObject().getLegislatorCount() > 1) {

				nameButton1_3.setText(getLegislatorObject().getLegislator(1));
				if (mySQLAddress.getTEST().equals("true")) {
					checkBox1_3.setEnabled(false);
					checkBox1_3.setChecked(false);
				} else {
					checkBox1_3.setEnabled(true);
					checkBox1_3.setChecked(false);
				}
				checkBox1_3.setVisibility(View.VISIBLE);
				nameButton1_3.setVisibility(View.VISIBLE);
				webButton1_3.setVisibility(View.VISIBLE);
				phoneButton1_3.setVisibility(View.VISIBLE);

				webButton1_3.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getWebsite(1).equals("null") && isOnline()) {
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(getLegislatorObject().getWebsite(1)));
							startActivity(myWebLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getLegislatorObject().getLegislator(1), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {

						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});

				phoneButton1_3.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getPhone(1).equals("null") && isOnline()) {
							Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
							myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getLegislatorObject().getPhone(1)));
							startActivity(myPhoneLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getLegislatorObject().getLegislator(1), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});

			}

			/* Third Legislator */
			if (getLegislatorObject().getLegislatorCount() > 2) {

				nameButton1_4.setText(getLegislatorObject().getLegislator(2));
				if (mySQLAddress.getTEST().equals("true")) {
					checkBox1_4.setEnabled(false);
					checkBox1_4.setChecked(false);
				} else {
					checkBox1_4.setEnabled(true);
					checkBox1_4.setChecked(false);
				}
				checkBox1_4.setVisibility(View.VISIBLE);
				nameButton1_4.setVisibility(View.VISIBLE);
				webButton1_4.setVisibility(View.VISIBLE);
				phoneButton1_4.setVisibility(View.VISIBLE);

				webButton1_4.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getWebsite(2).equals("null") && isOnline()) {
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(getLegislatorObject().getWebsite(2)));
							startActivity(myWebLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getLegislatorObject().getLegislator(2), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});

				phoneButton1_4.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getPhone(2).equals("null") && isOnline()) {
							Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
							myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getLegislatorObject().getPhone(2)));
							startActivity(myPhoneLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getLegislatorObject().getLegislator(2), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});
			}

			/* Fourth Legislator */
			if (getLegislatorObject().getLegislatorCount() > 3) {

				nameButton1_5.setText(getLegislatorObject().getLegislator(3));
				if (mySQLAddress.getTEST().equals("true")) {
					checkBox1_5.setEnabled(false);
					checkBox1_5.setChecked(false);
					checkBox1_5.setChecked(false);
				} else {
					checkBox1_5.setEnabled(true);
				}
				checkBox1_5.setVisibility(View.VISIBLE);
				nameButton1_5.setVisibility(View.VISIBLE);
				webButton1_5.setVisibility(View.VISIBLE);
				phoneButton1_5.setVisibility(View.VISIBLE);

				webButton1_5.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getWebsite(3).equals("null") && isOnline()) {
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(getLegislatorObject().getWebsite(3)));
							startActivity(myWebLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_website) + getLegislatorObject().getLegislator(3), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});

				phoneButton1_5.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						if (!getLegislatorObject().getPhone(3).equals("null") && isOnline()) {
							Intent myPhoneLink = new Intent(Intent.ACTION_DIAL);
							myPhoneLink.setData(Uri.parse(getString(R.string.telphone_parse) + getLegislatorObject().getPhone(3)));
							startActivity(myPhoneLink);
						} else {
							Toast.makeText(getApplicationContext(), getString(R.string.no_phone) + getLegislatorObject().getLegislator(3), Toast.LENGTH_SHORT).show();
						}
					}

					private boolean isOnline() {
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = cm.getActiveNetworkInfo();
						return netInfo != null && netInfo.isConnected();
					}
				});
			}
		} else {

			/* If Zip Code is incorrect open the SettingsActivity Form */
			Toast.makeText(getApplicationContext(), R.string.invalid_zip, Toast.LENGTH_LONG).show();

			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivityForResult(intent, 0);
		}
	}

	private void setRepresentativeButtonDefaults() {

		checkBox1_1.setEnabled(false);
		checkBox1_1.setChecked(false);
		nameButton1_1.setText(null);
		webButton1_1.setOnClickListener(null);
		phoneButton1_1.setOnClickListener(null);

		checkBox1_2.setEnabled(false);
		checkBox1_2.setChecked(false);
		nameButton1_2.setText(null);
		webButton1_2.setOnClickListener(null);
		phoneButton1_2.setOnClickListener(null);

		checkBox1_3.setEnabled(false);
		checkBox1_3.setChecked(false);
		nameButton1_3.setText(null);
		webButton1_3.setOnClickListener(null);
		phoneButton1_3.setOnClickListener(null);

		checkBox1_4.setEnabled(false);
		checkBox1_4.setChecked(false);
		nameButton1_4.setText(null);
		webButton1_4.setOnClickListener(null);
		phoneButton1_4.setOnClickListener(null);

		checkBox1_5.setEnabled(false);
		checkBox1_5.setChecked(false);
		nameButton1_5.setText(null);
		webButton1_5.setOnClickListener(null);
		phoneButton1_5.setOnClickListener(null);

		// There may not be a fourth representative, so hide until populated
		checkBox1_5.setVisibility(View.GONE);
		nameButton1_5.setVisibility(View.GONE);
		webButton1_5.setVisibility(View.GONE);
		phoneButton1_5.setVisibility(View.GONE);
	}

	public void setNavigationViewAccount() {

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		View header=navigationView.getHeaderView(0);

		TextView email =(TextView)header.findViewById(R.id.textView);
		if (googleAccount == null) {
			email.setText(R.string.permissions_not_set);
		} else {
			email.setText(googleAccount);
		}

        TextView textViewVersion = (TextView) header.findViewById(R.id.textViewVersion);
        textViewVersion.setText(getString(R.string.econgress) + " v" + getString(R.string.version));
	}

	private void sendMail() {

		if (!this.isOnline()) {
			Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
			return;
		}

 		if (googleAcountNoPermissions) {
			Toast.makeText(getApplicationContext(), R.string.permissions_not_set, Toast.LENGTH_SHORT).show();
			return;
		} else if (noGoogleAccount) {
			Toast.makeText(getApplicationContext(), R.string.no_email_account, Toast.LENGTH_SHORT).show();
			return;
		} else if (!oauthOnTokenAcquired) {
			Toast.makeText(getApplicationContext(), "Warning: Oauth Token Not Acquired!", Toast.LENGTH_SHORT).show();
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

		if (checkBox1_1.isChecked()) {
			emailAddresses.add(getString(R.string.presidentemail));
		}

		if (sunlightIsParsed && getLegislatorObject().getLegislatorCount() > 0) {

			if (checkBox1_2.isChecked()) {
				if (!getLegislatorObject().getEmail(0).equals("null")) {
					emailAddresses.add(getLegislatorObject().getEmail(0));
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.email_address_not_available) + getLegislatorObject().getLegislator(0), Toast.LENGTH_SHORT).show();
				}
			}

			if (checkBox1_3.isChecked()) {
				if (!getLegislatorObject().getEmail(1).equals("null")) {
					emailAddresses.add(getLegislatorObject().getEmail(1));
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.email_address_not_available) + getLegislatorObject().getLegislator(1), Toast.LENGTH_SHORT).show();
				}
			}

			if (checkBox1_4.isChecked()) {
				if (!getLegislatorObject().getEmail(2).equals("null")) {
					emailAddresses.add(getLegislatorObject().getEmail(2));
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.email_address_not_available) + getLegislatorObject().getLegislator(2), Toast.LENGTH_SHORT).show();
				}
			}

			if (checkBox1_5.isChecked()) {
				if (!getLegislatorObject().getEmail(3).equals("null")) {
					emailAddresses.add(getLegislatorObject().getEmail(3));
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.email_address_not_available) + getLegislatorObject().getLegislator(3), Toast.LENGTH_SHORT).show();
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
			checkBox1_1.setChecked(false);
			checkBox1_2.setChecked(false);
			checkBox1_3.setChecked(false);
			checkBox1_4.setChecked(false);

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
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		return netInfo != null && netInfo.isConnected();
	}


}