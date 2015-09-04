package com.example.android.proximitymanager;

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

import com.example.android.proximitymanager.api.BeaconsLoader;
import com.example.android.proximitymanager.api.ProximityApi;
import com.example.android.proximitymanager.data.Beacon;
import com.example.android.proximitymanager.data.BeaconAdapter;

import java.util.List;

public class BeaconListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Beacon>>,
        AdapterView.OnItemClickListener {

    private static final int REQUEST_AUTH = 42;

    private ListView mList;
    private BeaconAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mList = (ListView) findViewById(R.id.list);

        mAdapter = new BeaconAdapter(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);

        if (!ProximityApi.getInstance(this).hasToken()) {
            //Launch account picker for first runs
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivityForResult(intent, REQUEST_AUTH);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTH) {
            if (resultCode != RESULT_OK) {
                Utils.showToast(this,
                        "You must authenticate a valid Google account");
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ProximityApi.getInstance(this).hasToken()) {
            refreshLoader();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_beacon_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshLoader();
                return true;
            case R.id.action_register:
                Intent intent = new Intent(this, BeaconRegisterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Beacon beacon = mAdapter.getItem(position);

        Intent intent = new Intent(this, AttachmentsListActivity.class);
        intent.putExtra(Intent.EXTRA_UID, beacon.name);
        startActivity(intent);
    }

    @Override
    public Loader<List<Beacon>> onCreateLoader(int id, Bundle args) {
        return new BeaconsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Beacon>> loader, List<Beacon> data) {
        if (mList.getEmptyView() == null) {
            mList.setEmptyView(findViewById(R.id.empty));
        }

        if (data == null) {
            Utils.showToast(this, "Unable to load beacons");
            return;
        }

        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Beacon>> loader) {
        mAdapter.clear();
    }

    private void refreshLoader() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }
}
