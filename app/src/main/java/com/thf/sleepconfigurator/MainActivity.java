package com.thf.sleepconfigurator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.FileUtil;
import com.thf.sleepconfigurator.utils.SimpleDialog;
import java.util.List;
import java.util.stream.Collectors;
import com.thf.sleepconfigurator.R;

/* loaded from: classes.dex */
public class MainActivity extends AppCompatActivity {
    private static Activity activity;
    private static Context context;
    private static PrefFragment settingsFragment;
    private static SleepConfiguratorApp sleepConfiguratorApp;

    /* loaded from: classes.dex */
    public static class PrefFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        }

        @Override // androidx.fragment.app.Fragment
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            List<AppData> packagesSelectedByUser = FileUtil.getPackagesSelectedByUser();
            //List list = (List) packagesSelectedByUser.stream().filter(MainActivity$PrefFragment$$ExternalSyntheticLambda0.INSTANCE).collect(Collectors.toList());
            //List list2 = (List) packagesSelectedByUser.stream().filter(MainActivity$PrefFragment$$ExternalSyntheticLambda1.INSTANCE).collect(Collectors.toList());
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ boolean lambda$onResume$0(AppData appData) {
            return (appData.getListColor() == null || appData.getInFile() || AppData.LIST_COLOR_REMOVE.equals(appData.getListColor())) ? false : true;
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public static /* synthetic */ boolean lambda$onResume$1(AppData appData) {
            return AppData.LIST_COLOR_REMOVE.equals(appData.getListColor()) && !appData.getInFile();
        }

        @Override // androidx.fragment.app.Fragment
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override // androidx.preference.PreferenceFragmentCompat
        public void onCreatePreferences(Bundle bundle, String str) {
            getPreferenceManager().setSharedPreferencesName("USERDATA");
            setPreferencesFromResource(R.xml.preferences, str);
            try {
                PackageInfo packageInfo = MainActivity.context.getPackageManager().getPackageInfo(MainActivity.context.getPackageName(), 0);
                String str2 = packageInfo.versionName;
                Preference findPreference = findPreference("prefAbout");
                findPreference.setSummary("version " + packageInfo.versionName);
            } catch (Exception unused) {
            }
        }
    }

    protected boolean shouldAskPermissions() {
        return Build.VERSION.SDK_INT > 22;
    }

    protected void askPermissions() {
        requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getApplicationContext();
        sleepConfiguratorApp = SleepConfiguratorApp.getInstance();
        activity = this;
        setContentView(R.layout.activity_main);
        if (shouldAskPermissions()) {
            askPermissions();
        }
        settingsFragment = new PrefFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frmSettings, settingsFragment).commit();
        if (SleepConfiguratorApp.DEBUG) {
            new SimpleDialog(activity, null, "App in debug mode", "App is running in debug mode. No changes to config file will be made. Please ask the developer to provide the productive version.", false).show();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (iArr.length <= 0 || iArr[0] != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, strArr[0])) {
                Toast.makeText(context, "Go to Settings and Grant the permission to use this feature.", 0).show();
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(268435456);
                intent.addFlags(1073741824);
                intent.addFlags(8388608);
                context.startActivity(intent);
                return;
            }
            Toast.makeText(context, "Permission Denied", 0).show();
        }
    }
}
