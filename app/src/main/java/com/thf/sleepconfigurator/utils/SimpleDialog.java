package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import androidx.appcompat.app.AlertDialog;
import com.thf.sleepconfigurator.R;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class SimpleDialog {
    private Context context;
    private SimpleDialogCallbacks listener;
    private String message;
    List<String> listItems;
    private String title;
    private String selectedItem;
    private String identifier;

    /* loaded from: classes.dex */
    public interface SimpleDialogCallbacks {
        void onClick(String identifier, boolean result, String item);
    }

    public SimpleDialog(
            Context context,
            String identifier,
            SimpleDialogCallbacks simpleDialogCallbacks,
            String title,
            List<String> listItems) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.listItems = listItems;
        this.listener = simpleDialogCallbacks;
        this.identifier = identifier;
    }

    public SimpleDialog(
            Context context,
            SimpleDialogCallbacks simpleDialogCallbacks,
            String title,
            List<String> listItems) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.listItems = listItems;
        this.listener = simpleDialogCallbacks;
    }

    public SimpleDialog(
            Context context,
            String identifier,
            SimpleDialogCallbacks simpleDialogCallbacks,
            String title,
            String message) {
        this.context = context;
        this.identifier = identifier;
        this.title = title;
        this.message = message;
        this.listItems = listItems;
        this.listener = simpleDialogCallbacks;
    }

    public SimpleDialog(Context context, String title, String message) {
        this.context = context;
        this.title = title;
        this.message = message;
    }

    public SimpleDialog(
            Context context,
            SimpleDialogCallbacks simpleDialogCallbacks,
            String title,
            String message) {
        this.context = context;
        this.title = title;
        this.message = message;
        String selectedItem;
        this.listener = simpleDialogCallbacks;
    }

    public void show() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(
                                new ContextThemeWrapper(
                                        this.context, (int) R.style.AlertDialogCustom))
                        .setTitle(this.title)
                        .setMessage(this.message)
                        .setPositiveButton(
                                R.string.yes,
                                new DialogInterface.OnClickListener() { // from class:
                                    // com.thf.sleepconfigurator.utils.SimpleDialog.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (SimpleDialog.this.listener != null) {
                                            SimpleDialog.this.listener.onClick(
                                                    identifier, true, selectedItem);
                                        }
                                    }
                                })
                        .setIcon(R.mipmap.ic_launcher);

        if (this.listener != null) {
            builder.setNegativeButton(
                    R.string.no,
                    new DialogInterface.OnClickListener() { // from class:
                        // com.thf.sleepconfigurator.utils.SimpleDialog.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (SimpleDialog.this.listener != null) {
                                SimpleDialog.this.listener.onClick(identifier, false, null);
                            }
                        }
                    });
        }

        if (this.listItems != null) {
            builder.setMessage(null);
            String[] stringArray = listItems.toArray(new String[listItems.size()]);
            builder.setSingleChoiceItems(
                    stringArray,
                    -1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selectedItem = stringArray[i];
                        }
                    });
        }

        builder.show();
    }
}
