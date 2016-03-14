package au.edu.uq.civil.atlasii;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Behrang Assemi on 9/03/2016.
 * Splash screen
 * Redirect the user to either sign in page or the main app page, depending on whether the user has
 * signed in or not.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Fix how the subsequent activities are initiated.
        Thread timerThread = new Thread(){
            public void run(){
                try {
                    sleep(2000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // Checking whether the user has logged in or not
                    // Retrieving login data from shared preferences
                    SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.shared_preferences), 0);
                    String username = settings.getString("Username", "");

                    // If the user has not already signed in, he/she is redirected to sign in activity
                    if(username == "") {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    // The user has already signed in and is redirected to the main application activity
                    else {
                        Intent intent = new Intent(SplashActivity.this, AtlasII.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timerThread.start();
    }
}