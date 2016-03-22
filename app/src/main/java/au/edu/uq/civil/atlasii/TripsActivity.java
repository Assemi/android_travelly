package au.edu.uq.civil.atlasii;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import au.edu.uq.civil.atlasii.data.AtlasContract;

public class TripsActivity extends AppCompatActivity {

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

        // Retrieving trips' date
        String strTripsDate;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                strTripsDate = null;
            } else {
                strTripsDate = extras.getString(getString(R.string.intent_msg_trips_date));
            }
        } else {
            strTripsDate = (String) savedInstanceState.getSerializable(
                    getString(R.string.intent_msg_trips_date));
        }

        // TODO: Add other required attributes
        // Extracting the trips recorded on the retrieved date
        Cursor cursor = getContentResolver().query(
                AtlasContract.TripEntry.buildTripWithDateUri(strTripsDate.replace("/", "")),
                new String[]{AtlasContract.TripEntry._ID,
                        AtlasContract.TripEntry.COLUMN_START_TIME},
                null,
                null,
                AtlasContract.TripEntry.COLUMN_START_TIME + " ASC");

        CursorAdapter cursorAdapter = new CursorAdapter(
                this,
                cursor,
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
                // TODO: Update
                // Set the labelled trips' icon
                /*ImageView imageView = (ImageView) view.findViewById(R.id.icon_trip);
                imageView.setImageResource(R.drawable.ic_done);*/
                TimeZone timeZone = Calendar.getInstance().getTimeZone();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                formatter.setTimeZone(timeZone);
                long tripStartTime = cursor.getLong(cursor.getColumnIndexOrThrow(
                        AtlasContract.TripEntry.COLUMN_START_TIME));
                String strTripStartTime = formatter.format(new Date(tripStartTime));
                //textViewTrip.setText(strTripDate);
                textViewTripTime.setText(strTripStartTime);

                // Specify click action for the listview items
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    // Redirecting to trip view page
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        Intent intent = new Intent(getApplication(), TripViewActivity.class);
                        // Extract selected trip's ID
                        intent.putExtra(getString(R.string.intent_msg_trip_id), id);
                        // TODO: Fix the back button: http://developer.android.com/training/implementing-navigation/temporal.html
                        startActivity(intent);
                    }
                });
            }
        };

        // Attach cursor adapter to the ListView
        listView.setAdapter(cursorAdapter);
    }

}
