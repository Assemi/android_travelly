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
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import au.edu.uq.civil.atlasii.data.AtlasContract;

import static java.lang.Math.abs;

/**
 * Created by Behrang Assemi on 16/02/2016.
 */
public class LocationReceiver extends BroadcastReceiver {

    // Last known location
    Location mLastLocation = null;

    // Active trip attributes
    boolean mIsLiveTracking = false;
    long mTripID = 0;
    float mTripDistance = 0;
    int mTripDuration = 0;
    double mTripMinLat = 0;
    double mTripMaxLat = 0;
    double mTripMinLon = 0;
    double mTripMaxLon = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationResult locations = LocationResult.extractResult(intent);

        // Retrieving recorded variables
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor;
        double lastLat = Double.parseDouble(settings.getString("Location_Last_Lat", "0"));
        double lastLon = Double.parseDouble(settings.getString("Location_Last_Lon", "0"));
        long lastTimestamp = settings.getLong("Location_Last_Time", -1);
        if(lastTimestamp != -1) {
            mLastLocation = new Location("dummyprovider");
            mLastLocation.setLatitude(lastLat);
            mLastLocation.setLongitude(lastLon);
            mLastLocation.setTime(lastTimestamp);
        }
        mIsLiveTracking = settings.getBoolean("Location_Recording", false);

