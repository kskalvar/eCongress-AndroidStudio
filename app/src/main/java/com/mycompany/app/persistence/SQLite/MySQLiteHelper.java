package com.mycompany.app.persistence.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE = "address.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE = "address";

	public static final String PREFIX 		= "prefix";
	public static final String FIRSTNAME 	= "firstname";
	public static final String MI 			= "mi";
	public static final String LASTNAME 	= "lastname";
	public static final String ADDRESS1 	= "address1";
	public static final String ADDRESS2 	= "address2";
	public static final String ZIP 			= "zip";
	public static final String PLUS4 		= "plus4";
	public static final String STATE 		= "state";
	public static final String TELEPHONE 	= "telephone";
	public static final String TEST			= "test";
	public static final String JSON         = "json";

	// Database creation sql statement
	private static final String CREATE_TABLE = "create table " + TABLE + "("
			+ PREFIX 		+ " text not null, "
			+ FIRSTNAME 	+ " text not null, "
			+ MI 			+ " text not null, "
			+ LASTNAME 		+ " text not null, "
			+ ADDRESS1 		+ " text not null, "
			+ ADDRESS2 		+ " text not null, "
			+ ZIP 			+ " text not null, "
			+ PLUS4 		+ " text not null, "
			+ STATE 		+ " text not null, "
			+ TELEPHONE 	+ " text not null, "			
			+ TEST          + " text not null, "
			+ JSON          + " text not null) ";


	public MySQLiteHelper(Context context) {
		super(context, DATABASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("MySQLiteHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		
		db.execSQL("DROP TABLE IF EXISTS " + TABLE);
		onCreate(db);
	}

}
