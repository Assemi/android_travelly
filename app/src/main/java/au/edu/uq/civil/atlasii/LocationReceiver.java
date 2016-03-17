package au.edu.uq.civil.atlasii;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.location.LocationResult;

import java.util.Calendar;

import au.edu.uq.civil.atlasii.data.AtlasContract;

/**
 * Created by Behrang Assemi on 16/02/2016.
 */
public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationResult location = LocationResult.extractResult(intent);
        if(location != null) {
            long geodataRowId;

            // Extracting location details
            String timeStamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
            String latitude = String.valueOf(location.getLastLocation().getLatitude());
            String longitude = String.valueOf(location.getLastLocation().getLongitude());

            // TODO: add other required details
            // Persisting the location details in the database
            // Create a new map of values, where column names are the keys
            ContentValues geodataValues = new ContentValues();
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_TIMESTAMP, timeStamp);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_LATITUDE, latitude);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_LONGITUDE, longitude);
            // Insert the record into the database
            Uri returnUri = context.getContentResolver().insert(
                    AtlasContract.GeoEntry.CONTENT_URI,
                    geodataValues);
            long id = ContentUris.parseId(returnUri);
            /*// Register a content observer for our insert.  This time, directly with the content resolver
            TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
            mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, tco);
            // Fantastic.  Now that we have a location, add some weather!
            ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);
            // The TestContentObserver is a one-shot class
            tco = TestUtilities.getTestContentObserver();

            mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, tco);

            Uri weatherInsertUri = mContext.getContentResolver()
                    .insert(WeatherEntry.CONTENT_URI, weatherValues);
            assertTrue(weatherInsertUri != null);

            // Did our content observer get called?  If this fails, your insert weather
            // in your ContentProvider isn't calling
            // getContext().getContentResolver().notifyChange(uri, null);
            tco.waitForNotificationOrFail();
            mContext.getContentResolver().unregisterContentObserver(tco);

            // A cursor is your primary interface to the query results.
            Cursor weatherCursor = mContext.getContentResolver().query(
                    WeatherEntry.CONTENT_URI,  // Table to Query
                    null, // leaving "columns" null just returns all the columns.
                    null, // cols for "where" clause
                    null, // values for "where" clause
                    null // columns to group by
            );

            TestUtilities.validateCursor("testInsertReadProvider. Error validating WeatherEntry insert.",
                    weatherCursor, weatherValues);
*/
            // TODO: Move updating the map to AtlasII, after implementing providers
            //MapView mapView = (MapView) ((Activity) context).getWindow().getDecorView().findViewById(R.id.today_map);
            /*googleMap
                    .addPolyline((new PolylineOptions())
                            .add(TIMES_SQUARE, BROOKLYN_BRIDGE, LOWER_MANHATTAN,
                                    TIMES_SQUARE).width(5).color(Color.BLUE)
                            .geodesic(true));
            // move camera to zoom on map
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOWER_MANHATTAN,
                    13));*/
        }
    }
}
