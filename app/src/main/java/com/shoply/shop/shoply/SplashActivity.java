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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                new LoadAllShopsTask().execute();

                //Intent i = new Intent(SplashActivity.this, SearchActivity.class);
                //startActivity(i);
                // close this activity
                //finish();
            }
        }, SPLASH_TIME_OUT);
    }

    //To use the AsyncTask, it must be subclassed
    private class LoadAllShopsTask extends AsyncTask<String, Void, String[]> // TODO: can ruturn Void, no need to String cause no adapter.git
    {

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
                final String SHOPS_LIST_BASE_URL =
                        "https://infinite-eyrie-7266.herokuapp.com/shops.json";
//                final String QUERY_PARAM = "q";
//                final String FORMAT_PARAM = "mode";

                Uri builtUri = Uri.parse(SHOPS_LIST_BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
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
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            startActivity(i);
        }


        /* --------- Parsing the JSON Strings --------------- */

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getShopsFromJson(String shopsJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_SHOP = "shop";

            JSONArray shopsJsonArray = new JSONArray(shopsJsonStr);

            SharedPreferences.Editor sharedPrefsEditor = getSharedPreferences("SplashActivitySharedPref", MODE_PRIVATE).edit(); //TODO: put const
            String[] shops = new String[10]; // TODO: CHANGE THAT

            for(int i = 0; i < shopsJsonArray.length(); i++) {
                JSONObject singleShop = shopsJsonArray.getJSONObject(i);
                String shopName = singleShop.getString("name");
                int id = singleShop.getInt("id");
                String beaconGroupId = singleShop.getString("beacon_group_id");
                sharedPrefsEditor.putInt(shopName, id);
                sharedPrefsEditor.putString("beacon_group_id", beaconGroupId);
                sharedPrefsEditor.commit();

                shops[i] = shopName;
                Log.v(LOG_TAG, "Shop is: " + shopName +" id is: " + id + " beacon_group_id " + beaconGroupId);
            }

            SharedPreferences prefs = getSharedPreferences("SplashActivitySharedPref", MODE_PRIVATE);
            int idName = prefs.getInt("ampm", 0); //0 is the default value.
            Log.v(LOG_TAG, "TAG VIEW DANIELLA: " + String.valueOf(idName));

            return shops;

        }
    }
}