        if(locations != null) {
            // Extracting the last recorded location
            Location lastLocation = locations.getLastLocation();
            // Extracting location details
            long currentTimestamp = lastLocation.getTime();
            double currentLatitude = lastLocation.getLatitude();
            double currentLongitude = lastLocation.getLongitude();
            float currentHeading = lastLocation.getBearing();
            float currentAccuracy = lastLocation.getAccuracy();
            float currentSpeed = lastLocation.getSpeed();
            
            // TODO: Use projection
            // Extracting variables
            LatLng point = new LatLng(currentLatitude, currentLongitude);

            // Check the updated location to see whether it is newer than the last location or not
            if (mLastLocation != null) {
                float distance = mLastLocation.distanceTo(lastLocation);

                if (mLastLocation.getTime() < currentTimestamp) {
                    // Checking whether the recording is in progress
                    if (mIsLiveTracking) {
                        // TODO: Make sure this is the right place - THE TIME CANNOT BE COMPARED WITH LAST LOCATION
                        // Checking the idle time and the distance between the current and last known location
                        if ((abs(Calendar.getInstance().getTimeInMillis() - mLastLocation.getTime()) >= 150000) &&
                                (distance <= 100)){
                            // Stop recording the trip
                            stopRecordingTrip(context);
                        }
                        else { // Idle time is less than the specified threshold
                            // Checking the location accuracy
                            if (currentAccuracy <= 200) {
                                // Record the geodata in the database
                                persistLocation(context, currentTimestamp, currentLatitude,
                                        currentLongitude, currentHeading, currentAccuracy,
                                        currentSpeed);

                                // Update the minMax latitudes and longitudes
                                updateMinMaxCoord(context, currentLatitude, currentLongitude);

                                // Recording the last known location
                                mLastLocation = new Location("dummyprovider");
                                mLastLocation.setLatitude(point.latitude);
                                mLastLocation.setLongitude(point.longitude);
                                mLastLocation.setTime(currentTimestamp);

                                editor = settings.edit();
                                editor.putString("Location_Last_Lat", String.valueOf(mLastLocation.getLatitude()));
                                editor.putString("Location_Last_Lon", String.valueOf(mLastLocation.getLongitude()));
                                editor.putLong("Location_Last_Time", mLastLocation.getTime());
                                editor.commit();
                            }
                        }
                    }
                    else { // A recording is not in progress
                        // Check the distance between the current and last known location to see
                        // whether it is more than 50m
                        // TODO: For test only, change the threshold to 50m
                        if(distance >= 2) {
                            // If so, start recording a trip
                            mTripID = recordTrip(context, currentTimestamp, point);
                            editor = settings.edit();
                            editor.putLong("Trip_ID", mTripID);
                            editor.commit();
                        }
                    }
                }
            } else { // mLastLocation == null
                // Recording the last known location
                mLastLocation = new Location("dummyprovider");
                mLastLocation.setLatitude(point.latitude);
                mLastLocation.setLongitude(point.longitude);
                mLastLocation.setTime(currentTimestamp);

                editor = settings.edit();
                editor.putString("Location_Last_Lat", String.valueOf(mLastLocation.getLatitude()));
                editor.putString("Location_Last_Lon", String.valueOf(mLastLocation.getLongitude()));
                editor.putLong("Location_Last_Time", mLastLocation.getTime());
                editor.commit();
            }
        }
    }

    private long recordTrip(Context context, long currentTimestamp, LatLng point) {
        // Reset the last known location
        mLastLocation = null;

        // Reset min and max latitudes and longitudes
        mTripMinLat = 0;
        mTripMaxLat = 0;
        mTripMinLon = 0;
        mTripMaxLon = 0;

        // Reset the trip distance and duration
        mTripDistance = 0;
        mTripDuration = 0;

        // Set the live tracking flag
        mIsLiveTracking = true;

        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Location_Last_Lat", "0");
        editor.putString("Location_Last_Lon", "0");
        editor.putLong("Location_Last_Time", -1);
        editor.putString("Trip_Min_Lat", "0");
        editor.putString("Trip_Max_Lat", "0");
        editor.putString("Trip_Min_Lon", "0");
        editor.putString("Trip_Max_Lon", "0");
        editor.putFloat("Trip_Distance", 0);
        editor.putInt("Trip_Duration", 0);
        editor.putBoolean("Location_Recording", true);
        editor.commit();

        // Create a new trip in the database
        // Extract current date
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(timeZone);
        String tripDate = formatter.format(new Date(
                Calendar.getInstance().getTimeInMillis()));

        // Create a new map of values, where column names are the keys
        ContentValues tripValues = new ContentValues();
        tripValues.put(AtlasContract.TripEntry.COLUMN_DATE, tripDate);
        tripValues.put(AtlasContract.TripEntry.COLUMN_START_TIME, currentTimestamp);
        tripValues.put(AtlasContract.TripEntry.COLUMN_EXPORTED, false);
        tripValues.put(AtlasContract.TripEntry.COLUMN_ACTIVE, false);
        tripValues.put(AtlasContract.TripEntry.COLUMN_LABELLED, false);

        // Insert the record into the database
        Uri returnUri = context.getContentResolver().insert(
                AtlasContract.TripEntry.CONTENT_URI,
                tripValues);

        return ContentUris.parseId(returnUri);
    }

    private void updateMinMaxCoord(Context context, double currentLatitude, double currentLongitude) {
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();
        mTripMinLat = Double.parseDouble(settings.getString("Trip_Min_Lat", "0"));
        mTripMaxLat = Double.parseDouble(settings.getString("Trip_Max_Lat", "0"));
        mTripMinLon = Double.parseDouble(settings.getString("Trip_Min_Lon", "0"));
        mTripMaxLon = Double.parseDouble(settings.getString("Trip_Max_Lon", "0"));

        if (mTripMinLat > currentLatitude) {
            mTripMinLat = currentLatitude;
            editor.putString("Trip_Min_Lat", String.valueOf(mTripMinLat));
        }

        if (mTripMaxLat < currentLatitude) {
            mTripMaxLat = currentLatitude;
            editor.putString("Trip_Max_Lat", String.valueOf(mTripMaxLat));
        }

        if (mTripMinLon > currentLongitude) {
            mTripMinLon = currentLongitude;
            editor.putString("Trip_Min_Lon", String.valueOf(mTripMinLon));
        }

        if (mTripMaxLon < currentLongitude) {
            mTripMaxLon = currentLongitude;
            editor.putString("Trip_Max_Lon", String.valueOf(mTripMaxLon));
        }

        editor.commit();
    }

    /*
     Persisting the location details in the database
      */
    private long persistLocation(Context context,
                                 long timestamp, double latitude,
                                 double longitude, float heading,
                                 float accuracy, float speed) {
        long geodataRowId;
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
        mTripID = settings.getLong("Trip_ID", 0);

        // Create a new map of values, where column names are the keys
        ContentValues geodataValues = new ContentValues();
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_TIMESTAMP, timestamp);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_LATITUDE, latitude);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_LONGITUDE, longitude);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_HEADING, heading);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_ACCURACY, accuracy);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_SPEED, speed);
        geodataValues.put(AtlasContract.GeoEntry.COLUMN_TRIP_KEY, mTripID);

        // Insert the record into the database
        Uri returnUri = context.getContentResolver().insert(
                AtlasContract.GeoEntry.CONTENT_URI,
                geodataValues);
        geodataRowId = ContentUris.parseId(returnUri);

        return geodataRowId;
    }

    // TODO: This method should be completed
    private void stopRecordingTrip(Context context) {
        // Setting the recording in progress flag to false
        SharedPreferences settings = context.getSharedPreferences(context.getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Location_Recording", false);
        editor.commit();

        mIsLiveTracking = false;
    }
}
