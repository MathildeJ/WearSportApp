package com.example.mathilde.wearsportapp;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class OpenLinkService extends WearableListenerService {
        private static final String OPEN_LINK_PATH = "/open_link";

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if(OPEN_LINK_PATH.equals(messageEvent.getPath())){
                String url = new String(messageEvent.getData());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
}


