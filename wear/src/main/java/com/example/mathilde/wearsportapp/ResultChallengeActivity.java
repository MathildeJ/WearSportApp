package com.example.mathilde.wearsportapp;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
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

public class ResultChallengeActivity extends WearableActivity implements
        DataApi.DataListener{

    private static final String SESSION_INFO = "com.example.mathilde.wearsportapp.sessioninfo";
    private static final String CHALLENGE_DONE_PATH = "/challenge_done";
    private static final String SESSION_READ_PATH = "/session_read";
    private static final String fragmentName = "apiClient";

    @Bind(R.id.text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_challenge);
        ButterKnife.bind(this);

        initializeFragment();
    }

    public static Intent createIntent(Context context){
        Intent intent = new Intent(context, ResultChallengeActivity.class);
        return intent;
    }

    private void initializeFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleApiClientWithNodeFragment fragment = GoogleApiClientWithNodeFragment.newInstance(false, true, false, CHALLENGE_DONE_PATH);
        fragmentTransaction.add(fragment, fragmentName);
        fragmentTransaction.commit();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if(SESSION_READ_PATH.equals(path)){
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String string = map.getString(SESSION_INFO);
                mTextView.setText(string);
            }
        }
    }
}
