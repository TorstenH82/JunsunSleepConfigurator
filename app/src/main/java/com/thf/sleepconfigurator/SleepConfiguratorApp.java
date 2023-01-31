package com.thf.sleepconfigurator;

import android.app.Application;
import com.thf.sleepconfigurator.utils.FileUtil;

/* loaded from: classes.dex */
public class SleepConfiguratorApp extends Application {
    public static boolean DEBUG = false;
    private static final String TAG = "SleepConfigurator";
    private static SleepConfiguratorApp sInstance;

    public void setRunned() {
    }

    public static SleepConfiguratorApp getInstance() {
        return sInstance;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        FileUtil.setContext(getApplicationContext());
        DEBUG = Boolean.parseBoolean(getString(R.string.DEBUG));
            
    }

    @Override // android.app.Application
    public void onTerminate() {
        super.onTerminate();
    }
}
