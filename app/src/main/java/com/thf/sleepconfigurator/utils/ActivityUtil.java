package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/* loaded from: classes.dex */
public class ActivityUtil {
    private static final String TAG = "SleepConfigurator";
    private Context context;
    private ActivityUtilCallbacks listener;
    private Boolean stop = false;
    private volatile List<AppData> appDataList = new ArrayList();
    private Handler execHandler = new Handler(Looper.getMainLooper());

    /* loaded from: classes.dex */
    public interface ActivityUtilCallbacks {
        void onDataLoaded(List<AppData> list);
    }

    public void register(ActivityUtilCallbacks activityUtilCallbacks) {
        this.listener = activityUtilCallbacks;
    }

    public ActivityUtil(Context context, String str) {
        this.context = context;
    }

    public void stopProgress() {
        this.stop = true;
    }

    public void startProgress() {
        new Thread(
                        new Runnable() { // from class:
                            // com.thf.sleepconfigurator.utils.ActivityUtil.1
                            @Override // java.lang.Runnable
                            public void run() {
                                PackageManager packageManager =
                                        ActivityUtil.this.context.getPackageManager();
                                List<ApplicationInfo> installedApps =
                                        packageManager.getInstalledApplications(
                                                PackageManager.GET_META_DATA);

                                appDataList.clear();
                                for (ApplicationInfo installedApp : installedApps) {
                                    AppData appData = new AppData();
                                    appData.setPackageName(installedApp.packageName);

                                    String name =
                                            packageManager
                                                    .getApplicationLabel(installedApp)
                                                    .toString();

                                    if (name == null) name = installedApp.packageName;

                                    appData.setName(name);
                                    appData.setCategory("installedApplication");
                                    try {
                                        String[] strArr =
                                                packageManager.getPackageInfo(
                                                                installedApp.packageName,
                                                                PackageManager.GET_PERMISSIONS)
                                                        .requestedPermissions;
                                        if (strArr != null) {
                                            appData.setPermissions(strArr);
                                        }
                                    } catch (PackageManager.NameNotFoundException ignore) {
                                    }
                                    appDataList.add(appData);
                                }

                                Collections.sort(
                                        ActivityUtil.this.appDataList,
                                        new Comparator<AppData>() { // from class:
                                            // com.thf.sleepconfigurator.utils.ActivityUtil.1.1
                                            @Override // java.util.Comparator
                                            public int compare(AppData appData2, AppData appData3) {
                                                int compareTo =
                                                        appData2.getName()
                                                                .compareTo(appData3.getName());
                                                if (compareTo == 0) {
                                                    compareTo =
                                                            appData2.getPackageName()
                                                                    .compareTo(
                                                                            appData3
                                                                                    .getPackageName());
                                                }
                                                return compareTo == 0
                                                        ? appData3.getCategory()
                                                                .compareTo(appData2.getCategory())
                                                        : compareTo;
                                            }
                                        });

                                new Handler(Looper.getMainLooper())
                                        .post(
                                                new Runnable() { // from class:
                                                    // com.thf.sleepconfigurator.utils.ActivityUtil.1.2
                                                    @Override // java.lang.Runnable
                                                    public void run() {
                                                        ActivityUtil.this.listener.onDataLoaded(
                                                                ActivityUtil.this.appDataList);
                                                    }
                                                });
                            }
                        })
                .start();
    }

    public List<AppData> getValue() {
        return this.appDataList;
    }
}
