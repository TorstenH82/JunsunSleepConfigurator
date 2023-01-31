package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import androidx.appcompat.app.AlertDialog;
import com.thf.sleepconfigurator.R;

/* loaded from: classes.dex */
public class SimpleDialog {
    private Context context;
    private SimpleDialogCallbacks listener;
    private String message;
    private boolean showNegative;
    private String title;

    /* loaded from: classes.dex */
    public interface SimpleDialogCallbacks {
        void onClick(boolean z);
    }

    public SimpleDialog(Context context, SimpleDialogCallbacks simpleDialogCallbacks, String str, String str2, boolean z) {
        this.context = context;
        this.title = str;
        this.message = str2;
        this.showNegative = z;
        this.listener = simpleDialogCallbacks;
    }

    public void show() {
        AlertDialog.Builder icon = new AlertDialog.Builder(new ContextThemeWrapper(this.context, (int) R.style.AlertDialogCustom)).setTitle(this.title).setMessage(this.message).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { // from class: com.thf.sleepconfigurator.utils.SimpleDialog.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                if (SimpleDialog.this.listener != null) {
                    SimpleDialog.this.listener.onClick(true);
                }
            }
        }).setIcon(R.mipmap.ic_launcher);
        if (this.showNegative) {
            icon.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() { // from class: com.thf.sleepconfigurator.utils.SimpleDialog.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (SimpleDialog.this.listener != null) {
                        SimpleDialog.this.listener.onClick(false);
                    }
                }
            });
        }
        icon.show();
    }
}
