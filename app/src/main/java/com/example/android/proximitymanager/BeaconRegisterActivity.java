package com.example.android.proximitymanager;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.proximitymanager.api.ApiDataCallback;
import com.example.android.proximitymanager.api.ProximityApi;
import com.example.android.proximitymanager.data.Beacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BeaconRegisterActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        ApiDataCallback {
    private static final String TAG =
            BeaconRegisterActivity.class.getSimpleName();

    // Eddystone service uuid (0xfeaa)
    private static final ParcelUuid UID_SERVICE =
            ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ProximityApi mProximityApi;
    private AbleBeaconsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView list = new ListView(this);
        setContentView(list);

        mAdapter = new AbleBeaconsAdapter(this);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mProximityApi = ProximityApi.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.showToast(this, "No LE Support.");
            finish();
            return;
        }


        mProximityApi.registerDataCallback(this);
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
        mProximityApi.unregisterDataCallback(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Beacon selected = mAdapter.getItem(position);
        switch (selected.status) {
            case STATUS_UNSPECIFIED:
                Utils.showToast(this, "Unable to determine beacon status");
                break;
            case UNREGISTERED:
                showRegisterDialog(selected);
                break;
            case UNAUTHORIZED:
                Utils.showToast(this, "Sorry! This beacon belongs to someone else!");
                break;
            default:
                Utils.showToast(this, "Congratulations! You already own this beacon.");
                break;
        }
    }

    private void showRegisterDialog(final Beacon selected) {
        //Show dialog to confirm and get additional data
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Register New Beacon")
                .setView(R.layout.register_dialog)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Finally make registration request with API
                        EditText descriptionText = (EditText) ((AlertDialog) dialog)
                                .findViewById(R.id.text_description);
                        Beacon beacon = new Beacon(selected.advertisedId,
                                Beacon.Status.ACTIVE,
                                descriptionText.getText().toString());
                        mProximityApi.registerBeacon(beacon);
                    }
                })
                .create();
        dialog.show();
    }

    /* Bluetooth LE Scanning Methods */

    private void startScan() {
        //Return only Eddystone beacons matching the filter
        ScanFilter beaconFilter = new ScanFilter.Builder()
                .setServiceUuid(UID_SERVICE)
                .setServiceData(UID_SERVICE, Beacon.FRAME_FILTER, Beacon.FILTER_MASK)
                .build();
        List<ScanFilter> filters = Collections.singletonList(beaconFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    /*
     * Callback to handle Bluetooth LE scan results
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w(TAG, "LE Scan Failed: " + errorCode);
        }

        private void processResult(ScanResult result) {
            ScanRecord record = result.getScanRecord();
            if (record == null || record.getServiceData(UID_SERVICE) == null) {
                Log.w(TAG, "Invalid Eddystone scan result.");
                return;
            }

            //Convert scan result into an AdvertisedId
            Beacon.AdvertisedId advertisedId =
                    Beacon.AdvertisedId.fromAdvertisement(record.getServiceData(UID_SERVICE));
            final Beacon discovered = new Beacon(advertisedId, Beacon.Status.STATUS_UNSPECIFIED);
            //Scan callbacks are not on the main thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Notify the adapter, and get beacon resource from API
                    boolean added = mAdapter.addDiscoveredBeacon(discovered);
                    if (added) {
                        mProximityApi.getBeacon(discovered.advertisedId);
                    }
                }
            });
        }
    };

    /* ApiDataCallback Methods */

    /*
     * Beacon data callback with latest response from API. Update
     * the adapter with the new beacon status. This will be called
     * when a beacon is observed or a new beacon registration.
     */
    @Override
    public void onBeaconResponse(Beacon beacon) {
        mAdapter.updateBeacon(beacon);
    }

    @Override
    public void onAttachmentResponse() {
        //Not used in this context
    }

    /* Custom List Adapter */

    private static class AbleBeaconsAdapter extends BaseAdapter {

        //Always list unregistered beacons at the top
        private static Comparator<Beacon> STATUS_SORT = new Comparator<Beacon>() {
            @Override
            public int compare(Beacon lhs, Beacon rhs) {
                if (Beacon.Status.UNREGISTERED == lhs.status) {
                    return -1;
                }
                if (Beacon.Status.UNREGISTERED == rhs.status) {
                    return 1;
                }

                return lhs.status.compareTo(rhs.status);
            }
        };

        private LayoutInflater mInflater;
        private ArrayList<Beacon> mDiscoveredItems;

        public AbleBeaconsAdapter(Context context) {
            mInflater = LayoutInflater.from(context);

            mDiscoveredItems = new ArrayList<>();
        }

        public void updateBeacon(Beacon beacon) {
            if (mDiscoveredItems.contains(beacon)) {
                int position = mDiscoveredItems.indexOf(beacon);
                mDiscoveredItems.remove(position);
                mDiscoveredItems.add(position, beacon);
                refreshListData();
            }
        }

        public boolean addDiscoveredBeacon(Beacon beacon) {
            if (!mDiscoveredItems.contains(beacon)) {
                mDiscoveredItems.add(beacon);
                refreshListData();
                return true;
            }

            //No change
            return false;
        }

        private void refreshListData() {
            Collections.sort(mDiscoveredItems, STATUS_SORT);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDiscoveredItems.size();
        }

        @Override
        public Beacon getItem(int position) {
            return mDiscoveredItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Beacon item = getItem(position);
            TextView title = (TextView) convertView.findViewById(android.R.id.text1);
            TextView subtitle = (TextView) convertView.findViewById(android.R.id.text2);

            title.setText(item.advertisedId.getId());

            switch (item.status) {
                case STATUS_UNSPECIFIED:
                    //We're not sure yet
                    title.setTextColor(Color.DKGRAY);
                    subtitle.setText("Fetching Beacon Data…");
                    break;
                case UNREGISTERED:
                    //Beacon is usable
                    title.setTextColor(Color.GREEN);
                    subtitle.setText("Able to Register");
                    break;
                case UNAUTHORIZED:
                    //Beacon belongs to someone else
                    title.setTextColor(Color.RED);
                    subtitle.setText("Beacon Registered to Another Application");
                    break;
                default:
                    title.setTextColor(Color.rgb(255, 127, 0));
                    subtitle.setText("Beacon Already Registered");
                    break;
            }

            return convertView;
        }
    }
}
