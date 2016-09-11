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
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
    private Spinner mSpinnerLicenceCar;
    private Spinner mSpinnerLicenceMotor;
    private EditText mTxtDisabilityOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographics_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Showing/hiding questions based on the responses to other questions
        // Employment and work hour questions
        Spinner mSpinnerEmployment = (Spinner) findViewById(R.id.spinnerEmployment);

        final TextView mLblWork = (TextView) findViewById(R.id.lblWork);
        final EditText mTxtWork = (EditText) findViewById(R.id.txtWork);
        final View mSepWork = (View) findViewById(R.id.sepWork);

        mSpinnerEmployment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch(position) {
                    case 0:case 1:
                        mLblWork.setVisibility(View.VISIBLE);
                        mTxtWork.setVisibility(View.VISIBLE);
                        mSepWork.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mLblWork.setVisibility(View.GONE);
                        mTxtWork.setVisibility(View.GONE);
                        mSepWork.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        mSpinnerLicenceCar = (Spinner) findViewById(R.id.spinnerLicenceCar);
        mSpinnerLicenceMotor = (Spinner) findViewById(R.id.spinnerLicenceMotor);
        mTxtDisabilityOther = (EditText) findViewById(R.id.txtDisabilityOther);

        mSpinnerLicenceCar.setVisibility(View.GONE);
        mSpinnerLicenceMotor.setVisibility(View.GONE);
        mTxtDisabilityOther.setVisibility(View.GONE);

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

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        final CheckBox mChkLicence1 = (CheckBox) findViewById(R.id.chkLicence1);
        final CheckBox mChkLicence2 = (CheckBox) findViewById(R.id.chkLicence2);
        final CheckBox mChkLicence3 = (CheckBox) findViewById(R.id.chkLicence3);
        final CheckBox mChkLicence4 = (CheckBox) findViewById(R.id.chkLicence4);

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.chkLicence1:
                if (checked) {
                    // Uncheck other licence options, when "no licence" is selected
                    mChkLicence2.setChecked(false);
                    mChkLicence3.setChecked(false);
                    mChkLicence4.setChecked(false);
                }
                else {

                }
                break;
            case R.id.chkLicence2:
                if (checked) {
                    // Show car licence types, when "car licence" is selected
                    mSpinnerLicenceCar.setVisibility(View.VISIBLE);
                    // Uncheck "no licence", when a licence is selected
                    mChkLicence1.setChecked(false);
                }
                else {
                    mSpinnerLicenceCar.setVisibility(View.GONE);
                }
                break;
            case R.id.chkLicence3:
                if (checked) {
                    // Show motorcycle licence types, when "motorcycle licence" is selected
                    mSpinnerLicenceMotor.setVisibility(View.VISIBLE);
                    // Uncheck "no licence", when a licence is selected
                    mChkLicence1.setChecked(false);
                }

                else {
                    mSpinnerLicenceMotor.setVisibility(View.GONE);
                }
                break;
            case R.id.chkLicence4:
                if (checked) {
                    // Uncheck "no licence", when a licence is selected
                    mChkLicence1.setChecked(false);
                } else {

                }
                break;
            case R.id.chkDisability6:
                if (checked) {
                    mTxtDisabilityOther.setVisibility(View.VISIBLE);
                } else {
                    mTxtDisabilityOther.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
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
        Spinner spinnerGender = (Spinner) findViewById(R.id.spinnerGender);
        // Question 2
        Spinner spinnerCampus = (Spinner) findViewById(R.id.spinnerCampus);
        // Question 3
        Spinner spinnerStudy = (Spinner) findViewById(R.id.spinnerStudy);
        // Question 4
        Spinner spinnerLocale = (Spinner) findViewById(R.id.spinnerLocale);
        // Question 5
        EditText txtAge = (EditText) findViewById(R.id.txtAge);
        // Question 6
        Spinner spinnerEmployment = (Spinner) findViewById(R.id.spinnerEmployment);
        EditText txtWork = (EditText) findViewById(R.id.txtWork);
        // Question 7
        Spinner spinnerCarAccess = (Spinner) findViewById(R.id.spinnerCarAccess);
        // Question 8
        Spinner spinnerBikeAccess = (Spinner) findViewById(R.id.spinnerBikeAccess);
        // Question 9
        Spinner spinnerGoCard = (Spinner) findViewById(R.id.spinnerGoCardAccess);
        // Question 10
        CheckBox chkLicence1 = (CheckBox) findViewById(R.id.chkLicence1);
        CheckBox chkLicence2 = (CheckBox) findViewById(R.id.chkLicence2);
        Spinner spinnerLicenceCar = (Spinner) findViewById(R.id.spinnerLicenceCar);
        CheckBox chkLicence3 = (CheckBox) findViewById(R.id.chkLicence3);
        Spinner spinnerLicenceMotor = (Spinner) findViewById(R.id.spinnerLicenceMotor);
        CheckBox chkLicence4 = (CheckBox) findViewById(R.id.chkLicence4);
        // Question 11
        Spinner spinnerAssistance = (Spinner) findViewById(R.id.spinnerAssistance);
        // Question 12
        CheckBox chkDisability1 = (CheckBox) findViewById(R.id.chkDisability1);
        CheckBox chkDisability2 = (CheckBox) findViewById(R.id.chkDisability2);
        CheckBox chkDisability3 = (CheckBox) findViewById(R.id.chkDisability3);
        CheckBox chkDisability4 = (CheckBox) findViewById(R.id.chkDisability4);
        CheckBox chkDisability5 = (CheckBox) findViewById(R.id.chkDisability5);
        CheckBox chkDisability6 = (CheckBox) findViewById(R.id.chkDisability6);
        EditText txtDisabilityOther = (EditText) findViewById(R.id.txtDisabilityOther);
        // Question 13
        Spinner spinnerDayStart = (Spinner) findViewById(R.id.spinnerDayStart);
        // Question 14
        Spinner spinnerDayLeaveIntention = (Spinner) findViewById(R.id.spinnerDayLeaveIntention);
        // Question 15
        CheckBox chkActivity1 = (CheckBox) findViewById(R.id.chkActivity1);
        CheckBox chkActivity2 = (CheckBox) findViewById(R.id.chkActivity2);
        CheckBox chkActivity3 = (CheckBox) findViewById(R.id.chkActivity3);
        CheckBox chkActivity4 = (CheckBox) findViewById(R.id.chkActivity4);
        // Question 16
        EditText txtDayNotLeaveReason = (EditText) findViewById(R.id.txtDayNotLeaveReason);
        // Question 17
        EditText txtDayLastLeave = (EditText) findViewById(R.id.txtDayLastLeave);

        String strLicences = ";";
        String strDisabilities = ";";
        String strActivities = ";";

        if(chkLicence1.isChecked()) { // No Licence
            strLicences = "1;";
        } else {
            if(chkLicence2.isChecked()) { // Car Licence
                strLicences += ((2 + spinnerLicenceCar.getSelectedItemId()) + ";");
            }
            if(chkLicence3.isChecked()) { // Motorcycle Licence
                strLicences += ((6 + spinnerLicenceCar.getSelectedItemId()) + ";");
            }
            if(chkLicence4.isChecked()) { // Other Licence
                strLicences += "10;";
            }
        }

        if(chkDisability1.isChecked()) {
            strDisabilities += "1;";
        }
        if(chkDisability2.isChecked()) {
            strDisabilities += "2;";
        }
        if(chkDisability3.isChecked()) {
            strDisabilities += "3;";
        }
        if(chkDisability4.isChecked()) {
            strDisabilities += "4;";
        }
        if(chkDisability5.isChecked()) {
            strDisabilities += "5;";
        }
        if(chkDisability6.isChecked()) {
            strDisabilities += ("6-" + txtDisabilityOther.getText() + ";");
        }

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
        responses = responses + ";" + (spinnerGender.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerCampus.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerStudy.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerLocale.getSelectedItemId() + 1) + ";," +
                txtAge.getText() + "," +
                ";" + (spinnerEmployment.getSelectedItemId() + 1) + "-" + txtWork.getText() + ";," +
                ";" + (spinnerCarAccess.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerBikeAccess.getSelectedItemId() + 1) + ";," +
                strLicences + "," +
                ";" + (spinnerAssistance.getSelectedItemId() + 1) + ";," +
                strDisabilities + "," +
                ";" + (spinnerGoCard.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerDayStart.getSelectedItemId() + 1) + ";," +
                ";" + (spinnerDayLeaveIntention.getSelectedItemId() + 1) + ";," +
                strActivities + "," +
                txtDayNotLeaveReason.getText() + "," +
                txtDayLastLeave.getText();

        // TODO: Question numbers should be dynamic
        // Adding question numbers
        responses += "],questions:[1038,1039,1040,1041,1042,1043,1044,1045,1046,1047,1048,1051,1057,1058,1059,1060,1061]}";

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
