package com.thf.sleepconfigurator;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thf.sleepconfigurator.utils.ActivityUtil;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.FileUtil;
import com.thf.sleepconfigurator.utils.FlashButton;
import com.thf.sleepconfigurator.utils.SimpleDialog;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/* loaded from: classes.dex */
public class ListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SleepConfigurator";
    private static MyAdapter adapter;
    private Boolean apps;
    ActivityUtil au;
    private Button btnCancel;
    private Button btnNext;
    private Context context;
    private String data;
    private RecyclerView.LayoutManager layoutManager;
    private Intent mainIntent;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    SleepConfiguratorApp sleepConfiguratorApp;
    private List<AppData> appList = new ArrayList();
    private List<AppData> packagesList = new ArrayList();
    private List<AppData> addedPackagesList = new ArrayList();
    private int checkedPos = -1;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity,
    // androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.sleepConfiguratorApp = SleepConfiguratorApp.getInstance();
        this.context = getApplicationContext();

        this.addedPackagesList = FileUtil.getPackagesSelectedByUser();
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.recyclerView = (RecyclerView) findViewById(R.id.listView);
        Button button = (Button) findViewById(R.id.btnCancel);
        this.btnCancel = button;
        button.setOnClickListener(this);
        Button button2 = (Button) findViewById(R.id.btnNext);
        this.btnNext = button2;
        button2.setOnClickListener(this);
        this.recyclerView.setHasFixedSize(true);
        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setItemAnimator(new DefaultItemAnimator());
        this.recyclerView.setLayoutManager(this.layoutManager);
        MyAdapter myAdapter = new MyAdapter(this.appList);
        adapter = myAdapter;
        this.recyclerView.setAdapter(myAdapter);
        this.progressBar.setVisibility(0);
        String stringExtra = getIntent().getStringExtra("appDataList");
        this.data = stringExtra;
        stringExtra.hashCode();
        String str = "app";
        if (stringExtra.equals("apps")) {
            this.apps = true;
        } else if (!stringExtra.equals("activities")) {
            this.apps = true;
        } else {
            this.apps = false;
            str = "activity";
        }
        ActivityUtil activityUtil = new ActivityUtil(getApplicationContext(), str);
        this.au = activityUtil;
        activityUtil.register(
                new ActivityUtil.ActivityUtilCallbacks() { // from class:
                    // com.thf.sleepconfigurator.ListActivity.2
                    @Override // com.thf.sleepconfigurator.utils.ActivityUtil.ActivityUtilCallbacks
                    public void onDataLoaded(List<AppData> list) {

                        ListActivity.adapter.setItems(ListActivity.this.au.getValue());
                        ListActivity.this.progressBar.setVisibility(8);
                        ListActivity.this.btnNext.setVisibility(0);
                        ListActivity.this.btnCancel.setVisibility(0);
                    }
                });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        try {
            this.packagesList = FileUtil.getPackagesFromFile();
        } catch (FileUtil.ReadFileException e) {
            if ("Closing XML tag not found".equals(e.getMessage())) {
                this.packagesList = new ArrayList<AppData>();
                new SimpleDialog(
                                this,
                                "Error reading config file",
                                "Config file may be corrupt. You can continue but this will create a new file.")
                        .show();
            } else {
                new SimpleDialog(
                                this,
                                new SimpleDialog.SimpleDialogCallbacks() {
                                    @Override
                                    public void onClick(String identifier, boolean z, String item) {
                                        ListActivity.this.finish();
                                    }
                                },
                                "Error reading config file",
                                e.getMessage())
                        .show();
            }
        }
        this.au.startProgress();
    }

    /* loaded from: classes.dex */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private List<AppData> appDataList0;
        private List<AppData> appDataList0All;
        private Hashtable<String, Boolean> htExpanded;

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            FlashButton fbListColor;
            ImageView itemChanged;
            ImageView logo;
            TextView name;
            TextView packageName;
            ImageView wakelock;

            MyViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.name = (TextView) view.findViewById(R.id.itemName);
                this.packageName = (TextView) view.findViewById(R.id.itemPackageName);
                this.logo = (ImageView) view.findViewById(R.id.itemLogo);
                this.itemChanged = (ImageView) view.findViewById(R.id.itemChanged);
                this.wakelock = (ImageView) view.findViewById(R.id.itemWakelock);
                FlashButton flashButton = (FlashButton) view.findViewById(R.id.listCol);
                this.fbListColor = flashButton;
                flashButton.setFlashListener(
                        new FlashButton.FlashListener() { // from class:
                            // com.thf.sleepconfigurator.ListActivity.MyAdapter.MyViewHolder.1
                            @Override // com.thf.sleepconfigurator.utils.FlashButton.FlashListener
                            public void onState(FlashButton.FlashEnum flashEnum) {
                                AppData appData =
                                        MyAdapter.this.appDataList0.get(
                                                MyViewHolder.this.getAdapterPosition());

                                int i = flashEnum.ordinal();
                                if (i == 0) {
                                    appData.setListColor(AppData.LIST_COLOR_REMOVE);
                                } else if (i == 1) {
                                    appData.setListColor(AppData.LIST_COLOR_WHITE);
                                } else if (i == 2) {
                                    appData.setListColor(AppData.LIST_COLOR_YELLOW);
                                }

                                FileUtil.addPackage(appData);
                                ListActivity.this.addedPackagesList =
                                        FileUtil.getPackagesSelectedByUser();
                                ListActivity.adapter.notifyItemChanged(
                                        MyViewHolder.this.getAdapterPosition());
                            }
                        });
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                this.fbListColor.performClick();
            }
        }

        MyAdapter(List<AppData> list) {
            this.appDataList0 = list;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MyViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.item_view, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            AppData app = appDataList0.get(position);
            String key = app.getKey();
            holder.name.setText(app.getName());
            Drawable icon = app.getIcon(context);
            if (icon != null) {
                holder.logo.setImageDrawable(icon);
            }
            holder.packageName.setText(app.getPackageName());

            if (app.isRequestingPermission("WAKE_LOCK")) {
                holder.wakelock.setVisibility(View.VISIBLE);
            } else {
                holder.wakelock.setVisibility(View.GONE);
            }

            holder.fbListColor.setState(FlashButton.FlashEnum.OFF);
            holder.fbListColor.setEnabled(true);

            int idxAdded = addedPackagesList.indexOf(app);
            if (idxAdded != -1) {
                String listColor = addedPackagesList.get(idxAdded).getListColor();
                switch (listColor) {
                    case AppData.LIST_COLOR_WHITE:
                        holder.fbListColor.setState(FlashButton.FlashEnum.WHITE);
                        break;
                    case AppData.LIST_COLOR_YELLOW:
                        holder.fbListColor.setState(FlashButton.FlashEnum.YELLOW);
                        break;
                }
                holder.itemChanged.setVisibility(View.VISIBLE);
            } else {
                holder.itemChanged.setVisibility(View.GONE);
            }

            int idxFile = packagesList.indexOf(app);
            if (idxFile != -1 && idxAdded == -1) {
                String listColor = packagesList.get(idxFile).getListColor();
                switch (listColor) {
                    case AppData.LIST_COLOR_WHITE:
                        holder.fbListColor.setState(FlashButton.FlashEnum.WHITE);
                        break;
                    case AppData.LIST_COLOR_YELLOW:
                        holder.fbListColor.setState(FlashButton.FlashEnum.YELLOW);
                        break;
                }
                if (packagesList.get(idxFile).getFactoryDefault()) {
                    holder.fbListColor.setEnabled(false);
                }
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.appDataList0.size();
        }

        public void expandCollapseList(String str) {
            if (this.htExpanded.containsKey(str)) {
                this.htExpanded.remove(str);
            } else {
                this.htExpanded.put(str, true);
            }
            setItems(this.appDataList0All);
        }

        public void setItems(List<AppData> list) {
            this.appDataList0 = list;
            ListActivity.adapter.notifyDataSetChanged();
        }
    }

    /*
    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            FileUtil.resetAddedPackagesList();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
    */

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCancel /* 2131230821 */:
                FileUtil.resetAddedPackagesList();
                finish();
                return;
            case R.id.btnNext /* 2131230822 */:
                startActivity(new Intent(this, ApplyActivity.class));
                return;
            default:
                return;
        }
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        super.onBackPressed();
        FileUtil.resetAddedPackagesList();
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

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
