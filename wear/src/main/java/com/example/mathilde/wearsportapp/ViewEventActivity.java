package com.example.mathilde.wearsportapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewEventActivity extends WearableActivity {

    private static final String EXTRA_TITLE = "ViewEventActivity.EXTRA_TITLE";
    private static final String EXTRA_TEXT = "ViewEventActivity.EXTRA_TEXT";

    @Bind(R.id.name_text) TextView nameText;
    @Bind(R.id.message_text) TextView messageText;

    public static Intent createIntent(Context context, String title, String text){
        Intent intent = new Intent(context, ViewEventActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_TEXT, text);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        nameText.setText(intent.getStringExtra(EXTRA_TITLE));
        messageText.setText(intent.getStringExtra(EXTRA_TEXT));
    }
}
