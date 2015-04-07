package com.shoply.shop.shoply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

/**
 * Created by galpeer on 4/7/15.
 */
public class SearchActivity extends ActionBarActivity {

    private int shopID = 1337;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_page);
        findViewById(R.id.searchPageGoBtn).setOnClickListener(createGoButtonListener());
//        // Get the intent, verify the action and get the query
//        Intent intent = getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            doMySearch(query);
//        }
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
                intent.putExtra("shopID",shopID);
                startActivity(intent);

            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager =
//                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.search).getActionView();
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }




    private void doMySearch(String query) {
        Log.d("Event", query);
    }
}
