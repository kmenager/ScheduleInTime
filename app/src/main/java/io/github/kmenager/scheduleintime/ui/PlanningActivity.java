package io.github.kmenager.scheduleintime.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import fr.esipe.agenda.parser.Event;
import io.github.kmenager.scheduleintime.PlanningPreference;
import io.github.kmenager.scheduleintime.R;
import io.github.kmenager.scheduleintime.providers.EventContentProvider;
import io.github.kmenager.scheduleintime.receiver.AlarmManagerBroadcastReceiver;
import io.github.kmenager.scheduleintime.services.UpdatingEventDbService;
import io.github.kmenager.scheduleintime.services.UploadingToCalendar;

public class PlanningActivity extends FragmentActivity{

	private MyFragmentPagerAdapter pagerAdapter;
	private SharedPreferences preferences;
	private ViewPager pager;
	private Menu menu;

	private UpdatedEventDbServiceReceiver receiverEvent;
	private UpdatedCalendarReceiver receiverCalendar;
	private int weekOfYear;
	private int year;
	private String formationId;
	private TransparentPanel popup;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weekviewpager);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Intent intent = getIntent();
		year = intent.getIntExtra(getResources().getString(R.string.event_intent_year), 0);
		weekOfYear = intent.getIntExtra(getResources().getString(R.string.event_intent_week_of_year), 2);
		formationId = preferences.getString(getResources().getString(R.string.formation_key), null);
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("currWeek", weekOfYear);
		editor.commit();
		pager = (ViewPager) findViewById(R.id.pager);

		FragmentManager fm = getSupportFragmentManager();

		pagerAdapter = new MyFragmentPagerAdapter(this, fm);
		pagerAdapter.setData(null);
		pager.setAdapter(pagerAdapter);

		new UpdatingUiFromDatabase().execute("");

		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
			getActionBar().setTitle(getResources().getString(R.string.year_txt) + " " + String.valueOf(year));
		}
		initPopup();
	}


	/**
	 * InitPopup to display on non current page
	 */
	private void initPopup() {

		popup = (TransparentPanel) findViewById(R.id.panel);
		popup.setVisibility(View.GONE);
		TextView tv = (TextView)findViewById(R.id.tv);
		tv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pager.setCurrentItem(weekOfYear);

			}
		});
	}

	/**
	 * Check if database file exist
	 * @param name name
	 * @return true or false
	 */
	public boolean databaseExist(String name) {
		File d = getApplicationContext().getDatabasePath(name);
		return d.exists();
	}


	@Override
	protected void onStart() {
		super.onStart();
		
		IntentFilter filterEventEvent = new IntentFilter(UpdatingEventDbService.DATABASE_EVENTS_UPDATED);
		receiverEvent = new UpdatedEventDbServiceReceiver();
		registerReceiver(receiverEvent, filterEventEvent);
		
		IntentFilter filterEventCalendar = new IntentFilter(UploadingToCalendar.CALENDAR_UPDATED);
		receiverCalendar = new UpdatedCalendarReceiver();
		registerReceiver(receiverCalendar, filterEventCalendar);
		
		boolean autoRefresh = preferences.getBoolean(getResources().getString(R.string.enable_refresh_key), false);
		boolean uploadCalendar = preferences.getBoolean(getResources().getString(R.string.up_calendar_key), false);
		AlarmManagerBroadcastReceiver receiverAlarm = new AlarmManagerBroadcastReceiver();
		
		String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);
		
		if(autoRefresh) {
			receiverAlarm.SetAlarm(this);
		} else {
			receiverAlarm.CancelAlarm(this);
		}
		
		Intent intent = new Intent(this,UploadingToCalendar.class);
		
		if(uploadCalendar){
			intent.putExtra("addElement", true);
		}else {
			intent.putExtra("addElement", false);
		}
		startService(intent);
		
		if(!containsFormationDatabase(EventContentProvider.CONTENT_URI, weekOfYear, formationId)) {
			setRefreshing(true, true);
			startUpdatingEventDbService(formationId, year, weekOfYear, 2);
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiverEvent);
		unregisterReceiver(receiverCalendar);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}
	
	public class UpdatedCalendarReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, getResources().getString(R.string.calendar_updated), Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Start service to update event database
	 * @param formationId formation id
	 * @param year	year
	 * @param weekOfYear week of year
	 * @param numberOfWeek number of week
	 */
	public void startUpdatingEventDbService(String formationId, int year, int weekOfYear, int numberOfWeek) {
		Intent intent = new Intent(this,UpdatingEventDbService.class);
		intent.putExtra(getResources().getString(R.string.event_intent_formation_id), formationId);
		intent.putExtra(getResources().getString(R.string.event_intent_year), year);
		intent.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
		intent.putExtra(getResources().getString(R.string.event_intent_number_of_week), numberOfWeek);
		startService(intent);
	}
	/**
	 * Receiver when event database is updated
	 * @author Kevin M
	 *
	 */
	public class UpdatedEventDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean networkState = intent.getBooleanExtra("network", true);
			if(networkState){
				new UpdatingUiFromDatabase().execute("");
				setRefreshing(false, true);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(PlanningActivity.this);
				builder.setTitle(getResources().getString(R.string.error_diag_title));
				builder.setMessage(getResources().getString(R.string.error_diag_msg));
				builder.setPositiveButton(getResources().getString(R.string.ok), 
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
						PlanningActivity.this.finish();
						
					}
				});
				
				builder.setNegativeButton(getResources().getString(R.string.cancel), 
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				builder.create().show();
				setRefreshing(false, false);
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.title_bar, menu);
		this.menu = menu;
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_refresh:
			setRefreshing(true, true);
			preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
			String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);
			startUpdatingEventDbService(formationId, year, preferences.getInt("currWeek", weekOfYear), 2);
			break;

		case R.id.menu_settings:
			startActivity(new Intent(PlanningActivity.this,PlanningPreference.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * Set view of refresh button
	 * @param refreshing is refreshing
	 * @param isUpdated is updated
	 */
	private void setRefreshing(boolean refreshing, boolean isUpdated) {

		MenuItem item = menu.findItem(R.id.menu_refresh);
		
		if (refreshing)
			item.setActionView(R.layout.action_bar_progress);
		else {
			item.setActionView(null);
			if(isUpdated)
				Toast.makeText(PlanningActivity.this, getResources().getString(R.string.database_updated), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * AsyncTask for updating UI from database
	 * @author Kevin M
	 *
	 */
	public class UpdatingUiFromDatabase extends AsyncTask<String, Void, SparseArray<Vector<Event>>> {

		private Uri mUri; 
		private ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mUri = EventContentProvider.CONTENT_URI;

			dialog = ProgressDialog.show(PlanningActivity.this, 
					null,
					getResources().getString(R.string.event_refresh_prog_diag)+"...",
					true);
			dialog.setCancelable(false);
		}

		@Override
		protected SparseArray<Vector<Event>> doInBackground(String... params) {

			SparseArray<Vector<Event>> eventsByWeek = new SparseArray<>();
			Cursor cursor = null;
			int t = - 1;
			while ( t < 2 ) {

				int mweekOfYear = preferences.getInt("currWeek", weekOfYear) + t;
				cursor = getContentResolver().query(mUri, null,
						EventContentProvider.WEEK_OF_EVENTS + "=?",
						new String[] { String.valueOf(mweekOfYear) },
						null);

				Calendar now = Calendar.getInstance();
				now.setFirstDayOfWeek(Calendar.MONDAY);
				now.set(Calendar.WEEK_OF_YEAR, mweekOfYear);
				now.set(Calendar.HOUR_OF_DAY, 6);
				now.set(Calendar.MINUTE, 0);
				now.set(Calendar.SECOND, 0);
				
				Vector<Event> listEvents = new Vector<>();
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						do {
							Event event = new Event();
							List<String> labels = new Vector<>();
							String startTime1 = cursor.getString(cursor
								.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN));
							Calendar nml = Calendar.getInstance();
							nml.setTime(new Date(Long.parseLong(startTime1)));
							if(now.get(Calendar.HOUR_OF_DAY) == nml.get(Calendar.HOUR_OF_DAY)){
								event.setFormationId(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.FORMATION_ID_COLUMN)));
								long date = Long
										.parseLong(cursor.getString(cursor
												.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN)));
								event.setStartTime(new Date(date));
								labels.add(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.TOPIC_NAME_COLUMN)));
								event.setLabels(labels);
								listEvents.add(event);
							}
							else {
								event.setFormationId(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.FORMATION_ID_COLUMN)));
								long date = Long
										.parseLong(cursor.getString(cursor
												.getColumnIndex(EventContentProvider.START_TIME_NAME_COLUMN)));
								event.setStartTime(new Date(date));

								date = Long
										.parseLong(cursor.getString(cursor
												.getColumnIndex(EventContentProvider.END_TIME_NAME_COLUMN)));
								event.setEndTime(new Date(date));

								labels.add(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.TOPIC_NAME_COLUMN)));
								labels.add(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.TEACHERS_NAME_COLUMN)));
								labels.add(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.BRANCH_NAME_COLUMN)));
								labels.add(cursor.getString(cursor
										.getColumnIndex(EventContentProvider.CLASSROOM_NAME_COLUMN)));
								event.setLabels(labels);

								listEvents.add(event);
							}
						} while (cursor.moveToNext());
						eventsByWeek.put(mweekOfYear, listEvents);
					}
					t++;
				}
			}
			if (cursor != null)
				cursor.close();
			return eventsByWeek;

		}


		@Override
		protected void onPostExecute(SparseArray<Vector<Event>> result) {
			super.onPostExecute(result);
			dialog.dismiss();
			updatingUi(result);

		}
	}

	//-- End Get Event from DB-----------------


	//-----------------------------------------
	//  Updating UI
	//-----------------------------------------

	/**
	 * Update UI with the result of the AsyncTask Class
	 * @param result result
	 */
	public void updatingUi(SparseArray<Vector<Event>> result) {

		pagerAdapter.setData(result);
		pagerAdapter.notifyDataSetChanged();
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(preferences.getInt("currWeek", weekOfYear));
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				SharedPreferences.Editor editor = preferences.edit();
				if(position == weekOfYear){
					popup.startAnimation(AnimationUtils.loadAnimation(PlanningActivity.this, R.anim.popup_hide));
					popup.setVisibility(View.GONE);
				}else if(popup.getVisibility() != View.VISIBLE) {
					popup.setVisibility(View.VISIBLE);
					popup.startAnimation(AnimationUtils.loadAnimation(PlanningActivity.this, R.anim.popup_show));
					TextView tv = (TextView)findViewById(R.id.tv);
					tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							pager.setCurrentItem(weekOfYear);
							getActionBar().setTitle(getResources().getString(R.string.year_txt) + " " + String.valueOf(year));

						}
					});
					
				}

				if(!containsEvent(position)) {
					setRefreshing(true, true);
					startUpdatingEventDbService(formationId, year, position, 2);
				}
				editor.putInt("currWeek", pager.getCurrentItem());
				editor.commit();
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


			}

			@Override
			public void onPageScrollStateChanged(int state) {
				preferences = PreferenceManager.getDefaultSharedPreferences(PlanningActivity.this);
				int pageCount = pagerAdapter.getCount();
				if(state == ViewPager.SCROLL_STATE_IDLE) {
					if ( pager.getCurrentItem() == 0){
						pager.setCurrentItem(pageCount - 2, false);
						int intYear = year - 1;
						if (getActionBar() != null)
							getActionBar().setTitle(getResources().getString(R.string.year_txt) + " " + String.valueOf(intYear));
					} else if (pager.getCurrentItem() == pageCount-1){
						if (getActionBar() != null)
							getActionBar().setTitle(getResources().getString(R.string.year_txt) + " " + String.valueOf(year));
						pager.setCurrentItem(1, false);
					}
				}
			}
		}); 
	}

	/**
	 * Check if database contains event
	 * @param week
	 * @return true or false
	 */
	public boolean containsEvent(int week) {
		Uri uri = EventContentProvider.CONTENT_URI;
		Cursor cursor = getContentResolver().query(uri, null, EventContentProvider.WEEK_OF_EVENTS + "=?", new String[] {String.valueOf(week)}, null);
		return cursor.moveToFirst();
	}
	
	/**
	 * Check if database contains formations
	 * @param uri uri
	 * @param currentWeek current week
	 * @param formationId formation id
	 * @return true or false
	 */
	public boolean containsFormationDatabase(Uri uri, int currentWeek, String formationId) {
		Cursor cursor = null;
		if(formationId == null)
			return false;
		cursor = getContentResolver().query(uri,
				new String[] {EventContentProvider.FORMATION_ID_COLUMN},
				EventContentProvider.FORMATION_ID_COLUMN + " =?", 
				new String[]{formationId}, null);
		boolean state = false;


		if(cursor != null && cursor.getCount() > 0)
			state = true;

		if (cursor != null)
			cursor.close();
		return state;
	}
}
