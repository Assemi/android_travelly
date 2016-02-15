package au.edu.uq.civil.atlasii;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

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
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mGoogleApiClient = null;
    // Last known location
    Location mLastLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atlas_ii);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the primary sections of the
        // activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        /// TEST
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_tab_home);
        ///

        /// UPDATE
        // Create an instance of GoolgeAPIClient
        if(mGoogleApiClient == null) {
            //mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_atlas_ii, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        /// TEST
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                TextView textView = (TextView) this.findViewById(R.id.section_label);
                textView.setText("Latitude = " + String.valueOf(mLastLocation.getLatitude()) +
                        "Longitude = " + String.valueOf(mLastLocation.getLongitude()));
            }
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

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


        /// TEST
        private TextView txtView;
        private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls) {

                // params comes from the execute() call: params[0] is the url.
                try {
                    return downloadUrl(urls[0]);
                } catch (IOException e) {
                    return "Unable to retrieve web page. URL may be invalid.";
                }
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {
                /// UPDATE
                /// TEST
                txtView.setText(result);
                ///
            }

            // Given a URL, establishes an HttpUrlConnection and retrieves
            // the web page content as a InputStream, which it returns as
            // a string.
            private String downloadUrl(String myurl) throws IOException {
                InputStream is = null;
                // Only display the first 500 characters of the retrieved
                // web page content.
                int len = 10000;

                try {
                    URL url = new URL(myurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    String postParameters = "username=behrang&password=123";
                    conn.setFixedLengthStreamingMode(postParameters.getBytes().length);
                    PrintWriter out = new PrintWriter(conn.getOutputStream());
                    out.print(postParameters);
                    out.close();

                    // Starts the query
                    conn.connect();
                    int response = conn.getResponseCode();
                    /// TEST Log.d(DEBUG_TAG, "The response is: " + response);
                    is = conn.getInputStream();

                    // Convert the InputStream into a string
                    String contentAsString = readIt(is, len);
                    return contentAsString;

                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

            // Reads an InputStream and converts it to a String.
            public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
                Reader reader = null;
                reader = new InputStreamReader(stream, "UTF-8");
                char[] buffer = new char[len];
                reader.read(buffer);
                return new String(buffer);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            TextView textView;

            switch (sectionNumber) {
                case 1:
                case 2:
                case 3:
                    /// TEST
                    /// REMOVE when finished testing ///
                    //NetwrokConnection nc = new NetwrokConnection(getContext());
                    //String res = nc.login("behrang", "123");


                    rootView = inflater.inflate(R.layout.fragment_atlas_ii, container, false);
                    textView = (TextView) rootView.findViewById(R.id.section_label);
                    //textView.setText(getString(R.string.section_format, sectionNumber));
                    textView.setText("test");
                    txtView = textView;
                    /// UPDATE: AsyncTask<String, Void, String> res = new DownloadWebpageTask().execute("http://atlaservt.somee.com/Login.aspx");
                    ///
                    break;
                case 4:
                    // Setting the help pages' url
                    String helpURL = "http://atlaservt.somee.com/mobile/index.html";
                    // Loading the help pages
                    rootView = inflater.inflate(R.layout.atlas_help_pages, container, false);
                    WebView webView = (WebView) rootView.findViewById(R.id.webView_helpPages);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(helpURL);
                    break;
                case 5:
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
                    rootView = inflater.inflate(R.layout.about_atlas_page, container, false);
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
