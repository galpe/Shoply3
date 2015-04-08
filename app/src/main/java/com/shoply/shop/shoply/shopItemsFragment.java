package com.shoply.shop.shoply;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class shopItemsFragment extends ListFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "itemNames";
    private static final String ARG_PARAM2 = "itemIDs";

    // TODO: Rename and change types of parameters
    private HashMap<Integer,String> shoppingItems = new HashMap<Integer,String>();

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types of parameters
    public static shopItemsFragment newInstance(String[] itemNames, int[] itemIDs) {
        shopItemsFragment fragment = new shopItemsFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_PARAM1, itemNames);
        args.putIntArray(ARG_PARAM2, itemIDs);
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
            String[] itemNames = getArguments().getStringArray(ARG_PARAM1);
            Integer[] itemIDs = (Integer[])getArguments().getIntegerArrayList(ARG_PARAM2).toArray();
            //hidden assumption arrays are length equivalent
            for (int i = 0; i< itemIDs.length && i< itemNames.length; i++)
            {
                shoppingItems.put(itemIDs[i],itemNames[i]);
            }
        }
        List<String> valuesToMatch=(List<String>) shoppingItems.values();
        // TODO: Change Adapter to display your content
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, valuesToMatch));
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
