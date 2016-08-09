package au.edu.uq.civil.atlasii;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import au.edu.uq.civil.atlasii.data.AtlasContract;

public class TripViewActivity extends AppCompatActivity {

    // Google Map on Trip View page
    MapView mMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String tripDate = "";
        long tripStartTime = 0;
        long tripEndTime = 0;
        long tripDistance = 0;
        double minLat = 0;
        double maxLat = 0;
        double minLong = 0;
        double maxLong = 0;

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Retrieving trip's id
        long tripID = -1;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                tripID = extras.getLong(getString(R.string.intent_msg_trip_id));
            }
        } else {
            if(savedInstanceState.getSerializable(getString(R.string.intent_msg_trip_id)) != null) {
                tripID = (long) savedInstanceState.getSerializable(
                        getString(R.string.intent_msg_trip_id));
            }
        }

        if(tripID != -1) {
            // Initiating Google Map
            mMapView = (MapView) findViewById(R.id.map_trip);
            mMapView.onCreate(savedInstanceState);

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    googleMap.setIndoorEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                }
            });

            // TODO: Add other required trip attributes
            // Retrieving trip's data from the database
            Cursor cursor = getContentResolver().query(
                    //AtlasContract.TripEntry.buildTripUri(tripID),
                    AtlasContract.TripEntry.CONTENT_URI,
                    new String[]{AtlasContract.TripEntry._ID,
                            AtlasContract.TripEntry.COLUMN_DATE,
                            AtlasContract.TripEntry.COLUMN_START_TIME,
                            AtlasContract.TripEntry.COLUMN_END_TIME,
                            AtlasContract.TripEntry.COLUMN_DISTANCE,
                            AtlasContract.TripEntry.COLUMN_MIN_LATITUDE,
                            AtlasContract.TripEntry.COLUMN_MIN_LONGITUDE,
                            AtlasContract.TripEntry.COLUMN_MAX_LATITUDE,
                            AtlasContract.TripEntry.COLUMN_MAX_LONGITUDE
                    },
                    AtlasContract.TripEntry._ID + " = ?",
                    new String[]{String.valueOf(tripID)},
                    null);
            while (cursor.moveToNext()) {
                tripDate = cursor.getString(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_DATE));
                tripStartTime = cursor.getLong(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_START_TIME));
                tripEndTime = cursor.getLong(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_END_TIME));
                tripDistance = cursor.getLong(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_DISTANCE));
                minLat = cursor.getDouble(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MIN_LATITUDE));
                maxLat = cursor.getDouble(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MAX_LATITUDE));
                minLong = cursor.getDouble(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MIN_LONGITUDE));
                maxLong = cursor.getDouble(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MAX_LONGITUDE));
            }
            // Updating trip attributes on the view
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            TextView txtTripDistance = (TextView) findViewById(R.id.txtTripDistance);
            TextView txtTripDuration = (TextView) findViewById(R.id.txtTripDuration);
            TextView txtTripDate = (TextView) findViewById(R.id.txtTripDate);
            TextView txtTripStart = (TextView) findViewById(R.id.txtTripStart);
            TextView txtTripEnd = (TextView) findViewById(R.id.txtTripEnd);
            txtTripDistance.setText(String.format("%.2f", (float) (tripDistance / 1000.0)) + "km");
            txtTripDate.setText("Trip on " + tripDate);
            long tripDurationMillis = tripEndTime - tripStartTime;
            String tripDuration = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(tripDurationMillis),
                    TimeUnit.MILLISECONDS.toMinutes(tripDurationMillis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tripDurationMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(tripDurationMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tripDurationMillis)));
            txtTripDuration.setText(tripDuration);
            txtTripStart.setText("Started at: " + formatter.format(new Date(tripStartTime)));
            txtTripEnd.setText("Finished at: " + formatter.format(new Date(tripEndTime)));

            // Retrieving trip's geo data from the database
            cursor = getContentResolver().query(
                    AtlasContract.GeoEntry.buildGeoWithTripUri(tripID),
                    new String[]{AtlasContract.GeoEntry._ID,
                            AtlasContract.GeoEntry.COLUMN_TIMESTAMP,
                            AtlasContract.GeoEntry.COLUMN_LATITUDE,
                            AtlasContract.GeoEntry.COLUMN_LONGITUDE,
                            AtlasContract.GeoEntry.COLUMN_TRIP_KEY},
                    null,
                    null,
                    AtlasContract.GeoEntry.COLUMN_TIMESTAMP + " ASC");

            ArrayList<LatLng> points = new ArrayList<LatLng>();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            while (cursor.moveToNext()) {
                LatLng point = new LatLng(
                        cursor.getDouble(cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LONGITUDE)));
                points.add(point);
                builder.include(point);
            }

            if (mMapView != null) {
                // Drawing the trip's trajectory
                final GoogleMap googleMap = mMapView.getMap();
                googleMap.addPolyline((new PolylineOptions())
                        .addAll(points)
                        .width(7)
                        .color(Color.BLUE)
                        .geodesic(true));

                // Adding the trip start/end points
                if (points.size() > 1) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(points.get(1))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title("Origin"));
                    googleMap.addMarker(new MarkerOptions()
                            .position(points.get(points.size() - 1))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .title("Destination"));

                    // Moving camera to zoom on map
                    /*if((minLat != 0) && (maxLat != 0)) {
                        LatLng pointMin = new LatLng(minLat, minLong);
                        LatLng pointMax = new LatLng(maxLat, maxLong);
                        builder.include(pointMin);
                        builder.include(pointMax);
                    }*/
                    final LatLngBounds bounds = builder.build();
                    googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition arg0) {
                            // Move camera.
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
                            // Remove listener to prevent position reset on camera move.
                            googleMap.setOnCameraChangeListener(null);
                        }
                    });
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(1), 18));
                }
            }
        }
        else {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMapView != null) {
            mMapView.onDestroy();
        }
    }
}
