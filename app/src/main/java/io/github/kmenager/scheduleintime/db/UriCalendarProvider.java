package io.github.kmenager.scheduleintime.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UriCalendarProvider {

	private final UriCalendarHelper helper;

	public UriCalendarProvider(Context context) {
		helper = new UriCalendarHelper(context);
	}

	/**
	 * Return all formations from database
	 * @return the calendar id
	 */
	public Cursor getCalendarId() {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + UriCalendarHelper.CALENDAR_ID_TABLE_NAME;
		return sqLiteDb.rawQuery(selectQuery, null);
	}
	
	public Cursor getItemEventId(long id) {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		return sqLiteDb.query(UriCalendarHelper.CALENDAR_ID_TABLE_NAME, null,
				UriCalendarHelper.EVENT_ID_COLUMN + " =? ", 
				new String[] {String.valueOf(id)}, null, null, null);
	}

	/**
	 * Insert a formation in database
	 * @param event_id the id of the event
	 * @return if state of the update or insert
	 */
	public long insert(long event_id ) {
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(UriCalendarHelper.EVENT_ID_COLUMN, String.valueOf(event_id));

		if(!exists(event_id)) {
			return sqLiteDb.insert(UriCalendarHelper.CALENDAR_ID_TABLE_NAME, null, values);
		}
		else {
			return sqLiteDb.update(UriCalendarHelper.CALENDAR_ID_TABLE_NAME, values, 
					UriCalendarHelper.EVENT_ID_COLUMN+ " =? ", null);
		}
		
	}

	public long delete(long event_id){
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();
		return sqLiteDb.delete(UriCalendarHelper.CALENDAR_ID_TABLE_NAME, UriCalendarHelper.EVENT_ID_COLUMN + "=? ", new String[] {String.valueOf(event_id)});
	}

	
	public boolean exists(long id) {

		Cursor c = getCalendarId();
		
		boolean isPresent = false;
		if(c.getCount() > 0){

			if(c.moveToFirst()) {
				do {
				String tmp = c.getString(c.getColumnIndex(UriCalendarHelper.EVENT_ID_COLUMN));

				if( tmp.equals(String.valueOf(id)))
					isPresent = true;
				
				} while(c.moveToNext());
			}
			
		}
		return isPresent;
	}
	
	public void close() {

		helper.close();
	}


}
