package io.github.kmenager.scheduleintime.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UriCalendarHelper extends SQLiteOpenHelper {
	
	protected static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "uriCalendar.db";
	protected static final String CALENDAR_ID_TABLE_NAME = "uricalendar";
	public static final String KEY_ID = "_id";
	public static final String EVENT_ID_COLUMN = "event_id";
	


	public UriCalendarHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDb) {
		sqLiteDb.execSQL("CREATE TABLE IF NOT EXISTS "
				+ CALENDAR_ID_TABLE_NAME	
				+ " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ EVENT_ID_COLUMN + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDb, int arg1, int arg2) {
		sqLiteDb.execSQL("DROP TABLE IF EXISTS "
				+CALENDAR_ID_TABLE_NAME + ";");
		onCreate(sqLiteDb);
	}

}
