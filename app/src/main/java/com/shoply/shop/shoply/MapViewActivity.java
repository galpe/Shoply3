package com.shoply.shop.shoply;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.shoply.shop.shoply.SearchFragment.ReceiveBeaconListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by daniellag on 4/7/15.
 */
public class MapViewActivity extends Activity implements ReceiveBeaconListener{


    private static final String TAG = MapViewActivity.class.getSimpleName();
    private static final String LOG_TAG = "MAP_VIEW_ACTIVITY";
    private Beacon closest = null;

    private static final String BASE_URL = "https://infinite-eyrie-7266.herokuapp.com/shops/";

    private int shopID;
    private int currentClosestBeacon = 0;
    private String finalUrl;
    private String itemsUrl;
    WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        shopID = this.getIntent().getExtras().getInt("shopID");

        web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new ShopMapWebView());
        web.getSettings().setJavaScriptEnabled(true);
        finalUrl = BASE_URL + String.valueOf(shopID).toString() +".json";
        itemsUrl = BASE_URL + String.valueOf(shopID).toString() + "/items.json";
        Log.v("MAP_VIEW_ACTIVITY", finalUrl);
        web.loadUrl(finalUrl); // TODO: change to correct view

        //TODO: REMOVE
//        getSpecificShopInfo(shopID);
        try {
            HashMap<String, Integer> map1 = new GetSpecificShopInfoTask().execute(1).get();
            Log.e(LOG_TAG, "Got Map with: " + map1.get("ampm"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void onGeoClick(View view) {
        Log.d(TAG,"geoClick");
        updateClosest();
        if (0 == currentClosestBeacon) {
            Toast.makeText(MapViewActivity.this, "Cannot find a nearby beacon. We're sorry",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onSearchClick(View view) {
        Log.d(TAG,"SearchClick");
    }


    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Member variable stored to reflect user's choice
    //private String mUserUrl = "http://stackoverflow.com";

    public class ShopMapWebView extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // This line right here is what you're missing.
            // Use the url provided in the method.  It will match the member URL!
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

    }



    public void onBeaconsDiscovered(Beacon closeBeacon)
    {
        closest = closeBeacon;
        getBeaconID();
    }

    private void updateClosest()
    {
        currentClosestBeacon = getBeaconID();
    }

    private int getBeaconID()
    {
        int minor = 0;
        if (null != closest) {
            minor = closest.getMinor();
        }
        return minor;
    }


    ///////////////////////////////////////////
    private class GetSpecificShopInfoTask extends AsyncTask<Integer, Void, HashMap<String, Integer>> {


        protected HashMap<String, Integer> doInBackground(Integer... params) { //TODO: remove integer



            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String specificShopStr = null;

            try {

                Uri builtUri = Uri.parse(itemsUrl).buildUpon()
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
                specificShopStr = buffer.toString();
                Log.v(LOG_TAG, "JSON STRING IS: " + specificShopStr);
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
                return createJSONArray(specificShopStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private HashMap<String, Integer> createJSONArray(String specificShopStr) throws JSONException {

            JSONObject specificShopJsonObj = new JSONObject(specificShopStr);
            JSONArray shelves = specificShopJsonObj.getJSONArray("groceries");
            HashMap<String, Integer> nameToIdMap = new HashMap<String, Integer>();

            for(int i = 0; i < shelves.length(); i++) {
                int id = shelves.getJSONObject(i).getInt("id");
                String name = shelves.getJSONObject(i).getString("name");
                nameToIdMap.put(name, id);
                Log.e(LOG_TAG, "name: " + name + " id: " + id);
            }

            return nameToIdMap;


        }

    }
}
