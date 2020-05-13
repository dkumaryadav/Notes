package com.deepakyadav.multinote;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "NotesAdapter";
    private List<Notes> noteList;
    private MainActivity mainAct;

    NotesAdapter(List<Notes> list, MainActivity ma) {
        this.noteList = list;
        mainAct = ma;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW ViewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notes_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notes ntList = noteList.get(position);
        holder.title.setText(ntList.getTitle());

        holder.time.setText(convertDateToString(ntList.getTime()));
        // Check if desc is  > 80
        String tempDesc = ntList.getDescription();
        if( tempDesc.length() > 80){
            tempDesc = tempDesc.substring(0,80)+"...";
        }
        holder.description.setText(tempDesc);
    }

    private String convertDateToString(Date time) {
        Log.d(TAG, "onCreateViewHolder: covertDateToString");
        try {
            if (time != null) {
                Log.d(TAG, "onCreateViewHolder: starint");
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                formatter.applyPattern("EEE MMM dd, hh:mm aaa");
                Log.d(TAG, "onCreateViewHolder: pattern"+formatter.format(time));
                return formatter.format(time);
            }
        } catch (Exception e) {
            Log.d(TAG, "onCreateViewHolder: excpetion");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

}
