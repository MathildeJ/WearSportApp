package com.example.mathilde.wearsportapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;


public class GoogleApiClientWithNodeFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    private GoogleApiClient apiClient;
    private Node node;

    private boolean showConfirmation;
    private boolean useDataApiListener;
    private boolean disconnectClientOnPause;
    private String sendMessageOnDestroy;


    public GoogleApiClientWithNodeFragment() {
        // Required empty public constructor
    }

    public static GoogleApiClientWithNodeFragment newInstance(boolean param1, boolean param2, boolean param3, String param4) {
        GoogleApiClientWithNodeFragment fragment = new GoogleApiClientWithNodeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        args.putBoolean(ARG_PARAM2, param2);
        args.putBoolean(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showConfirmation = getArguments().getBoolean(ARG_PARAM1);
            useDataApiListener = getArguments().getBoolean(ARG_PARAM2);
            disconnectClientOnPause = getArguments().getBoolean(ARG_PARAM3);
            sendMessageOnDestroy = getArguments().getString(ARG_PARAM4);
        }

        initializeClient();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        resolveNode();
        if(useDataApiListener){
            Wearable.DataApi.addListener(apiClient, (DataApi.DataListener)getActivity());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        GoogleApiClientWithNodeFragment.this.node = node;
                    }
                }
            }
        });
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void sendMessage(String path, String message){
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), path, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(showConfirmation){
                                if(sendMessageResult.getStatus().isSuccess()){
                                    showConfirmation();
                                } else {
                                    showError("Could not send message");
                                }
                            }
                        }
                    }
            );
        } else if(node==null && showConfirmation) {
            showError("Could not find node");
        } else if(showConfirmation) {
            showError("Could not connect to API");
        }
    }

    private void showConfirmation(){
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Open on phone");
        startActivity(intent);
    }

    private void showError(String error){
        Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                error);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(disconnectClientOnPause){
            if(apiClient.isConnected()){
                apiClient.disconnect();
            }
        }
        if(useDataApiListener){
            Wearable.DataApi.removeListener(apiClient, (DataApi.DataListener)getActivity());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(apiClient == null || !apiClient.isConnected()){
            apiClient.connect();
        }
        if(useDataApiListener){
            Wearable.DataApi.addListener(apiClient, (DataApi.DataListener)getActivity());
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(!sendMessageOnDestroy.equals("")){
            sendMessage(sendMessageOnDestroy, "");
        }
    }

}
