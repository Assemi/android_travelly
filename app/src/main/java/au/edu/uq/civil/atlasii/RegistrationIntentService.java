package au.edu.uq.civil.atlasii;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Behrang Assemi on 4/07/2016.
 */
public class RegistrationIntentService extends IntentService {

    private DeviceRegistrationTask mRegTask = null;
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            /*// Subscribe to topic channels
            subscribeTopics(token);*/

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean("SENT_TOKEN_TO_SERVER", true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean("SENT_TOKEN_TO_SERVER", false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent("REGISTRATION_COMPLETE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
        String username = settings.getString( "Username", "");
        mRegTask = new DeviceRegistrationTask(username, token);
        mRegTask.execute((Void) null);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    /**
     * Represents an asynchronous device registration task used to send the device's token to server
     */
    public class DeviceRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        private String mUsername;
        private String mToken;
        private String mResult = "";

        DeviceRegistrationTask(String username, String token) {
            mUsername = username;
            mToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Attempt sending the device's token through a network service
            try {
                return sendToken();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        // This method sends the device's token to the server
        private boolean sendToken() throws IOException {

            InputStream inputStream = null;
            boolean result = false;

            // Retrieving app info to send to server along with the device's token
            String appVersion = "0.0.0";
            PackageManager pManager = getApplicationContext().getPackageManager();
            String appName = getApplicationContext().getPackageName();
            int stringId = getApplicationInfo().labelRes;
            String appLabel = getString(stringId);
            try {
                appVersion = pManager.getPackageInfo(appName, 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String deviceName = Build.DEVICE + " " + Build.MODEL;
            String deviceOS = System.getProperty("os.version");

            String urlParameters = "token=" + URLEncoder.encode(mToken, "UTF-8") +
                    "&username=" + URLEncoder.encode(mUsername, "UTF-8") +
                    "&app=" + URLEncoder.encode(appLabel, "UTF-8") +
                    "&version=" + URLEncoder.encode(appVersion, "UTF-8") +
                    "&device=" + URLEncoder.encode(deviceName, "UTF-8") +
                    "&os=android_" + URLEncoder.encode(deviceOS, "UTF-8");

            try {
                // Creating request URL
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(getString(R.string.atlas_server_url))
                        .appendPath(getString(R.string.path_push_request));
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
            mRegTask = null;

            if (success) {
                // TODO: Record successful transmission of the token to server
            } else {
                // TODO: Show error of not being able to send the token to server
            }
        }

        @Override
        protected void onCancelled() {
            mRegTask = null;
        }
    }
}
