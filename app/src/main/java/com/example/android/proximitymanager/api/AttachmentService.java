package com.example.android.proximitymanager.api;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.proximitymanager.R;
import com.example.android.proximitymanager.Utils;

import org.json.JSONObject;

public class AttachmentService extends Service {
    private static final String TAG =
            AttachmentService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 42;

    private static final String ACTION_DELETE = "AttachmentService.DELETE";
    private static final String ACTION_CREATE = "AttachmentService.CREATE";

    private static final String PARAM_BEACON = "beacon";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_DATA = "data";
    private static final String PARAM_TYPE = "type";


    public static void delete(Context context, String attachmentName) {
        Intent intent = new Intent(context, AttachmentService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(PARAM_NAME, attachmentName);

        context.startService(intent);
    }

    public static void create(Context context, String beaconName, String data, String type) {
        Intent intent = new Intent(context, AttachmentService.class);
        intent.setAction(ACTION_CREATE);
        intent.putExtra(PARAM_BEACON, beaconName);
        intent.putExtra(PARAM_DATA, data);
        intent.putExtra(PARAM_TYPE, type);

        context.startService(intent);
    }

    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AttachmentService", "Service Started…");
        mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AttachmentService", "Service Stopped…");
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_CREATE.equals(intent.getAction())) {
            postNotification("Creating New Attachment");
            doAttachmentCreate(startId,
                    intent.getStringExtra(PARAM_BEACON),
                    intent.getStringExtra(PARAM_DATA),
                    intent.getStringExtra(PARAM_TYPE) );
        }

        if (ACTION_DELETE.equals(intent.getAction())) {
            postNotification("Deleting Attachment");
            doAttachmentDelete(startId,
                    intent.getStringExtra(PARAM_NAME) );
        }

        return START_NOT_STICKY;
    }

    private void doAttachmentDelete(int startId, String attachmentName) {
        ResponseCallback callback = new ResponseCallback(startId);
        ProximityApi.getInstance(this)
                .deleteAttachment(attachmentName, callback, callback);
    }

    private void doAttachmentCreate(int startId, String beaconName, String data, String type) {
        ResponseCallback callback = new ResponseCallback(startId);
        ProximityApi.getInstance(this)
                .createAttachment(beaconName, data, type, callback, callback);
    }

    private void postNotification(String message) {
        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle("Updating Attachments")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, note);
    }

    private class ResponseCallback implements
            Response.Listener<JSONObject>,
            Response.ErrorListener {
        private int mStartId;

        public ResponseCallback(int startId) {
            mStartId = startId;
        }

        @Override
        public void onResponse(JSONObject response) {
            Log.v(TAG, "Attachment Request Completed");
            Utils.showToast(AttachmentService.this,
                    "Attachment Updated. You may need to refresh.");
            stopSelf(mStartId);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.w(TAG, "Attachment API Error", error);
        }
    }
}
