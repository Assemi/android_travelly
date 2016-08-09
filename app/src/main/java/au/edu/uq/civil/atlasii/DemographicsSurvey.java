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

public class DemographicsSurvey extends AppCompatActivity /*implements View.OnClickListener*/ {

    private SurveySubmitTask mSubmitTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographics_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        /*EditText editText1 = (EditText)findViewById(R.id.txtCarShare);
        EditText editText2 = (EditText)findViewById(R.id.txtPTShare);
        EditText editText3 = (EditText)findViewById(R.id.txtWalkShare);
        EditText editText4 = (EditText)findViewById(R.id.txtBicycleShare);
        EditText editText5 = (EditText)findViewById(R.id.txtOtherShare);

        editText1.setOnClickListener(this);
        editText2.setOnClickListener(this);
        editText3.setOnClickListener(this);
        editText4.setOnClickListener(this);
        editText5.setOnClickListener(this);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demographics_survey, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_submit) {
            String responses = readResponses();
            submitResponses(responses);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*@Override
    public void onClick(View v) {
        EditText editText1 = (EditText)findViewById(R.id.txtCarShare);
        EditText editText2 = (EditText)findViewById(R.id.txtPTShare);
        EditText editText3 = (EditText)findViewById(R.id.txtWalkShare);
        EditText editText4 = (EditText)findViewById(R.id.txtBicycleShare);
        EditText editText5 = (EditText)findViewById(R.id.txtOtherShare);

        switch(v.getId())
        {
            case R.id.txtCarShare:
                editText1.setText("");
                break;
            case R.id.txtPTShare:
                editText2.setText("");
                break;
            case R.id.txtWalkShare:
                editText3.setText("");
                break;
            case R.id.txtBicycleShare:
                editText4.setText("");
                break;
            case R.id.txtOtherShare:
                editText5.setText("");
                break;
            default:
                break;
        }
    }*/

    // TODO: Questions should be read dynamically: question numbers are now hard-coded, check multiple options condition
    String readResponses() {
        // TODO: Response numbers for multiple choice questions should be dynamic
        String responses = "";
        // Question 1
        EditText editTextAge = (EditText) findViewById(R.id.txtAge);
        // Question 2
        Spinner spinGender = (Spinner) findViewById(R.id.spinnerGender);
        // Question 3
        Spinner spinAddrRecency = (Spinner) findViewById(R.id.spinnerAddrRececncy);
        // Question 4
        EditText editTextHSize = (EditText) findViewById(R.id.txtHSize);
        // Question 5
        EditText editTextChildren = (EditText) findViewById(R.id.txtChildren);
        // Question 6
        Spinner spinEmployment = (Spinner) findViewById(R.id.spinnerEmployment);
        // Question 7
        Spinner spinDog = (Spinner) findViewById(R.id.spinnerDogOwnership);
        // Question 8
        Spinner spinCar = (Spinner) findViewById(R.id.spinnerCarAccess);
        // Question 9
        EditText editTextModeCar = (EditText) findViewById(R.id.txtCarShare);
        // Question 10
        EditText editTextModePT = (EditText) findViewById(R.id.txtPTShare);
        // Question 11
        EditText editTextModeWalk = (EditText) findViewById(R.id.txtWalkShare);
        // Question 12
        EditText editTextModeBicycle = (EditText) findViewById(R.id.txtBicycleShare);
        // Question 13
        EditText editTextModeOther = (EditText) findViewById(R.id.txtOtherShare);
        // Question 14
        Spinner spinAcquaintance = (Spinner) findViewById(R.id.spinnerAcquaintance);
        // Question 15
        Spinner spinCommunity = (Spinner) findViewById(R.id.spinnerLocalCommunity);

        responses += "{responses:[";

        // Reading the responses from the activity page
        responses = responses + editTextAge.getText() + "," +
                ";" + (spinGender.getSelectedItemId() + 1) + ";," +
                ";" + (spinAddrRecency.getSelectedItemId() + 1) + ";," +
                editTextHSize.getText() + "," +
                editTextChildren.getText() + "," +
                ";" + (spinEmployment.getSelectedItemId() + 1) + ";," +
                ";" + (spinDog.getSelectedItemId() + 1) + ";," +
                ";" + (spinCar.getSelectedItemId() + 1) + ";," +
                editTextModeCar.getText() + "," +
                editTextModePT.getText() + "," +
                editTextModeWalk.getText() + "," +
                editTextModeBicycle.getText() + "," +
                editTextModeOther.getText() + "," +
                ";" + (spinAcquaintance.getSelectedItemId() + 1) + ";," +
                ";" + (spinCommunity.getSelectedItemId() + 1) + ";";

        // TODO: Question numbers should be dynamic
        // Adding question numbers
        responses += "],questions:[1023,1024,1025,1026,1027,1028,1029,1030,1031,1032,1033,1034,1035,1036,1037]}";

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
            Intent intent = new Intent(DemographicsSurvey.this, AtlasII.class);
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
}
