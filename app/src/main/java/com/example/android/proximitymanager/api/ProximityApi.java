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
    private String mAuthToken;

    private ProximityApi(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public boolean hasToken() {
        return !TextUtils.isEmpty(mAuthToken);
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
    public void registerBeacon(Beacon beacon,
                               Response.Listener<JSONObject> listener,
                               Response.ErrorListener errorListener) {
        final String endpoint = String.format("%s/beacons:register", BEACON_ROOT);
        Log.v(TAG, "Creating beacon: " + endpoint);
        try {
            performPostRequest(endpoint, beacon.toJson(), listener, errorListener);
        } catch (JSONException e) {
            Log.w(TAG, "Unable to serialize beacon", e);
            errorListener.onErrorResponse(new VolleyError(e));
        }
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
     * beacons.get
     * Return beacon resource matching the given name
     */
    public void getBeacon(String beaconName,
                          Response.Listener<JSONObject> listener,
                          Response.ErrorListener errorListener) {
        final String endpoint = String.format("%s/%s", BEACON_ROOT, beaconName);
        Log.v(TAG, "Getting beacon: " + endpoint);
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
                                 String namespacedType,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        Attachment toCreate = new Attachment(data, namespacedType);
        final String endpoint = String.format("%s/%s/attachments",
                BEACON_ROOT, beaconName);
        Log.v(TAG, "Creating attachment: " + endpoint);
        try {
            performPostRequest(endpoint, toCreate.toJson(), listener, errorListener);
        } catch (JSONException e) {
            Log.w(TAG, "Unable to create attachment object");
        }
    }

    /**
     * beacons.attachments.delete
     * Delete the data matching the given attachment name
     */
    public void deleteAttachment(String attachmentName,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        //Attachment name contains beacon name
        final String endpoint = String.format("%s/%s",
                BEACON_ROOT, attachmentName);
        Log.v(TAG, "Deleting attachment: " + endpoint);
        performDeleteRequest(endpoint, listener, errorListener);
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
