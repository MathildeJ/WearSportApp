package com.example.mathilde.wearsportapp;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StepChallengeActivity extends WearableActivity implements
        DataApi.DataListener,
        DelayedConfirmationView.DelayedConfirmationListener{

    private static final String START_SESSION_PATH = "/start_session";
    private static final String SESSION_STARTED_PATH = "/session_started";
    private static final String TIMER_FINISHED_PATH = "/timer_finished";
    private static final String TIMER_STOPPED_PATH = "/timer_stopped";
    private static final String RESPONSE = "com.example.mathilde.wearsportapp.response";
    private static final String TAG = "StepChallengeActivity";
    private static final String fragmentName = "apiClient";

    private static final long THREE_MINUTES = 180000;

    @Bind(R.id.delayed_confirmation)
    DelayedConfirmationView delayedConfirmationView;

    @Bind(R.id.text)
    TextView mTextView;

    @Bind(R.id.button)
    Button mButton;

    private static final long ONE_MINUTE = 60000;
    private int timeRemaining;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_challenge);
        ButterKnife.bind(this);

        initializeFragment();

        handler = new Handler();
        handler.postDelayed(showTimeRemaining, ONE_MINUTE);
    }

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, StepChallengeActivity.class);
        return intent;
    }

    private void initializeFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleApiClientWithNodeFragment fragment = GoogleApiClientWithNodeFragment.newInstance(false, true, true, "");
        fragmentTransaction.add(fragment, fragmentName);
        fragmentTransaction.commit();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleApiClientWithNodeFragment fragment = (GoogleApiClientWithNodeFragment)getFragmentManager().findFragmentByTag(fragmentName);
                fragment.sendMessage(START_SESSION_PATH,"");
            }
        });
    }

    private void configureDelayedStart() {
        delayedConfirmationView.setTotalTimeMs(THREE_MINUTES);
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

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if(SESSION_STARTED_PATH.equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(RESPONSE);
                Log.i(TAG, "Data changed: " + string);
                configureDelayedStart();
            }
        }
    }


    @Override
    public void onTimerFinished(View view) {
        GoogleApiClientWithNodeFragment fragment = (GoogleApiClientWithNodeFragment)getFragmentManager().findFragmentByTag(fragmentName);
        fragment.sendMessage(TIMER_FINISHED_PATH, "step_challenge_finished");
        startResultChallenge();
        finish();
    }

    @Override
    public void onTimerSelected(View view) {
        delayedConfirmationView.setListener(null);
        startActivity(createConfirmationCanceledIntent());
        GoogleApiClientWithNodeFragment fragment = (GoogleApiClientWithNodeFragment)getFragmentManager().findFragmentByTag(fragmentName);
        fragment.sendMessage(TIMER_STOPPED_PATH, "step_challenge_stopped");
        finish();
    }

    private Intent createConfirmationCanceledIntent(){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                "Challenge canceled");
        return intent;
    }

    private void startResultChallenge() {
        startActivity(ResultChallengeActivity.createIntent(this));
    }
}
