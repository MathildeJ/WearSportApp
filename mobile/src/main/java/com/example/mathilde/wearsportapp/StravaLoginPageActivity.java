package com.example.mathilde.wearsportapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
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
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.sweetzpot.stravazpot.activity.api.ActivityAPI;
import com.sweetzpot.stravazpot.activity.model.Activity;
import com.sweetzpot.stravazpot.athlete.api.AthleteAPI;
import com.sweetzpot.stravazpot.athlete.model.Athlete;
import com.sweetzpot.stravazpot.athlete.model.Stats;
import com.sweetzpot.stravazpot.authenticaton.api.AccessScope;
import com.sweetzpot.stravazpot.authenticaton.api.ApprovalPrompt;
import com.sweetzpot.stravazpot.authenticaton.api.AuthenticationAPI;
import com.sweetzpot.stravazpot.authenticaton.api.StravaLogin;
import com.sweetzpot.stravazpot.authenticaton.model.AppCredentials;
import com.sweetzpot.stravazpot.authenticaton.model.LoginResult;
import com.sweetzpot.stravazpot.authenticaton.model.Token;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginActivity;
import com.sweetzpot.stravazpot.authenticaton.ui.StravaLoginButton;
import com.sweetzpot.stravazpot.common.api.AuthenticationConfig;
import com.sweetzpot.stravazpot.common.api.StravaConfig;
import com.sweetzpot.stravazpot.common.api.exception.StravaAPIException;
import com.sweetzpot.stravazpot.upload.api.UploadAPI;
import com.sweetzpot.stravazpot.upload.model.DataType;
import com.sweetzpot.stravazpot.upload.model.UploadActivityType;
import com.sweetzpot.stravazpot.upload.model.UploadStatus;
import com.sweetzpot.tcxzpot.TCXDate;
import com.sweetzpot.tcxzpot.serializers.FileSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.sweetzpot.tcxzpot.Activities.activities;
import static com.sweetzpot.tcxzpot.Intensity.ACTIVE;
import static com.sweetzpot.tcxzpot.Notes.notes;
import static com.sweetzpot.tcxzpot.Position.position;
import static com.sweetzpot.tcxzpot.Sport.RUNNING;
import static com.sweetzpot.tcxzpot.TCXDate.tcxDate;
import static com.sweetzpot.tcxzpot.Track.trackWith;
import static com.sweetzpot.tcxzpot.TriggerMethod.MANUAL;
import static com.sweetzpot.tcxzpot.builders.ActivityBuilder.activity;
import static com.sweetzpot.tcxzpot.builders.ApplicationBuilder.application;
import static com.sweetzpot.tcxzpot.builders.BuildBuilder.aBuild;
import static com.sweetzpot.tcxzpot.builders.DeviceBuilder.device;
import static com.sweetzpot.tcxzpot.builders.LapBuilder.aLap;
import static com.sweetzpot.tcxzpot.builders.TrackpointBuilder.aTrackpoint;
import static com.sweetzpot.tcxzpot.builders.TrainingCenterDatabaseBuilder.trainingCenterDatabase;
import static com.sweetzpot.tcxzpot.builders.VersionBuilder.version;
import static java.util.Calendar.FEBRUARY;

