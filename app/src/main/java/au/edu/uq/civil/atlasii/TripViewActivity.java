package au.edu.uq.civil.atlasii;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

import java.util.ArrayList;

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
        long tripID;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                tripID = 0;
            } else {
                tripID = extras.getLong(getString(R.string.intent_msg_trip_id));
            }
        } else {
            tripID = (long) savedInstanceState.getSerializable(
                    getString(R.string.intent_msg_trip_id));
        }

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

        // Retrieving trip's geo data from the database
        Cursor cursor = getContentResolver().query(
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
        while(cursor.moveToNext())
        {
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
            googleMap.addMarker(new MarkerOptions()
                    .position(points.get(1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("Origin"));
            googleMap.addMarker(new MarkerOptions()
                    .position(points.get(points.size() - 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("Destination"));

            // Moving camera to zoom on map
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
