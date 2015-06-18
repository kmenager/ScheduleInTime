package io.github.kmenager.scheduleintime;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.List;
import java.util.Vector;

import io.github.kmenager.scheduleintime.db.FormationHelper;
import io.github.kmenager.scheduleintime.db.FormationProvider;

public class PlanningPreference extends Activity {

	private static List<String> listFormationName;
	private static List<String> listFormationId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getFormation();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, new PreferencePlanning());
		ft.commit();
	}
	/**
	 * Class to create Preference Activity without deprecated method
	 * @author Kevin M
	 *
	 */
	public static class PreferencePlanning extends PreferenceFragment implements OnSharedPreferenceChangeListener
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			ListPreference formation_lp = (ListPreference) findPreference(getResources().getString(R.string.formation_key));
			formation_lp.setEntries(entries());
			formation_lp.setEntryValues(entryValues());
			formation_lp.setSummary(formation_lp.getEntry());

			ListPreference frequency_lp = (ListPreference) findPreference(getResources().getString(R.string.frequency_key));
			frequency_lp.setSummary(frequency_lp.getEntry());
			frequency_lp.setValueIndex(4);
		}
		
		
		/**
		 * Get entries for ListPreference
		 * @return entries
		 */
		private CharSequence[] entries() {
			CharSequence[] entries = new CharSequence[listFormationName.size()];
			for(int i = 0; i< listFormationName.size(); i++) {
				entries[i] = listFormationName.get(i);
			}
			return entries;
		}
		
		/**
		 * Get entry values for ListPreference
		 * @return entryValues
		 */
		private CharSequence[] entryValues() {
			CharSequence[] entryValues = new CharSequence[listFormationId.size()];
			for(int i = 0; i< listFormationId.size(); i++) {
				entryValues[i] = listFormationId.get(i);
			}
			return entryValues;
		}
		
		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}
		
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
				updatingSummary(findPreference(key));
		}
		
		/**
		 * Refresh the summary of listpreference
		 * @param p preference
		 */
		public void updatingSummary(Preference p) {
			if( p instanceof ListPreference ) {
				ListPreference lp = (ListPreference) p;
				p.setSummary(lp.getEntry());
			}
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
		}
	}
	
	/**
	 * Get formation from database to complete
	 * ListPreference
	 */
	public void getFormation() {
		FormationProvider provider = new FormationProvider(this);
		Cursor cursor = provider.getFormations();
		listFormationName = new Vector<>();
		listFormationId = new Vector<>();
		try {
			if (cursor != null) {
				while (cursor.moveToNext()) {
					listFormationName.add(cursor.getString(cursor.getColumnIndex(FormationHelper.NAME_COLUMN)));
					listFormationId.add(cursor.getString(cursor.getColumnIndex(FormationHelper.FORMATION_ID)));
				}
			}
		} finally {
			provider.close();
			if (cursor != null)
				cursor.close();
		}
	}
}
