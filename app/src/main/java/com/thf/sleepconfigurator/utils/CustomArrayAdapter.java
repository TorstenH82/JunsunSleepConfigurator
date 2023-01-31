package com.thf.sleepconfigurator.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.thf.sleepconfigurator.R;

import java.util.List;

/* loaded from: classes.dex */
public class CustomArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private int currentPosition;
    private final List<String> valuesList;

    public CustomArrayAdapter(Context context, List<String> list) {
        super(context, R.layout.log_listview, list);
        this.currentPosition = -1;
        this.context = context;
        this.valuesList = list;
    }

    
    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.log_listview, (ViewGroup) null);
        String str = this.valuesList.get(i);

        ((TextView) inflate.findViewById(R.id.text)).setText(str);
        if (this.currentPosition == i || str.toLowerCase().contains("error")) {
            inflate.setBackgroundColor(ContextCompat.getColor(this.context, R.color.colorAccent));
        }
        
        return inflate;
    }

    public void setPosition(int i) {
        if (i == this.currentPosition) {
            i = -1;
        }
        this.currentPosition = i;
        notifyDataSetChanged();
    }

    public String getCurrentEntry() {
        int i = this.currentPosition;
        if (i == -1) {
            return null;
        }
        return this.valuesList.get(i);
    }
}
