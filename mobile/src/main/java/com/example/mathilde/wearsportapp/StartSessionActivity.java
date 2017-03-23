package com.example.mathilde.wearsportapp;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.sweetzpot.stravazpot.athlete.model.Athlete;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.text.DateFormat.getTimeInstance;

public class StartSessionActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        MessageApi.MessageListener{

    private GoogleApiClient mClient = null;
    private Session mSession;

    private static final String TAG = "StartSessionActivity";
    private GoogleApiClient apiClient;
    private static final String RESPONSE = "com.example.mathilde.wearsportapp.response";
    private static final String SESSION_INFO = "com.example.mathilde.wearsportapp.sessioninfo";
    private static final String TIMER_FINISHED_PATH = "/timer_finished";
    private static final String TIMER_STOPPED_PATH = "/timer_stopped";
    private static final String CHALLENGE_DONE_PATH = "/challenge_done";
    private static final String SESSION_READ_PATH = "/session_read";
    private static final String SESSION_STARTED_PATH = "/session_started";
    private boolean permissionFineLocation = false;

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private static final int REQUEST_FINE_LOCATION = 100;

    private static final DataType[] valuesDataType = new DataType[]{DataType.TYPE_STEP_COUNT_DELTA, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_CALORIES_EXPENDED, DataType.TYPE_DISTANCE_DELTA};

    @Bind(R.id.text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);
        ButterKnife.bind(this);

