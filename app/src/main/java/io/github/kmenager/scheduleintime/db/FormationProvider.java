package io.github.kmenager.scheduleintime.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.esipe.agenda.parser.Formation;

public class FormationProvider {

	private final FormationHelper helper;

	public FormationProvider(Context context) {
		helper = new FormationHelper(context);
	}

	/**
	 * Return all formations from database
	 * @return cursor
	 */
	public Cursor getFormations() {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		String selectQuery = "SELECT  * FROM " + FormationHelper.FORMATIONS_TABLE_NAME;
		return sqLiteDb.rawQuery(selectQuery, null);
	}
	
	/**
	 * Get selected formation
	 * @param formation label of formation
	 * @return cursor
	 */
	public Cursor getItemFormation(String formation) {
		SQLiteDatabase sqLiteDb = helper.getReadableDatabase();
		return sqLiteDb.query(FormationHelper.FORMATIONS_TABLE_NAME, null,
				FormationHelper.NAME_COLUMN + " =? ", 
				new String[] {formation}, null, null, null);
	}

	/**
	 * Insert a formation in database
	 * @param formation formation to insert in database
	 * @return id
	 */
	public long insert(Formation formation) {
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(FormationHelper.GROUP_COLUMN, formation.getGroup());
		values.put(FormationHelper.NAME_COLUMN, formation.getName());
		values.put(FormationHelper.FORMATION_ID, formation.getId());

		if(!exists(formation.getId())) {
			return sqLiteDb.insert(FormationHelper.FORMATIONS_TABLE_NAME, null, values);
		}
		else {
			return sqLiteDb.update(FormationHelper.FORMATIONS_TABLE_NAME, values, 
					FormationHelper.GROUP_COLUMN + " =? AND " + FormationHelper.NAME_COLUMN + " =? "
			, null);
		}
		
	}

	/**
	 * Delete selected formation
	 * @param formation formation to delete in database
	 * @return id
	 */
	public long delete(Formation formation){
		SQLiteDatabase sqLiteDb = helper.getWritableDatabase();
		return sqLiteDb.delete(FormationHelper.FORMATIONS_TABLE_NAME, FormationHelper.FORMATION_ID+ "=? ", new String[] {formation.getId()});
	}

	/**
	 * Check if formation exist in database
	 * @param id id of formation
	 * @return true or false
	 */
	public boolean exists(String id) {

		Cursor c = getFormations();
		
		boolean isPresent = false;
		if(c.getCount() > 0){

			if(c.moveToFirst()) {
				do {
				String tmp = c.getString(c.getColumnIndex(FormationHelper.FORMATION_ID));

				if( tmp.equals(id))
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
