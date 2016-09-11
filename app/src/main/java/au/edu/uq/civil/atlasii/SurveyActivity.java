package au.edu.uq.civil.atlasii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

public class SurveyActivity extends AppCompatActivity {

    int mSurveyCount = 0;
    private SurveySubmitTask mSubmitTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*// TODO: Complete retrieving surveys list
        SurveyListTask task = new SurveyListTask(this);
        task.execute();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_survey_submit) {
            String responses = readResponses();
            submitResponses(responses);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: Questions should be read dynamically: question numbers are now hard-coded, check multiple options condition
    String readResponses() {
        // TODO: Response numbers for multiple choice questions should be dynamic
        String responses = "";
        // Question 1
        Spinner spinnerDayStart = (Spinner) findViewById(R.id.spinnerPassedDayStart);
        // Question 2
        Spinner spinnerDayLeaveIntention = (Spinner) findViewById(R.id.spinnerPassedDayLeave);
        // Question 3
        CheckBox chkActivity1 = (CheckBox) findViewById(R.id.chkPassedActivity1);
        CheckBox chkActivity2 = (CheckBox) findViewById(R.id.chkPassedActivity2);
        CheckBox chkActivity3 = (CheckBox) findViewById(R.id.chkPassedActivity3);
        CheckBox chkActivity4 = (CheckBox) findViewById(R.id.chkPassedActivity4);
        // Question 4
        EditText txtDayNotLeaveReason = (EditText) findViewById(R.id.txtPassedDayNotLeaveReason);
        // Question 5
        EditText txtDayLastLeave = (EditText) findViewById(R.id.txtPassedDayLastLeave);

        String strActivities = ";";

        if(chkActivity1.isChecked()) {
            strActivities += "1;";
        }
        if(chkActivity2.isChecked()) {
            strActivities += "2;";
        }
        if(chkActivity3.isChecked()) {
            strActivities += "3;";
        }
        if(chkActivity4.isChecked()) {
            strActivities += "4;";
        }

        responses += "{responses:[";

        // Reading the responses from the activity page
        responses = responses + ";" + (spinnerDayStart.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerDayLeaveIntention.getSelectedItemId() + 1) + ";," +
                strActivities + "," +
                txtDayNotLeaveReason.getText() + "," +
                txtDayLastLeave.getText();

        // TODO: Question numbers should be dynamic
        // Adding question numbers
        responses += "],questions:[1052,1053,1054,1055,1056]}";

        return responses;
    }

    private void submitResponses(String responses) {
        if (mSubmitTask != null) {
            return;
        }

        // Retrieving the username from the shared preferences
        SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
        String username = settings.getString("Username", "");

        // Kick off a background task to perform the survey submit attempt.
        mSubmitTask = new SurveySubmitTask(username, responses);
        mSubmitTask.execute((Void) null);
    }

    /**
     * Represents an asynchronous task used to send survey responses to the server.
     */
    public class SurveySubmitTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String mSurveyResponses;
        private String mResult = "";

        SurveySubmitTask(String username, String responses) {
            mUsername = username;
            mSurveyResponses = responses;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt authentication against a network service.
            try {
                return submitSurvey();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        // This method send survey responses to the server
        private boolean submitSurvey() throws IOException {

            InputStream inputStream = null;
            boolean result = false;
            String strContent = "uid=" + UUID.randomUUID().toString() +
                    "&pnum=" + "1" +
                    "&total=" + "1" +
                    "&username=" + mUsername +
                    "&content=" + mSurveyResponses;
            byte[] compressedContent = compress(strContent);

            try {
                // Creating request URL
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(getString(R.string.atlas_server_url))
                        .appendPath(getString(R.string.path_submit_survey_data));
                URL url = new URL(builder.build().toString());

                // Requesting the server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", "" + Integer.toString(compressedContent.length));
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (
                        conn.getOutputStream ());
                wr.write(compressedContent);
                wr.flush();
                wr.close();

                // Starting the query
                conn.connect();
                int response = conn.getResponseCode();

                // Checking whether the results are fine
                if(response == 200) {
                    result = true;
                    inputStream = conn.getInputStream();
                    // Convert the InputStream into a string
                    mResult = readIt(inputStream);
                }
                // TODO: Change- Errors should be managed and appropriate messages should be shown
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (inputStream != null) {
                    inputStream.close();
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
        protected void onPostExecute(final Boolean success) {
            mSurveyResponses = null;

            if (success) {

            } else {
                // TODO: Show an error message
            }

            // The user is redirected to the main activity
            Intent intent = new Intent(SurveyActivity.this, AtlasII.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onCancelled() {
            mSubmitTask = null;
        }

        // Compresses the content to send with HTTP Post
        private byte[] compress(String string) throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(string.getBytes());
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            return compressed;
        }
    }

    /*public class SurveyListTask extends AsyncTask<Void, Void, Boolean> {

        String mResult = "";

        public AsyncResponse delegate = null;

        SurveyListTask(AsyncResponse delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt retrieving active surveys from a network service.
            try {
                return getActiveSurveys();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // TODO: Manage showing a relevant error message
            return true;
        }

        // This method retrieves the list of active surveys from the server
        private boolean getActiveSurveys() throws IOException {
            InputStream inputStream = null;
            String contentAsString = "";
            boolean result = false;

            try {
                // Creating request URL
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        //.authority(getString(R.string.atlas_server_url))
                        .authority(getString(R.string.atlas_server_url))
                        //.appendPath(getString(R.string.path_active_surveys));
                        .appendPath("GetActiveSurveys.aspx");
                String myUrl = builder.build().toString();
                URL url = new URL(builder.build().toString());

                // Requesting the server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds * /);
                conn.setConnectTimeout(15000 /* milliseconds * /);
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);

                // Starting the query
                conn.connect();
                int response = conn.getResponseCode();

                // Checking whether the results are fine
                if(response == 200) {
                    result = true;
                    inputStream = conn.getInputStream();
                    // Convert the InputStream into a string
                    mResult = readIt(inputStream);
                }
                // TODO: Change- Errors should be managed and appropriate messages should be shown
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (inputStream != null) {
                    inputStream.close();
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
        protected void onPostExecute(final Boolean success) {
            String surveyCount = "";
            if (success) {
                // Retrieving the survey count from the server response
                int inx = mResult.indexOf("?numOfSurveys=");
                surveyCount = mResult.substring(inx + 14, mResult.indexOf(";", inx + 1));
                String temo = mResult.substring(1, 5);

                /* // Storing the username, password and email in shared preferences
                SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Username", mUsername);
                editor.putString("Password", mPassword);
                editor.putString("Email", email);
                // Commit the edits!
                editor.commit();

                finish();

                // The user is redirected to the main activity
                Intent intent = new Intent(getApplicationContext(), AtlasII.class);
                startActivity(intent);* /
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }

            delegate.processFinish(surveyCount);
        }
    }*/
}
