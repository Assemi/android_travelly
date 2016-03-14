package au.edu.uq.civil.atlasii;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Behrang Assemi on 18/02/2016.
 *
 * Database helper class
 */
public class AtlasOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String GEODATA_TABLE_NAME = "HTS_GeoData";
    private static final String GEODATA_TABLE_CREATE =
            "CREATE TABLE " + GEODATA_TABLE_NAME + " (" +
                    "TimeStamp TEXT, " +
                    "Latitude REAL, " +
                    "Longitude REAL);";

    AtlasOpenHelper(Context context) {
        super(context, "HTS.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GEODATA_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