        if(savedInstanceState != null){
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
        initializeClient();
    }


    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void buildFitnessClient() {
         // Create the Google API Client
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(this)
                    .useDefaultAccount()
                    .addApi(Fitness.SESSIONS_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.RECORDING_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "connected");
                                    startSession();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {

                                    }
                                }
                            }
                    )
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            if( !authInProgress ) {
                                try {
                                    authInProgress = true;
                                    connectionResult.startResolutionForResult(StartSessionActivity.this, REQUEST_OAUTH);
                                } catch(IntentSender.SendIntentException e ) {

                                }
                            } else {
                                Log.i(TAG, "authInProgress");
                            }
                        }
                    })
                    .build();
        }

    }

    private void subscribeFitnessData(){
        for(DataType dataType : valuesDataType){
            subscribeToType(dataType);
        }
        permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(permissionFineLocation){
            subscribeToType(DataType.TYPE_SPEED);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_FINE_LOCATION);
        }
    }

    private void subscribeToType(final DataType dataType){
        Fitness.RecordingApi.subscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Already subscribed to " + dataType.getName());
                            } else {
                                Log.i(TAG, "Subscribed to " + dataType.getName());
                            }
                        } else {
                            Log.i(TAG, "Subscription to " + dataType.getName() + " failed");
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                permissionFineLocation = true;
                subscribeToType(DataType.TYPE_SPEED);
            } else {
                // User refused to grant permission
                Log.i(TAG, "User didn't give permission to access device location");
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.MessageApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class StartAndVerifySessionTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            subscribeFitnessData();
            startNewSession();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            Log.i(TAG, "startandverify returned");
            syncDataMap(SESSION_STARTED_PATH, RESPONSE, "started_session_" + mSession.getIdentifier());
            mTextView.setText("Challenge started!");
        }
    }


    private void startSession(){
        new StartAndVerifySessionTask().execute();
    }

    private void startNewSession(){
        mSession = new Session.Builder()
                .setName("sessionName")
                .setIdentifier("ID_" + System.currentTimeMillis())
                .setDescription("session description")
                .setStartTime(System.currentTimeMillis()-1, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.WALKING)
                .build();

        PendingResult<Status> pendingResult = Fitness.SessionsApi.startSession(mClient, mSession);
        Status status = pendingResult.await(20, TimeUnit.SECONDS);
        if(status.isSuccess()){
            Log.i(TAG, "session started");
        } else {
            Log.i(TAG, "failed to start session " + status.getStatusMessage()+status.getStatus() + status.getStatusCode() );

        }
    }

    private void syncDataMap(String path, String type, String string){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
        putDataMapReq.getDataMap().putString(type, string);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        putDataReq.setUrgent();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(TIMER_FINISHED_PATH)) {
            Log.i(TAG, "message received (timer finished)");
            new StopSessionTask().execute();
        } else if (messageEvent.getPath().equals(TIMER_STOPPED_PATH) || messageEvent.getPath().equals(CHALLENGE_DONE_PATH)){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void insertSession(){
        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(mSession)
                .build();
        Status insertStatus = Fitness.SessionsApi.insertSession(mClient, insertRequest).await(1, TimeUnit.MINUTES);
        if(!insertStatus.isSuccess()){
            Log.i(TAG, "There was a problem inserting the session: " +
                    insertStatus.getStatusMessage());
        } else {
            Log.i(TAG, "Session inserted " +
                    insertStatus.getStatusMessage() + insertStatus.getStatusCode());
        }
    }

    private class StopSessionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            stopSession();
            insertSession();
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            long duration = mSession.getEndTime(TimeUnit.MILLISECONDS) - mSession.getStartTime(TimeUnit.MILLISECONDS);
            Log.i(TAG, "session " + mSession.getIdentifier() + " started " + mSession.getStartTime(TimeUnit.MILLISECONDS) + " stopped " + mSession.getEndTime(TimeUnit.MILLISECONDS) + ",duration: " + duration);
            mTextView.setText("Challenge stopped");
            new ReadDataTask().execute();
        }
    }

    private class ReadDataTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return readDataSession();
        }

        @Override
        protected void onPostExecute(String result){
          syncDataMap(SESSION_READ_PATH, SESSION_INFO, result);
          mTextView.setText("Reading results...");
        }
    }

    private String readDataSession(){
        String res = "";

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        SessionReadRequest readRequest = null;
        if(permissionFineLocation){
            readRequest = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .setSessionId(mSession.getIdentifier())
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .read(DataType.TYPE_SPEED)
                    .build();
        } else {
            readRequest = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .setSessionId(mSession.getIdentifier())
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .build();
        }

        SessionReadResult sessionReadResult = Fitness.SessionsApi.readSession(mClient, readRequest).await(30, TimeUnit.SECONDS);
        if(sessionReadResult.getStatus().isSuccess()){
            Log.i(TAG, "Read session success " + sessionReadResult.getSessions().size());
            for(Session session : sessionReadResult.getSessions()){
                dumpSession(session);
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for(DataSet dataSet : dataSets){
                    res+=dumpDataSet(dataSet);
                }
            }
        } else {
            Log.i(TAG, "Failed to read data");
        }
        return res;
    }

    // test reading data from HistoryAPI (in addition to SessionsAPI)
    /*
    private String readDataTest(){
        String res = "";

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MINUTE, -5);
        long startTime = cal.getTimeInMillis();

        //read from session
        SessionReadRequest readRequest = null;
        if(permissionFineLocation){
            readRequest = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .setSessionId(mSession.getIdentifier())
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_SPEED)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .build();
        } else {
            readRequest = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .setSessionId(mSession.getIdentifier())
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .build();
        }

        SessionReadResult sessionReadResult = Fitness.SessionsApi.readSession(mClient, readRequest).await(1, TimeUnit.MINUTES);
        if(sessionReadResult.getStatus().isSuccess()){
            Log.i(TAG, "Read session success " + sessionReadResult.getSessions().size());
            for(Session session : sessionReadResult.getSessions()){
                dumpSession(session);
                //res+=dumpSession(session);
                List<DataSet> dataSets = sessionReadResult.getDataSet(session);
                for(DataSet dataSet : dataSets){
                    res+=dumpDataSet(dataSet);
                }
            }
        } else {
            Log.i(TAG, "Failed to read data");
        }

        //read data from history api
        DataReadRequest readRequestH = null;
        if(permissionFineLocation){
            readRequestH = new DataReadRequest.Builder()
                    //.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    //.bucketByTime(10, TimeUnit.SECONDS)
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_SPEED)
                    //.aggregate(DataType.TYPE_SPEED, DataType.AGGREGATE_SPEED_SUMMARY)
                    //.aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                    //.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                    //.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    //.bucketByTime(30, TimeUnit.SECONDS)
                    //.bucketBySession(30, TimeUnit.SECONDS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();
        } else {
            readRequestH = new DataReadRequest.Builder()
                    //.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    //.bucketByTime(10, TimeUnit.SECONDS)
                    .read(DataType.TYPE_STEP_COUNT_DELTA)
                    .read(DataType.TYPE_STEP_COUNT_CADENCE)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();
        }
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequestH).await(1, TimeUnit.MINUTES);
        if(dataReadResult.getStatus().isSuccess()){
            Log.i(TAG, "Read data success " + dataReadResult.getDataSets().size());
            if(dataReadResult.getBuckets()!=null){
                Log.i(TAG, "Buckets: " + dataReadResult.getBuckets().size());
                List<Bucket> buckets = dataReadResult.getBuckets();
                for(Bucket bucket : buckets){
                    Log.i(TAG, "Bucket: " + bucket.toString() + "session: " + bucket.getSession());
                }
            }
            List<DataSet> dataSets = dataReadResult.getDataSets();
            for(DataSet dataSet : dataSets){
                res+=dumpDataSet(dataSet);
            }

        } else {
            Log.i(TAG, "Failed to read data");
        }
        return res;
    }
    */

    private String dumpDataSet(DataSet dataSet) {
        String res = "";
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            DateFormat dateFormat = getTimeInstance();
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                res+=field.getName() + ": " + dp.getValue(field) + "\n";
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
        return res;
    }

    private void dumpSession(Session session) {
        DateFormat dateFormat = getTimeInstance();
        String res = "Data returned for Session: " + session.getName()
                + "\n\tDescription: " + session.getDescription()
                + "\n\tStart: " + dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS))
                + "\n\tEnd: " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS));
        Log.i(TAG, res);
    }

    private void stopSession(){
        PendingResult<SessionStopResult> pendingResult =
                Fitness.SessionsApi.stopSession(mClient, mSession.getIdentifier());
        SessionStopResult sessionStopResult = pendingResult.await(30, TimeUnit.SECONDS);
        if(sessionStopResult.getStatus().isSuccess()){
            Log.i(TAG, "size sessions: " + sessionStopResult.getSessions().size());
            Session session = sessionStopResult.getSessions().get(0);
            Log.i(TAG,"session id: " + session.getIdentifier() + ", start time: "+ session.getStartTime(TimeUnit.MILLISECONDS) + ", end time: "+ session.getEndTime(TimeUnit.MILLISECONDS) );
            mSession = session;
        } else {
            Log.i(TAG, "Failed to end session");
        }
    }

    private void unsubscribeFitnessData(){
        for(DataType dataType : valuesDataType){
            unsubscribeToType(dataType);
        }
        permissionFineLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if(permissionFineLocation){
            unsubscribeToType(DataType.TYPE_SPEED);
        }
    }

    private void unsubscribeToType(final DataType dataType){
        Fitness.RecordingApi.unsubscribe(mClient, dataType);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mClient == null || !mClient.isConnected()){
          mClient.connect();
        }
        if(apiClient == null ||!apiClient.isConnected()){
          apiClient.connect();
        }
        Wearable.MessageApi.addListener(apiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(apiClient, this);
        if(apiClient.isConnected()){
          apiClient.disconnect();
        }
    }

    protected void onStop() {
        super.onStop();
        unsubscribeFitnessData();
        if(mClient.isConnected()){
            mClient.disconnect();
        }
        if(apiClient.isConnected()){
            apiClient.disconnect();
        }
    }
}
