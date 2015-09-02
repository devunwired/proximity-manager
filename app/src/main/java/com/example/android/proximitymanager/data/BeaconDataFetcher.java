package com.example.android.proximitymanager.data;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.proximitymanager.api.ProximityApi;

import org.json.JSONObject;

public class BeaconDataFetcher {
    private static final String TAG = BeaconDataFetcher.class.getSimpleName();

    private ProximityApi mProximityApi;

    public BeaconDataFetcher(ProximityApi proximityApi) {
        mProximityApi = proximityApi;
    }

    public interface OnBeaconDataListener {
        void onBeaconResponse(Beacon beacon);
    }

    private OnBeaconDataListener mBeaconDataListener;

    public void setBeaconDataListener(OnBeaconDataListener listener) {
        mBeaconDataListener = listener;
    }

    public void registerBeacon(Beacon beacon) {
        BeaconDataCallback callback = new BeaconDataCallback(beacon.advertisedId);
        mProximityApi.registerBeacon(beacon, callback, callback);
    }

    public void fetchBeaconData(Beacon.AdvertisedId beaconId) {
        BeaconDataCallback callback = new BeaconDataCallback(beaconId);
        mProximityApi.getBeacon(beaconId.toBeaconName(), callback, callback);
    }

    private class BeaconDataCallback implements
            Response.Listener<JSONObject>, Response.ErrorListener {
        private Beacon.AdvertisedId mAdvertisedId;
        public BeaconDataCallback(Beacon.AdvertisedId id) {
            mAdvertisedId = id;
        }

        @Override
        public void onResponse(JSONObject response) {
            //Called for 2xx responses
            Beacon beacon = new Beacon(response);
            notifyListener(beacon);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            //Called for 4xx responses
            switch (error.networkResponse.statusCode) {
                case 403:
                    notifyListener(new Beacon(mAdvertisedId, Beacon.Status.UNAUTHORIZED));
                    break;
                case 404:
                    notifyListener(new Beacon(mAdvertisedId, Beacon.Status.UNREGISTERED));
                    break;
                default:
                    Log.w(TAG, "Unknown error response from Proximity API", error);
            }
        }
    }

    private void notifyListener(Beacon result) {
        if (mBeaconDataListener != null) {
            mBeaconDataListener.onBeaconResponse(result);
        }
    }
}
