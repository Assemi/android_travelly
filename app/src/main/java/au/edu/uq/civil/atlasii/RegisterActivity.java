package au.edu.uq.civil.atlasii;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * A registration screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Keep track of the registration task
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private EditText mTxtUsername;
    private EditText mTxtEmail;
    private EditText mTxtPassword;
    private EditText mTxtConfirmPassword;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mTxtUsername = (EditText) findViewById(R.id.username);
        mTxtEmail = (EditText) findViewById(R.id.email);
        mTxtPassword = (EditText) findViewById(R.id.password);
        mTxtConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        /*txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });*/

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    /**
     * Attempts to register the account specified by the register form.
     * If there are form errors (missing fields, etc.), the
     * errors are presented and no actual attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mTxtUsername.setError(null);
        mTxtEmail.setError(null);
        mTxtPassword.setError(null);
        mTxtConfirmPassword.setError(null);

        // Store values at the time of the registration attempt.
        String username = mTxtUsername.getText().toString();
        String email = mTxtEmail.getText().toString();
        String password = mTxtPassword.getText().toString();
        String confirmPassword = mTxtConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        Pattern validUsernamePattern = Pattern.compile("[a-zA-Z0-9\\.\\_]+");

        // Check if the user entered an acceptable username
        if (TextUtils.isEmpty(username)) {
            mTxtUsername.setError(getString(R.string.error_field_required));
            focusView = mTxtUsername;
            cancel = true;
        } else if (!validUsernamePattern.matcher(username).matches()) {
            mTxtUsername.setError(getString(R.string.error_username_invalid));
            focusView = mTxtUsername;
            cancel = true;
        }

        // Check if the user entered an acceptable email
        if (TextUtils.isEmpty(email)) {
            mTxtEmail.setError(getString(R.string.error_field_required));
            focusView = mTxtEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mTxtEmail.setError(getString(R.string.error_email_invalid));
            focusView = mTxtEmail;
            cancel = true;
        }

        // Check if the user entered a password
        if (TextUtils.isEmpty(password)) {
            mTxtPassword.setError(getString(R.string.error_field_required));
            focusView = mTxtPassword;
            cancel = true;
        }

        // Check if the user correctly confirmed the password
        if (TextUtils.isEmpty(confirmPassword)) {
            mTxtConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = mTxtConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            mTxtConfirmPassword.setError(getString(R.string.error_wrong_password));
            focusView = mTxtConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(username, email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String mEmail;
        private String mPassword;
        private String mResult = "";

        UserRegisterTask(String username, String email, String password) {
            mUsername = username;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt authentication against a network service.
            try {
                return registerUser();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        // This method authenticates user on the server
        private boolean registerUser() throws IOException {

            InputStream inputStream = null;
            boolean result = false;
            String urlParameters = "username=" + URLEncoder.encode(mUsername, "UTF-8") +
                    "&password=" + URLEncoder.encode(mPassword, "UTF-8") +
                    "&email=" + URLEncoder.encode(mEmail, "UTF-8");

            try {
                // Creating request URL
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(getString(R.string.atlas_server_url))
                        .appendPath(getString(R.string.path_register));
                String myUrl = builder.build().toString();
                URL url = new URL(builder.build().toString());

                // Requesting the server
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                conn.setRequestProperty("Content-Language", "en-US");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (
                        conn.getOutputStream ());
                wr.writeBytes(urlParameters);
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

                    // Check whether the username was accepted by the server
                    if(!mResult.substring(0,3).equals("200")) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "An error occurred while registering your username. Please try a different username!",
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                        result = false;
                    }
                }
                // TODO: Change- Errors should be managed and appropriate messages should be shown
                else {
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) {
                        String strError = readIt(errorStream);
                        errorStream.close();
                    }
                }

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
            mAuthTask = null;
            showProgress(false);

            if (success) {
                /*// Retrieving the email address from the server response
                // Finding the email in the response
                int inx = mResult.indexOf("?email=");
                int endInx = mResult.indexOf("&", inx + 1);
                String email = "";
                if(endInx > 0) {
                    mResult.substring(inx + 7, endInx);
                }*/

                // Storing the username, password and email in shared preferences
                SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Username", mUsername);
                editor.putString("Password", mPassword);
                editor.putString("Email", mEmail);
                // Commit the edits!
                editor.commit();

                finish();

                /*// The user is redirected to the main activity
                Intent intent = new Intent(getApplicationContext(), AtlasII.class);
                startActivity(intent);*/
                // The user is redirected to the demographics survey
                Intent intent = new Intent(getApplicationContext(), DemographicsSurvey.class);
                startActivity(intent);
            } else {
                /*mTxtPassword.setError(getString(R.string.error_incorrect_password));
                mTxtPassword.requestFocus();*/
                runOnUiThread(new Runnable() {

                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "An error occurred while registering your username. Please try a different username!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

