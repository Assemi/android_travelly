package au.edu.uq.civil.atlasii;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.location.LocationResult;

import java.text.DateFormat;

/**
 * Created by Behrang Assemi on 16/02/2016.
 */
public class LocationReceiver extends BroadcastReceiver {

    private AtlasOpenHelper mDBHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationResult location = LocationResult.extractResult(intent);
        if(location != null) {
            // Extracting location details
            String timeStamp = DateFormat.getTimeInstance().format(location.getLastLocation().getTime());
            String latitude = String.valueOf(location.getLastLocation().getLatitude());
            String longitude = String.valueOf(location.getLastLocation().getLongitude());

            // TODO: Update the database persisting procedure- use providers, add other required details
            // Persisting the location details in the database
            // Getting the database handler
            mDBHandler = new AtlasOpenHelper(context);
            // Creating the insert string
            // TODO: move the insert string to the helper class
            String strInsert = "INSERT INTO HTS_GeoData (TimeStamp, Latitude, Longitude) values ('" +
                    timeStamp + "', " + latitude + ", " + longitude + ")";
            SQLiteDatabase db = mDBHandler.getWritableDatabase();
            db.beginTransaction();
            db.execSQL(strInsert);
            db.setTransactionSuccessful();
            db.endTransaction();
            mDBHandler.close();

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
