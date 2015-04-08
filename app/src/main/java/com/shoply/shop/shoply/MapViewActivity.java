package com.shoply.shop.shoply;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.shoply.shop.shoply.EtimoteLocateFragment.ReceiveBeaconListener;

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

    /* -- end wear -- */


    private static final String TAG = MapViewActivity.class.getSimpleName();
    private static final String BASE_SHOPS_URL = "https://infinite-eyrie-7266.herokuapp.com/shops/";
    private static final String VIEW_URL = "https://infinite-eyrie-7266.herokuapp.com/viewer/"; // +shopId

    private int shopID;

    //Location tracking variables
    private Beacon closest = null;
    private int currentClosestBeacon = 0;

    //Items variables
    AsyncTask<String, Void, HashMap<String, Integer>> task; //We'll need to wait on this for item search

    private String baseUrl;
    private String itemsUrl;
    private String viewUrl;
    WebView webView;
    WebSettings webSettings;

    //////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        shopID = this.getIntent().getExtras().getInt("shopID");

        baseUrl = BASE_SHOPS_URL + String.valueOf(shopID).toString() +".json";
        itemsUrl = BASE_SHOPS_URL + String.valueOf(shopID).toString() + "/items.json";
        viewUrl = VIEW_URL + String.valueOf(shopID).toString(); //13/

        task = new GetAllShopInfoByUrlTask();

        webView = (WebView) findViewById(R.id.webView);

        final ProgressDialog progress =  ProgressDialog.show(this,  "Getting your map", "Loading...", true);

        webView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                        progress.dismiss();
                        progress.cancel();
                        return true;
                    }
                }
                // process normally
                progress.dismiss();
                progress.cancel();
                webView.onFinishTemporaryDetach();
                return false;
            }
        });

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setWebViewClient(new ShopMapWebView(progress));
        webView.loadUrl(viewUrl);
        //Setup an async task and let it go.
        task.execute(itemsUrl);

    }

    public void onGeoClick(View view) {
        Log.d(TAG,"geoClick");
        updateClosest();
        if (0 == currentClosestBeacon) {
            Toast.makeText(MapViewActivity.this, "Cannot find a nearby beacon. We're sorry",
                    Toast.LENGTH_LONG).show(); // TODO: Dont send
        } else {
            showNotification(view);
            String urlWithGeoLocation = viewUrl + "?beacon_id=" + "\"" + currentClosestBeacon + "\"" ; // TODO: DO SOMETHING
            webView.loadUrl(urlWithGeoLocation); // TODO: Have zoom?
            webView.getSettings().setJavaScriptEnabled(true);
            Log.e(TAG, urlWithGeoLocation);
        }
    }

    public void onSearchClick(View view) {
        Log.d(TAG, "SearchClick");

        try {
            HashMap<String, Integer> map = task.get();
            Intent i = new Intent(this, ItemPicker.class);
            i.putExtra("items",map);
            startActivityForResult(i,0);


        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //We can ignore the requestCode and resultCode because we only send one.
        int itemID = data.getIntExtra("result",-1);
        if (-1 ==itemID ) {
            //Very very wrong.
            Log.d(TAG,"got back result from subsequent activity");
        }
        //TODO new request URL using shop item.

    }


    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }

        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }


    public class ShopMapWebView extends WebViewClient {


        private ProgressDialog progressBar;

        public ShopMapWebView(ProgressDialog progressBar) {
            this.progressBar=progressBar;
            progressBar.show();
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            progressBar.hide();
        }


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


////////////////////////////////////////////////////////////////////////////////////////////////////////////
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private class GetAllShopInfoByUrlTask extends AsyncTask<String, Void, HashMap<String, Integer>> {


        protected HashMap<String, Integer> doInBackground(String... params) {

            //check length is indeed 1 - this is the url to execute.
            String urlToLoad = params[0];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String specificShopStr = null;

            try {

                Uri builtUri = Uri.parse(urlToLoad).buildUpon()
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

        @Override
        protected void onPostExecute(HashMap<String, Integer> stringIntegerHashMap) {
            super.onPostExecute(stringIntegerHashMap);
        }
    }
            /* ---------- ANDROID WEAR ------------- */
    /**
     * Handles the button to launch a notification.
     */
    public void showNotification(View view) {
        Notification notification = new NotificationCompat.Builder(MapViewActivity.this).setSmallIcon(R.drawable.shopli)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.drawable.ic_launcher) // addAction
                .build();

        NotificationManager notificationManger =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManger.notify(0, notification);
    }
}
