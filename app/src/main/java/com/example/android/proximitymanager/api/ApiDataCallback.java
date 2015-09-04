package com.example.android.proximitymanager.api;

import com.example.android.proximitymanager.data.Beacon;


public interface ApiDataCallback {
    void onBeaconResponse(Beacon beacon);
    void onAttachmentResponse();
}
