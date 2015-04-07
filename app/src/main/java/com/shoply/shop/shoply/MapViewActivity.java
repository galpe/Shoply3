package com.shoply.shop.shoply;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shoply.shop.shoply.SearchFragment.ReceiveBeaconListener;
import com.estimote.sdk.Beacon;

/**
 * Created by daniellag on 4/7/15.
 */
public class MapViewActivity extends Activity implements ReceiveBeaconListener{

    WebView web;

    private Beacon closest = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new HelloWebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl("http://www.google.com");
    }

    // Member variable stored to reflect user's choice
    private String mUserUrl = "http://stackoverflow.com";

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // This line right here is what you're missing.
            // Use the url provided in the method.  It will match the member URL!
            view.loadUrl(url);
            return true;
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
