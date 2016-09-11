package au.edu.uq.civil.atlasii;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SurveyActivity extends AppCompatActivity  implements
        AsyncResponse {

    int mSurveyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // TODO: Complete retrieving surveys list
        SurveyListTask task = new SurveyListTask(this);
        task.execute();
    }

    @Override
    public void processFinish(String output) {

    }

    public class SurveyListTask extends AsyncTask<Void, Void, Boolean> {

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
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
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

                /*// Storing the username, password and email in shared preferences
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
                startActivity(intent);*/
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }

            delegate.processFinish(surveyCount);
        }
    }
}
