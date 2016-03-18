package au.edu.uq.civil.atlasii;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;

import com.google.android.gms.location.LocationResult;

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
            // Calendar.getInstance().getTimeInMillis()
            Location lastLocation = location.getLastLocation();
            String timeStamp = String.valueOf(lastLocation.getTime());
            String latitude = String.valueOf(lastLocation.getLatitude());
            String longitude = String.valueOf(lastLocation.getLongitude());
            String heading = String.valueOf(lastLocation.getBearing());
            String accuracy = String.valueOf(lastLocation.getAccuracy());
            String speed = String.valueOf(lastLocation.getSpeed());

            // Persisting the location details in the database
            // Create a new map of values, where column names are the keys
            ContentValues geodataValues = new ContentValues();
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_TIMESTAMP, timeStamp);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_LATITUDE, latitude);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_LONGITUDE, longitude);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_HEADING, heading);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_ACCURACY, accuracy);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_SPEED, speed);
            // Retrieving current trip id
            SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
            long tripID = settings.getLong("Location_Current_Trip", 0);
            geodataValues.put(AtlasContract.GeoEntry.COLUMN_TRIP_KEY, tripID);
            // Insert the record into the database
            Uri returnUri = context.getContentResolver().insert(
                    AtlasContract.GeoEntry.CONTENT_URI,
                    geodataValues);
            long id = ContentUris.parseId(returnUri);
        }
    }
}
