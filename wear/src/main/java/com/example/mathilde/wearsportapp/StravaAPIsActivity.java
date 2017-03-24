package com.example.mathilde.wearsportapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StravaAPIsActivity extends WearableActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TEXT = "com.example.wearsportapp.text";
    private static final String RESPONSE = "com.example.wearsportapp.response";
    private GoogleApiClient apiClient;

    @OnClick(R.id.retrieve_athlete)
    public void onRetrieveAthleteClicked(){
        syncDataMap("RA");
    }

    @OnClick(R.id.stats_athlete)
    public void onRetrieveStatsAthleteClicked(){
        syncDataMap("SA");
    }

    @OnClick(R.id.list_activities)
    public void onCreateActivityClicked(){
        syncDataMap("LA");
    }

    @OnClick(R.id.upload_file)
    public void onUploadFileClicked(){ syncDataMap("UF"); }

    @Bind(R.id.text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_apis);
        ButterKnife.bind(this);

        initializeClient();
        mTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.DataApi.addListener(apiClient, this);
        if(apiClient==null ||!apiClient.isConnected()){
          apiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(apiClient, this);
        if(apiClient.isConnected()){
          apiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void syncDataMap(String string){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/action");
        putDataMapReq.getDataMap().putString(TEXT, string);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(apiClient, putDataReq);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        mTextView.setText("Fetching data...");
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/action_response".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(RESPONSE);
                mTextView.setText(string);
            }
        }
    }
}
