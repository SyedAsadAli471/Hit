package com.app.hit.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.hit.R;
import com.app.hit.service.BLEService;
import com.app.hit.ui.fragments.CalendarLogsFragment;
import com.app.hit.ui.fragments.HomeFragment;
import com.app.hit.util.Prefs;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        Intent gattServiceIntent = new Intent(getApplicationContext(), BLEService.class);
        startService(gattServiceIntent);

        createNotificationChannel();

        Fragment homeFragment = new HomeFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, homeFragment, "home");
        transaction.addToBackStack(null).commitAllowingStateLoss();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent gattServiceIntent = new Intent(getApplicationContext(), BLEService.class);
        stopService(gattServiceIntent);
    }

    @Override
    public void onBackPressed() {

        Log.e("count:", "count: " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userId = Prefs.getInstance(getApplicationContext()).getString("IS_CONCUSSION", "");
        if(!userId.equalsIgnoreCase("")) {
            Fragment calendarFragment = new CalendarLogsFragment();
            FragmentManager manager = getSupportFragmentManager();
            Bundle args = new Bundle();
            args.putString("PLAYER_ID", userId);
            calendarFragment.setArguments(args);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, calendarFragment, "log_calendar");
            transaction.addToBackStack(null);
            transaction.commit();
            Prefs.getInstance(getApplicationContext()).setString("IS_CONCUSSION", "");
        }

    }
}