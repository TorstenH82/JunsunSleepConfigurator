package com.thf.sleepconfigurator.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

/* loaded from: classes.dex */
public class AppData {
    public static final String LIST_COLOR_REMOVE = "";
    public static final String LIST_COLOR_WHITE = "WHITE";
    public static final String LIST_COLOR_YELLOW = "YELLOW";
    private String activityDescription;
    private String activityName;
    private String category = "app";
    private boolean factoryDefault = true;
    private Integer flags;
    private boolean inFile;
    private String listColor;
    private String name;
    private String packageName;
    private String[] permissions;
    private boolean selected;
    private Integer sort;

    public AppData() {}

    public AppData(String str) {
        this.packageName = str;
    }

    public String getListColor() {
        return this.listColor;
    }

    public void setListColor(String str) {
        this.listColor = str;
    }

    public String[] getPermissions() {
        return this.permissions;
    }

    public boolean isRequestingPermission(String str) {
        if (this.permissions != null) {
            int i = 0;
            while (true) {
                String[] strArr = this.permissions;
                if (i >= strArr.length) {
                    break;
                } else if (TextUtils.equals(strArr[i], str)) {
                    return true;
                } else {
                    String str2 = this.permissions[i];
                    if (TextUtils.equals(str2, "android.permission." + str)) {
                        return true;
                    }
                    i++;
                }
            }
        }
        return false;
    }

    public void setPermissions(String[] strArr) {
        this.permissions = strArr;
    }

    public String getActivityName() {
        return this.activityName;
    }

    public void setActivityName(String str) {
        this.activityName = str;
    }

    public String getFullDescription() {
        String str = this.activityDescription;
        if (str != null) {
            if (!str.equals(this.name)) {
                return this.name + " (" + this.activityDescription + ")";
            }
            return this.activityDescription;
        }
        return this.name;
    }

    public String getDescription() {
        String str = this.activityDescription;
        return str != null ? str : this.name;
    }

    public String getActivityDescription() {
        return this.activityDescription;
    }

    public void setActivityDescription(String str) {
        this.activityDescription = str;
    }

    public Integer getFlags() {
        return this.flags;
    }

    public void setFlags(Integer num) {
        this.flags = num;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String str) {
        this.category = str;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public Drawable getIcon(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.getApplicationIcon(getPackageName());
        } catch (Exception unused) {
            return null;
        }
    }

    public Integer getSort() {
        return this.sort;
    }

    public void setSort(Integer num) {
        this.sort = num;
    }

    public boolean getFactoryDefault() {
        return this.factoryDefault;
    }

    public void setFactoryDefault(boolean z) {
        this.factoryDefault = z;
    }

    public boolean getInFile() {
        return this.inFile;
    }

    public void setInFile(boolean z) {
        this.inFile = z;
    }

    public void setSelected(boolean z) {
        this.selected = z;
    }

    public String getKey() {
        if (getCategory().equals("app")) {
            return getPackageName();
        }
        return getPackageName() + "/" + getActivityName();
    }

    public boolean equals(Object obj) {
        if (obj instanceof AppData) {
            return getPackageName().equals(((AppData) obj).getPackageName());
        }
        if (obj instanceof String) {
            String str = (String) obj;
            Log.i("SleepConfigurator", "is string: " + str);
            return TextUtils.equals(getPackageName(), str);
        }
        Log.i("SleepConfigurator", "is something");
        return false;
    }
}
