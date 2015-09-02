package com.example.android.proximitymanager.api;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Base Loader implementation to provide the hooks to execute Volley
 * requests and parse the responses.
 */
public abstract class BaseApiLoader<T> extends Loader<T> implements
        Response.Listener<JSONObject>, Response.ErrorListener {
    T mData;

    public BaseApiLoader(final Context context) {
        super(context);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        cancelLoad();
        onStartRequest();
    }

    // Override to trigger new API call
    protected abstract void onStartRequest();

    protected abstract T onHandleResponse(JSONObject response);

    @Override
    public void deliverResult(final T data) {
        mData = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mData != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }


    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        // At this point we can release the resources associated with 'data' if needed.
        if (mData != null) {
            mData = null;
        }
    }

    /* Volley Listener Methods */

    @Override
    public void onResponse(JSONObject response) {
        deliverResult(onHandleResponse(response));
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.w("ProximityApi", "API Request Error", error);
        cancelLoad();
    }
}
