package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;
import com.thf.sleepconfigurator.R;
import com.thf.sleepconfigurator.SleepConfiguratorApp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public class FileUtil {
    private static final int ACTION_ALLOW = 1;
    private static final int ACTION_IGNORE = 0;
    private static final String TAG = "SleepConfigurator";
    private static List<AppData> addedPackagesList = new ArrayList<AppData>();
    private static List<AppData> packagesList = new ArrayList<AppData>();
    private static Context context;
    private static String pathQbListBackupCopy;
    private static int startTagPosition = -1;
    private static int endTagPosition = -1;
    private static int closingTagPosition = -1;
    private static List<String> log = new ArrayList<String>();
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
        addedPackagesList = new ArrayList<AppData>();
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

    public static String getFilePathQbList() {
        if (SleepConfiguratorApp.DEBUG) {
            return context.getExternalFilesDir(null)
                    + "/"
                    + context.getString(R.string.fileQbListXml);
        }
        return context.getString(R.string.pathToQblistXml)
                + "/"
                + context.getString(R.string.fileQbListXml);
    }

    public static String getPathQbListBackup() {
        return context.getExternalFilesDir(null) + "/backups";
    }

    public static String getFilePathQbListBackupCopy() {
        String str =
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        .toString();
        pathQbListBackupCopy =
                getPathQbListBackup()
                        + "/"
                        + str
                        + "__"
                        + context.getString(R.string.fileQbListXml);

        return pathQbListBackupCopy;
    }

    public static String modifyWorkingConfigFile() throws ModifyFileException {

        String message, line;
        String startTag = context.getString(R.string.tagStart);
        String endTag = context.getString(R.string.tagEnd);
        Pattern pattern = Pattern.compile("\"(.+?)\"", Pattern.DOTALL);
        boolean createNewFile = false;

        // remove entries we can not change
        List<AppData> packagesSelectedByUser = new ArrayList<>(getPackagesSelectedByUser());
        if (packagesSelectedByUser.isEmpty()) {
            log.add("No need to modify config file...");
            return null;
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
            log.add("Can't read existing config file. New file will be created...");
            createNewFile = true;
        }

        File fileTmp = new File(context.getExternalFilesDir(null) + "/qb_list_mod.xml");
        setPathModified(fileTmp.getAbsolutePath());
        log.add("temporary file: " + fileTmp.getAbsolutePath());

        try {
            PrintWriter pw = new PrintWriter(new FileWriter(fileTmp));
            if (createNewFile) {
                pw.println("<qb_list>");
                pw.println("\t<!--" + startTag + "-->");
                for (AppData app : packagesSelectedByUser) {
                    if (app.getListColor().equals(AppData.LIST_COLOR_WHITE)) {
                        pw.println("\t<app_whitelist name=\"" + app.getPackageName() + "\" />");
                        log.add("added " + app.getPackageName() + " to white list");
                    } else if (app.getListColor().equals(AppData.LIST_COLOR_WHITE)) {
                        pw.println("\t<app_yellowlist name=\"" + app.getPackageName() + "\" />");
                        log.add("added " + app.getPackageName() + " to yellow list");
                    }
                }
                pw.println("\t<!--" + endTag + "-->");
                pw.println("</qb_list>");
            } else {

                File fileCfg = new File(getFilePathQbList());
                BufferedReader br = new BufferedReader(new FileReader(fileCfg.getAbsolutePath()));

                boolean changeable = false;
                boolean writeRemaining = false;
                boolean sleepConfiguratorTagFound = false;

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
                                                    "added "
                                                            + paAdd.getPackageName()
                                                            + " to yellow list");
                                        } else {
                                            log.add(
                                                    "removed "
                                                            + paAdd.getPackageName()
                                                            + " from list");
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
                                pw.println(
                                        "\t<app_whitelist name=\""
                                                + app.getPackageName()
                                                + "\" />");
                                log.add("added " + app.getPackageName() + " to white list");
                            } else if (app.getListColor().equals(AppData.LIST_COLOR_WHITE)) {
                                pw.println(
                                        "\t<app_yellowlist name=\""
                                                + app.getPackageName()
                                                + "\" />");
                                log.add("added " + app.getPackageName() + " to yellow list");
                            }
                        }

                        if (!sleepConfiguratorTagFound) pw.println("\t<!--" + endTag + "-->");
                        writeRemaining = false;
                    }
                    if (printLine) pw.println(line);
                }

                br.close();
            }
            pw.close();

        } catch (IOException iOException) {
            message = "IOException: " + iOException.getMessage();
            throw new ModifyFileException(message);
        }

        return fileTmp.getAbsolutePath();
    }

    public static List<String> getLog() {
        ArrayList<String> arrayList = new ArrayList<String>(log);
        log = new ArrayList<String>();
        return arrayList;
    }

    public static List<AppData> getPackagesFromFile() throws ReadFileException {
        List<AppData> arrayList = new ArrayList<AppData>();
        PackageManager packageManager = context.getPackageManager();
        boolean setByJsc = false;
        try {
            FileReader fileReader = new FileReader(new File(getFilePathQbList()));
            try {
                XmlPullParser newPullParser = Xml.newPullParser();
                newPullParser.setInput(fileReader);
                for (int eventType = newPullParser.getEventType();
                        eventType != 1;
                        eventType = newPullParser.nextToken()) {

                    if (eventType != 0) {
                        if (eventType == XmlPullParser.COMMENT) {
                            if (context.getText(R.string.tagStart)
                                    .equals(newPullParser.getText())) {
                                setByJsc = true;
                            } else if (context.getText(R.string.tagEnd)
                                    .equals(newPullParser.getText())) {
                                setByJsc = false;
                            }
                        } else if (eventType == 2) {
                            String name = newPullParser.getName();
                            if (name.equals("app_whitelist") || name.equals("app_yellowlist")) {

                                AppData appData =
                                        new AppData(
                                                newPullParser.getAttributeValue(
                                                        (String) null, "name"));
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
                                appData.setFactoryDefault(!setByJsc);
                                Log.i(TAG, appData.getPackageName());
                                if (name.equals("app_whitelist")) {
                                    appData.setListColor(AppData.LIST_COLOR_WHITE);
                                } else {
                                    appData.setListColor(AppData.LIST_COLOR_YELLOW);
                                }
                                arrayList.add(appData);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            throw new ReadFileException("FileNotFoundException: " + ex.getMessage());
        }
        packagesList = arrayList;
        return arrayList;
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
                    SuCommands.sudoForResult(command);
                } catch (SuCommands.SuCommandException e) {
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

    public static List<String> getAvailableBackupFiles() {

        String[] list =
                new File(getPathQbListBackup())
                        .list(
                                new FilenameFilter() { // from class:
                                    // com.thf.sleepconfigurator.utils.FileUtil.1
                                    @Override // java.io.FilenameFilter
                                    public boolean accept(File file, String str) {
                                        return str.endsWith(".xml");
                                    }
                                });

        if (list != null) {
            List<String> asList = new ArrayList<String>();
            for (String f : list) {
                File file = new File(getPathQbListBackup() + "/" + f);
                asList.add(f + " (" + file.length() + ")");
            }

            // List<String> asList = Arrays.asList(list);
            Collections.sort(asList);
            Collections.reverse(asList);
            return asList;
        }
        return null;
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

    public static void clearPackagesSelectedByUser() {
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
