package io.github.kmenager.scheduleintime.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FormationHelper extends SQLiteOpenHelper {
	
	protected static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "listformation.db";
	protected static final String FORMATIONS_TABLE_NAME = "formationlist";
	public static final String KEY_ID = "_id";
	public static final String GROUP_COLUMN = "groups";
	public static final String NAME_COLUMN = "name";
	public static final String FORMATION_ID = "formationid";
	


	public FormationHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDb) {
		sqLiteDb.execSQL("CREATE TABLE IF NOT EXISTS "
				+ FORMATIONS_TABLE_NAME	
				+ " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ GROUP_COLUMN + " TEXT, " 
				+ NAME_COLUMN + " TEXT, " 
				+ FORMATION_ID + " TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDb, int arg1, int arg2) {
		sqLiteDb.execSQL("DROP TABLE IF EXISTS "
				+FORMATIONS_TABLE_NAME + ";");
		onCreate(sqLiteDb);
	}

}
