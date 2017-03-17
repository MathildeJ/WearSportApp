package com.example.mathilde.wearsportapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class StravaRequestService extends WearableListenerService {
    private static final String STRAVA_REQUEST = "/strava_request";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(STRAVA_REQUEST.equals(messageEvent.getPath())){
            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            String data = new String(messageEvent.getData());
            switch(data){
                case "SA":
                    break;
                case "LA":
                    break;
                case "RA":
                    break;
            }

        }
    }
}
