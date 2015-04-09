package com.shoply.shop.shoply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ItemPickerActivity extends ActionBarActivity
{

    private HashMap<String,Integer> shoppingItems;
    ListView x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_picker);

        shoppingItems = (HashMap<String,Integer>) this.getIntent().getSerializableExtra("items");

        List<String> valuesToMatch = new ArrayList<String>(shoppingItems.keySet());

        x = (ListView)findViewById(R.id.itemsListView);
        x.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice , android.R.id.text1, valuesToMatch));
        x.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //recheck this...

        x.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Log.d("test", "clicked");
                CheckedTextView Ctv = (CheckedTextView) arg1; //garunteed
                //Ctv.toggle();
            }
        });

    }



    public void onFindItemsClick(View v)
    {
        List<Integer> checkedItemIDs = new ArrayList<Integer>();
        SparseBooleanArray checked = x.getCheckedItemPositions();

        for (int i = 0; i < x.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                String itemName = ((String)x.getItemAtPosition(i));
                int id = shoppingItems.get(itemName);
                checkedItemIDs.add(id);
            }
        }
        // FUCK JAVA
        Integer[] stockArr = new Integer[checkedItemIDs.size()];
        stockArr = checkedItemIDs.toArray(stockArr);
        int[] result = new int[stockArr.length];
        for (int i = 0; i < stockArr.length; i++) {
            result[i] = stockArr[i].intValue();
        }


        itemsRequested(result);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        int[] arr = {-1};
        intent.putExtra("result",arr);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void itemsRequested(int[] id)
    {
        //Send this back to mapview activity.
        //if already passed back
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",id);
        setResult(RESULT_OK,returnIntent);
        finish();
        return;
    }

}
