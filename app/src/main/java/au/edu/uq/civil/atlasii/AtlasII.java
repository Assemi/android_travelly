package au.edu.uq.civil.atlasii;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
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

        // TODO: Update the database handling procedures, using providers
        // Get the handler to the database
        // mDBHandler = new AtlasDbHelper(this.getApplicationContext());
    }


    /**
     * Location services
     */
    // Creating location request by specifying the request parameters
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Setting location request parameters
        long LOCATION_INTERVAL = 2000; // 2 seconds
        long LOCATION_FASTEST_INTERVAL = 1000; // 1 seconds
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
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(GEO_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            TextView textView;

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
                    rootView = inflater.inflate(R.layout.atlas_history_page, container, false);

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

                    // TODO: For test, remove later
                    //ListView participantListView = (ListView) rootView.findViewById(R.id.participant_info);
                    //ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem_text, new String[]{"A", "B"});
                    //participantListView.setAdapter(stringArrayAdapter);

                    break;

                case 4: // Help tab
                    // Setting the help pages' url
                    String helpURL = "http://atlaservt.somee.com/mobile/index.html";
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
                            "For more information please visit:" + "\r\n" +
                            Html.fromHtml(
                                    "<a href=\"http://www.civil.uq.edu.au/atlas\">www.civil.uq.edu.au/atlas</a>"));
                    textView.setMovementMethod(LinkMovementMethod.getInstance());

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
            Intent intent = new Intent(getContext(), LoginActivity.class);
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
