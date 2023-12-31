package au.edu.uq.civil.atlasii.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;


/**
 * Created by Behrang Assemi on 16/03/2016.
 */


/**
 * Defines table and column names for the ATLAS database.
 */
public class AtlasContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "au.edu.uq.civil.atlasii";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://au.edu.uq.civil.wander/trip/ is a valid path for
    // looking at trip data. content://au.edu.uq.civil.wander/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    public static final String PATH_TRIP = "trip";
    public static final String PATH_TRIP_ALL = "trips"; // TODO: Merge with PATH_TRIP
    public static final String PATH_GEODATA = "geo";

    // To make it easy to query for the exact date, we normalise all dates that go into
    // the database to the start of the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the geodata table
     */
    public static final class GeoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GEODATA).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GEODATA;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GEODATA;

        public static Uri buildGeoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildGeoWithTripUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }

        public static String getTripIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        // Defining table name
        public static final String TABLE_NAME = "HTS_GeoData";

        // Defining table columns
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_HEADING = "heading";
        public static final String COLUMN_ACCURACY = "locationaccuracy";
        public static final String COLUMN_SPEED = "speed";
        // Column with the foreign key into the trip table.
        public static final String COLUMN_TRIP_KEY = "trip_id";
    }

    /*
     Inner class that defines the table contents of the trip table
      */
    public static final class TripEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIP).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRIP;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRIP;


        public static Uri buildTripUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTripWithDateUri(String date) {
            return CONTENT_URI.buildUpon().appendPath(date).build();
        }

        public static Uri buildTripsUri() {
            return BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIP_ALL).build();
        }

        public static String getDateFromUri(Uri uri) {
            String date = uri.getPathSegments().get(1);
            // Adding '/' between date parts
            date = date.substring(0, 2) + "/" + date.substring(2, 4) + "/" + date.substring(4, 8);

            return date;
        }

        // Defining table name
        public static final String TABLE_NAME = "HTS_Trip";

        // Defining table columns
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_START_TIME = "startTime";
        public static final String COLUMN_END_TIME = "endTime";
        public static final String COLUMN_EXPORTED = "isExported";
        public static final String COLUMN_ACTIVE = "isActive";
        public static final String COLUMN_LABELLED = "isLabelled";
        public static final String COLUMN_TRIP_ATTRIBUTES = "tripAttrs";
        public static final String COLUMN_MIN_LATITUDE = "minLatitude";
        public static final String COLUMN_MAX_LATITUDE = "maxLatitude";
        public static final String COLUMN_MIN_LONGITUDE = "minLongitude";
        public static final String COLUMN_MAX_LONGITUDE = "maxLongitude";
        public static final String COLUMN_TRIP_PURPOSE = "description";
        public static final String COLUMN_TRIP_MODES = "transport_modes";
    }
}