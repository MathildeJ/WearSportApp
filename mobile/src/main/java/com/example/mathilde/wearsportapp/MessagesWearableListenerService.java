package com.example.mathilde.wearsportapp;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class MessagesWearableListenerService extends WearableListenerService {
        private static final String OPEN_LINK_PATH = "/open_link";
        private static final String START_SESSION_PATH = "/start_session";
        private static final String STRAVA_LOGIN = "/strava_login";

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if(OPEN_LINK_PATH.equals(messageEvent.getPath())){
                String url = new String(messageEvent.getData());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if(START_SESSION_PATH.equals(messageEvent.getPath())){
                Intent intent = new Intent(this, StartSessionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if(STRAVA_LOGIN.equals(messageEvent.getPath())){
                Intent dialogIntent = new Intent(this, StravaLoginPageActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            }
        }
}


