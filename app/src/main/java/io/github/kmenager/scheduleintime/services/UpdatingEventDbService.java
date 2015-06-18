package io.github.kmenager.scheduleintime.services;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import fr.esipe.agenda.parser.Event;
import fr.esipe.agenda.parser.Parser;
import io.github.kmenager.scheduleintime.R;
import io.github.kmenager.scheduleintime.providers.EventContentProvider;

public class UpdatingEventDbService extends Service{

	public static final String DATABASE_EVENTS_UPDATED = "fr.esipe.oc3.km.UpdatingEventDbService.action.DATABASE_EVENTS_UPDATED";
	private String formationId;
	private int year;
	private int weekOfYear;
	private int numberOfWeek;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

	/**
	 * récupère le planning de la semaine courante et la semaine suivante
	 * programme le reveil de ce service
	 * stocke dans un nouveau calendrier l'emploi du temps actuel
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		formationId = intent.getStringExtra(getResources().getString(R.string.event_intent_formation_id));
		year = intent.getIntExtra(getResources().getString(R.string.event_intent_year), 2012);
		weekOfYear = intent.getIntExtra(getResources().getString(R.string.event_intent_week_of_year), 51);
		numberOfWeek = intent.getIntExtra(getResources().getString(R.string.event_intent_number_of_week), 6);
		if(networkIsEnabled()) {
			GetEventsFromServer recoverEvent = new GetEventsFromServer();
			recoverEvent.execute();
		} else {
			Intent intent2 = new Intent(DATABASE_EVENTS_UPDATED);
			intent2.putExtra("network", false);
			sendBroadcast(intent2);
		}

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent(DATABASE_EVENTS_UPDATED);
		sendBroadcast(intent);
	}
	
	/**
	 * Add events in database
	 * @param listEvents list of events
	 * @param mweekOfyear week of year
	 */
	public void addingEventDatabase(List<Event> listEvents, int mweekOfyear) {
		Uri mUri = EventContentProvider.CONTENT_URI;
		getContentResolver().delete(mUri, EventContentProvider.FORMATION_ID_COLUMN + "!=?", 
				new String[] {formationId});
		
		getContentResolver().delete(mUri, EventContentProvider.WEEK_OF_EVENTS + "=?",
				new String[] {String.valueOf(mweekOfyear)});
		
		String selection = EventContentProvider.START_TIME_NAME_COLUMN + "=? AND " +
				EventContentProvider.TOPIC_NAME_COLUMN + "=?";
		
		String[] columnsLabels = new String[] {
				EventContentProvider.TOPIC_NAME_COLUMN,
				EventContentProvider.TEACHERS_NAME_COLUMN,
				EventContentProvider.CLASSROOM_NAME_COLUMN,
				EventContentProvider.BRANCH_NAME_COLUMN,
				EventContentProvider.EXAMEN_NAME_COLUMN
		};
		if(listEvents.size() == 0) {
			ContentValues values = new ContentValues();
			Calendar refHour = Calendar.getInstance();
			refHour.setFirstDayOfWeek(Calendar.MONDAY);
			refHour.set(Calendar.WEEK_OF_YEAR, mweekOfyear);
			refHour.set(Calendar.HOUR_OF_DAY, 6);
			refHour.set(Calendar.MINUTE, 0);
			refHour.set(Calendar.SECOND, 0);
			for(int numberOfDay = 1; numberOfDay < 5 ; numberOfDay++) {
				
				values.put(EventContentProvider.WEEK_OF_EVENTS, mweekOfyear);
				values.put(EventContentProvider.FORMATION_ID_COLUMN, formationId);
				values.put(EventContentProvider.START_TIME_NAME_COLUMN, String.valueOf(refHour.getTimeInMillis()));
				List<String> labels = new ArrayList<>();
				values.put(EventContentProvider.TOPIC_NAME_COLUMN, labels.add("Entreprise"));

				Cursor cursor = getContentResolver().query(mUri,
						null, 
						EventContentProvider.START_TIME_NAME_COLUMN + "=? AND " + EventContentProvider.WEEK_OF_EVENTS + "=?",
						new String[] {String.valueOf(refHour.getTimeInMillis()), String.valueOf(mweekOfyear)}, 
						null);

				if(!cursor.moveToFirst()) {
					getContentResolver().insert(mUri, values);
				} else {
					getContentResolver().update(mUri, values,
							EventContentProvider.START_TIME_NAME_COLUMN + "=? AND " + EventContentProvider.WEEK_OF_EVENTS + "=?",
							new String[] {String.valueOf(refHour.getTimeInMillis()), String.valueOf(mweekOfyear)});
				}
				refHour.add(Calendar.DAY_OF_YEAR, numberOfDay);
				cursor.close();
			}
			
		} else {
			
			for(Event event : listEvents) {
				ContentValues values = new ContentValues();
				Cursor cursor = getContentResolver().query(mUri,
						null, 
						selection,
						new String[] {String.valueOf(event.getStartTime().getTime()),event.getLabels().get(0)}, 
						null);

				List<String> labels = event.getLabels();
				for(int i = 0; i < labels.size(); i++){
					values.put(columnsLabels[i], labels.get(i));
				}
				values.put(EventContentProvider.WEEK_OF_EVENTS, mweekOfyear);
				values.put(EventContentProvider.FORMATION_ID_COLUMN, event.getFormationId());
				values.put(EventContentProvider.START_TIME_NAME_COLUMN, event.getStartTime().getTime());
				values.put(EventContentProvider.END_TIME_NAME_COLUMN, event.getEndTime().getTime());

				if(!cursor.moveToFirst()) {
					getContentResolver().insert(mUri, values);
				} else {
					getContentResolver().update(mUri, values,
							selection,
							new String[] {String.valueOf(event.getStartTime().getTime()), event.getLabels().get(0)});
				}
				cursor.close();
			}
		}
	}
	
	/**
	 * Get all events from server
	 * @author Kevin M
	 *
	 */
	private class GetEventsFromServer extends AsyncTask<String, Void, Boolean> {

		private List<Event> listEvent;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listEvent = new Vector<>();
		}
		
		@Override
		protected Boolean doInBackground(String... param) {
			
			Parser p = new Parser();
			try {
				for(int i = -1; i < numberOfWeek - 1; i++){
					listEvent = p.parseWeeklyPlanning(formationId, year, weekOfYear + i);
					addingEventDatabase(listEvent, weekOfYear + i);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			stopSelf();
		}
	}
	
	/**
	 * Check the network status
	 * @return true or false
	 */
	public boolean networkIsEnabled() {
		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
		boolean isWifiConn = networkInfo.isConnected();
		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConn = networkInfo.isConnected();

		return isMobileConn | isWifiConn;
	}
}