public class StravaLoginPageActivity extends AppCompatActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient apiClient;
    private Node node;
    private static final String STRAVA_ACTIONS = "/strava_actions";
    private static final String TEXT = "com.example.wearsportapp.text";
    private static final String RESPONSE = "com.example.wearsportapp.response";
    private static final String SECRET = "b8d16c0afe8752c49d68cf30b5b52fa3fc7c98ef";

    private static final String STRAVA_DIRECTORY = "StravaZpot";
    private static final String TAG = "StravaLoginPageActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final int RQ_LOGIN = 1;
    private StravaConfig config;
    private AthleteAPI athleteAPI;
    private Athlete currAthlete;
    private ActivityAPI activityAPI;
    private UploadAPI uploadAPI;

    @Bind(R.id.login_button)
    StravaLoginButton loginButton;

    @Bind(R.id.text_code)
    TextView mTextView;

    public void setConfig(StravaConfig config) {
        this.config = config;
    }

    public StravaConfig getConfig() {
        return config;
    }

    public void setAthleteAPI(AthleteAPI athleteAPI) {
        this.athleteAPI = athleteAPI;
    }

    public AthleteAPI getAthleteAPI() {
        return athleteAPI;
    }

    public Athlete getCurrAthlete() {
        return currAthlete;
    }

    public void setCurrAthlete(Athlete currAthlete) {
        this.currAthlete = currAthlete;
    }

    public ActivityAPI getActivityAPI() {
        return activityAPI;
    }

    public void setActivityAPI(ActivityAPI activityAPI) {
        this.activityAPI = activityAPI;
    }

    public UploadAPI getUploadAPI() {
        return uploadAPI;
    }

    public void setUploadAPI(UploadAPI uploadAPI) {
        this.uploadAPI = uploadAPI;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_login_page);
        ButterKnife.bind(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        initializeClient();
    }

    public void initializeClient(){
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void login(){
        Intent intent = StravaLogin.withContext(this)
                .withClientID(16728)
                .withRedirectURI("http://blabla.com/test")
                .withApprovalPrompt(ApprovalPrompt.AUTO)
                .withAccessScope(AccessScope.VIEW_PRIVATE_WRITE)
                .makeIntent();
        startActivityForResult(intent, RQ_LOGIN);
    }

    private void obtainToken(String code){
        new obtainTokenTask().execute(code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RQ_LOGIN && resultCode == RESULT_OK && data != null) {
            loginButton.setVisibility(View.INVISIBLE);
            String code = data.getStringExtra(StravaLoginActivity.RESULT_CODE);
            obtainToken(code);
        }
    }

    private void startAPIMenuWatch(){
        sendMessage("");
        mTextView.setText("You are now logged in with Strava");
    }

    private void sendMessage(String message){
        if(node!=null & apiClient!=null && apiClient.isConnected()){
            Wearable.MessageApi.sendMessage(
                    apiClient, node.getId(), STRAVA_ACTIONS, message.getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(sendMessageResult.getStatus().isSuccess()){
                            } else {
                            }
                        }
                    }
            );
        }
    }

    private void syncDataMap(String string){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/action_response");
        putDataMapReq.getDataMap().putString(RESPONSE, string);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(apiClient, putDataReq);
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

    @Override
    protected void onStart() {
        super.onStart();
        if(apiClient == null || !apiClient.isConnected()){
          apiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(apiClient, this);
        if(apiClient.isConnected()){
          apiClient.disconnect();
        }
    }


    private void resolveNode() {
        Wearable.NodeApi.getConnectedNodes(apiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodesResult) {
                for (Node node : nodesResult.getNodes()) {
                    if (node.isNearby()) {
                        StravaLoginPageActivity.this.node = node;
                    }
                }
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/action".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(TEXT);
                switch(string){
                    case "RA":
                        retrieveAthlete();
                        break;
                    case "SA":
                        retrieveStatsAthlete();
                        break;
                    case "LA":
                        listActivities();
                        break;
                    case "UF":
                        uploadFile();
                        break;
                }
            }
        }

    }

    private class obtainTokenTask extends AsyncTask<String, Void, Token> {

        @Override
        protected Token doInBackground(String... params) {
            AuthenticationConfig config = AuthenticationConfig.create()
                    .debug()
                    .build();

            AuthenticationAPI api = new AuthenticationAPI(config);
            LoginResult result = api.getTokenForApp(AppCredentials.with(16728, SECRET))
                    .withCode(params[0])
                    .execute();

            setCurrAthlete(result.getAthlete());
            return result.getToken();
        }

        @Override
        protected void onPostExecute(Token result){
            //loginButton.setVisibility(View.INVISIBLE);

            StravaConfig config = StravaConfig.withToken(result)
                    .debug()
                    .build();

            setConfig(config);
            setAthleteAPI(new AthleteAPI(config));
            setActivityAPI(new ActivityAPI(config));
            setUploadAPI(new UploadAPI(config));

            startAPIMenuWatch();
        }

    }


    private void retrieveAthlete(){
        new Thread(new Runnable() {
            public void run() {
                final Athlete athlete = getAthleteAPI().retrieveCurrentAthlete().execute();
                setCurrAthlete(athlete);
                syncDataMap("Athlete: " + athlete.getFirstName());
            }
        }).start();
    }

    private void retrieveStatsAthlete(){
        new Thread(new Runnable() {
            public void run() {
                final Stats stats = getAthleteAPI().getAthleteTotalsAndStats((int)getCurrAthlete().getID())
                        .execute();
                syncDataMap("Stats: \nNumber of runs: " + stats.getAllRunTotals().getCount() + ", distance " + stats.getAllRunTotals().getDistance());
            }
        }).start();
    }

    private void listActivities(){
        new Thread(new Runnable() {
            public void run() {
                final List<Activity> activities =
                        getActivityAPI().listMyActivities()
                                .perPage(5)
                                .execute();
                if(activities.isEmpty()){
                    syncDataMap("No activities found.");
                } else {
                    String res = "";
                    for(Activity activity : activities){
                        res+="Activity: " + activity.getExternalID() + "\t" + activity.getName() + "\n";
                    }
                    syncDataMap(res);
                }

            }
        }).start();
    }

    private void uploadFile(){
        new Thread(new Runnable() {
            public void run() {
                if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
                    FileSerializer serializer = null;

                    File file = new File(Environment.getExternalStorageDirectory()+"/sample.tcx");
                    Calendar calendar = Calendar.getInstance();
                    TCXDate date = tcxDate(calendar.get(Calendar.DAY_OF_MONTH), FEBRUARY, calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

                        try {
                            serializer = new FileSerializer(file);
                            trainingCenterDatabase()
                                .withActivities(activities(
                                        activity(RUNNING)
                                                .withID(date)
                                                .withCreator(
                                                        device("BreathZpot Sensor")
                                                                .withVersion(version().major(1).minor(0))
                                                                .withUnitId(1)
                                                                .withProductId(1+(int)System.currentTimeMillis())
                                                )
                                                .withNotes(notes("A sample session"))
                                                .withLaps(
                                                        aLap(date)
                                                                .withTotalTime(3000)
                                                                .withDistance(1200)
                                                                .withCalories(100)
                                                                .withIntensity(ACTIVE)
                                                                .withTriggerMethod(MANUAL)
                                                                .withTracks(
                                                                        trackWith(
                                                                                aTrackpoint()
                                                                                        .onTime(date)
                                                                                        .withPosition(position(-3.6714, 36.8936)),
                                                                                aTrackpoint()
                                                                                        .onTime(date)
                                                                                        .withPosition(position(-3.6727, 36.8946)),
                                                                                aTrackpoint()
                                                                                        .onTime(date)
                                                                                        .withPosition(position(-3.6733, 36.901))
                                                                        )
                                                                )
                                                )
                                ))
                                .withAuthor(
                                        application("BreathZpot")
                                                .withBuild(aBuild()
                                                        .withVersion(version().major(2).minor(3)))
                                                .withLanguageID("en")
                                                .withPartNumber("123-45678-90")
                                ).build()
                                .serialize(serializer);
                            serializer.save();

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG, "error: " + e);
                        }

                        try{
                            UploadStatus uploadStatus = getUploadAPI().uploadFile(file)
                                    .withDataType(DataType.TCX)
                                    .withActivityType(UploadActivityType.RUN)
                                    .withName("A run around the city")
                                    .withDescription("No description")
                                    .isPrivate(false)
                                    .hasTrainer(false)
                                    .isCommute(false)
                                    .withExternalID("test.tcx")
                                    .execute();

                            int uploadID = uploadStatus.getId();
                            syncDataMap("Upload status: " + getUploadAPI().checkUploadStatus(uploadID).execute().getStatus());
                            Log.i(TAG, "Upload status: " + uploadStatus.getStatus());
                            Log.i(TAG, "Status: " + getUploadAPI().checkUploadStatus(uploadStatus.getId()).execute().getStatus());
                            Thread.sleep(2000);
                            syncDataMap("Upload status: " + getUploadAPI().checkUploadStatus(uploadID).execute().getStatus());
                        } catch(StravaAPIException ex){
                            Log.i(TAG, "Exception:" + ex);
                            syncDataMap("Failed to upload file");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                } else {
                    syncDataMap("External storage not accessible");
                }
            }
        }).start();
    }

}

