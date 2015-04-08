package com.shoply.shop.shoply;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by galpeer on 4/7/15.
 */
public class SplashActivity extends Activity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1;
    private static String LOG_TAG = "SPLASH_ACTIVITY";
    public static final String URL_TO_ALL_SHOPS =  "https://infinite-eyrie-7266.herokuapp.com/shops.json";
    public static final int NUM_OF_SHOPS_TO_FETCH =  100;
    public static final String CURRENT_SHARED_PREFERENCES_NAME =  "SplashActivitySharedPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                new LoadAllShopsTask().execute();


            }
        }, SPLASH_TIME_OUT);
    }

    private class LoadAllShopsTask extends AsyncTask<String, Void, String[]> // TODO: can ruturn Void, no need to String cause no adapter.git
    {
        boolean gotException = false;

        //The code to be executed in a background thread.
        @Override
        protected String[] doInBackground(String... params)
        {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String shoplySearchStr = null;
            String format = "json";

            try {
                final String SHOPS_LIST_BASE_URL = URL_TO_ALL_SHOPS;

                Uri builtUri = Uri.parse(SHOPS_LIST_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                shoplySearchStr = buffer.toString();
                Log.v(LOG_TAG, "JSON STRING IS: " + shoplySearchStr);
            } catch (IOException e) {
                gotException = true;
                Log.e(LOG_TAG, "Got exception on network ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }


            try {
                return getShopsFromJson(shoplySearchStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(gotException) {
                Log.e(LOG_TAG, "GOT A NETWORK ERROR");
                setContentView(R.layout.network_error);
            } else {
                Intent i = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(i);
            }

        }


        /* --------- Parsing the JSON Strings --------------- */

        private String[] getShopsFromJson(String shopsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String PRODUCT_NAME_JSON_KEY = "name";
            final String PRODUCT_ID_JSON_KEY = "id";
            final String PRODUCT_BEACON_GROUP_JSON_KEY = "beacon_group_id";

            JSONArray shopsJsonArray = new JSONArray(shopsJsonStr);

            SharedPreferences.Editor sharedPrefsEditor = getSharedPreferences(CURRENT_SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
            String[] shops = new String[NUM_OF_SHOPS_TO_FETCH]; // CHANGE THAT IN THE FUTURE

            for(int i = 0; i < shopsJsonArray.length(); i++) {
                JSONObject singleShop = shopsJsonArray.getJSONObject(i);
                String shopName = singleShop.getString(PRODUCT_NAME_JSON_KEY);
                int id = singleShop.getInt(PRODUCT_ID_JSON_KEY);
                String beaconGroupId = singleShop.getString(PRODUCT_BEACON_GROUP_JSON_KEY);
                JSONArray beaconsArray = singleShop.getJSONArray("beacons");
                for(int z =0; z < beaconsArray.length(); z++) {
                    sharedPrefsEditor.putInt(beaconsArray.getJSONObject(z).getString("external_id") + "_X", beaconsArray.getJSONObject(z).getInt("x"));
                    sharedPrefsEditor.putInt(beaconsArray.getJSONObject(z).getString("external_id") + "_Y", beaconsArray.getJSONObject(z).getInt("y"));
                }

                sharedPrefsEditor.putInt(shopName, id);
                sharedPrefsEditor.putString(PRODUCT_BEACON_GROUP_JSON_KEY, beaconGroupId);
                sharedPrefsEditor.commit();

                shops[i] = shopName;
                Log.v(LOG_TAG, "Shop is: " + shopName +" id is: " + id + " beacon_group_id " + beaconGroupId);
            }

            SharedPreferences prefs = getSharedPreferences(CURRENT_SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            return shops;
        }
    }
}