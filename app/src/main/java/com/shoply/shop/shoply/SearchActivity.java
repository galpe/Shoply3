package com.shoply.shop.shoply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by galpeer on 4/7/15.
 */
public class SearchActivity extends ActionBarActivity {

    AutoCompleteTextView textView;
    SharedPreferences sharedPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);
        findViewById(R.id.searchPageGoBtn).setOnClickListener(createGoButtonListener());

        // Get a reference to the AutoCompleteTextView in the layout
        textView = (AutoCompleteTextView) findViewById(R.id.searchShop);

        // Get the string array
        String[] shopsAutoComplete = getShopsNamesFromSharedPreferences();
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shopsAutoComplete);


        textView.setAdapter(adapter);

    }

    private String[] getShopsNamesFromSharedPreferences() {
        sharedPrefs = getSharedPreferences("SplashActivitySharedPref", MODE_PRIVATE);
        Set<String> shopNames = sharedPrefs.getAll().keySet();
        String[] shops = new String[shopNames.size()];
        Iterator<String> itr = shopNames.iterator();
        int i = 0;
        while (itr.hasNext()) {
            String nextShop = itr.next();
            shops[i] = nextShop;
            i++;
//            Log.v("SEARCH_ACTIVITY", "Shop names: " + nextShop);
        }


        return shops;
    }



    /**
     * Returns click listener on update minor button.
     * Triggers update minor value on the beacon.
     */
    private View.OnClickListener createGoButtonListener() {
        return new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent;
                intent = new Intent(SearchActivity.this,MapViewActivity.class);
                String finalSearchTerm = textView.getText().toString();
                int shopId = sharedPrefs.getInt(finalSearchTerm, -1);
                if (-1 == shopId) {
                    //invalid shopID, show toast and do not move on.
                    Toast.makeText(SearchActivity.this, "Non existent shop, we're very sorry.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                intent.putExtra("shopID",shopId);
                startActivity(intent);

            }
        };
    }


    /* ------------------------ */

//    public static void hideSoftKeyboard(Activity activity) {
//        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
//    }

}
