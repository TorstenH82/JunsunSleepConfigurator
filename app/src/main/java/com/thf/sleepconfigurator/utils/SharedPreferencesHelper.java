package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class SharedPreferencesHelper {
    private static Hashtable<String, AppData> dictHashtable;
    private static List<AppData> recentApps;
    private static String sDictAsString;
    private static String value;

    public static void SaveDict(Context context, Hashtable hashtable, String str) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("USERDATA", 0);
        sharedPreferences.edit().putString(str, new Gson().toJson(hashtable, new TypeToken<Hashtable<String, AppData>>() { // from class: com.thf.sleepconfigurator.utils.SharedPreferencesHelper.1
        }.getType())).apply();
    }

    public static Hashtable<String, AppData> LoadDict(Context context, String str) {
        String string = context.getSharedPreferences("USERDATA", 0).getString(str, new JsonObject().toString());
        if (!TextUtils.equals(string, "{}")) {
            return (Hashtable) new Gson().fromJson(string, new TypeToken<Hashtable<String, AppData>>() { // from class: com.thf.sleepconfigurator.utils.SharedPreferencesHelper.2
            }.getType());
        }
        return new Hashtable<>();
    }

    public static String getDictKeysAsString(Context context, String str) {
        Iterator<String> it = LoadDict(context, str).keySet().iterator();
        String str2 = AppData.LIST_COLOR_REMOVE;
        while (it.hasNext()) {
            str2 = str2 + it.next() + ";";
        }
        sDictAsString = str2;
        return str2;
    }

    public static Hashtable<String, AppData> putValueForKey(Hashtable hashtable, String str, AppData appData) {
        if (appData == null) {
            if (hashtable.containsKey(str)) {
                hashtable.remove(str);
            }
        } else {
            hashtable.put(str, appData);
        }
        return hashtable;
    }

    public static AppData getValueForKey(Context context, String str, String str2) {
        Hashtable<String, AppData> LoadDict = LoadDict(context, str);
        if (str2.equals("1st")) {
            if (LoadDict.isEmpty()) {
                return null;
            }
            return LoadDict.get(LoadDict.keySet().toArray()[0]);
        } else if (LoadDict.containsKey(str2)) {
            return LoadDict.get(str2);
        } else {
            return null;
        }
    }

    public static int getCountOfDict(Context context, String str) {
        return LoadDict(context, str).keySet().toArray().length;
    }

    
    private static String getResStringId(Context context, String aString) {
		String packageName = context.getPackageName();
		int resId = context.getResources().getIdentifier(aString, "string", packageName);
		if (resId == 0) {
			return "";
		} else {
			return context.getString(resId);
		}
	}

    public static String LoadString(Context context, String str) {
        String string = context.getSharedPreferences("USERDATA", 0).getString(str, AppData.LIST_COLOR_REMOVE);
        if (TextUtils.equals(string, AppData.LIST_COLOR_REMOVE)) {
            return getResStringId(context, "pref_" + str);
        }
        return string;
    }

    public static int LoadInteger(Context context, String str) {
        int i = context.getSharedPreferences("USERDATA", 0).getInt(str, -99);
        if (i == -99) {
            String resStringId = getResStringId(context, "pref_" + str);
            if (TextUtils.equals(resStringId, AppData.LIST_COLOR_REMOVE)) {
                return 0;
            }
            return Integer.parseInt(resStringId);
        }
        return i;
    }

    public static Boolean LoadBoolean(Context context, String str) {
        return Boolean.valueOf(context.getSharedPreferences("USERDATA", 0).getBoolean(str, false));
    }

    public static List<AppData> loadList(Context context, String str) {
        String string = context.getSharedPreferences("USERDATA", 0).getString(str, new JsonObject().toString());
        if (!TextUtils.equals(string, "{}")) {
            return (List) new Gson().fromJson(string, new TypeToken<List<AppData>>() { // from class: com.thf.sleepconfigurator.utils.SharedPreferencesHelper.3
            }.getType());
        }
        return new ArrayList();
    }

    public static void saveList(Context context, List list, String str) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("USERDATA", 0);
        sharedPreferences.edit().putString(str, new Gson().toJson(list, new TypeToken<List<AppData>>() { // from class: com.thf.sleepconfigurator.utils.SharedPreferencesHelper.4
        }.getType())).apply();
    }

    public static void putIntoRecentsList(Context context, AppData appData) {
        List<AppData> loadList = loadList(context, "recentAppsList");
        recentApps = loadList;
        if (loadList.size() > 10) {
            List<AppData> list = recentApps;
            list.remove(list.size() - 1);
        }
        Iterator<AppData> it = recentApps.iterator();
        while (it.hasNext()) {
            if (it.next().getKey().equals(appData.getKey())) {
                it.remove();
            }
        }
        recentApps.add(0, appData);
        saveList(context, recentApps, "recentAppsList");
    }

    public static List<AppData> getRecentsList(Context context) {
        if (recentApps == null) {
            try {
                recentApps = loadList(context, "recentAppsList");
            } catch (Exception unused) {
            }
        }
        return recentApps;
    }
}
