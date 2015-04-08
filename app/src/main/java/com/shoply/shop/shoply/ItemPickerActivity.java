package com.shoply.shop.shoply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ItemPickerActivity extends ActionBarActivity implements shopItemsFragment.OnItemPressedInterface
{

    private HashMap<String,Integer> shoppingItems;

    private shopItemsFragment firstFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_picker);

        shoppingItems = (HashMap<String,Integer>) this.getIntent().getSerializableExtra("items");

        /* Setup the auto complete items */
        // Get a reference to the AutoCompleteTextView in the layout
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.searchItems);
        // Get the string array
        List<String> valuesToMatch = new ArrayList<>(shoppingItems.keySet());
        // Create the adapter and set it to the AutoCompleteTextView
        final ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, valuesToMatch);
        textView.setAdapter(adapter);

        textView.addTextChangedListener(new TextWatcher() {

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                return; //Do nothing.
                                            }

                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                                                return;//Do nothing
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {
                                                String text = s.toString();
                                                // Call back the Adapter with current character to Filter
                                                firstFragment.filterText(text);
                                            }
                                        }
        );
            if (savedInstanceState == null) {
            // Check that the activity is using the layout version with
            // the fragment_container FrameLayout
            if (findViewById(R.id.fragment_container) != null) {
                // Create a new Fragment to be placed in the activity layout
                firstFragment = shopItemsFragment.newInstance(shoppingItems);

                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                   .add(R.id.fragment_container, firstFragment).commit();
            }
        }
    }

    public void onItemSelected(View view) {
        AutoCompleteTextView textView = (AutoCompleteTextView) view;
        Editable text = textView.getText();
        String key = text.toString();
        int id = shoppingItems.get(key);
        itemPressed(id);

    }

    public void itemPressed(int id)
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
