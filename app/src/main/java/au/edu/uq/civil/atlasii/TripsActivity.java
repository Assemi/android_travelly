package au.edu.uq.civil.atlasii;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import au.edu.uq.civil.atlasii.data.AtlasContract;

public class TripsActivity extends AppCompatActivity {

    static final String STATE_TRIPS_DATE = "tripsDate";
    static final String STATE_TRIPS_EXPORTED = "tripsExported";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the activity's current state
        savedInstanceState.putString(STATE_TRIPS_DATE, mTripsDate);
        savedInstanceState.putBoolean(STATE_TRIPS_EXPORTED, mExportedTrips);

        // Calling the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        mTripsDate = savedInstanceState.getString(STATE_TRIPS_DATE);
        mExportedTrips = savedInstanceState.getBoolean(STATE_TRIPS_EXPORTED);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PAGE_NUMBER", 2);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private boolean mExportedTrips = false;
    private TripsUploadTask mUploadTask = null;
    Cursor mCursor = null;
    private String mTripsDate = "";

    ArrayList<Long> mTrips = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
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

        mTrips = new ArrayList<Long>();

        // Retrieving the list view
        final ListView listView = (ListView) findViewById(R.id.listView_TripDetail);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());

        // Retrieving trips' date
        SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                // Reading the trips' date from the shared preferences
                //Calendar curCal = Calendar.getInstance();
                mTripsDate = settings.getString("TripsDate", currentDate);
                mExportedTrips = settings.getBoolean("ExportedTrips", false);
            } else {
                mTripsDate = extras.getString(getString(R.string.intent_msg_trips_date));
                mExportedTrips = extras.getBoolean(getString(R.string.intent_msg_trips_exported));
                // Saving the trips' date for future use
                editor.putString("TripsDate", mTripsDate);
                editor.putBoolean("ExportedTrips", mExportedTrips);
                editor.commit();
            }
        } else {
            mTripsDate = savedInstanceState.getString(STATE_TRIPS_DATE);
            mExportedTrips = savedInstanceState.getBoolean(STATE_TRIPS_EXPORTED);
            // Saving the trips' date for future use
            editor.putString("TripsDate", mTripsDate);
            editor.putBoolean("ExportedTrips", mExportedTrips);
            editor.commit();
        }

        // Extracting the trips recorded on the retrieved date
        if(mTripsDate != null) {
            if(!mExportedTrips) {
                mCursor = getContentResolver().query(
                        AtlasContract.TripEntry.buildTripWithDateUri(mTripsDate.replace("/", "")),
                        new String[]{AtlasContract.TripEntry._ID,
                                AtlasContract.TripEntry.COLUMN_START_TIME,
                                AtlasContract.TripEntry.COLUMN_END_TIME},
                        AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                        new String[]{"0", "0"},
                        AtlasContract.TripEntry.COLUMN_START_TIME + " ASC");
            } else {
                mCursor = getContentResolver().query(
                        AtlasContract.TripEntry.buildTripWithDateUri(mTripsDate.replace("/", "")),
                        new String[]{AtlasContract.TripEntry._ID,
                                AtlasContract.TripEntry.COLUMN_START_TIME,
                                AtlasContract.TripEntry.COLUMN_END_TIME},
                        AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_EXPORTED + " != ?",
                        new String[]{"0", "0"},
                        AtlasContract.TripEntry.COLUMN_START_TIME + " ASC");
            }

            CursorAdapter cursorAdapter = new CursorAdapter(
                    this,
                    mCursor,
                    0) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(context).inflate(
                            R.layout.listitem_tripdetails, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView textViewTrip = (TextView) view.findViewById(R.id.textview_tripdetail);
                    TextView textViewTripTime = (TextView) view.findViewById(R.id.textview_tripdetailtime);
                    // Set the labelled trips' icon
                    ImageView imageView = (ImageView) view.findViewById(R.id.icon_tripdetail);
                    if(mExportedTrips) {
                        imageView.setImageResource(R.drawable.ic_done);
                    }
                    else {
                        imageView.setImageResource(R.drawable.ic_to_upload);
                    }

                    TimeZone timeZone = Calendar.getInstance().getTimeZone();
                    long tripStartTime = cursor.getLong(cursor.getColumnIndexOrThrow(
                            AtlasContract.TripEntry.COLUMN_START_TIME));
                    long tripEndTime = cursor.getLong(cursor.getColumnIndexOrThrow(
                            AtlasContract.TripEntry.COLUMN_END_TIME));

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    formatter.setTimeZone(timeZone);
                    String strTripDate = formatter.format(new Date(tripStartTime));
                    textViewTrip.setText(strTripDate);

                    formatter = new SimpleDateFormat("HH:mm");
                    String strTripStartTime = formatter.format(new Date(tripStartTime));
                    String strTripEndTime = formatter.format(new Date(tripEndTime));
                    textViewTripTime.setText(strTripStartTime +
                            " - " +
                            strTripEndTime);

                    // Specify click action for the listview items
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        // Redirecting to trip view page
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            Intent intent = new Intent(getApplication(), TripViewActivity.class);
                            // Extract selected trip's ID
                            intent.putExtra(getString(R.string.intent_msg_trip_id), id);
                            startActivity(intent);
                        }
                    });
                }
            };

            // Attach cursor adapter to the ListView
            listView.setAdapter(cursorAdapter);

            // Setting the page's title
            if(!mExportedTrips) {
                this.setTitle("My Day on " + mTripsDate);
            }
            else {
                this.setTitle("My Day on " + mTripsDate);
            }

        }
        else
        {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!mExportedTrips) {
            getMenuInflater().inflate(R.menu.menu_trips_upload, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            uploadTrips();

            return true;
        }
        else if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadTrips() {
        if (mUploadTask != null) {
            return;
        }

        // Retrieving the username from the shared preferences
        SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
        String username = settings.getString("Username", "");

        // Extracting trip ids to upload
        if (mCursor != null) {
            mCursor.moveToFirst();
            do {
                long tripID = mCursor.getLong(mCursor.getColumnIndex(AtlasContract.TripEntry._ID));
                mTrips.add(tripID);
            } while (mCursor.moveToNext());
        }

        // Kick off a background task to perform the survey submit attempt.
        mUploadTask = new TripsUploadTask(username, mTrips);
        mUploadTask.execute((Void) null);
    }

    /**
     * Upload progress dialog
     */
    public static final int DIALOG_UPLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_UPLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("The upload is in progress ...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    /**
     * Represents an asynchronous task used to upload trips to the server.
     */
    public class TripsUploadTask extends AsyncTask<Void, Integer, Boolean> {

        private String mUsername;
        private ArrayList<Long> mLocalTrips = null;
        private ArrayList<Long> mToUpdateTrips = null;
        private String mResult = "";
        private boolean mError = false;

        TripsUploadTask(String username, ArrayList<Long> trips) {
            mUsername = username;
            mLocalTrips = new ArrayList<Long>();
            mToUpdateTrips = new ArrayList<Long>();

            for (Long t:trips) {
                mLocalTrips.add(t);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_UPLOAD_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            JSONObject tripData;
            boolean result = true;
            int inx = 0;

            // Attempt uploading trips through a network service.
            try {
                for (Long tripID: mLocalTrips) {
                    ++inx;

                    // Creating a JSON object to store trips' data
                    tripData = new JSONObject();

                    // Retrieving the trip's GEO data from the database
                    Cursor cursor = getContentResolver().query(
                            AtlasContract.GeoEntry.buildGeoWithTripUri(tripID),
                            new String[]{AtlasContract.GeoEntry._ID,
                                    AtlasContract.GeoEntry.COLUMN_HEADING,
                                    AtlasContract.GeoEntry.COLUMN_TIMESTAMP,
                                    AtlasContract.GeoEntry.COLUMN_LATITUDE,
                                    AtlasContract.GeoEntry.COLUMN_LONGITUDE,
                                    AtlasContract.GeoEntry.COLUMN_ACCURACY,
                                    AtlasContract.GeoEntry.COLUMN_SPEED},
                            null,
                            null,
                            AtlasContract.GeoEntry.COLUMN_TIMESTAMP + " ASC");

                    // Creating a JSON array of GEO samples
                    JSONArray jsonArrSamples = new JSONArray();
                    while (cursor.moveToNext()) {
                        JSONObject sampleObj = new JSONObject();
                        try {
                            sampleObj.put("heading", cursor.getFloat(
                                    cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_HEADING)));
                            sampleObj.put("timestamp", formatter.format(new Date(
                                    cursor.getLong(
                                            cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_TIMESTAMP)))));
                            sampleObj.put("latitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LATITUDE)));
                            sampleObj.put("longitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_LONGITUDE)));
                            sampleObj.put("location_accuracy", cursor.getFloat(
                                    cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_ACCURACY)));
                            sampleObj.put("speed", cursor.getFloat(
                                    cursor.getColumnIndex(AtlasContract.GeoEntry.COLUMN_SPEED)));
                            jsonArrSamples.put(sampleObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Retrieving the trip's data
                    cursor = getContentResolver().query(
                            AtlasContract.TripEntry.CONTENT_URI,
                            new String[]{AtlasContract.TripEntry._ID,
                                    AtlasContract.TripEntry.COLUMN_TRIP_ATTRIBUTES,
                                    AtlasContract.TripEntry.COLUMN_DATE,
                                    AtlasContract.TripEntry.COLUMN_START_TIME,
                                    AtlasContract.TripEntry.COLUMN_END_TIME,
                                    AtlasContract.TripEntry.COLUMN_MIN_LATITUDE,
                                    AtlasContract.TripEntry.COLUMN_MAX_LATITUDE,
                                    AtlasContract.TripEntry.COLUMN_MIN_LONGITUDE,
                                    AtlasContract.TripEntry.COLUMN_MAX_LONGITUDE,
                                    AtlasContract.TripEntry.COLUMN_DISTANCE},
                            AtlasContract.TripEntry._ID + " = ?",
                            new String[]{String.valueOf(tripID)},
                            AtlasContract.TripEntry.COLUMN_DATE + " ASC");

                    // Creating a JSON object of trip data
                    JSONObject jsonObjTrip = new JSONObject();
                    while (cursor.moveToNext()) {
                        try {
                            jsonObjTrip.put("description", "Unknown");
                            //jsonObjTrip.put("tripAttrs", "");
                            jsonObjTrip.put("date", formatter.format(new Date(
                                    cursor.getLong(
                                            cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_START_TIME)))));
                            jsonObjTrip.put("start_time", formatter.format(new Date(
                                    cursor.getLong(
                                            cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_START_TIME)))));
                            jsonObjTrip.put("end_time", formatter.format(new Date(
                                    cursor.getLong(
                                            cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_END_TIME)))));
                            jsonObjTrip.put("minLatitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MIN_LATITUDE)));
                            jsonObjTrip.put("maxLatitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MAX_LATITUDE)));
                            jsonObjTrip.put("minLongitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MIN_LONGITUDE)));
                            jsonObjTrip.put("maxLongitude", cursor.getDouble(
                                    cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_MAX_LONGITUDE)));
                            jsonObjTrip.put("distance", cursor.getFloat(
                                    cursor.getColumnIndex(AtlasContract.TripEntry.COLUMN_DISTANCE)));
                            jsonObjTrip.put("survey_id", "0");
                            // Creating a JSON array of transport modes
                            JSONArray jsonArrModes = new JSONArray();
                            jsonArrModes.put("Unlabelled");
                            jsonObjTrip.put("transport_modes", jsonArrModes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Creating the complete trip data object
                    JSONObject jsonObjectTrips = new JSONObject();
                    try {
                        jsonObjectTrips.put("samples", jsonArrSamples);
                        jsonObjectTrips.put("trip", jsonObjTrip);
                        tripData.put("trips", jsonObjectTrips);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    result &= uploadTripData(tripID, tripData);

                    // TODO: the logic to calculate progress and call

                    publishProgress((int)(inx * 100 / mLocalTrips.size()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        // This method send trips' data to the server
        private boolean uploadTripData(Long trip, JSONObject tripData) throws IOException {
            int errorCount = 0;
            boolean result = true;
            String uid = UUID.randomUUID().toString();
            final int chunkSize = 1000;
            // Dividing the trip data into small chunks
            String strContentFull = tripData.toString().replace("\"", "");
            int totalChunks = (int)((strContentFull.length() - 1) / chunkSize);
            for(int chunkNo = 0; chunkNo <= totalChunks; ++chunkNo) {
                InputStream inputStream = null;
                String strContent = "uid=" + uid +
                        "&pnum=" + String.valueOf(chunkNo + 1) +
                        "&total=" + String.valueOf(totalChunks + 1) +
                        "&username=" + mUsername +
                        "&content=" + strContentFull.substring(chunkNo * chunkSize, Math.min((chunkNo + 1) * chunkSize, strContentFull.length()));

                byte[] compressedContent = compress(strContent);

                try {
                    // Creating request URL
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority(getString(R.string.atlas_server_url))
                            .appendPath(getString(R.string.path_submit_trip_data));
                    URL url = new URL(builder.build().toString());

                    // Requesting the server
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(60000 /* milliseconds */);
                    conn.setConnectTimeout(60000 /* milliseconds */);
                    conn.setRequestMethod("POST");
                    //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Encoding", "gzip");
                    conn.setRequestProperty("Content-Length", Integer.toString(compressedContent.length));
                    //conn.setRequestProperty("Content-Language", "en-US");
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream(
                            conn.getOutputStream());
                    wr.write(compressedContent);
                    wr.flush();
                    wr.close();

                    // Starting the query
                    conn.connect();
                    int response = conn.getResponseCode();

                    // Checking whether the results are fine
                    if (response == 200) {
                        inputStream = conn.getInputStream();
                        // Convert the InputStream into a string
                        mResult = readIt(inputStream);

                        int inx = mResult.indexOf("?uid=");
                        // All parts have been correctly received by the server
                        if(inx > 0) {
                            int endInx = mResult.indexOf("\n", inx + 1);
                            String receivedUid = mResult.substring(inx + 6, endInx - 1);

                            if (receivedUid.equals(uid)) {
                                mToUpdateTrips.add(trip);
                            }
                        }
                    }
                    else {
                        result = false;
                        InputStream errorStream = conn.getErrorStream();
                        if (errorStream != null) {
                            String strError = readIt(errorStream);
                            errorStream.close();
                        }
                        ++errorCount;
                        --chunkNo;
                    }

                    conn.disconnect();

                    // TODO: Change- Errors should be managed and appropriate messages should be shown
                    if(errorCount > 5) {
                        mError = true;
                        break;
                    }
                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                }
                catch (IOException e) {
                    mError = true;
                    break;
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            }

            return result;
        }

        // Reads an InputStream and converts it to a String
        public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            dismissDialog(DIALOG_UPLOAD_PROGRESS);

            if (success) {
                // Retrieving the uid to set the respective sent message as successful
                updateTrips(mToUpdateTrips);
            }

            // Check whether an error has occurred during the upload process
            if(!success || mError) {
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "An error occurred while uploading the trips. Please try again later!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        }

        @Override
        protected void onCancelled() {
            mUploadTask = null;
        }

        // Compresses the content to send with HTTP Post
        private byte[] compress(String string) throws IOException {
            /*ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(string.getBytes());
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            return compressed;*/
            ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(string.getBytes());
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            return compressed;
        }
    }

    private void updateTrips(ArrayList<Long> trips) {
        for (Long tripID:trips) {
            /*ContentValues tripValues = new ContentValues();
            tripValues.put(AtlasContract.TripEntry.COLUMN_EXPORTED, true);

            // Update the record in the database
            int nRows = getContentResolver().update(
                    AtlasContract.TripEntry.CONTENT_URI,
                    tripValues,
                    AtlasContract.TripEntry._ID + " = ?",
                    new String[]{String.valueOf(tripID)});*/

            // delete the record from the database
            int nRows = getContentResolver().delete(
                    AtlasContract.TripEntry.CONTENT_URI,
                    AtlasContract.TripEntry._ID + " = ?",
                    new String[]{String.valueOf(tripID)});
        }

        mTrips.clear();
        finish();

        /*Intent intent = new Intent(TripsActivity.this, AtlasII.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
    }
}
