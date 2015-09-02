package com.example.android.proximitymanager.api;

import android.content.Context;
import android.util.Log;

import com.example.android.proximitymanager.data.Namespace;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Loader to execute and parse namespaces.list
 */
public class NamespacesLoader extends BaseApiLoader<List<Namespace>> {
    private static final String TAG = NamespacesLoader.class.getSimpleName();

    public NamespacesLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartRequest() {
        ProximityApi.getInstance(getContext())
                .getNamespaces(this, this);
    }

    @Override
    protected List<Namespace> onHandleResponse(JSONObject response) {
        //API Error
        if (response == null) return null;

        try {
            return Namespace.fromJson(response);
        } catch (JSONException e) {
            Log.w(TAG, "Parsing error", e);
            return null;
        }
    }
}
