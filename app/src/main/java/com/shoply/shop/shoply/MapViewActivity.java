package com.shoply.shop.shoply;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.estimote.sdk.Beacon;
import com.shoply.shop.shoply.SearchFragment.ReceiveBeaconListener;

/**
 * Created by daniellag on 4/7/15.
 */
public class MapViewActivity extends Activity implements ReceiveBeaconListener{

    private Beacon closest = null;

    private static final String BASE_URL = "https://infinite-eyrie-7266.herokuapp.com/shops/";

    private int shopID;
    WebView web;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        shopID = this.getIntent().getExtras().getInt("shopID");

        web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new ShopMapWebView());
        web.getSettings().setJavaScriptEnabled(true);
        String finalUrl = BASE_URL + String.valueOf(shopID).toString() +".json";
        Log.v("MAP_VIEW_ACTIVITY", finalUrl);
        web.loadUrl(finalUrl); // TODO: change to correct view
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
    }

    private int getBeaconID()
    {
        int minor = 0;
        if (null != closest) {
            minor = closest.getMinor();
        }
        return minor;
    }


}
