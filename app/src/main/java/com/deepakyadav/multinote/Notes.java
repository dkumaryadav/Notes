package com.deepakyadav.multinote;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.Date;

// implements serializable and comparable<Notes in order to sort and enable passing objects with Intent
public class Notes implements Comparable<Notes>, Serializable {

    private String title;
    private Date time;
    private String description;

    // Constructor
    Notes(String title, Date time, String description) {
        this.title = title;
        this.time = time;
        this.description = description;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    Date getTime() {
        return time;
    }

    String getDescription() {
        return description;
    }

    // compare to method
    @Override
    public int compareTo(Notes note) {
        if (note.getTime() == null) {
            return 0;
        }
        return note.getTime().compareTo(this.time);
    }

    @NonNull
    @Override
    public String toString() {
        return title + " (" + time+ "), " + description;
    }
}