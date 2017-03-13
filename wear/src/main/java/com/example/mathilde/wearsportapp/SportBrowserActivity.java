package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class SportBrowserActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SportInterface{

    private List<Sport> sports;
    private Node node;
    private GoogleApiClient apiClient;
    private static final String OPEN_LINK_PATH = "/open_link";

    public static Intent createIntent(Context context){
        return new Intent(context, SportBrowserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_browser);

        WearableRecyclerView rv = (WearableRecyclerView)findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        rv.setCenterEdgeItems(true);
        rv.setOffsettingHelper(new DefaultOffsettingHelper());

        initializeData();

        connectApiClient();

        SportBrowserAdapter adapter = new SportBrowserAdapter(sports, getApplicationContext(), this);
        rv.setAdapter(adapter);

    }

    private void initializeData(){
        sports = new ArrayList<>();
        sports.add(new Sport("Rowing", "description", R.drawable.ic_row, "https://en.wikipedia.org/wiki/Rowing_(sport)"));
        sports.add(new Sport("Skiing", "description", R.drawable.ic_ski, "https://en.wikipedia.org/wiki/Skiing"));
        sports.add(new Sport("Running", "description", R.drawable.ic_run, "https://en.wikipedia.org/wiki/Running"));
        sports.add(new Sport("CrossFit", "description", R.drawable.ic_cross, "https://en.wikipedia.org/wiki/CrossFit"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    private void connectApiClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        SportBrowserActivity.this.node = node;
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        apiClient.disconnect();
    }

    public void sendMessage(String message){
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), OPEN_LINK_PATH, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()){
                                showConfirmation();
                            } else {
                                showError("Could not send message");
                            }
                        }
                    }
            );
        } else if(node==null) {
            showError("Could not find node");
        } else {
            showError("Could not connect to API");
        }
    }

    private void showConfirmation(){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Open on phone");
        startActivity(intent);
    }

    private void showError(String error){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                error);
        startActivity(intent);
    }


}


