package com.example.android.proximitymanager.data;


import android.text.TextUtils;
import android.util.Base64;

import com.example.android.proximitymanager.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representation of a beacons resource
 */
public class Beacon {

    // Filter to ensure we only get Eddystone-UID advertisements
    public static final byte[] FRAME_FILTER = {
            0x00, //Frame type
            0x00, //TX power
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    // Force frame type only to match
    public static final byte[] FILTER_MASK = {
            (byte)0xFF,
            0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    public static List<Beacon> fromJson(JSONObject object) throws JSONException {
        ArrayList<Beacon> items = new ArrayList<>();

        JSONArray beaconList = object.optJSONArray("beacons");
        if (beaconList != null) {
            for (int i = 0; i < beaconList.length(); i++) {
                Beacon next = new Beacon(beaconList.getJSONObject(i));
                items.add(next);
            }
        }

        return items;
    }

    public enum Status {
        STATUS_UNSPECIFIED,
        ACTIVE,
        DECOMMISSIONED,
        INACTIVE,
        //These two statuses are used internally
        UNAUTHORIZED,
        UNREGISTERED
    }

    public final String name;
    public final AdvertisedId advertisedId;
    public final String description;
    public final Status status;

    public Beacon(JSONObject object) {
        this.name = object.optString("beaconName");
        this.status = Status.valueOf(object.optString("status"));
        this.advertisedId = new AdvertisedId(object.optJSONObject("advertisedId"));
        //If no description, use the beaconId from the name
        String description = object.optString("description");
        this.description = TextUtils.isEmpty(description) ?
                name.substring(name.indexOf('!')+1, name.length()) : description;
    }

    public Beacon(AdvertisedId advertisedId, Status status) {
        this(advertisedId, status, null);
    }

    public Beacon(AdvertisedId advertisedId, Status status, String description) {
        this.name = null;
        this.status = status;
        this.advertisedId = advertisedId;
        this.description = description;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        if (!TextUtils.isEmpty(this.name)) {
            object.put("beaconName", this.name);
        }
        object.put("advertisedId", this.advertisedId.toJson());
        object.put("status", this.status.toString());
        if (!TextUtils.isEmpty(this.description)) {
            object.put("description", this.description);
        }

        return object;
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Beacon
                && ((Beacon) object).advertisedId.id.equals(this.advertisedId.id));
    }

    /* Inner Model for nested AdvertisedId objects */
    public static class AdvertisedId {

        public static AdvertisedId fromAdvertisement(byte[] advertisement) {
            //Parse out the last 16 bytes for the Eddystone id
            int packetLength = 16;
            int offset = advertisement.length - packetLength;
            String id = Base64.encodeToString(advertisement, offset, packetLength, Base64.NO_WRAP);

            return new AdvertisedId(Type.EDDYSTONE, id);
        }

        public enum Type {
            EDDYSTONE(3),
            IBEACON(1),
            ALTBEACON(5);

            private final int mCode;
            Type(int code) {
                mCode = code;
            }
            public String getCode() {
                return mCode + "!";
            }
        }

        public final Type type;
        // The data field is always Base64 encoded
        public final String id;

        public AdvertisedId(JSONObject object) {
            this.type = Type.valueOf(object.optString("type"));
            this.id = object.optString("id");
        }

        private AdvertisedId(Type type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getId() {
            byte[] rawId = Base64.decode(this.id, Base64.NO_WRAP);
            return Utils.toHexString(rawId, 0, rawId.length);
        }

        public String toBeaconName() {
            StringBuilder sb = new StringBuilder();
            sb.append("beacons/");
            sb.append(type.getCode());
            sb.append(getId());

            return sb.toString();
        }

        public JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();

            object.put("type", this.type.toString());
            object.put("id", this.id);

            return object;
        }

        @Override
        public boolean equals(Object object) {
            return (object instanceof AdvertisedId
                    && ((AdvertisedId) object).id.equals(this.id));
        }
    }
}
