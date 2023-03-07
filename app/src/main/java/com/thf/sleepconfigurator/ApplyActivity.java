package com.thf.sleepconfigurator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.CustomArrayAdapter;
import com.thf.sleepconfigurator.utils.FileUtil;
import com.thf.sleepconfigurator.utils.SimpleDialog;
import com.thf.sleepconfigurator.utils.SuCommands;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ApplyActivity extends AppCompatActivity implements View.OnClickListener {
    private static MyAdapter adapter;
    private static SleepConfiguratorApp sleepConfiguratorApp;
    private Button btnApply;
    private Button btnBack;
    private Context context;

    private RecyclerView.LayoutManager layoutManager;
    private ListView listViewLog;
    ArrayAdapter<String> listViewLogArrayAdapter;
    private RecyclerView recyclerView;
    private List<AppData> packagesList = new ArrayList<AppData>();
    private List<AppData> addRemovePackages = new ArrayList<AppData>();
    private List<String> listLog = new ArrayList();
    private boolean finished = false;
    private SuCommands suCommands;

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity,
    // androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        sleepConfiguratorApp = SleepConfiguratorApp.getInstance();
        this.context = this;

        suCommands = new SuCommands(getString(R.string.su), sleepConfiguratorApp.DEBUG);

        setContentView(R.layout.activity_apply);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.listView);
        this.recyclerView = recyclerView;
        recyclerView.setHasFixedSize(true);
        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.setLayoutManager(this.layoutManager);
        MyAdapter myAdapter = new MyAdapter(this.addRemovePackages);
        adapter = myAdapter;
        this.recyclerView.setAdapter(myAdapter);
        Button button = (Button) findViewById(R.id.btnApply);
        this.btnApply = button;
        button.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.btnBack);
        this.btnBack = button2;
        button2.setOnClickListener(this);
        this.listViewLog = (ListView) findViewById(R.id.listview_log);
        CustomArrayAdapter customArrayAdapter = new CustomArrayAdapter(this.context, this.listLog);
        this.listViewLogArrayAdapter = customArrayAdapter;
        this.listViewLog.setAdapter((ListAdapter) customArrayAdapter);
        this.packagesList = FileUtil.getPackagesList();
        List<AppData> packagesSelectedByUser = FileUtil.getPackagesSelectedByUser();
        this.addRemovePackages = packagesSelectedByUser;
        adapter.setItems(packagesSelectedByUser);
        FileUtil.getPackagesSelectedByUser().size();
        ArrayAdapter<String> arrayAdapter = this.listViewLogArrayAdapter;
        // arrayAdapter.add(((List)
        // this.addRemovePackages.stream().filter(ApplyActivity$$ExternalSyntheticLambda0.INSTANCE).collect(Collectors.toList())).size() + " package(s) to add / change in config file");
        ArrayAdapter<String> arrayAdapter2 = this.listViewLogArrayAdapter;
        // arrayAdapter2.add(((List)
        // this.addRemovePackages.stream().filter(ApplyActivity$$ExternalSyntheticLambda1.INSTANCE).collect(Collectors.toList())).size() + " package(s) to remove from config file");

        listViewLogArrayAdapter.add("Click on Apply to accept changes and modify config file");
        this.finished = false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$onCreate$0(AppData appData) {
        return (appData.getListColor() == null
                        || appData.getInFile()
                        || AppData.LIST_COLOR_REMOVE.equals(appData.getListColor()))
                ? false
                : true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$onCreate$1(AppData appData) {
        return AppData.LIST_COLOR_REMOVE.equals(appData.getListColor()) && !appData.getInFile();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView itemFrom;
        ImageView itemTo;
        TextView name;
        TextView packageName;
        ImageView wakelock;

        MyViewHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(R.id.itemName);
            this.packageName = (TextView) view.findViewById(R.id.itemPackageName);
            this.itemFrom = (ImageView) view.findViewById(R.id.itemFrom);
            this.itemTo = (ImageView) view.findViewById(R.id.itemTo);
            this.wakelock = (ImageView) view.findViewById(R.id.itemWakelock);
        }
    }

    /* loaded from: classes.dex */
    public class MyAdapter extends RecyclerView.Adapter<ApplyActivity.MyViewHolder> {
        private List<AppData> appDataList0;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */

        MyAdapter(List<AppData> list) {
            this.appDataList0 = list;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.item_apply, viewGroup, false));
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            AppData appData = this.appDataList0.get(i);
            appData.getKey();
            myViewHolder.name.setText(appData.getName());
            myViewHolder.packageName.setText(appData.getPackageName());
            int indexOf = packagesList.indexOf(appData);
            if (indexOf == -1) {
                myViewHolder.itemFrom.setImageDrawable(context.getDrawable(R.drawable.off));
            } else {
                String listColor = packagesList.get(indexOf).getListColor();
                if (listColor.equals(AppData.LIST_COLOR_YELLOW)) {
                    myViewHolder.itemFrom.setImageDrawable(
                            ApplyActivity.this.context.getDrawable(R.drawable.yellow));
                } else if (listColor.equals(AppData.LIST_COLOR_WHITE)) {
                    myViewHolder.itemFrom.setImageDrawable(
                            ApplyActivity.this.context.getDrawable(R.drawable.white));
                }
            }

            switch (appData.getListColor()) {
                case AppData.LIST_COLOR_YELLOW:
                    myViewHolder.itemTo.setImageDrawable(context.getDrawable(R.drawable.yellow));
                    break;
                case AppData.LIST_COLOR_REMOVE:
                    myViewHolder.itemTo.setImageDrawable(context.getDrawable(R.drawable.off));
                    break;
                case AppData.LIST_COLOR_WHITE:
                    myViewHolder.itemTo.setImageDrawable(context.getDrawable(R.drawable.white));
                    break;
            }

            if (appData.isRequestingPermission("WAKE_LOCK")) {
                myViewHolder.wakelock.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.wakelock.setVisibility(View.GONE);
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.appDataList0.size();
        }

        public void setItems(List<AppData> list) {
            this.appDataList0 = list;
            ApplyActivity.adapter.notifyDataSetChanged();
        }
    }

    private String fileToDelete;
    SimpleDialog.SimpleDialogCallbacks simpleDialogCallbacks =
            new SimpleDialog.SimpleDialogCallbacks() { // from class:
                // com.thf.sleepconfigurator.ApplyActivity.1
                @Override // com.thf.sleepconfigurator.utils.SimpleDialog.SimpleDialogCallbacks
                public void onClick(String identifier, boolean confirm, String selection) {
                    String fileModPath;
                    boolean fileMod = false;

                    if ("INIT".equals(identifier)) {
                        ApplyActivity.this.listViewLogArrayAdapter.clear();
                    }

                    if (!confirm) {
                        ApplyActivity.this.listViewLogArrayAdapter.add("cancelled by user");
                        return;
                    }

                    try {
                        fileModPath = FileUtil.modifyWorkingConfigFile();
                        ApplyActivity.this.listViewLogArrayAdapter.addAll(FileUtil.getLog());
                        if (fileModPath != null) {
                            fileMod = true;
                            ApplyActivity.this.listViewLogArrayAdapter.add(
                                    fileModPath + " created");
                        }
                    } catch (FileUtil.ModifyFileException ex) {
                        ApplyActivity.this.listViewLogArrayAdapter.addAll(FileUtil.getLog());
                        ApplyActivity.this.listViewLogArrayAdapter.add(ex.getMessage());
                        return;
                    }

                    if (fileMod) {
                        try {
                            if (!suCommands.partitionHasFreeSpace()) {

                                if ("SELECT_DEL".equals(identifier)) {
                                    new SimpleDialog(
                                                    ApplyActivity.this,
                                                    "CONFIRM_DEL",
                                                    this,
                                                    "Select file to delete",
                                                    suCommands.getDeletionVictims())
                                            .show();
                                    return;
                                } else if ("CONFIRM_DEL".equals(identifier)) {
                                    new SimpleDialog(
                                                    ApplyActivity.this,
                                                    "DELETE",
                                                    this,
                                                    "Delete?",
                                                    "Execute deletion of file '" + selection + "'")
                                            .show();
                                    fileToDelete = selection;
                                    return;
                                } else if ("DELETE".equals(identifier)) {
                                    suCommands.moveFile(fileToDelete, "/sdcard/Download");
                                    ApplyActivity.this.listViewLogArrayAdapter.add(
                                            fileToDelete + " deleted");
                                    // no return... continue
                                } else {
                                    ApplyActivity.this.listViewLogArrayAdapter.add(
                                            "no free space on vendor partition");
                                    new SimpleDialog(
                                                    ApplyActivity.this,
                                                    "SELECT_DEL",
                                                    this,
                                                    "Not enough free space on vendor partition",
                                                    "Please select system app for deletion...")
                                            .show();
                                    return;
                                }
                            }

                            String pathBackupFile = FileUtil.getFilePathQbListBackupCopy();
                            suCommands.copyAndChmod(
                                    FileUtil.getFilePathQbList(), pathBackupFile, "777");
                            ApplyActivity.this.listViewLogArrayAdapter.add(
                                    "created backup " + pathBackupFile);

                            File file = new File(pathBackupFile);
                            if (!file.exists() || file.length() == 0) {
                                ApplyActivity.this.listViewLogArrayAdapter.add(
                                        "Validation of " + pathBackupFile + " failed");
                                return;
                            }

                            suCommands.copyAndChmod(fileModPath, FileUtil.getFilePathQbList(), "644");
                            ApplyActivity.this.listViewLogArrayAdapter.add(
                                    "overwritten " + FileUtil.getFilePathQbList());

                        } catch (SuCommands.SuCommandException ex) {
                            ApplyActivity.this.listViewLogArrayAdapter.add(ex.getMessage());
                        }
                    }

                    try {
                        FileUtil.allowIgnoreWakelocks();
                    } catch (FileUtil.SetWakelockException ex) {
                        ApplyActivity.this.listViewLogArrayAdapter.addAll(FileUtil.getLog());
                        ApplyActivity.this.listViewLogArrayAdapter.add(ex.getMessage());
                        return;
                    }

                    FileUtil.clearPackagesSelectedByUser();
                    ApplyActivity.this.listViewLogArrayAdapter.addAll(FileUtil.getLog());
                    recyclerView.setVisibility(View.GONE);
                }
            };

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnApply /* 2131230819 */:
                if (this.finished) {
                    startActivity(new Intent(this, MainActivity.class));
                    return;
                } else {
                    new SimpleDialog(
                                    this,
                                    "INIT",
                                    simpleDialogCallbacks,
                                    "Apply changes to config file?",
                                    "App will create a backup copy of configuration file.")
                            .show();
                    return;
                }
            case R.id.btnBack /* 2131230820 */:
                finish();
                return;
            default:
                return;
        }
    }
}
