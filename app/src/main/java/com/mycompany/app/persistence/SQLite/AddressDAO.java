package com.mycompany.app.persistence.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class AddressDAO {

	// Database fields
	private static SQLiteDatabase database;
	private static MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.PREFIX,
			MySQLiteHelper.FIRSTNAME, MySQLiteHelper.MI,
			MySQLiteHelper.LASTNAME, MySQLiteHelper.ADDRESS1,
			MySQLiteHelper.ADDRESS2, MySQLiteHelper.ZIP, MySQLiteHelper.PLUS4,
			MySQLiteHelper.STATE, MySQLiteHelper.TELEPHONE, MySQLiteHelper.TEST,
	        MySQLiteHelper.JSON };

	private static AddressDAO instance = null;

	public static AddressDAO getInstance(Context context) {
		if (instance == null) {
			instance = new AddressDAO(context);
			open();
		}
		return instance;
	}

	private AddressDAO(Context context) {
		dbHelper = new MySQLiteHelper(context);
		open();
	}

	protected static void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	private void createAddress(SQLAddress address) {

		ContentValues values = new ContentValues();

		values.put(MySQLiteHelper.PREFIX, address.getPREFIX());
		values.put(MySQLiteHelper.FIRSTNAME, address.getFIRSTNAME());
		values.put(MySQLiteHelper.MI, address.getMI());
		values.put(MySQLiteHelper.LASTNAME, address.getLASTNAME());
		values.put(MySQLiteHelper.ADDRESS1, address.getADDRESS1());
		values.put(MySQLiteHelper.ADDRESS2, address.getADDRESS2());
		values.put(MySQLiteHelper.ZIP, address.getZIP());
		values.put(MySQLiteHelper.PLUS4, address.getPLUS4());
		values.put(MySQLiteHelper.STATE, address.getSTATE());
		values.put(MySQLiteHelper.TELEPHONE, address.getTELEPHONE());
		values.put(MySQLiteHelper.TEST, address.getTEST());
        values.put(MySQLiteHelper.JSON, address.getJSON());

		database.insert(MySQLiteHelper.TABLE, null, values);
	}

	public SQLAddress getAddress() {

		SQLAddress address = new SQLAddress();

		Cursor cursor = database.query(MySQLiteHelper.TABLE, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {

			address.setPREFIX(cursor.getString(0));
			address.setFIRSTNAME(cursor.getString(1));
			address.setMI(cursor.getString(2));
			address.setLASTNAME(cursor.getString(3));
			address.setADDRESS1(cursor.getString(4));
			address.setADDRESS2(cursor.getString(5));
			address.setZIP(cursor.getString(6));
			address.setPLUS4(cursor.getString(7));
			address.setSTATE(cursor.getString(8));
			address.setTELEPHONE(cursor.getString(9));
			address.setTEST(cursor.getString(10));
			address.setJSON(cursor.getString(11));

            cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();

		return address;
	}

	private void changeAddress(SQLAddress address) {

		database.delete(MySQLiteHelper.TABLE, "1", null);
		createAddress(address);
	}

	public void saveAddress(SQLAddress address) {

		Cursor cursor = database.query(MySQLiteHelper.TABLE, allColumns, null,
				null, null, null, null);
		int count = cursor.getCount();

		if (count == 0) {
			createAddress(address);
		} else {
			changeAddress(address);
		}
		cursor.close();
	}
}
