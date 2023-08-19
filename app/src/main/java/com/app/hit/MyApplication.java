package com.app.hit;

import android.app.Application;
import android.content.Context;

import com.app.hit.util.Prefs;


public class MyApplication extends Application {
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
       // super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
        super.attachBaseContext(base);


    }
    @Override
    public void onCreate() {
        super.onCreate();
        //ZopimChat.init(AppConstant.CHAT_KEY);
        MyApplication.context = getApplicationContext();
        Prefs.initialize(getApplicationContext());

    }
    public static Context getAppContext() {
        return MyApplication.context;
    }


}

