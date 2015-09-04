package com.example.android.proximitymanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.proximitymanager.api.ApiDataCallback;
import com.example.android.proximitymanager.api.NamespacesLoader;
import com.example.android.proximitymanager.api.ProximityApi;
import com.example.android.proximitymanager.data.Beacon;
import com.example.android.proximitymanager.data.Namespace;

import java.util.List;

public class AttachmentCreateActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Namespace>>,
        View.OnClickListener,
        ApiDataCallback {

    private Spinner mNamespaceSelect;
    private EditText mAttachmentType, mAttachmentText;
    private Button mSaveButton;
    private ArrayAdapter<Namespace> mNamespaceAdapter;

    private ProximityApi mProximityApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mAttachmentType = (EditText) findViewById(R.id.text_type);
        mAttachmentText = (EditText) findViewById(R.id.text_attach);
        mSaveButton = (Button) findViewById(R.id.button_save);

        //Default button to disabled
        mSaveButton.setOnClickListener(this);
        mSaveButton.setEnabled(false);

        mNamespaceSelect = (Spinner) findViewById(R.id.spinner);

        mNamespaceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mNamespaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNamespaceSelect.setAdapter(mNamespaceAdapter);

        mProximityApi = ProximityApi.getInstance(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View v) {
        final String beaconName = getIntent().getStringExtra(Intent.EXTRA_UID);

        Namespace selectedNamespace = mNamespaceAdapter.getItem(
                mNamespaceSelect.getSelectedItemPosition());
        String typeString = mAttachmentType.getText().toString();
        final String namespacedType = selectedNamespace.getNamespacedType(typeString);

        final String data = mAttachmentText.getText().toString();

        mProximityApi.createAttachment(beaconName, data, namespacedType);
        finish();
    }

    private void updateSaveButton() {
        mSaveButton.setEnabled(!mNamespaceAdapter.isEmpty());
    }

    /* LoaderCallbacks Methods */

    @Override
    public Loader<List<Namespace>> onCreateLoader(int id, Bundle args) {
        return new NamespacesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Namespace>> loader, List<Namespace> data) {
        mNamespaceAdapter.setNotifyOnChange(false);
        mNamespaceAdapter.clear();
        mNamespaceAdapter.addAll(data);
        mNamespaceAdapter.notifyDataSetChanged();
        updateSaveButton();
    }

    @Override
    public void onLoaderReset(Loader<List<Namespace>> loader) {
        mNamespaceAdapter.clear();
        updateSaveButton();
    }

    /* ApiDataCallback Methods */

    @Override
    public void onBeaconResponse(Beacon beacon) {
        //Not used in this context
    }

    @Override
    public void onAttachmentResponse() {

    }
}
