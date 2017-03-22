package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResultChallengeActivity extends WearableActivity implements
        DataApi.DataListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private static final String SESSION_INFO = "com.example.mathilde.wearsportapp.sessioninfo";
    private GoogleApiClient apiClient;
    private Node node;
    private static final String CHALLENGE_DONE_PATH = "/challenge_done";

    @Bind(R.id.text)
    TextView mTextView;

    @OnClick(R.id.button)
    public void onButtonClicked(){
        finish();
        sendMessage("done");
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_challenge);
        ButterKnife.bind(this);

        initializeClient();
    }

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, ResultChallengeActivity.class);
        return intent;
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/session_read".equals(path)){
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(SESSION_INFO);
                mTextView.setText(string);
            }
        }
    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        ResultChallengeActivity.this.node = node;
                    }
                }
            }
        });
    }

    public void sendMessage(String message){
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), CHALLENGE_DONE_PATH, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()){
                            } else {
                            }
                        }
                    }
            );
        } else if(node==null) {
        } else {
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        Wearable.DataApi.removeListener(apiClient, this);
        if(apiClient.isConnected()){
            apiClient.disconnect();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(apiClient == null || !apiClient.isConnected()){
          apiClient.connect();
        }
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
