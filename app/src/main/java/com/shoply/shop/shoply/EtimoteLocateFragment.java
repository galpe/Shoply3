package com.shoply.shop.shoply;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EtimoteLocateFragment.ReceiveBeaconListener} interface
 * to handle interaction events.
 * Use the {@link EtimoteLocateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class EtimoteLocateFragment extends Fragment {

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
    public interface ReceiveBeaconListener {
        public void onBeaconsDiscovered(Beacon closeBeacon);
    }

    private static final String ARG_PARAM1 = "uuidParam";

    // TODO: Rename and change types of parameters
    private String areaUUID;

    Activity savedActivity = null;


    private static final String TAG = EtimoteLocateFragment.class.getSimpleName();
    /* Etimote vars */
    private static final Region LEET_ESTIMOTE_BEACONS_REGION = new Region("testing","b9407f301337466eaff925556b57fe6d",null,null);
    private static final int REQUEST_ENABLE_BT = 1234;

    private DeviceList devList = new DeviceList();
    private Region searchRegion = null;
    private BeaconManager beaconManager;
    private Beacon nearest;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uuid Beacon ProximityUUID to scan for.
     * @return A new instance of fragment EtimoteLocateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EtimoteLocateFragment newInstance(String uuid) {
        EtimoteLocateFragment fragment = new EtimoteLocateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uuid);
        fragment.setArguments(args);
        return fragment;
    }

    public EtimoteLocateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            areaUUID = getArguments().getString(ARG_PARAM1);
            searchRegion = new Region("testing",areaUUID,null,null);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_etimotefind, container, false);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        savedActivity = activity;
        // Configure BeaconManager.
        beaconManager = new BeaconManager(activity);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                if (beacons.isEmpty()) {
                    return;
                }
                devList.replaceWith(beacons);
                nearest = devList.getClosest();
                double distance  = Utils.computeAccuracy(nearest);
                Utils.Proximity prox = Utils.proximityFromAccuracy(distance);
                if ((Utils.Proximity.NEAR == prox) || (Utils.Proximity.IMMEDIATE == prox) ){
                    if (null != savedActivity) {
                        ((ReceiveBeaconListener) savedActivity).onBeaconsDiscovered(nearest);
                    }
                }
            }});
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }




    @Override
    public void onDestroy() {
        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if application is setup properly
        if (!beaconManager.checkPermissionsAndService()) {
            Log.d(TAG, "Application does not have appropiate setup");
            return;
        }

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Log.d(TAG, "Device does not have Bluetooth Low Energy");
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            connectToService();
        }
    }

    @Override
    public void onStop() {
        try {
            if (null != searchRegion) {
                beaconManager.stopRanging(searchRegion);
            } else {
                beaconManager.stopRanging(LEET_ESTIMOTE_BEACONS_REGION);
            }
        } catch (RemoteException e) {
            Log.d(TAG, "Error while stopping ranging", e);
        }

        super.onStop();
    }

    private void connectToService() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    if (null != searchRegion) {
                        beaconManager.startRanging(searchRegion);
                    } else {
                        beaconManager.startRanging(LEET_ESTIMOTE_BEACONS_REGION);
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "Cannot start ranging, something terrible happened");
                }
            }
        });
    }

    /**
     * Created by acepace on 07/04/2015.
     */
    public class DeviceList {

        private ArrayList<Beacon> beacons;

        public DeviceList() {
            this.beacons = new ArrayList<Beacon>();
        }

        public void replaceWith(Collection<Beacon> newBeacons) {
            this.beacons.clear();
            this.beacons.addAll(newBeacons);
        }

        public int getCount() {
            return beacons.size();
        }
        public Beacon getItem(int position) {
            return beacons.get(position);
        }

        /**
         * Gets the closest beacon.
         * TODO get algo
         * @return A beacon instance, containing all relevant data.
         */
        public Beacon getClosest() {
            if (beacons.size() >= 1) {
                return beacons.get(0);
            } else {
                return null;
            }

        }
    }

}
