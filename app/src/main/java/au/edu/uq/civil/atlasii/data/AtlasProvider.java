package au.edu.uq.civil.atlasii.data;

/**
 * Created by Behrang Assemi on 16/03/2016.
 */
import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class AtlasProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // The database helper
    private AtlasDbHelper mOpenHelper;

    static final int GEODATA = 100;
    static final int GEODATA_WITH_TRIP = 101;
    static final int TRIP = 200;

    // private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
    private static final SQLiteQueryBuilder sGeoDataQueryBuilder;

    static{
        // sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sGeoDataQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        /*sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);*/

        sGeoDataQueryBuilder.setTables(
                AtlasContract.GeoEntry.TABLE_NAME);
    }

    /*//location.location_setting = ?
    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    //location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " >= ? ";

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ";*/

    /*private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == 0) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }*/

    private Cursor getGeoData(
            Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        return sGeoDataQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        This UriMatcher will match each URI to the integer constants defined above.
     */
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AtlasContract.CONTENT_AUTHORITY;

        // Using the addURI function to match each of the types.  Use the constants from
        // AtlasContract to help define the types to the UriMatcher.
        matcher.addURI(authority, AtlasContract.PATH_GEODATA, GEODATA);
        matcher.addURI(authority, AtlasContract.PATH_GEODATA + "/#", GEODATA_WITH_TRIP);
        matcher.addURI(authority, AtlasContract.PATH_TRIP, TRIP);

        return matcher;
    }

    /*
        We just create a new AtlasDbHelper for later use here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new AtlasDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Using the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GEODATA_WITH_TRIP:
                return AtlasContract.GeoEntry.CONTENT_TYPE;
            case GEODATA:
                return AtlasContract.GeoEntry.CONTENT_TYPE;
            /*case TRIP:
                return WeatherContract.LocationEntry.CONTENT_TYPE;*/
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "geo"
            case GEODATA:
            {
                retCursor = getGeoData(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "geo/#"
            case GEODATA_WITH_TRIP: {
                // TODO: Update- Implement the method
                retCursor = getGeoData(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "trip"
            case TRIP: {
                //TODO: Update- Implement the method
                retCursor = getGeoData(uri, projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case GEODATA: {
                //normalizeDate(values);
                long _id = db.insert(AtlasContract.GeoEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = AtlasContract.GeoEntry.buildGeoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            /*case TRIP: {
                long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }*/
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Getting a writable database

        // Using the uriMatcher to match the URI's we are going to handle.  If it doesn't match
        // these, throw an UnsupportedOperationException.

        // A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.

        // Returning the actual rows deleted
        return 0;
    }

    /*private void normalizeDate(ContentValues values) {
        // normalise the date value
        if (values.containsKey(AtlasContract.GeoEntry.COLUMN_TIMESTAMP)) {
            long dateValue = values.getAsLong(AtlasContract.GeoEntry.COLUMN_TIMESTAMP);
            values.put(AtlasContract.GeoEntry.COLUMN_TIMESTAMP, AtlasContract.normalizeDate(dateValue));
        }
    }*/

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // We return the number of rows impacted by the update.
        return 0;
    }

    /*@Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }*/

    // This is a method specifically to assist the testing framework in running smoothly. You can
    // read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}