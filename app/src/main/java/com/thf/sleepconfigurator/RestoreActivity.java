package com.thf.sleepconfigurator;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.CustomArrayAdapter;
import com.thf.sleepconfigurator.utils.FileUtil;
import com.thf.sleepconfigurator.utils.SimpleDialog;
import com.thf.sleepconfigurator.utils.SuCommands;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class RestoreActivity extends AppCompatActivity {
    private static SleepConfiguratorApp sleepConfiguratorApp;
    private String backupFile;
    private Button btnApply;
    private Context context;

    private ListView listViewLog;
    CustomArrayAdapter listViewLogArrayAdapter;
    private List<AppData> addRemovePackages = new ArrayList();
    private List<String> backupFiles = new ArrayList();
    private SuCommands suCommands;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity,
    // androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        sleepConfiguratorApp = SleepConfiguratorApp.getInstance();
        this.context = this;

        suCommands = new SuCommands(getString(R.string.su), sleepConfiguratorApp.DEBUG);

        setContentView(R.layout.activity_restore);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button button = (Button) findViewById(R.id.btnApply);
        this.btnApply = button;
        button.setOnClickListener(
                new View.OnClickListener() { // from class:
                    // com.thf.sleepconfigurator.RestoreActivity.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SimpleDialog.SimpleDialogCallbacks simpleDialogCallbacks =
                                new SimpleDialog.SimpleDialogCallbacks() { // from class:
                                    // com.thf.sleepconfigurator.RestoreActivity.1.1
                                    @Override // com.thf.sleepconfigurator.utils.SimpleDialog.SimpleDialogCallbacks
                                    public void onClick(String identifier, boolean z, String item) {
                                        if (z) {
                                            try {
                                                suCommands.copyAndChmod(
                                                        FileUtil.getPathQbListBackup()
                                                                + "/"
                                                                + RestoreActivity.this.backupFile,
                                                        FileUtil.getFilePathQbList(),
                                                        "644");

                                                RestoreActivity restoreActivity =
                                                        RestoreActivity.this;
                                                new SimpleDialog(
                                                                restoreActivity,
                                                                "Done",
                                                                "Restored file "
                                                                        + RestoreActivity.this
                                                                                .backupFile
                                                                        + ". Please reboot your device for the change to take effect.")
                                                        .show();
                                            } catch (SuCommands.SuCommandException e) {
                                                new SimpleDialog(
                                                                RestoreActivity.this,
                                                                "Error",
                                                                e.getMessage())
                                                        .show();
                                            }
                                        }
                                    }
                                };
                        RestoreActivity restoreActivity = RestoreActivity.this;
                        restoreActivity.backupFile =
                                restoreActivity.listViewLogArrayAdapter.getCurrentEntry();
                        if (RestoreActivity.this.backupFile != null) {
                            restoreActivity.backupFile = restoreActivity.backupFile.split(" ")[0];
                            // RestoreActivity restoreActivity2 = RestoreActivity.this;
                            new SimpleDialog(
                                            restoreActivity,
                                            simpleDialogCallbacks,
                                            "Restore?",
                                            "Restore file " + RestoreActivity.this.backupFile + "?")
                                    .show();
                            return;
                        }
                        new SimpleDialog(
                                        RestoreActivity.this,
                                        "No backup selected",
                                        "Please select file you want to backup first")
                                .show();
                    }
                });
        this.listViewLog = (ListView) findViewById(R.id.listview_log);
        CustomArrayAdapter customArrayAdapter =
                new CustomArrayAdapter(this.context, this.backupFiles);
        this.listViewLogArrayAdapter = customArrayAdapter;
        this.listViewLog.setAdapter((ListAdapter) customArrayAdapter);
        this.listViewLog.setOnItemClickListener(
                new AdapterView.OnItemClickListener() { // from class:
                    // com.thf.sleepconfigurator.RestoreActivity.2
                    @Override // android.widget.AdapterView.OnItemClickListener
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                        RestoreActivity.this.listViewLogArrayAdapter.setPosition(i);
                    }
                });
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        List<String> availableBackupFiles = FileUtil.getAvailableBackupFiles();
        this.backupFiles = availableBackupFiles;
        this.listViewLogArrayAdapter.clear();
        if (availableBackupFiles != null) {
            this.listViewLogArrayAdapter.addAll(availableBackupFiles);
        } else {
            this.listViewLogArrayAdapter.add("No backup files found");
        }
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
