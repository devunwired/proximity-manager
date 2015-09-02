package com.example.android.proximitymanager.data;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.proximitymanager.R;
import com.example.android.proximitymanager.api.AttachmentService;


public class AttachmentAdapter extends ArrayAdapter<Attachment> implements
        View.OnClickListener {

    public AttachmentAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.attachment_item, parent, false);
        }

        final Attachment item = getItem(position);
        TextView text1 = (TextView) convertView.findViewById(R.id.text1);
        TextView text2 = (TextView) convertView.findViewById(R.id.text2);
        text1.setText(item.getData());
        text2.setText(item.namespacedType);

        View deleteButton = convertView.findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(this);
        deleteButton.setTag(position);

        return convertView;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        final Attachment item = getItem(position);
        new AlertDialog.Builder(getContext())
                .setTitle("Are You Sure?")
                .setMessage("Do you want to delete " + item.name + " ?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AttachmentService.delete(getContext(), item.name);
                    }
                })
                .show();
    }
}
