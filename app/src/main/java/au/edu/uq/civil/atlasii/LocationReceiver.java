package au.edu.uq.civil.atlasii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.LocationResult;

import java.text.DateFormat;

/**
 * Created by Behrang Assemi on 16/02/2016.
 */
public class LocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationResult location = LocationResult.extractResult(intent);
        if(location != null) {
            /*String locLog = DateFormat.getTimeInstance().format(location.getLastLocation().getTime()) +
                    " Lat: " + String.valueOf(location.getLastLocation().getLatitude()) +
                    " Long: " + String.valueOf(location.getLastLocation().getLongitude());*/
            String timeStamp = DateFormat.getTimeInstance().format(location.getLastLocation().getTime());
            String latitude = String.valueOf(location.getLastLocation().getLatitude());
            String longitude = String.valueOf(location.getLastLocation().getLongitude());
            //Log.d("Location", locLog);
            /*AtlasOpenHelper dbHandler = new AtlasOpenHelper(context);
            String temp = "insert into HTS_GeoData (TimeStamp, Longitude, Latitude) values ('" +
                    timeStamp + "', " + longitude + ", " + latitude + ")";
            SQLiteDatabase db = dbHandler.getWritableDatabase();
            db.beginTransaction();
            db.execSQL("insert into HTS_GeoData (TimeStamp, Longitude, Latitude) values ('" +
                    timeStamp + "', " + longitude + ", " + latitude + ")");
            db.setTransactionSuccessful();
            db.endTransaction();
            dbHandler.close();*/
        }
    }
}
