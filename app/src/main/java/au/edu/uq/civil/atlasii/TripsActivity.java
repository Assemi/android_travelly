package au.edu.uq.civil.atlasii;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import au.edu.uq.civil.atlasii.data.AtlasContract;

public class TripsActivity extends AppCompatActivity {

    static final String STATE_TRIPS_DATE = "tripsDate";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the activity's current state
        savedInstanceState.putString(STATE_TRIPS_DATE, mTripsDate);

        // Calling the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        mTripsDate = savedInstanceState.getString(STATE_TRIPS_DATE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PAGE_NUMBER", 2);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private boolean mLabelledTrips = false;
    Cursor mCursor = null;
    private String mTripsDate = "";

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
                mLabelledTrips = settings.getBoolean("LabelledTrips", false);
            } else {
                mTripsDate = extras.getString(getString(R.string.intent_msg_trips_date));
                mLabelledTrips = extras.getBoolean(getString(R.string.intent_msg_trips_labelled));
                // Saving the trips' date for future use
                editor.putString("TripsDate", mTripsDate);
                editor.putBoolean("LabelledTrips", mLabelledTrips);
                editor.commit();
            }
        } else {
            mTripsDate = savedInstanceState.getString(STATE_TRIPS_DATE);
            // Saving the trips' date for future use
            editor.putString("TripsDate", mTripsDate);
            editor.putBoolean("LabelledTrips", mLabelledTrips);
            editor.commit();
        }

        // Extracting the trips recorded on the retrieved date
        if(mTripsDate != null) {
            if(!mLabelledTrips) {
                mCursor = getContentResolver().query(
                        AtlasContract.TripEntry.buildTripWithDateUri(mTripsDate.replace("/", "")),
                        new String[]{AtlasContract.TripEntry._ID,
                                AtlasContract.TripEntry.COLUMN_START_TIME,
                                AtlasContract.TripEntry.COLUMN_END_TIME},
                                AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_LABELLED + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                        new String[]{"0", "0", "0"},
                        AtlasContract.TripEntry.COLUMN_START_TIME + " ASC");
            } else {
                mCursor = getContentResolver().query(
                        AtlasContract.TripEntry.buildTripWithDateUri(mTripsDate.replace("/", "")),
                        new String[]{AtlasContract.TripEntry._ID,
                                AtlasContract.TripEntry.COLUMN_START_TIME,
                                AtlasContract.TripEntry.COLUMN_END_TIME},
                                AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_LABELLED + " != ? and " +
                                AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                        new String[]{"0", "0", "0"},
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
                    if(mLabelledTrips) {
                        imageView.setImageResource(R.drawable.ic_to_upload);
                    }
                    else {
                        imageView.setImageResource(R.drawable.ic_question);
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
            if(!mLabelledTrips) {
                this.setTitle(getApplicationContext().getString(R.string.activity_trips_unlabelled) +
                        " " + mTripsDate);
            }
            else {
                this.setTitle(getApplicationContext().getString(R.string.activity_trips_labelled) +
                        " " + mTripsDate);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_upload) {
            uploadTrips();

            return true;
        }
        else*/
        if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Refreshing the unlabelled trips list: removing labelled trips from the unlabelled list
    @Override
    protected void onRestart() {
        super.onRestart();

        // Checking if the activity shows unlabelled trips
        if(!mLabelledTrips) {
            // Extracting the trips recorded on the retrieved date
            if(mTripsDate != null) {
                final ListView listView = (ListView) findViewById(R.id.listView_TripDetail);
                mCursor = getContentResolver().query(
                        AtlasContract.TripEntry.buildTripWithDateUri(mTripsDate.replace("/", "")),
                        new String[]{AtlasContract.TripEntry._ID,
                                AtlasContract.TripEntry.COLUMN_START_TIME,
                                AtlasContract.TripEntry.COLUMN_END_TIME},
                        AtlasContract.TripEntry.COLUMN_ACTIVE + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_LABELLED + " = ? and " +
                                AtlasContract.TripEntry.COLUMN_EXPORTED + " = ?",
                        new String[]{"0", "0", "0"},
                        AtlasContract.TripEntry.COLUMN_START_TIME + " ASC");


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
                        if(mLabelledTrips) {
                            imageView.setImageResource(R.drawable.ic_to_upload);
                        }
                        else {
                            imageView.setImageResource(R.drawable.ic_question);
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
            }
        }
    }
}
