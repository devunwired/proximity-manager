package com.example.android.proximitymanager;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.proximitymanager.api.AttachmentsLoader;
import com.example.android.proximitymanager.data.Attachment;
import com.example.android.proximitymanager.data.AttachmentAdapter;

import java.util.List;


public class AttachmentsListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Attachment>>,
        AdapterView.OnItemClickListener {

    private ListView mList;
    private AttachmentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mList = (ListView) findViewById(R.id.list);

        mAdapter = new AttachmentAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLoader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attachment_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshLoader();
                return true;
            case R.id.action_add:
                Intent intent = new Intent(this, AttachmentCreateActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Attachment item = mAdapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("Copy Data?")
                .setMessage("Do you want to copy this attachments data to the clipboard?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        manager.setPrimaryClip(ClipData.newPlainText("Attachment Item", item.getData()));

                        Utils.showToast(AttachmentsListActivity.this, "Saved to Clipboard");
                    }
                })
                .show();
    }

    @Override
    public Loader<List<Attachment>> onCreateLoader(int id, Bundle args) {
        return new AttachmentsLoader(this, args.getString(Intent.EXTRA_UID));
    }

    @Override
    public void onLoadFinished(Loader<List<Attachment>> loader, List<Attachment> data) {
        if (mList.getEmptyView() == null) {
            mList.setEmptyView(findViewById(R.id.empty));
        }

        if (data == null) {
            Utils.showToast(this, "Unable to load attachments");
            return;
        }

        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Attachment>> loader) {
        mAdapter.clear();
    }

    private void refreshLoader() {
        getSupportLoaderManager()
                .restartLoader(0, getIntent().getExtras(), this);
    }

}
