package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Log;
import com.thf.sleepconfigurator.R;
import com.thf.sleepconfigurator.SleepConfiguratorApp;
import com.thf.sleepconfigurator.utils.AppData;
import com.thf.sleepconfigurator.utils.FileUtil;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kotlin.reflect.KCallable;

/* loaded from: classes.dex */
public class FileUtil {
    private static final int ACTION_ALLOW = 1;
    private static final int ACTION_IGNORE = 0;
    private static final String TAG = "SleepConfigurator";
    private static List<AppData> addedPackagesList = new ArrayList();
    private static List<AppData> packagesList;
    private static Context context;
    private static String pathQbListBackupCopy;
    private static int startTagPosition = -1;
    private static int endTagPosition = -1;
    private static int closingTagPosition = -1;
    private static List<String> log = new ArrayList();
    private static String pathModFile;

    /* loaded from: classes.dex */
    public static class OverwriteQbListException extends Exception {
        public OverwriteQbListException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class BackupCopyException extends Exception {
        public BackupCopyException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class BackupRestoreException extends Exception {
        public BackupRestoreException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class SuCommandException extends Exception {
        public SuCommandException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class BackupException extends Exception {
        public BackupException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class ReadFileException extends Exception {
        public ReadFileException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class ModifyFileException extends Exception {
        public ModifyFileException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class SetWakelockException extends Exception {
        public SetWakelockException(String str) {
            super(str);
        }
    }

    public static void setContext(Context context) {
        FileUtil.context = context;
    }

    public static List<AppData> getPackagesSelectedByUser() {
        return addedPackagesList;
    }

    public static void resetAddedPackagesList() {
        addedPackagesList = new ArrayList();
    }

    public static List<AppData> getPackagesList() {
        return packagesList;
    }

    private static String getPathModified() {
        return pathModFile;
    }

    private static void setPathModified(String path) {
        pathModFile = path;
    }

    private static String getPathQbList() {
        if (SleepConfiguratorApp.DEBUG) {
            return context.getExternalFilesDir(null)
                    + "/"
                    + context.getString(R.string.fileQbListXml);
        }
        return context.getString(R.string.pathToQblistXml)
                + "/"
                + context.getString(R.string.fileQbListXml);
    }

    private static String getPathQbListBackupCopy() {
        return context.getExternalFilesDir(null) + "/backups";
    }

    private static String getFilePathQbListBackupCopy() {

        String str =
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        .toString();
        pathQbListBackupCopy =
                getPathQbListBackupCopy()
                        + "/"
                        + str
                        + "__"
                        + context.getString(R.string.fileQbListXml);

        return pathQbListBackupCopy;
    }

    private static String getPathSpecificBackupCopy(String str) {
        return Environment.getExternalStorageDirectory()
                + "/"
                + context.getString(R.string.rootAppDir)
                + "/"
                + str;
    }

    public static void createBackup() throws BackupCopyException {
        createDirectory(context.getExternalFilesDir(null) + "/backups");

        if (SleepConfiguratorApp.DEBUG) {
            try {
                FileUtils.copy(
                        new FileInputStream(getPathQbList()),
                        new FileOutputStream(getFilePathQbListBackupCopy()));
            } catch (IOException e) {
                throw new BackupCopyException(
                        "DEBUG: Error creating backup copy of configuration file: "
                                + e.getMessage());
            }
        } else {
            String format =
                    String.format(
                            context.getString(R.string.commandCopy),
                            getPathQbList(),
                            getFilePathQbListBackupCopy());
            log.add("executing su command: " + format);
            try {
                execSuCommand(format);
            } catch (SuCommandException e2) {
                throw new BackupCopyException("Error executing su command: " + e2.getMessage());
            }
        }
        if (!new File(getPathQbListBackupCopy()).exists()) {
            throw new BackupCopyException(
                    "Backup file '" + getPathQbListBackupCopy() + "' does not exist");
        }
        log.add("created backup of config file here: " + getPathQbListBackupCopy());
    }

    public static boolean modifyWorkingConfigFile() throws ModifyFileException {

        String message, line;
        String startTag = context.getString(R.string.tagStart);
        String endTag = context.getString(R.string.tagEnd);
        Pattern pattern = Pattern.compile("\"(.+?)\"", Pattern.DOTALL);

        // remove entries we can not change
        List<AppData> packagesSelectedByUser = new ArrayList<>(getPackagesSelectedByUser());
        if (packagesSelectedByUser.isEmpty()) {
            log.add("No need to modify config file...");
            return false;
        }

        try {
            List<AppData> packages = getPackagesFromFile();
            for (AppData app : packages) {
                int idx = packagesSelectedByUser.indexOf(app);
                if (idx != -1 && app.getFactoryDefault()) {
                    packagesSelectedByUser.remove(app);
                }
            }
        } catch (ReadFileException ex) {
        }

        try {
            File fileWc = new File(getPathQbList());
            File fileTmp = new File(context.getExternalFilesDir(null) + "/qb_list_mod.xml");
            setPathModified(fileTmp.getAbsolutePath());
            log.add("temporary file: " + fileTmp.getAbsolutePath());

            BufferedReader br = new BufferedReader(new FileReader(fileWc.getAbsolutePath()));
            PrintWriter pw = new PrintWriter(new FileWriter(fileTmp));

            Boolean changeable = false;
            Boolean writeRemaining = false;
            Boolean sleepConfiguratorTagFound = false;

            while ((line = br.readLine()) != null) {
                boolean printLine = true;

                if (line.contains(startTag)) {
                    changeable = true;
                } else if (line.contains(endTag)) {
                    writeRemaining = true;
                    sleepConfiguratorTagFound = true;
                    changeable = false;
                } else if (line.contains("</qb_list>")) {
                    if (!sleepConfiguratorTagFound) writeRemaining = true;
                } else {
                    if (changeable) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String pa = matcher.group(1);
                            AppData paApp = new AppData(pa);
                            int idx = packagesSelectedByUser.indexOf(paApp);
                            if (idx != -1) {

                                String paColor = "";
                                if (line.contains("app_whitelist")) {
                                    paColor = AppData.LIST_COLOR_WHITE;
                                } else if (line.contains("app_yellowlist")) {
                                    paColor = AppData.LIST_COLOR_YELLOW;
                                }

                                AppData paAdd = packagesSelectedByUser.get(idx);
                                String paAddColor = paAdd.getListColor();

                                if (changeable && !paColor.equals(paAddColor)) {
                                    printLine = false;
                                    if (paAddColor.equals(AppData.LIST_COLOR_WHITE)) {
                                        pw.println(
                                                "\t<app_whitelist name=\""
                                                        + paAdd.getPackageName()
                                                        + "\" />");
                                        log.add(
                                                "added "
                                                        + paAdd.getPackageName()
                                                        + " to white list");
                                    } else if (paAddColor.equals(AppData.LIST_COLOR_YELLOW)) {
                                        pw.println(
                                                "\t<app_yellowlist name=\""
                                                        + paAdd.getPackageName()
                                                        + "\" />");
                                        log.add(
                                                "added x"
                                                        + paAdd.getPackageName()
                                                        + " to yellow list");
                                    } else {
                                        log.add("removed " + paAdd.getPackageName() + " from list");
                                    }
                                }

                                packagesSelectedByUser.remove(paAdd);
                            }
                        }
                    }
                }

                if (writeRemaining) {

                    if (!sleepConfiguratorTagFound) pw.println("\t<!--" + startTag + "-->");

                    for (AppData app : packagesSelectedByUser) {
                        if (app.getListColor().equals(AppData.LIST_COLOR_WHITE)) {
                            pw.println("\t<app_whitelist name=\"" + app.getPackageName() + "\" />");
                            log.add("added " + app.getPackageName() + " to xwhite list");
                        } else if (app.getListColor().equals(AppData.LIST_COLOR_WHITE)) {
                            pw.println(
                                    "\t<app_yellowlist name=\"" + app.getPackageName() + "\" />");
                            log.add("added " + app.getPackageName() + " to yellow list");
                        }
                    }

                    if (!sleepConfiguratorTagFound) pw.println("\t<!--" + endTag + "-->");
                    writeRemaining = false;
                }

                if (printLine) pw.println(line);
            }

            pw.close();
            br.close();

        } catch (IOException iOException) {
            message = "IOException: " + iOException.getMessage();
            throw new ModifyFileException(message);
        }

        return true;
    }

    public static List<String> getLog() {
        ArrayList arrayList = new ArrayList(log);
        log = new ArrayList();
        return arrayList;
    }

    public static void overwriteQbList() throws OverwriteQbListException {
        if (SleepConfiguratorApp.DEBUG) {
            try {
                FileUtils.copy(
                        new FileInputStream(getPathModified()),
                        new FileOutputStream(getPathQbList()));
                Log.d(TAG, "DEBUG: overwritten config file with working copy: " + getPathQbList());
                return;
            } catch (IOException e) {
                throw new OverwriteQbListException(
                        "DEBUG: Error overwriting configuration file with working copy: "
                                + e.getMessage());
            }
        }

        log.add("Remount /vendor partition...");
        try {
            execSuCommand(context.getString(R.string.commandRemount1));
        } catch (SuCommandException ignore) {

        }

        String command = context.getString(R.string.commandRemount2);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during remount with command: " + command);
            if (!SleepConfiguratorApp.DEBUG)
                throw new OverwriteQbListException(
                        "Error executing su command: " + ex.getMessage());
        }

        command =
                String.format(
                        context.getString(R.string.commandCopy),
                        getPathModified(),
                        getPathQbList());
        Log.d(TAG, "Copy to target: " + command);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during copy command: " + command);
            if (!SleepConfiguratorApp.DEBUG)
                throw new OverwriteQbListException(
                        "Error executing su command: " + ex.getMessage());
        }

        command = String.format(context.getString(R.string.commandChmod), getPathQbList());
        Log.d(TAG, "Change file permission: " + command);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during file permission change: " + command);
            if (!SleepConfiguratorApp.DEBUG)
                throw new OverwriteQbListException(
                        "Error executing su command: " + ex.getMessage());
        }
    }

    public static List<AppData> getPackagesFromFile() throws ReadFileException {
        String startTag = context.getString(R.string.tagStart);
        String endTag = context.getString(R.string.tagEnd);
        List<AppData> arrayList = new ArrayList();
        PackageManager packageManager = context.getPackageManager();
        Pattern compile = Pattern.compile("\"(.+?)\"", 32);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(getPathQbList())));
            int i = 0;
            String line;
            boolean z = true;
            while ((line = br.readLine()) != null) {

                i++;
                if (line.contains(startTag)) {
                    startTagPosition = i;
                    z = false;
                } else if (line.contains(endTag)) {
                    z = true;
                } else if (TextUtils.equals(
                        line.trim().replaceAll("\t", AppData.LIST_COLOR_REMOVE), "</qb_list>")) {
                    closingTagPosition = i;
                } else if (line.contains("<app_whitelist") || line.contains("<app_yellowlist")) {
                    if (line.contains("name=") && !line.contains("<!--")) {
                        Matcher matcher = compile.matcher(line);
                        if (matcher.find()) {
                            AppData appData = new AppData(matcher.group(1).trim());
                            try {
                                String[] strArr =
                                        packageManager.getPackageInfo(
                                                        appData.getPackageName(), 4096)
                                                .requestedPermissions;
                                if (strArr != null) {
                                    appData.setPermissions(strArr);
                                }
                            } catch (PackageManager.NameNotFoundException unused) {
                            }
                            appData.setInFile(true);
                            appData.setFactoryDefault(z);
                            Log.i(TAG, appData.getPackageName());
                            if (line.contains("<app_whitelist")) {
                                appData.setListColor(AppData.LIST_COLOR_WHITE);
                            } else {
                                appData.setListColor(AppData.LIST_COLOR_YELLOW);
                            }
                            arrayList.add(appData);
                        }
                    }
                }
                endTagPosition = i;
            }
            br.close();
            if (closingTagPosition == -1) {
                throw new ReadFileException("Closing XML tag not found.");
            }
            packagesList = arrayList;
            return arrayList;
        } catch (IOException e) {
            throw new ReadFileException("IOException: " + e.getMessage());
        }
    }

    public static void allowIgnoreWakelocks() throws SetWakelockException {
        String string = context.getString(R.string.commandAllowWakelock);
        String string2 = context.getString(R.string.commandIgnoreWakelock);
        List<AppData> packagesSelectedByUser = new ArrayList<>(getPackagesSelectedByUser());
        boolean error = false;

        for (AppData appData : packagesList) {
            if (!appData.getFactoryDefault()) {
                int idx = packagesSelectedByUser.indexOf(appData);
                if (idx == -1) {
                    packagesSelectedByUser.add(appData);
                }
            }
        }
        String command;
        for (AppData appData : packagesSelectedByUser) {
            if (!appData.getFactoryDefault() && appData.isRequestingPermission("WAKE_LOCK")) {
                if (AppData.LIST_COLOR_REMOVE.equals(appData.getListColor())) {
                    log.add("allow wakelock of package " + appData.getPackageName());
                    command =
                            String.format(
                                    context.getString(R.string.commandAllowWakelock),
                                    appData.getPackageName());
                } else {
                    log.add("ignore wakelock of package " + appData.getPackageName());
                    command =
                            String.format(
                                    context.getString(R.string.commandIgnoreWakelock),
                                    appData.getPackageName());
                }
                try {
                    execSuCommand(command);
                } catch (SuCommandException e) {
                    log.add(
                            "error executing su command: "
                                    + command
                                    + " Error msg: "
                                    + e.getMessage());
                }
            }
        }

        if (error) throw new SetWakelockException("error(s) occured setting wakelocks");
    }

    public static void createDirectory(String str) {
        File file = new File(str);
        if (file.exists()) {
            return;
        }
        file.mkdir();
    }

    public static void execSuCommand(String str) throws SuCommandException {
        Boolean.valueOf(false);
        if (str == null || "".equals(str)) {
            throw new SuCommandException("command is empty");
        }
        try {
            Process exec = Runtime.getRuntime().exec(context.getString(R.string.su));
            DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());
            if (!TextUtils.equals(str, AppData.LIST_COLOR_REMOVE)) {
                dataOutputStream.writeBytes(str + "\n");
                dataOutputStream.flush();
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            exec.waitFor();
        } catch (IOException e) {
            throw new SuCommandException("IOException: " + e.getMessage());
        } catch (InterruptedException e2) {
            throw new SuCommandException("InterruotedException: " + e2.getMessage());
        }
    }

    public static List<String> getAvailableBackupFiles() {
        String[] list =
                new File(getPathQbListBackupCopy())
                        .list(
                                new FilenameFilter() { // from class:
                                    // com.thf.sleepconfigurator.utils.FileUtil.1
                                    @Override // java.io.FilenameFilter
                                    public boolean accept(File file, String str) {
                                        return str.endsWith(".xml");
                                    }
                                });
        if (list != null) {
            List<String> asList = Arrays.asList(list);
            Collections.sort(asList);
            Collections.reverse(asList);
            return asList;
        }
        return null;
    }

    public static void restoreBackup(String filename) throws BackupRestoreException {



        try {
            execSuCommand(context.getString(R.string.commandRemount1));
        } catch (SuCommandException ignore) {

        }

        String command = context.getString(R.string.commandRemount2);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during remount with command: " + command);
            throw new BackupRestoreException(
                    "Error executing su command: '" + command + "': " + ex.getMessage());
        }
        command =
                String.format(
                        context.getString(R.string.commandCopy),
                        getPathQbListBackupCopy() + "/" + filename,
                        getPathQbList());
        Log.d(TAG, "Copy to target: " + command);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during copy command: " + command);
            throw new BackupRestoreException(
                    "Error executing su command: '" + command + "': " + ex.getMessage());
        }

        command = String.format(context.getString(R.string.commandChmod), getPathQbList());
        Log.d(TAG, "Change file permission: " + command);
        try {
            execSuCommand(command);
        } catch (SuCommandException ex) {
            log.add("Error during file permission change: " + command);
            throw new BackupRestoreException(
                    "Error executing su command: '" + command + "': " + ex.getMessage());
        }
    }

    public static void addPackage(AppData appData) {
        appData.setFactoryDefault(false);
        boolean noChange = false;
        appData.setInFile(false);
        int indexOf = packagesList.indexOf(appData);
        int indexOf2 = addedPackagesList.indexOf(appData);
        if (indexOf != -1) {
            if (packagesList.get(indexOf).getFactoryDefault()) {
                return;
            }
            if (appData.getListColor().equals(packagesList.get(indexOf).getListColor())) {
                if (indexOf2 != -1) {
                    addedPackagesList.remove(indexOf2);
                }
                return;
            }
        }
        if (indexOf2 != -1) {
            addedPackagesList.remove(indexOf2);
        }
        addedPackagesList.add(appData);
    }
    
    public static void clearPackagesSelectedByUser()
        {
            addedPackagesList.clear();
        }
    

    public int getApplicationUid() {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).uid;
        } catch (PackageManager.NameNotFoundException unused) {
            return -1;
        }
    }
}
