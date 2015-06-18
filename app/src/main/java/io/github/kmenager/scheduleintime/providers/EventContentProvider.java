package io.github.kmenager.scheduleintime.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class EventContentProvider extends ContentProvider{

	private static final String TAG = "EventContentProvider";
	private static final String AUTHORITY = "fr.esipe.oc3.km.provider.EventContentProvider";
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.km.events";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/events");

	public static final String DATABASE_NAME = "events.db";
	public static final int DATABASE_VERSION = 1;
	public static final String EVENTS_TABLE_NAME = "events";

	public static final String EVENT_ID = "_id";
	public static final String WEEK_OF_EVENTS = "weekOfEvents";
	public static final String FORMATION_ID_COLUMN = "formationid";
	public static final String TOPIC_NAME_COLUMN = "topic";
	public static final String TEACHERS_NAME_COLUMN = "teacher";
	public static final String CLASSROOM_NAME_COLUMN = "classroom";
	public static final String BRANCH_NAME_COLUMN = "branch";
	public static final String EXAMEN_NAME_COLUMN = "examen";
	public static final String START_TIME_NAME_COLUMN = "startime";
	public static final String END_TIME_NAME_COLUMN = "endtime";

	private static final UriMatcher mUriMatcher;
	private static final int EVENTS = 1;
	private static final int EVENTS_ID = 2;

	private EventDatabaseHelper eDbHelper;
	private static final HashMap<String, String> eventsProjectionMap;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME, EVENTS);
		mUriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME + "/#", EVENTS_ID);

		eventsProjectionMap = new HashMap<>();
		eventsProjectionMap.put(EVENT_ID, EVENT_ID);
		eventsProjectionMap.put(WEEK_OF_EVENTS, WEEK_OF_EVENTS);
		eventsProjectionMap.put(FORMATION_ID_COLUMN, FORMATION_ID_COLUMN);
		eventsProjectionMap.put(TOPIC_NAME_COLUMN, TOPIC_NAME_COLUMN);
		eventsProjectionMap.put(TEACHERS_NAME_COLUMN, TEACHERS_NAME_COLUMN);
		eventsProjectionMap.put(CLASSROOM_NAME_COLUMN, CLASSROOM_NAME_COLUMN);
		eventsProjectionMap.put(BRANCH_NAME_COLUMN, BRANCH_NAME_COLUMN);
		eventsProjectionMap.put(START_TIME_NAME_COLUMN, START_TIME_NAME_COLUMN);
		eventsProjectionMap.put(END_TIME_NAME_COLUMN, END_TIME_NAME_COLUMN);
		eventsProjectionMap.put(EXAMEN_NAME_COLUMN, EXAMEN_NAME_COLUMN);
	}

	private static class EventDatabaseHelper extends SQLiteOpenHelper {

		public EventDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + EVENTS_TABLE_NAME 
					+ " (" + EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ WEEK_OF_EVENTS + " TEXT, "
					+ FORMATION_ID_COLUMN + " TEXT, "
					+ TOPIC_NAME_COLUMN + " TEXT, "
					+ TEACHERS_NAME_COLUMN + " TEXT, "
					+ CLASSROOM_NAME_COLUMN + " TEXT, "
					+ BRANCH_NAME_COLUMN + " TEXT, "
					+ EXAMEN_NAME_COLUMN + " TEXT, "
					+ START_TIME_NAME_COLUMN + " TEXT, "
					+ END_TIME_NAME_COLUMN + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading " + EVENTS_TABLE_NAME + " from version " + oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS "
					+EVENTS_TABLE_NAME + ";");
			onCreate(db);
		}
	}



	@Override
	public boolean onCreate() {
		eDbHelper = new EventDatabaseHelper(getContext());
		return true;
	}


	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		SQLiteDatabase db = eDbHelper.getWritableDatabase();
		switch (mUriMatcher.match(uri)) {
		case EVENTS:
			break;
		case EVENTS_ID:
			where = where + "_id =? " + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.delete(EVENTS_TABLE_NAME, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case EVENTS:
			return CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		if (mUriMatcher.match(uri) != EVENTS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = eDbHelper.getWritableDatabase();
		long rowId = db.insert(EVENTS_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {


		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(EVENTS_TABLE_NAME);
		qb.setProjectionMap(eventsProjectionMap);

		switch (mUriMatcher.match(uri)) {    
		case EVENTS:
			break;
		case EVENTS_ID:
			selection = selection + "_id =? " + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = eDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {

		SQLiteDatabase db = eDbHelper.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case EVENTS:
			count = db.update(EVENTS_TABLE_NAME, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
