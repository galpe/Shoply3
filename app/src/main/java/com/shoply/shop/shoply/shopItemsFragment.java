package com.shoply.shop.shoply;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnItemPressedInterface}
 * interface.
 */
public class shopItemsFragment extends ListFragment {

    private HashMap<String,Integer> shoppingItems;

    private OnItemPressedInterface mListener;

    public static shopItemsFragment newInstance(HashMap<String, Integer> items) {
        shopItemsFragment fragment = new shopItemsFragment();
        Bundle args = new Bundle();

        args.putSerializable("items", items);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public shopItemsFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shoppingItems = (HashMap<String,Integer>) getArguments().getSerializable("items");
        }
        List<String> valuesToMatch = new ArrayList<String>(shoppingItems.keySet());

        // TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, valuesToMatch));
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        getListView().setTextFilterEnabled(true);
    }


    public void filterText(String s){
        //getListView().setFilterText(s);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemPressedInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemPressedInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.itemPressed(
                    shoppingItems.get(l.getItemAtPosition(position))
            );
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemPressedInterface {

        public void itemPressed(int id);
    }

}
