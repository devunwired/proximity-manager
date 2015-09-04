package com.example.android.proximitymanager.api;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.android.proximitymanager.data.Attachment;
import com.example.android.proximitymanager.data.Beacon;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class ProximityApi {
    private static final String TAG = ProximityApi.class.getSimpleName();

    private static ProximityApi sInstance;
    public static synchronized ProximityApi getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ProximityApi(context.getApplicationContext());
        }

        return sInstance;
    }

    private static final String BEACON_ROOT = "https://proximitybeacon.googleapis.com/v1beta1";

    private RequestQueue mRequestQueue;
    private HashSet<ApiDataCallback> mCallbacks;
    private String mAuthToken;

    private ProximityApi(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mCallbacks = new HashSet<>();
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public boolean hasToken() {
        return !TextUtils.isEmpty(mAuthToken);
    }

    /* Callback Handlers */

    public boolean registerDataCallback(ApiDataCallback callback) {
        return mCallbacks.add(callback);
    }

    public boolean unregisterDataCallback(ApiDataCallback callback) {
        return mCallbacks.remove(callback);
    }

    //Callback to receive beacon register/get responses
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
            notifyListeners(beacon);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (error.networkResponse == null) {
                Log.w(TAG, "Unknown error response from Proximity API", error);
                return;
            }

            //Called for 4xx responses
            switch (error.networkResponse.statusCode) {
                case 403:
                    notifyListeners(new Beacon(mAdvertisedId, Beacon.Status.UNAUTHORIZED));
                    break;
                case 404:
                    notifyListeners(new Beacon(mAdvertisedId, Beacon.Status.UNREGISTERED));
                    break;
                default:
                    Log.w(TAG, "Unknown error response from Proximity API", error);
            }
        }

        private void notifyListeners(Beacon result) {
            for (ApiDataCallback callback : mCallbacks) {
                callback.onBeaconResponse(result);
            }
        }
    }

    //Callback to receive attachment create/delete responses
    private class AttachmentDataCallback implements
            Response.Listener<JSONObject>, Response.ErrorListener {
        @Override
        public void onResponse(JSONObject response) {
            Log.v(TAG, "Attachment Request Completed");
            notifyListeners();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.w(TAG, "Attachment API Error", error);
            notifyListeners();
        }

        private void notifyListeners() {
            for (ApiDataCallback callback : mCallbacks) {
                callback.onAttachmentResponse();
            }
        }
    }

    /* API Endpoint Methods */

    /**
     * namespaces.list
     * Return a list of usable project namespaces for attachments
     */
    public void getNamespaces(Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        final String endpoint = String.format("%s/namespaces", BEACON_ROOT);
        Log.v(TAG, "Getting namespaces: " + endpoint);
        performGetRequest(endpoint, listener, errorListener);
    }

    /**
     * beacons.register
     * Post an observed beacon to attach it to this API project
     */
    public void registerBeacon(Beacon beacon) {
        BeaconDataCallback callback = new BeaconDataCallback(beacon.advertisedId);
        final String endpoint = String.format("%s/beacons:register", BEACON_ROOT);
        Log.v(TAG, "Creating beacon: " + endpoint);
        try {
            performPostRequest(endpoint, beacon.toJson(), callback, callback);
        } catch (JSONException e) {
            Log.w(TAG, "Unable to serialize beacon", e);
            callback.onErrorResponse(new VolleyError(e));
        }
    }

    /**
     * beacons.get
     * Return beacon resource matching the given name
     */
    public void getBeacon(Beacon.AdvertisedId beaconId) {
        BeaconDataCallback callback = new BeaconDataCallback(beaconId);
        final String endpoint = String.format("%s/%s", BEACON_ROOT,
                beaconId.toBeaconName());
        Log.v(TAG, "Getting beacon: " + endpoint);
        performGetRequest(endpoint, callback, callback);
    }

    /**
     * beacons.list
     * Return a list of all beacons in this API project
     */
    public void getBeaconsList(Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        final String endpoint = String.format("%s/beacons", BEACON_ROOT);
        Log.v(TAG, "Getting beacons: " + endpoint);
        performGetRequest(endpoint, listener, errorListener);
    }

    /**
     * beacons.attachments.list
     * Return a list of attachments for the given beacon name
     */
    public void getAttachmentsList(String beaconName,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        final String endpoint = String.format("%s/%s/attachments",
                BEACON_ROOT, beaconName);
        Log.v(TAG, "Getting attachments: " + endpoint);
        performGetRequest(endpoint, listener, errorListener);
    }

    /**
     * beacons.attachments.create
     * Post a new attachment to the given beacon name
     */
    public void createAttachment(String beaconName, String data,
                                 String namespacedType) {
        AttachmentDataCallback callback = new AttachmentDataCallback();
        Attachment toCreate = new Attachment(data, namespacedType);
        final String endpoint = String.format("%s/%s/attachments",
                BEACON_ROOT, beaconName);
        Log.v(TAG, "Creating attachment: " + endpoint);
        try {
            performPostRequest(endpoint, toCreate.toJson(), callback, callback);
        } catch (JSONException e) {
            Log.w(TAG, "Unable to create attachment object");
        }
    }

    /**
     * beacons.attachments.delete
     * Delete the data matching the given attachment name
     */
    public void deleteAttachment(Attachment attachment) {
        AttachmentDataCallback callback = new AttachmentDataCallback();
        //Attachment name contains beacon name
        final String endpoint = String.format("%s/%s",
                BEACON_ROOT, attachment.name);
        Log.v(TAG, "Deleting attachment: " + endpoint);
        performDeleteRequest(endpoint, callback, callback);
    }

    /* Base Request Handler Methods */

    private void performPostRequest(String endpoint, JSONObject body,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {
        AuthenticatedRequest request = new AuthenticatedRequest(
                Request.Method.POST, endpoint, body, listener, errorListener);
        request.setAuthToken(mAuthToken);
        mRequestQueue.add(request);
    }

    private void performDeleteRequest(String endpoint,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        AuthenticatedRequest request = new AuthenticatedRequest(
                Request.Method.DELETE, endpoint, null, listener, errorListener);
        request.setAuthToken(mAuthToken);
        mRequestQueue.add(request);
    }

    private void performGetRequest(String endpoint,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        AuthenticatedRequest request = new AuthenticatedRequest(
                Request.Method.GET, endpoint, null, listener, errorListener);
        request.setAuthToken(mAuthToken);
        mRequestQueue.add(request);
    }

}
