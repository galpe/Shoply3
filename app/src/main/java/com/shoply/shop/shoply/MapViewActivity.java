package com.shoply.shop.shoply;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
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
    private static final String BASE_SHOPS_URL = "https://infinite-eyrie-7266.herokuapp.com/shops/";
    private static final String VIEW_URL = "https://infinite-eyrie-7266.herokuapp.com/viewer/"; // +shopId

    private int shopID;

    //Location tracking variables
    private Beacon closest = null;
    private int currentClosestBeacon = 0;

    //Items variables
    AsyncTask<Integer, Void, HashMap<String, Integer>> task; //We'll need to wait on this for item search

    private String baseUrl;
    private String itemsUrl;
    private String viewUrl;
    WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        shopID = this.getIntent().getExtras().getInt("shopID");

        task = new GetSpecificShopInfoTask().execute(shopID);

        baseUrl = BASE_SHOPS_URL + String.valueOf(shopID).toString() +".json";
        itemsUrl = BASE_SHOPS_URL + String.valueOf(shopID).toString() + "/items.json";
        viewUrl = VIEW_URL + "13/";//String.valueOf(shopID).toString();

        web = (WebView) findViewById(R.id.webView);
        
        
        web.loadUrl(viewUrl);
        web.setWebViewClient(new ShopMapWebView());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        web.getSettings().setBuiltInZoomControls(true);
        //Setup an async task and let it go.
        task = new GetSpecificShopInfoTask().execute(shopID);



    }

    public void onGeoClick(View view) {
        Log.d(TAG,"geoClick");
        updateClosest();
        if (0 == currentClosestBeacon) {
            Toast.makeText(MapViewActivity.this, "Cannot find a nearby beacon. We're sorry",
                    Toast.LENGTH_LONG).show(); // TODO: Dont send
        }
    }

    public void onSearchClick(View view) {
        Log.d(TAG,"SearchClick");

        try {
            HashMap<String, Integer> map = task.get();

            // Check that the activity is using the layout version with
            // the fragment_container FrameLayout
            if (findViewById(R.id.fragment_container) != null) {
                // Create a new Fragment to be placed in the activity layout
                shopItemsFragment firstFragment = shopItemsFragment.newInstance(map);

                // Add the fragment to the 'fragment_container' FrameLayout
                //getFragmentManager().beginTransaction()
                //    .add(R.id.fragment_container, firstFragment).commit();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

                Uri builtUri = Uri.parse(viewUrl).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(TAG, "Built URI " + builtUri.toString());

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
                Log.v(TAG, "JSON STRING IS: " + specificShopStr);
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
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
                Log.e(TAG, e.getMessage(), e);
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
                Log.e(TAG, "name: " + name + " id: " + id);
            }

            return nameToIdMap;


        }

    }
}
