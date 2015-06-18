package io.github.kmenager.scheduleintime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

import java.util.Calendar;
import java.util.List;

import io.github.kmenager.scheduleintime.db.FormationHelper;
import io.github.kmenager.scheduleintime.db.FormationProvider;
import io.github.kmenager.scheduleintime.providers.EventContentProvider;
import io.github.kmenager.scheduleintime.services.UpdatingEventDbService;
import io.github.kmenager.scheduleintime.services.UpdatingFormationDbService;
import io.github.kmenager.scheduleintime.ui.PlanningActivity;

public class MainActivity extends Activity {


	SharedPreferences preferences;
	UpdatedFormationDbServiceReceiver receiverFormation;
	UpdatedEventDbServiceReceiver receiverEvent;
	int year;
	int weekOfYear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(false);
		Calendar now = Calendar.getInstance();
		year = now.get(Calendar.YEAR);
		weekOfYear = now.get(Calendar.WEEK_OF_YEAR);
	}

	@Override
	protected void onStart() {
		super.onStart();

		IntentFilter filterEvent = new IntentFilter(UpdatingEventDbService.DATABASE_EVENTS_UPDATED);
		receiverEvent = new UpdatedEventDbServiceReceiver();
		registerReceiver(receiverEvent, filterEvent);

		IntentFilter filterFormation = new IntentFilter(UpdatingFormationDbService.DATABASE_FORMATIONS_UPDATED);
		receiverFormation = new UpdatedFormationDbServiceReceiver();
		registerReceiver(receiverFormation, filterFormation);

		preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		String formationId = preferences.getString(getResources().getString(R.string.formation_key), null);

		if(networkIsEnabled()) {
			if(formationId == null) {
				startUpdatingFormationDbService();
			} else if(!containsEventsDatabase(EventContentProvider.CONTENT_URI, weekOfYear, formationId)) {
				startUpdatingEventDbService(formationId, year, weekOfYear, 2);
			} else {
				Intent intent = new Intent(MainActivity.this,PlanningActivity.class);
				intent.putExtra(getResources().getString(R.string.event_intent_year), year);
				intent.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
				startActivity(intent);
				this.finish();
			}
		} else {
			if(containsEventsDatabase(EventContentProvider.CONTENT_URI, weekOfYear, formationId)) {
				Intent intent = new Intent(MainActivity.this,PlanningActivity.class);
				intent.putExtra(getResources().getString(R.string.event_intent_year), year);
				intent.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
				startActivity(intent);
				this.finish();
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(getResources().getString(R.string.error_diag_title));
				builder.setMessage(getResources().getString(R.string.error_diag_msg));
				builder.setCancelable(false);
				builder.setIcon(R.drawable.ic_alerts_and_states_error);
				builder.setPositiveButton(getResources().getString(R.string.ok), 
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
						MainActivity.this.finish();
						
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.cancel), 
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
						
					}
				});
				builder.create().show();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(receiverEvent);
		unregisterReceiver(receiverFormation);
	}

	/**
	 * Start service to update event database
	 * @param formationId the formation id
	 * @param year the year
	 * @param weekOfYear the week of the year
	 * @param numberOfWeek the number of week
	 */
	public void startUpdatingEventDbService(String formationId, int year, int weekOfYear, int numberOfWeek) {
		Intent intent = new Intent(this,UpdatingEventDbService.class);
		intent.putExtra(getResources().getString(R.string.event_intent_formation_id), formationId);
		intent.putExtra(getResources().getString(R.string.event_intent_year), year);
		intent.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
		intent.putExtra(getResources().getString(R.string.event_intent_number_of_week), numberOfWeek);
		startService(intent);
	}

	public class UpdatedEventDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent intent1 = new Intent(MainActivity.this,PlanningActivity.class);
			intent1.putExtra(getResources().getString(R.string.event_intent_year), year);
			intent1.putExtra(getResources().getString(R.string.event_intent_week_of_year), weekOfYear);
			startActivity(intent1);
			MainActivity.this.finish();
		}
	}


	/**
	 * Start service to update formation database
	 */
	private void startUpdatingFormationDbService() {
		Intent intent = new Intent(MainActivity.this,UpdatingFormationDbService.class);
		startService(intent);
	}

	/**
	 * Receiver when formation database is updated
	 * @author Kevin M
	 *
	 */
	public class UpdatedFormationDbServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			List<String> listNameFormation = intent.getStringArrayListExtra(
					getResources().getString(R.string.formation_intent_list_name));
			if(networkIsEnabled())
				showFormationDialog(listNameFormation);
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(getResources().getString(R.string.formation_diag_title));
				builder.setCancelable(false);
				builder.setMessage("Network error");
				builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				builder.create().show();
			}
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


	/**
	 * Show dialog to select formation 
	 * @param listNameFormation list name formation
	 */
	private void showFormationDialog(final List<String> listNameFormation) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listNameFormation);
		builder.setTitle(getResources().getString(R.string.formation_diag_title));
		builder.setCancelable(false);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Saved in preference the formation Id
				final FormationProvider provider = new FormationProvider(MainActivity.this);
				Cursor c = provider.getItemFormation(listNameFormation.get(which));
				String formationId = null;
				if(c != null) {
					if(c.moveToFirst()) {
						formationId = c.getString(c.getColumnIndex(FormationHelper.FORMATION_ID));
					}
				}
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString(getResources().getString(R.string.formation_key), formationId);
				editor.commit();
				
				
				startUpdatingEventDbService(formationId, year, weekOfYear, 3);
			}
		});
		builder.create().show();
	}

	
	/**
	 * Check if database is empty
	 * @param uri to events
	 * @return isEmpty ? true : false
	 */
	public boolean containsEventsDatabase(Uri uri, int currentWeek, String formationId) {
		Cursor cursor = null;
		if(formationId == null)
			return false;
		cursor = getContentResolver().query(uri,
				new String[] {EventContentProvider.FORMATION_ID_COLUMN},
				EventContentProvider.FORMATION_ID_COLUMN + " =?", 
				new String[]{formationId}, null);
		boolean state = false;


		if(cursor != null && cursor.getCount() > 0) {
			state = true;
			cursor.close();
		}

		return state;
	}
}
