package com.example.android.proximitymanager.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.proximitymanager.R;

public class BeaconAdapter extends ArrayAdapter<Beacon> {

    public BeaconAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.beacon_item, parent, false);
        }

        final Beacon item = getItem(position);
        TextView text1 = (TextView) convertView.findViewById(R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(R.id.text2);
        text1.setText(item.description);
        text2.setText(String.format("%s: %s", item.name, item.status.toString()));

        return convertView;
    }
}
