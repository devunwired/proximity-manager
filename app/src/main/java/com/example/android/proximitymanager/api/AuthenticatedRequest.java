package com.example.android.proximitymanager.api;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of Volley JsonObjectRequest that applies the
 * proper request headers for the beacon API client OAuth.
 */
public class AuthenticatedRequest extends JsonObjectRequest {

    private String mAuthToken;

    public AuthenticatedRequest(int method, String url, JSONObject jsonRequest,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public void setAuthToken(String token) {
        mAuthToken = token;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + mAuthToken);

        return headers;
    }
}
