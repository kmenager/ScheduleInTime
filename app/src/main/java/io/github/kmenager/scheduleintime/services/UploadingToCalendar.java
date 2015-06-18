package io.github.kmenager.scheduleintime.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.CalendarContract.Events;

import io.github.kmenager.scheduleintime.db.UriCalendarHelper;
import io.github.kmenager.scheduleintime.db.UriCalendarProvider;
import io.github.kmenager.scheduleintime.providers.EventContentProvider;

public class UploadingToCalendar extends Service {

	public static final String CALENDAR_UPDATED = "fr.esipe.oc3.km.UploadingToCalendar.action.CALENDAR_UPDATED";
	private boolean addElement;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		addElement = intent.getBooleanExtra("addElement", true);
		if(addElement)
			new UploadingToCalendarTask().execute("");
		else
			new DeleteTaskFromCalendar().execute("");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(CALENDAR_UPDATED);
		sendBroadcast(intent);
	}

	/**
	 * Delete task from calendar when switch is off
	 * @author Kevin M
	 *
	 */
	private class DeleteTaskFromCalendar extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			UriCalendarProvider provider = new UriCalendarProvider(getApplicationContext());
			Cursor c = provider.getCalendarId();

			if(c.moveToFirst()) {
				do {

					long event_id = Long.valueOf(c.getString(c.getColumnIndex(UriCalendarHelper.EVENT_ID_COLUMN)));
					String[] selArgs = 
							new String[]{Long.toString(event_id)};
					getContentResolver().delete(
							Events.CONTENT_URI, 
							Events._ID + " =? ", 
							selArgs);
					provider.delete(event_id);
				}while(c.moveToNext());
			}
			c.close();
			provider.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			stopSelf();
		}
	}

	/**
	 * Upload task to calendar when switch is on
	 * @author Kevin M
	 *
	 */
	private class UploadingToCalendarTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}


		@Override
		protected Void doInBackground(String... params) {
			Uri mUri = EventContentProvider.CONTENT_URI;
			UriCalendarProvider provider = new UriCalendarProvider(getApplicationContext());
			Cursor cursor = getContentResolver().query(mUri,
					null, 
					EventContentProvider.WEEK_OF_EVENTS + "=?",
					new String[] {String.valueOf(7)}, 
					null);

			if(cursor.getCount() > 0){

				if(cursor.moveToFirst()) {

					do {
						String startTime = cursor.getString(cursor.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN));
						String endTime = cursor.getString(cursor.getColumnIndex(EventContentProvider.END_TIME_NAME_COLUMN));
						String classroom = cursor.getString(cursor.getColumnIndex(EventContentProvider.CLASSROOM_NAME_COLUMN));
						String topic = cursor.getString(cursor.getColumnIndex(EventContentProvider.TOPIC_NAME_COLUMN));
						String teacher = cursor.getString(cursor.getColumnIndex(EventContentProvider.TEACHERS_NAME_COLUMN));
						Cursor qcursor = getContentResolver().query(Events.CONTENT_URI,
								null, 
								Events.DTSTART + "=? AND " + Events.TITLE + "=?",
								new String[] {startTime,topic}, 
								null);
						ContentValues values = new ContentValues();
						values.put(Events.CALENDAR_ID, 1);
						values.put(Events.DTSTART, Long.parseLong(startTime));
						values.put(Events.DTEND, Long.parseLong(endTime));
						values.put(Events.TITLE, topic);
						values.put(Events.EVENT_LOCATION, classroom);
						values.put(Events.DESCRIPTION, teacher);
						values.put(Events.EVENT_TIMEZONE, "Europe/Paris");

						
						if(!qcursor.moveToFirst()) {
							Uri uri = getContentResolver().insert(Events.CONTENT_URI, values);
							long event_id = Long.valueOf(uri.getLastPathSegment());
							provider.insert(event_id);
						} else {
							getContentResolver().update(Events.CONTENT_URI, values,
									Events.DTSTART + "=? AND " + Events.TITLE + "=?",
									new String[] {startTime, topic});
						}
						qcursor.close();
					} while(cursor.moveToNext());
					provider.close();
				}
				cursor.close();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			stopSelf();
		}
	}
}
