package com.example.android.proximitymanager.data;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representation of a beacons.attachments resource
 */
public class Attachment {

    public static List<Attachment> fromJson(JSONObject object) throws JSONException {
        ArrayList<Attachment> items = new ArrayList<>();

        JSONArray attachmentList = object.optJSONArray("attachments");
        if (attachmentList != null) {
            for (int i = 0; i < attachmentList.length(); i++) {
                Attachment next =
                        new Attachment(attachmentList.getJSONObject(i));
                items.add(next);
            }
        }

        return items;
    }

    public final String name;
    public final String namespacedType;
    // The data field is always Base64 encoded
    public final String data;

    public Attachment(JSONObject object) {
        this.name = object.optString("attachmentName");
        this.namespacedType = object.optString("namespacedType");
        this.data = object.optString("data");
    }

    public Attachment(String data, String namespacedType) {
        this.name = "";
        this.namespacedType = namespacedType;
        this.data = encodeRawData(data);
    }

    private String encodeRawData(String raw) {
        return Base64.encodeToString(raw.getBytes(), Base64.NO_WRAP);
    }

    private String decodeData() {
        return new String(Base64.decode(this.data, Base64.NO_WRAP));
    }

    public String getData() {
        return TextUtils.isEmpty(this.data) ?
                this.data : decodeData();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(this.name)) {
            object.put("attachmentName", this.name);
        }
        object.put("namespacedType", this.namespacedType);
        object.put("data", this.data);

        return object;
    }

    @Override
    public String toString() {
        return getData();
    }
}
