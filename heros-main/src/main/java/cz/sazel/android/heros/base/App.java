package cz.sazel.android.heros.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.otto.Bus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by wojta on 27.4.14.
 */
public class App extends Application {

    private static final String SENDER_ID = "644662548442";
    static Bus sBus;
    private static Handler sHandler;
    private String sRegid;
    private GoogleCloudMessaging sGcm;
    public static final String TAG = App.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    public void onCreate() {
        super.onCreate();
        sBus = new Bus();
        sBus.register(this);
        sHandler = new Handler();
        if (!checkPlayServices()) {
            Log.e("App", "GooglePlayServices failed");
            System.exit(1);
        } else {
            sGcm = GoogleCloudMessaging.getInstance(this);
            sRegid = getRegistrationId(this);

            if (sRegid.isEmpty()) {
                registerInBackground();
            } else {
                Log.v(TAG, "registrationId:" + sRegid);
            }
        }
    }


    public static Bus bus() {
        return sBus;
    }

    public static void postBus(final Object event) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                sBus.post(event);
            }
        });
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        final String registrationId = prefs.getString(PROPERTY_REG_ID, "");

        if (registrationId.isEmpty())

        {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)

        {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                if (sGcm == null) {
                    sGcm = GoogleCloudMessaging.getInstance(App.this);
                }
                try {
                    sRegid = sGcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + sRegid;
                    storeRegistrationId(App.this, sRegid);
                    try {
                        Uri.Builder builder = Uri.parse(Constants.REG_IDS_URL).buildUpon();
                        builder.appendQueryParameter("id", sRegid);
                        builder.appendQueryParameter("info", Build.MODEL + ":" + Settings.Secure.getString(getContentResolver(),
                                Settings.Secure.ANDROID_ID));

                        URL url = new URL(builder.build().toString());

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        try {

                            connection.setRequestMethod("GET");
                            connection.setDoOutput(true);
                            connection.setReadTimeout(10000);
                            connection.connect();
                            if (connection.getResponseCode() == 200) {
                                Log.v(TAG, "sucessfully written regid");
                            } else {
                                Log.e(TAG, "regid failed");
                            }
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            connection.disconnect();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (IOException ex)

                {
                    msg = "Error :" + ex.getMessage();
                }

                return msg;
            }

            ;


            @Override
            protected void onPostExecute(String msg) {
                Log.v(TAG, msg);
            }
        }.execute(null, null, null);

    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */

    private static int getAppVersion(Context context) {
        try {

            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
