package com.deepakyadav.multinote;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView description;
    public TextView time;

    ViewHolder(View view) {
        super(view);
        title = view.findViewById(R.id.viewTitle);
        description = view.findViewById(R.id.viewDesc);
        time = view.findViewById(R.id.viewTime);
    }
}
