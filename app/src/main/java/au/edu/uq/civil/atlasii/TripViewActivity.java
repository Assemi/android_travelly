package au.edu.uq.civil.atlasii;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    private static ArrayList mTripModes = null;

    long mTripID = -1;

    // Trip mode picker dialog
    public static class ModePickerFragment extends DialogFragment {
        private String[] tripModes;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            tripModes = getResources().getStringArray(R.array.array_trip_modes);
            if(mTripModes == null)
                mTripModes = new ArrayList();  // Where the selected modes are stored
            // Create a list of currently selected items
            boolean[] checkedItems = new boolean[tripModes.length];
            for (Object o:mTripModes) {
                checkedItems[Integer.valueOf(o.toString())] = true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set the dialog title
            builder.setTitle(R.string.pick_modes)
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(R.array.array_trip_modes, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                    if (isChecked) {
                                        // If the user checked the item, add it to the selected items
                                        mTripModes.add(which);
                                    } else if (mTripModes.contains(which)) {
                                        // Else, if the item is already in the array, remove it
                                        mTripModes.remove(Integer.valueOf(which));
                                    }
                                }
                            })
                    // Set the action buttons
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String strSelectedModes = "  ";
                            TextView txtTripModes = (TextView) getActivity().findViewById(R.id.txtTripModes);
                            for (Object o:mTripModes) {
                                strSelectedModes += tripModes[Integer.valueOf(o.toString())];
                                strSelectedModes += ", ";
                            }
                            if(!strSelectedModes.equals("  "))
                                strSelectedModes = strSelectedModes.substring(0, strSelectedModes.lastIndexOf(","));
                            strSelectedModes += " > Tap for change ...";
                            txtTripModes.setText(strSelectedModes);
                        }
                    });

            return builder.create();
        }
    }

    public void showModePickerDialog(View v) {
        DialogFragment newFragment = new ModePickerFragment();
        newFragment.show(getSupportFragmentManager(), "modePicker");
    }


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
        int isLabelled = 0;
        String tripPurpose = "";
        String tripModes = "";

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
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null) {
                mTripID = extras.getLong(getString(R.string.intent_msg_trip_id));
            }
        } else {
            if(savedInstanceState.getSerializable(getString(R.string.intent_msg_trip_id)) != null) {
                mTripID = (long) savedInstanceState.getSerializable(
                        getString(R.string.intent_msg_trip_id));
            }
        }

        if(mTripID != -1) {
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
                            AtlasContract.TripEntry.COLUMN_MAX_LONGITUDE,
                            AtlasContract.TripEntry.COLUMN_LABELLED,
                            AtlasContract.TripEntry.COLUMN_TRIP_PURPOSE,
                            AtlasContract.TripEntry.COLUMN_TRIP_MODES
                    },
                    AtlasContract.TripEntry._ID + " = ?",
                    new String[]{String.valueOf(mTripID)},
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
                isLabelled = cursor.getInt(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_LABELLED));
                tripPurpose = cursor.getString(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_TRIP_PURPOSE));
                tripModes = cursor.getString(cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_TRIP_MODES));
            }
            // Updating trip attributes on the view
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            TextView txtTripDistance = (TextView) findViewById(R.id.txtTripDistance);
            TextView txtTripDuration = (TextView) findViewById(R.id.txtTripDuration);
            TextView txtTripDate = (TextView) findViewById(R.id.txtTripDate);
            //TextView txtTripStart = (TextView) findViewById(R.id.txtTripStart);
            //TextView txtTripEnd = (TextView) findViewById(R.id.txtTripEnd);
            txtTripDistance.setText(String.format("%.2f", (float) (tripDistance / 1000.0)) + "km");
            txtTripDate.setText("Trip on " + tripDate +
                    ", " + formatter.format(new Date(tripStartTime)) +
                    " - " + formatter.format(new Date(tripEndTime)));
            long tripDurationMillis = tripEndTime - tripStartTime;
            String tripDuration = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(tripDurationMillis),
                    TimeUnit.MILLISECONDS.toMinutes(tripDurationMillis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tripDurationMillis)),
                    TimeUnit.MILLISECONDS.toSeconds(tripDurationMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tripDurationMillis)));
            txtTripDuration.setText(tripDuration);
            //txtTripStart.setText("Started at: " + formatter.format(new Date(tripStartTime)));
            //txtTripEnd.setText("Finished at: " + formatter.format(new Date(tripEndTime)));
            // Showing trip purpose and modes, if the trip is labelled
            if(isLabelled != 0) {
                // Setting trip purpose
                Spinner spinnerPurpose = (Spinner) findViewById(R.id.spinner_trip_purpose);
                ArrayAdapter<CharSequence> adapterPurpose = ArrayAdapter.createFromResource(this, R.array.array_trip_purpose, android.R.layout.simple_spinner_item);
                adapterPurpose.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPurpose.setAdapter(adapterPurpose);
                if (!tripPurpose.equals(null)) {
                    int spinnerPosition = adapterPurpose.getPosition(tripPurpose);
                    spinnerPurpose.setSelection(spinnerPosition);
                }

                //Setting trip modes
                if(mTripModes != null)
                    mTripModes.clear();
                else
                    mTripModes = new ArrayList();

                String[] allTripModes = getResources().getStringArray(R.array.array_trip_modes);
                String[] selectedTripModes = tripModes.split(",");

                for (String selectedMode:selectedTripModes) {
                    for(int inx = 0; inx < allTripModes.length; ++inx) {
                        if(allTripModes[inx].equals(selectedMode)) {
                            mTripModes.add(inx);
                            break;
                        }
                    }
                }

                TextView txtTripModes = (TextView) findViewById(R.id.txtTripModes);
                txtTripModes.setText("  " +
                        tripModes.replaceAll(",", ", ") + " > Tap for change ...");
            }

            // Retrieving trip's geo data from the database
            cursor = getContentResolver().query(
                    AtlasContract.GeoEntry.buildGeoWithTripUri(mTripID),
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_label, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_label) {
            saveTrip();

            return true;
        }
        else if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveTrip() {
        // Checking whether both modes and purpose have been specified for the trip
        Spinner spinnerPurpose = (Spinner) findViewById(R.id.spinner_trip_purpose);
        long selectedPurpose = spinnerPurpose.getSelectedItemId();
        if(selectedPurpose == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Trip purpose required")
                    .setMessage("Please specify the reason for this trip.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                            /*.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })*/
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return;
        }
        if (mTripModes == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Trip mode(s) required")
                    .setMessage("Please specify the travel modes for this trip.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                            /*.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })*/
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return;
        }

        // Retrieving trip purpose and mode strings
        String strTripPurpose = spinnerPurpose.getSelectedItem().toString();
        String strTripModes = "";
        String[] tripModes = getResources().getStringArray(R.array.array_trip_modes);
        for (Object o:mTripModes) {
            strTripModes += tripModes[Integer.valueOf(o.toString())];
            strTripModes += ",";
        }
        if(!strTripModes.equals(""))
            strTripModes = strTripModes.substring(0, strTripModes.lastIndexOf(","));

        // Updating the trip labels in the database
        if(mTripID != -1) {
            ContentValues tripValues = new ContentValues();
            tripValues.put(AtlasContract.TripEntry.COLUMN_TRIP_PURPOSE,
                    strTripPurpose);
            tripValues.put(AtlasContract.TripEntry.COLUMN_TRIP_MODES,
                    strTripModes);
            tripValues.put(AtlasContract.TripEntry.COLUMN_LABELLED, true);

            // Update the record in the database
            int nRows = getContentResolver().update(
                    AtlasContract.TripEntry.CONTENT_URI,
                    tripValues,
                    AtlasContract.TripEntry._ID + " = ?",
                    new String[]{String.valueOf(mTripID)});
        }

        finish();
    }
}
