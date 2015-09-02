package com.example.android.proximitymanager.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representation of a namespaces resource
 */
public class Namespace {

    public static List<Namespace> fromJson(JSONObject object) throws JSONException {
        ArrayList<Namespace> items = new ArrayList<>();

        JSONArray namespaces = object.getJSONArray("namespaces");
        for (int i=0; i < namespaces.length(); i++) {
            items.add(new Namespace(namespaces.getJSONObject(i)));
        }

        return items;
    }

    public enum Visibility {
        UNLISTED,
        PUBLIC
    }

    public final String name;
    public final Visibility visibility;

    public Namespace(JSONObject object) {
        this.name = object.optString("namespaceName");
        this.visibility = Visibility.valueOf(object.optString("servingVisibility"));
    }

    public String getCleanName() {
        return this.name.replace("namespaces/", "");
    }

    @Override
    public String toString() {
        //Returned for adapter display only
        return getCleanName() + "/";
    }

    public String getNamespacedType(String type) {
        return String.format("%s/%s", getCleanName(), type);
    }
}
