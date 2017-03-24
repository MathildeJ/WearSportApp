package com.example.mathilde.wearsportapp;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;


public class MainActivity extends WearableActivity implements
        WearableActionDrawer.OnMenuItemClickListener{

    private WearableDrawerLayout mWearableDrawerLayout;
    private WearableActionDrawer mWearableActionDrawer;

    private static final String STRAVA_LOGIN = "/strava_login";
    private static final String fragmentName = "apiClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureMenu();
        configureDrawer();
        initializeFragment();
    }

    private void initializeFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleApiClientWithNodeFragment fragment = GoogleApiClientWithNodeFragment.newInstance(true, false, true,"");
        fragmentTransaction.add(fragment, fragmentName);
        fragmentTransaction.commit();
    }

    private void configureDrawer(){
        mWearableDrawerLayout = (WearableDrawerLayout) findViewById(R.id.drawer_layout);
        mWearableActionDrawer = (WearableActionDrawer) findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.setOnMenuItemClickListener(this);

        ViewTreeObserver observer = mWearableDrawerLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mWearableDrawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mWearableDrawerLayout.peekDrawer(Gravity.BOTTOM);
            }
        });

    }

    private void configureMenu(){
        WearableRecyclerView menu_list = (WearableRecyclerView) findViewById(R.id.menu_list);
        menu_list.setCenterEdgeItems(true);
        menu_list.setOffsettingHelper(new DefaultOffsettingHelper());

        com.example.mathilde.wearsportapp.MenuAdapter ma = new com.example.mathilde.wearsportapp.MenuAdapter();
        ma.setContext(getApplicationContext());
        menu_list.setAdapter(ma);
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
                GoogleApiClientWithNodeFragment fragment = (GoogleApiClientWithNodeFragment)getFragmentManager().findFragmentByTag(fragmentName);
                fragment.sendMessage(STRAVA_LOGIN, "");
                break;
        }
        mWearableDrawerLayout.closeDrawer(mWearableActionDrawer);
        return true;
    }
}
