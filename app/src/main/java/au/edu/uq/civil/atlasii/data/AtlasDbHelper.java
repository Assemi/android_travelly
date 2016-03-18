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
    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "HTS.db";

    public AtlasDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold geo data. A geo data entry consists of the string supplied in the
        // timestamp, latitude and longitude
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

        sqLiteDatabase.execSQL(SQL_CREATE_GEODATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // TODO: Update the database upgrade process
        // This database is only a cache, so its upgrade policy is to simply discard the data and
        // start over.
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GeoEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}