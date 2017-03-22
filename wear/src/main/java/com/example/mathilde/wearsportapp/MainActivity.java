package com.example.mathilde.wearsportapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements
        WearableActionDrawer.OnMenuItemClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private WearableDrawerLayout mWearableDrawerLayout;
    private WearableActionDrawer mWearableActionDrawer;

    private static final String STRAVA_LOGIN = "/strava_login";
    private GoogleApiClient apiClient;
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WearableRecyclerView menu_list = (WearableRecyclerView) findViewById(R.id.menu_list);
        menu_list.setCenterEdgeItems(true);
        menu_list.setOffsettingHelper(new DefaultOffsettingHelper());

        com.example.mathilde.wearsportapp.MenuAdapter ma = new com.example.mathilde.wearsportapp.MenuAdapter();
        ma.setContext(getApplicationContext());
        menu_list.setAdapter(ma);

        mWearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawer_layout);
        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.setOnMenuItemClickListener(this);

        initializeClient();
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        final int itemId = menuItem.getItemId();
        Toast.makeText(getApplicationContext(), menuItem.toString(), Toast.LENGTH_LONG);
        switch(itemId){
            case R.id.menu_send_notification:
                startActivity(SendNotificationActivity.createIntent(getApplicationContext(), "Send notifications"));
                break;
            case R.id.menu_send_message:
                sendStravaLoginMessage("");
                break;
        }
        mWearableDrawerLayout.closeDrawer(mWearableActionDrawer);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        MainActivity.this.node = node;
                    }
                }
            }
        });
    }

    private void sendStravaLoginMessage(String message){
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), STRAVA_LOGIN, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()){
                                showConfirmation();
                            } else {
                                showError("Could not send Strava login request");
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

/*
    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }
*/
    @Override
    protected void onPause() {
        super.onPause();
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
    }
/*
    @Override
    protected void onStop(){
        apiClient.disconnect();
        super.onStop();
    }
*/
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
