package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StepChallengeActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        DelayedConfirmationView.DelayedConfirmationListener{

    private Node node;
    private GoogleApiClient apiClient;
    private static final String START_SESSION_PATH = "/start_session";
    private static final String TIMER_FINISHED_PATH = "/timer_finished";
    private static final String TIMER_STOPPED_PATH = "/timer_stopped";
    private static final String RESPONSE = "com.example.mathilde.wearsportapp.response";
    private static final String TAG = "StepChallengeActivity";

    private static final long THREE_MINUTES = 180000;

    @Bind(R.id.delayed_confirmation)
    DelayedConfirmationView delayedConfirmationView;

    @Bind(R.id.text)
    TextView mTextView;

    @OnClick(R.id.button)
    public void onButtonClicked(){sendMessage(START_SESSION_PATH,"");}

    private static final long ONE_MINUTE = 60000;
    private int timeRemaining;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_challenge);
        ButterKnife.bind(this);

        initializeClient();

        handler = new Handler();
        handler.postDelayed(showTimeRemaining, ONE_MINUTE);
    }

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, StepChallengeActivity.class);
        return intent;
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void configureDelayedStart() {
        delayedConfirmationView.setTotalTimeMs(10000);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.setVisibility(View.VISIBLE);
        delayedConfirmationView.start();
        timeRemaining = 3;
    }

    private Runnable showTimeRemaining = new Runnable(){
        @Override
        public void run(){
            if(timeRemaining < 0){
                finish();
            } else {
                timeRemaining--;
                if(timeRemaining == 1){
                    mTextView.setText("Only " + String.format("%d", timeRemaining) + " minute remaining");
                } else if(timeRemaining > 1) {
                    mTextView.setText("Only " + String.format("%d", timeRemaining) + " minutes remaining");
                }
                handler.postDelayed(this, ONE_MINUTE);
            }
        }
    };

    /*
    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
        Wearable.DataApi.addListener(apiClient, this);
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mTextView.setText("connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed");
    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        StepChallengeActivity.this.node = node;
                    }
                }
            }
        });
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
/*
    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(apiClient, this);
        apiClient.disconnect();
    }*/

    public void sendMessage(String path, String message){
        Log.i(TAG, "Send message " + message + "with path " + path);
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), path, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()){
                                Log.i(TAG, "Send message success");
                            } else {
                                Log.i(TAG, "Failed to send message");
                            }
                        }
                    }
            );
        } else if(node==null) {
            Log.i(TAG, "Could not find node");
        } else {

        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/session_started".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(RESPONSE);
                Log.i(TAG, "Data changed: " + string);
                configureDelayedStart();
            }
        }
    }


    @Override
    public void onTimerFinished(View view) {
        finish();
        sendMessage(TIMER_FINISHED_PATH, "step_challenge_finished");
        startResultChallenge();
    }

    @Override
    public void onTimerSelected(View view) {
        delayedConfirmationView.setListener(null);
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Session cancelled");
        startActivity(intent);
        finish();
        sendMessage(TIMER_STOPPED_PATH, "step_challenge_stopped");
    }

    private void startResultChallenge() {
        startActivity(ResultChallengeActivity.createIntent(this));
    }
}
