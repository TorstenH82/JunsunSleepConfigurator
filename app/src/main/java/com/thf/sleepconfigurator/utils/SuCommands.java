package com.thf.sleepconfigurator.utils;

import android.app.TabActivity;
import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.thf.sleepconfigurator.R;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SuCommands {
    private static final String TAG = "SleepConfigurator";
    private static String su;
    private static final String partition = "/vendor";
    private static boolean isPartitionRw = false;
    private static boolean debug = false;

    public SuCommands(String su, boolean debug) {
        SuCommands.su = su;
        SuCommands.debug = debug;
    }

    public static class SuCommandException extends Exception {
        public SuCommandException(String message) {
            super(message);
        }
    }

    public boolean partitionHasFreeSpace() throws SuCommandException {

        if (debug) return true;

        List<String> dfResult = sudoForResult("df " + partition);
        for (String line : dfResult) {
            if (line.contains(partition)) {
                if (line.contains("100%")) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        throw new SuCommandException("unable to check free space on partition " + partition);
    }

    public void remountPartition() throws SuCommandException {
        if ("825X_Pro".equals(android.os.Build.DEVICE)) {
            sudoForResult("remount");
        } else {
            sudoForResult("mount -o rw,remount " + partition);
        }
        isPartitionRw = true;
    }

    public List<String> getDeletionVictims() throws SuCommandException {
        List<String> findResult = sudoForResult("find " + partition + " -name *.apk");

        List<String> output =
                findResult.stream() // convert list to stream
                        .filter(line -> line.startsWith(partition) && line.contains("app/"))
                        .collect(Collectors.toList());

        return output;
    }

    public void deleteFile(String pathToFile) throws SuCommandException {
        if (!isPartitionRw) {
            remountPartition();
        }
        sudoForResult("rm " + pathToFile);
    }

    public void copyFile(String from, String to) throws SuCommandException {
        if (to.startsWith(partition)) {
            if (!partitionHasFreeSpace()) {
                throw new SuCommandException("no free space on " + partition);
            }
            if (!isPartitionRw) {
                remountPartition();
            }
        }
        sudoForResult("cp -f " + from + " " + to);
    }

    public void moveFile(String from, String to) throws SuCommandException {
        if (!isPartitionRw) {
            remountPartition();
        }
        sudoForResult("mv " + from + " " + to);
    }

    public void copyAndChmod(String from, String to, String chmod) throws SuCommandException {
        copyFile(from, to);
        sudoForResult("chmod " + chmod + " " + to);
    }

    public static List<String> sudoForResult(String command) throws SuCommandException {

        if (command == null || "".equals(command)) {
            throw new SuCommandException("command is empty");
        }
        if (su == null || "".equals(su)) {
            throw new SuCommandException("su is not set");
        }

        List<String> output = new ArrayList<String>();

        class ReadLog implements Runnable {
            private BufferedReader br;

            ReadLog(BufferedReader br) {
                this.br = br;
            }

            @Override
            public void run() {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        output.add(line);
                    }
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        }
        ;

        try {

            Process exec;

            if (debug) {
                exec = Runtime.getRuntime().exec(command);
            } else {
                exec = Runtime.getRuntime().exec(su);
                DataOutputStream dataOutputStream = new DataOutputStream(exec.getOutputStream());
                dataOutputStream.writeBytes(command + "\n");
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            Thread.sleep(10);

            Thread t1 = new Thread(new ReadLog(in));
            t1.start();

            BufferedReader er = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
            Thread.sleep(10);

            Thread t2 = new Thread(new ReadLog(er));
            t2.start();

            t1.join();
            t2.join();

            exec.waitFor();

        } catch (IOException ex) {
            throw new SuCommandException("IOException: " + ex.getMessage());
        } catch (InterruptedException e) {
            throw new SuCommandException("InterruptedException: " + e.getMessage());
        }

        return output;
    }

    public class Del_ReadStream implements Runnable {
        String name;
        InputStream is;
        Thread thread;

        public Del_ReadStream(String name, InputStream is) {
            this.name = name;
            this.is = is;
        }

        public void start() {
            thread = new Thread(this);
            thread.start();
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                while (true) {
                    String s = br.readLine();
                    if (s == null) break;
                    System.out.println("[" + name + "] " + s);
                }
                is.close();
            } catch (Exception ex) {
                System.out.println("Problem reading stream " + name + "... :" + ex);
                ex.printStackTrace();
            }
        }
    }
}
