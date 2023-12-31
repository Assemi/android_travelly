package au.edu.uq.civil.atlasii.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static au.edu.uq.civil.atlasii.data.AtlasContract.*;

/**
 * Created by Behrang Assemi on 18/02/2016.
 * Database helper class
 */
public class AtlasDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 12;

    static final String DATABASE_NAME = "HTS_ATLAS.db";

    public AtlasDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO: Add the foreign key to GeoData table
        // Create a table to hold geo data.
        final String SQL_CREATE_GEODATA_TABLE = "CREATE TABLE " + GeoEntry.TABLE_NAME +
                " (" +
                GeoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GeoEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                GeoEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                GeoEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                GeoEntry.COLUMN_HEADING + " REAL NOT NULL, " +
                GeoEntry.COLUMN_ACCURACY + " REAL NOT NULL, " +
                GeoEntry.COLUMN_SPEED + " REAL NOT NULL, " +
                GeoEntry.COLUMN_TRIP_KEY + " INTEGER NOT NULL" +
                ");";
                /* Sample foreign key:
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";*/

        // Create a table to hold trip data.
        final String SQL_CREATE_TRIP_TABLE = "CREATE TABLE " + TripEntry.TABLE_NAME +
                " (" +
                TripEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TripEntry.COLUMN_ACTIVE + " INTEGER NOT NULL, " +
                TripEntry.COLUMN_EXPORTED + " INTEGER NOT NULL, " +
                TripEntry.COLUMN_LABELLED + " INTEGER NOT NULL, " +
                TripEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                TripEntry.COLUMN_START_TIME + " INTEGER NOT NULL, " +
                TripEntry.COLUMN_END_TIME + " INTEGER, " +
                TripEntry.COLUMN_DISTANCE + " INTEGER, " +
                TripEntry.COLUMN_TRIP_ATTRIBUTES+ " TEXT, " +
                TripEntry.COLUMN_MIN_LATITUDE + " REAL, " +
                TripEntry.COLUMN_MAX_LATITUDE + " REAL, " +
                TripEntry.COLUMN_MIN_LONGITUDE + " REAL, " +
                TripEntry.COLUMN_MAX_LONGITUDE + " REAL, " +
                TripEntry.COLUMN_TRIP_PURPOSE + " TEXT, " +
                TripEntry.COLUMN_TRIP_MODES + " TEXT" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_GEODATA_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRIP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache, so its upgrade policy is to simply discard the data and
        // start over.
        // Note that this only fires if you change the version number of the database.
        // It does NOT depend on the version number of the application.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GeoEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TripEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}