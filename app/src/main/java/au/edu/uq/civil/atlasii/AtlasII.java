package au.edu.uq.civil.atlasii;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Calendar;

import au.edu.uq.civil.atlasii.data.AtlasContract;

/**
 * Main activity of the app:
 * It shows a pager layout with the application tabs.
 **/
public class AtlasII extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener {

    // Permission constants
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 100;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 200;

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * ATTENTION: This was auto-generated to implement the Google API.
     */
    private GoogleApiClient mGoogleApiClient = null;

    // Location request
    LocationRequest mLocationRequest = null;

    // Fragment manager
    FragmentManager mFragmentManager;

    // App Notifications
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atlas_ii);

        /*
            Initiating app's visual appearance
         */
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the primary sections of the
        // activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Set the action bar icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_tab_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_history);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_profile);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_help);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_info);


        /*
            Initiating Google Play Services
         */
        // Checking whether Google Play Services are available or not
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status == ConnectionResult.SUCCESS) {
            createLocationRequest();
            // Create an instance of GoolgeAPIClient
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
            }

            mFragmentManager = getSupportFragmentManager();
        } else { // Error: Google Play Services are not available
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            finish();
        }

        /*
            Initiating app notification services
         */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean("SENT_TOKEN_TO_SERVER", false);
                if (sentToken) {

                } else {

                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        // Checking required permissions
        checkPermissions();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    /**
     *  Check permissions
     */
    private void checkPermissions() {
        // Check storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            // The callback method gets the result of the request.
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // The callback method gets the result of the request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied! Disable the
                    // functionality that depends on this permission.
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Sorry, without the required permission, the app cannot work properly!")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied! Disable the
                    // functionality that depends on this permission.
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Sorry, without the required permission, the app cannot work properly!")
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
                }
                return;
            }

            default:
                break;
        }
    }


    /**
     *  Notifications
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter("REGISTRATION_COMPLETE"));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Location services
     */
    // Creating location request by specifying the request parameters
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Setting location request parameters
        long LOCATION_INTERVAL = 10000; // 10 seconds
        long LOCATION_FASTEST_INTERVAL = 2000; // 2 seconds
        int LOCATION_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
        SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("Location_Interval", LOCATION_INTERVAL);
        editor.putLong("Location_Fastest_Interval", LOCATION_FASTEST_INTERVAL);
        editor.putInt("Location_Accuracy", LOCATION_ACCURACY);
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LOCATION_ACCURACY);
        editor.putBoolean("Location_Recording", false);
        editor.putLong("Location_Current_Trip", 0);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // TODO: Delete, if not used anymore
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                TextView textView = (TextView) this.findViewById(R.id.section_label);
                textView.setText("Latitude = " + String.valueOf(mLastLocation.getLatitude()) +
                        " Longitude = " + String.valueOf(mLastLocation.getLongitude()));
            }
        }*/

        // TODO: Update this section- remove if not used anymore
        // Checking the current state of the location settings
        //PendingResult<LocationSettingsResult> result =
        //        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
        //                builder.build());

        // Creating a log file
        /*try {
            logFile = openFileOutput("ATLAS_Log.log", Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        // Request location updates
        //if (mRequestingLocationUpdates) {
        /*if (true) {
            startLocationUpdates();
        }*/

        Intent intent = new Intent(this, LocationReceiver.class);
        PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 14872, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Creating a location request
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, locationIntent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        /// UPDATE: Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        /// UPDATE: Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /*@Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
        updateLog();
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor> {
        /**
         * ContentObserver class for Trip changes
         */
        public class TripObserver extends ContentObserver {
            Context context;

            public TripObserver(Handler handler, Context context) {
                super(handler);
                this.context = context;
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                updateHistory();
            }
        }

        public void updateHistory() {
            final Bundle extras = new Bundle();
            final Activity atlasActivity = getActivity();
            if(atlasActivity != null) {
                final ListView listViewToUploadTrips = (ListView) atlasActivity.findViewById(R.id.listView_ToUploadTrips);

                if (listViewToUploadTrips != null) {
                    // Extracting trips to upload
                    Cursor cursorToUploadTrips = getContext().getContentResolver().query(
                            AtlasContract.TripEntry.CONTENT_URI,
                            new String[]{AtlasContract.TripEntry._ID,
                                    "count(" + AtlasContract.TripEntry.COLUMN_DATE + ")",
                                    AtlasContract.TripEntry.COLUMN_DATE},
                            AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                    AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                            new String[]{"0", "0"},
                            AtlasContract.TripEntry.COLUMN_DATE + " ASC");
                    CursorAdapter cursorAdapter = new CursorAdapter(
                            getContext(),
                            cursorToUploadTrips,
                            0) {
                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return LayoutInflater.from(context).inflate(
                                    R.layout.listitem_trip, parent, false);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            TextView textViewTrip = (TextView) view.findViewById(R.id.textview_trip);
                            TextView textViewTripCount = (TextView) view.findViewById(R.id.textview_tripcount);

                            // Set the labelled trips' icon
                            ImageView imageView = (ImageView) view.findViewById(R.id.icon_trip);
                            imageView.setImageResource(R.drawable.ic_to_upload);
                                    /*TimeZone timeZone = Calendar.getInstance().getTimeZone();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    formatter.setTimeZone(timeZone);*/
                            String strTripDate = cursor.getString(cursor.getColumnIndexOrThrow(
                                    AtlasContract.TripEntry.COLUMN_DATE));
                            int tripCount = cursor.getInt(
                                    cursor.getColumnIndexOrThrow("count(" + AtlasContract.TripEntry.COLUMN_DATE + ")"));
                            //String tripDate = formatter.format(new Date(Long.parseLong(strTripDate)));
                            //textViewTrip.setText(tripDate);
                            textViewTrip.setText(strTripDate);
                            textViewTripCount.setText(tripCount + " trip(s)");

                            // Specify click action for the listview items
                            listViewToUploadTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                // Redirecting to trip details page
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position,
                                                        long id) {
                                    Intent intent = new Intent(getContext(), TripsActivity.class);
                                    // Extract selected trips' date
                                    String tripsDate = (String) ((TextView) view.
                                            findViewById(R.id.textview_trip))
                                            .getText();
                                    extras.putBoolean(getContext().getString(
                                            R.string.intent_msg_trips_exported),
                                            false);
                                    extras.putString(getContext().getString(
                                            R.string.intent_msg_trips_date),
                                            tripsDate);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                            });
                        }
                    };

                    // Attach cursor adapter to the ListView
                    listViewToUploadTrips.setAdapter(cursorAdapter);
                }
            }
        }

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Last location
        Location mLastLocation = null;

        // Google Map on Today tab
        MapView mMapView = null;

        // Loader ID
        private static final int GEO_LOADER = 0;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
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
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(GEO_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            final TextView textView;

            switch (sectionNumber) {
                case 1: // Today tab
                    // Loading the today page
                    rootView = inflater.inflate(R.layout.atlas_today_page, container, false);

                    // Initiating Google Map
                    mMapView = (MapView) rootView.findViewById(R.id.today_map);
                    mMapView.onCreate(savedInstanceState);

                    mMapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            googleMap.setIndoorEnabled(true);
                            googleMap.getUiSettings().setZoomControlsEnabled(true);
                        }
                    });

                    break;

                case 2: // History tab
                    final Bundle extras = new Bundle();
                    rootView = inflater.inflate(R.layout.atlas_history_page, container, false);
                    //final ListView listViewUploadedTrips = (ListView) rootView.findViewById(R.id.listView_UploadedTrips);
                    final ListView listViewToUploadTrips = (ListView) rootView.findViewById(R.id.listView_ToUploadTrips);

                    // Adding content observer to update the trips list, when the list changes in the DB
                    getContext().getContentResolver().registerContentObserver(
                            AtlasContract.TripEntry.CONTENT_URI,
                            true,
                            new TripObserver(new Handler(), getContext()));

                    // TODO: Add comments
                    // Extracting trips to upload
                    Cursor cursorToUploadTrips = getContext().getContentResolver().query(
                            AtlasContract.TripEntry.CONTENT_URI,
                            new String[]{AtlasContract.TripEntry._ID,
                                    "count(" + AtlasContract.TripEntry.COLUMN_DATE + ")",
                                    AtlasContract.TripEntry.COLUMN_DATE},
                            AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                    AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                            new String[]{"0", "0"},
                            AtlasContract.TripEntry.COLUMN_DATE + " ASC");
                    CursorAdapter cursorAdapter = new CursorAdapter(
                            getContext(),
                            cursorToUploadTrips,
                            0) {
                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return LayoutInflater.from(context).inflate(
                                    R.layout.listitem_trip, parent, false);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            TextView textViewTrip = (TextView) view.findViewById(R.id.textview_trip);
                            TextView textViewTripCount = (TextView) view.findViewById(R.id.textview_tripcount);

                            // Set the labelled trips' icon
                            ImageView imageView = (ImageView) view.findViewById(R.id.icon_trip);
                            imageView.setImageResource(R.drawable.ic_to_upload);
                            /*TimeZone timeZone = Calendar.getInstance().getTimeZone();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            formatter.setTimeZone(timeZone);*/
                            String strTripDate = cursor.getString(cursor.getColumnIndexOrThrow(
                                    AtlasContract.TripEntry.COLUMN_DATE));
                            int tripCount = cursor.getInt(
                                    cursor.getColumnIndexOrThrow("count(" + AtlasContract.TripEntry.COLUMN_DATE + ")"));
                            //String tripDate = formatter.format(new Date(Long.parseLong(strTripDate)));
                            //textViewTrip.setText(tripDate);
                            textViewTrip.setText(strTripDate);
                            textViewTripCount.setText(tripCount + " trip(s)");

                            // Specify click action for the listview items
                            listViewToUploadTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                // Redirecting to trip details page
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position,
                                                        long id) {
                                    Intent intent = new Intent(getContext(), TripsActivity.class);
                                    // Extract selected trips' date
                                    String tripsDate = (String) ((TextView) view.
                                            findViewById(R.id.textview_trip))
                                            .getText();
                                    extras.putBoolean(getContext().getString(
                                            R.string.intent_msg_trips_exported),
                                            false);
                                    extras.putString(getContext().getString(
                                            R.string.intent_msg_trips_date),
                                            tripsDate);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                            });
                        }
                    };

                    // Attach cursor adapter to the ListView
                    listViewToUploadTrips.setAdapter(cursorAdapter);


                    /*// Extracting uploaded trips
                    Cursor cursorUploadedTrips = getContext().getContentResolver().query(
                            AtlasContract.TripEntry.CONTENT_URI,
                            new String[]{AtlasContract.TripEntry._ID,
                                    "count(" + AtlasContract.TripEntry.COLUMN_DATE + ")",
                                    AtlasContract.TripEntry.COLUMN_DATE},
                            AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                    AtlasContract.TripEntry.COLUMN_EXPORTED + " != ?",
                            new String[]{"0", "0"},
                            AtlasContract.TripEntry.COLUMN_DATE + " ASC");
                    cursorAdapter = new CursorAdapter(
                            getContext(),
                            cursorUploadedTrips,
                            0) {
                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return LayoutInflater.from(context).inflate(
                                    R.layout.listitem_trip, parent, false);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            TextView textViewTrip = (TextView) view.findViewById(R.id.textview_trip);
                            TextView textViewTripCount = (TextView) view.findViewById(R.id.textview_tripcount);

                            // Set the labelled trips' icon
                            ImageView imageView = (ImageView) view.findViewById(R.id.icon_trip);
                            imageView.setImageResource(R.drawable.ic_done);
                            String strTripDate = cursor.getString(cursor.getColumnIndexOrThrow(
                                    AtlasContract.TripEntry.COLUMN_DATE));
                            int tripCount = cursor.getInt(
                                    cursor.getColumnIndexOrThrow("count(" + AtlasContract.TripEntry.COLUMN_DATE + ")"));
                            //String tripDate = formatter.format(new Date(Long.parseLong(strTripDate)));
                            //textViewTrip.setText(tripDate);
                            textViewTrip.setText(strTripDate);
                            textViewTripCount.setText(tripCount + " trip(s)");

                            // Specify click action for the listview items
                            listViewUploadedTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                // Redirecting to trip details page
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position,
                                                        long id) {
                                    Intent intent = new Intent(getContext(), TripsActivity.class);
                                    // Extract selected trips' date
                                    String tripsDate = (String) ((TextView) view.
                                            findViewById(R.id.textview_trip))
                                            .getText();
                                    extras.putBoolean(getContext().getString(
                                            R.string.intent_msg_trips_exported),
                                            true);
                                    extras.putString(getContext().getString(
                                            R.string.intent_msg_trips_date),
                                            tripsDate);
                                    intent.putExtras(extras);
                                    startActivity(intent);
                                }
                            });
                        }
                    };

                    // Attach cursor adapter to the ListView
                    listViewUploadedTrips.setAdapter(cursorAdapter);*/

                    /*ViewGroup.LayoutParams params = listViewUploadedTrips.getLayoutParams();
                    params.height = 500;
                    listViewUploadedTrips.setLayoutParams(params);
                    listViewUploadedTrips.requestLayout();*/

                    break;

                case 3: // Profile tab
                    rootView = inflater.inflate(R.layout.atlas_profile_page, container, false);

                    // Retrieving the participant's username and email
                    SharedPreferences settings = getActivity().getSharedPreferences(getContext().getString(R.string.shared_preferences), 0);
                    String username = settings.getString("Username", "");
                    String email = settings.getString("Email", "");

                    // Showing the participant's username and email
                    TextView textViewUsername = (TextView) rootView.findViewById(R.id.textview_profile_username);
                    TextView textViewEmail = (TextView) rootView.findViewById(R.id.textview_profile_email);
                    textViewUsername.setText("Username: " + username);
                    textViewEmail.setText("Email: " + email);

                    // Setting the button action listeners
                    // Logout button:
                    Button logoutButton = (Button) rootView.findViewById(R.id.button_logout);
                    logoutButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signOut();
                        }
                    });
                    // Take Survey button:
                    Button takeSurveyButton = (Button) rootView.findViewById(R.id.button_surveys);
                    takeSurveyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            takeSurvey();
                        }
                    });

                    //For test, remove later
                    //ListView participantListView = (ListView) rootView.findViewById(R.id.participant_info);
                    //ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem_text, new String[]{"A", "B"});
                    //participantListView.setAdapter(stringArrayAdapter);

                    break;

                case 4: // Help tab
                    // Setting the help pages' url
                    String helpURL = getString(R.string.atlas_help_url);
                    // Loading the help pages
                    rootView = inflater.inflate(R.layout.atlas_help_pages, container, false);
                    WebView webView = (WebView) rootView.findViewById(R.id.webView_helpPages);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(helpURL);

                    break;

                case 5: // Info tab
                    // Retrieving the app's version
                    String pVersion = "X.X.X";
                    PackageManager pManager = getActivity().getPackageManager();
                    String pName = getActivity().getPackageName();
                    try {
                        pVersion = pManager.getPackageInfo(pName, 0).versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    // Getting the current date for copyright information
                    Calendar curCal = Calendar.getInstance();

                    // Loading the about page
                    rootView = inflater.inflate(R.layout.atlas_about_page, container, false);
                    textView = (TextView) rootView.findViewById(R.id.txtAboutAtlas);
                    textView.setText("ATLAS II Version " + pVersion + "\r\n" +
                            "Copyright 2012 - " + curCal.get(Calendar.YEAR) + "\r\n" +
                            "University of Queensland, All rights reserved." + "\r\n" +
                            "Tertiary Student Travel Survey 2016" + "\r\n" +
                            "For more information please visit:" + "\r\n" +
                            Html.fromHtml(
                                    "<a href=\"http://civil.uq.edu.au/atlas\">civil.uq.edu.au/atlas</a>"));
                    textView.setMovementMethod(LinkMovementMethod.getInstance());

                    // For test, remove the button and the code when done
                    /*Button testButton = (Button) rootView.findViewById(R.id.btn_test);
                    testButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ContentValues tripValues = new ContentValues();
                            tripValues.put(AtlasContract.TripEntry.COLUMN_EXPORTED, false);
                            int nRows = getContext().getContentResolver().update(
                                    AtlasContract.TripEntry.CONTENT_URI,
                                    tripValues,
                                    null,
                                    null);
                        }
                    });*/
                    /*Button testButton = (Button) rootView.findViewById(R.id.btn_test);
                    testButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            signOut();
                        }
                    });*/
                    ////////////////////////////////////////////////////////////

                    break;
            }

            return rootView;
        }

        private void takeSurvey() {
            // The user is redirected to the surveys activity
            Intent intent = new Intent(getContext(), SurveyListActivity.class);
            startActivity(intent);
        }

        private void signOut() {
            // Retrieving shared preferences to remove the credentials
            SharedPreferences settings = getActivity().getSharedPreferences(getContext().getString(R.string.shared_preferences), 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("Username");
            editor.remove("Password");
            editor.remove("Email");
            editor.commit();

            // The user is redirected to the login activity
            Intent intent = new Intent(getContext(), RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // Sort order:  Ascending, by date.
            String sortOrder = AtlasContract.GeoEntry.COLUMN_TIMESTAMP + " ASC";
            // TODO: Update to show the data only for the current trip
            Uri geoUri = AtlasContract.GeoEntry.CONTENT_URI;

            return new CursorLoader(getActivity(),
                    geoUri,
                    null,
                    null,
                    null,
                    sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // TODO: Complete this method
            // Update today's map
            if(data.moveToLast()) {
                // Extracting variables
                long currentTimestamp = data.getLong(data.getColumnIndex(AtlasContract.GeoEntry.COLUMN_TIMESTAMP));
                SharedPreferences settings = getActivity().getSharedPreferences(getContext().getString(R.string.shared_preferences), 0);
                boolean isLiveTracking = settings.getBoolean("Location_Recording", false);

                LatLng point = new LatLng(
                        data.getDouble(data.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LATITUDE)),
                        data.getDouble(data.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LONGITUDE))
                );

                // Check the updated location to see whether it is newer than the last location or not
                if (mLastLocation != null) {
                    if (mLastLocation.getTime() < currentTimestamp) {
                        // Checking whether the recording is in progress
                        if (isLiveTracking) {
                            // Updating the today's map
                            if (mMapView != null) {
                                // Extracting the last location's coordinates
                                LatLng lastLatLng = new LatLng(
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude());
                                GoogleMap googleMap = mMapView.getMap();
                                googleMap.addPolyline((new PolylineOptions())
                                        .add(lastLatLng, point)
                                        .width(7)
                                        .color(Color.BLUE)
                                        .geodesic(true));
                                // move camera to zoom on map
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 18));
                            }

                            // TODO: Update trip's distance and duration
                            // mActiveTrip_Distance
                            // mActiveTrip_Duration

                            // TODO: Ensure everything necessary is correctly recorded in the shared preferences
                            // Recording the last known location
                            mLastLocation = new Location("dummyprovider");
                            mLastLocation.setLatitude(point.latitude);
                            mLastLocation.setLongitude(point.longitude);
                            mLastLocation.setTime(currentTimestamp);
                        }
                        else { // A recording is not in progress
                            mLastLocation = null;
                        }
                    }
                }
                else if(isLiveTracking) { // mLastLocation == null
                    // Recording the last known location
                    mLastLocation = new Location("dummyprovider");
                    mLastLocation.setLatitude(point.latitude);
                    mLastLocation.setLongitude(point.longitude);
                    mLastLocation.setTime(currentTimestamp);

                    // This is the origin: Add a marker to the map
                    if (mMapView != null) {
                        GoogleMap googleMap = mMapView.getMap();
                        googleMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title("Origin"));

                        // move camera to zoom on map
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 18));
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // TODO: Complete this method
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            //return 4;
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Today";
                case 1:
                    return "History";
                case 2:
                    return "Profile";
                case 3:
                    return "Help";
                case 4:
                    return "Info";
            }
            return null;
        }
    }
}

