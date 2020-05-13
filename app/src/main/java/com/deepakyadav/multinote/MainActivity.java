package com.deepakyadav.multinote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private Notes notes;
    private EditText noteTitle;
    private EditText noteDescription;
    private static final String TAG = "MainActivity";
    private final List<Notes> notesList = new ArrayList<>();
    private boolean notesUpdated = false;
    private static final int NEW_NOTE_CODE = 1;
    private static final int EXISTING_NOTE_CODE = 2;

    // From OnClickListener to addNote activity
    @Override
    public void onClick(View v) {  // click listener called by ViewHolder clicks
        if (notesList != null) {
            addNoteActitiy( recyclerView.getChildLayoutPosition(v),
                    true,
                             notesList.get(recyclerView.getChildLayoutPosition(v))
                            );
        }
    }

    // From OnLongClickListener to delete note
    @Override
    public boolean onLongClick(View v) {  // long click listener called by ViewHolder long clicks
        final int pos = recyclerView.getChildLayoutPosition(v);
        if (notesList != null) {
            String noteTitle = "";
            Notes note = notesList.get(pos);
            if (note != null) {
                noteTitle = note.getTitle();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if ( notesList != null) {
                        notesList.remove( pos );
                        notesUpdated = true;
                        Collections.sort(notesList);
                        updateNotesCount();
                        notesAdapter.notifyDataSetChanged();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Do nothing
                }
            });
            builder.setMessage("Delete Note '" + noteTitle + "'?");
            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        }
        return false;
    }

    // orientation changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putBoolean("notesUpdated", notesUpdated);
        super.onSaveInstanceState(outState);
    }

    // orientation changes
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedState);
        notesUpdated = savedState.getBoolean("notesUpdated");
    }

    // load notes and update count when app loads onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        notesAdapter = new NotesAdapter(notesList, this);
        recyclerView.setAdapter(notesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check if we have data in JSON file
        loadNotes();
    }

    // Adding options menu to add note and go to help
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    // Navigate to AddNote activity
    private void addNoteActitiy(int notePos, boolean existingNote, Notes note){
        int requstCode = NEW_NOTE_CODE;
        Intent intent = new Intent(this, AddNote.class);
        intent.putExtra("existingNote", existingNote);
        // For_an_existing_note,_pass_existing-note_object_&_its-position
        if (existingNote) {
            requstCode = EXISTING_NOTE_CODE;
            if (note != null) {
                intent.putExtra("Note position", notePos);
                intent.putExtra("Note", (Serializable) note);
            }
        }
        startActivityForResult(intent, requstCode);

    }

    // Handling click events on the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.about:
                // Navigate to about activity
                intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
            case R.id.addNote:
                // Navigate to add note activity with position 0
                addNoteActitiy(0, false, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Handling result from intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_NOTE_CODE) {
            if (resultCode == RESULT_OK) {
                Notes note = (Notes) data.getSerializableExtra("NEW_NOTE");
                if (note != null) {
                    notesList.add(note);
                    Log.d(TAG, "onActivityResult: newNoteAdded: " + note.toString());
                    notesUpdated = true;
                    Collections.sort(notesList);
                    updateNotesCount();
                    notesAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d(TAG, "onActivityResult: result Code: " + resultCode);
            }

        } else if (requestCode == EXISTING_NOTE_CODE) {
            if (resultCode == RESULT_OK) {
                boolean isNoteChanged = data.getBooleanExtra("noteUpdated", false);
                if (isNoteChanged) {
                    Notes existingNote = (Notes) data.getSerializableExtra("note");
                    int notePosition = data.getIntExtra("notePos", 0);
                    if (existingNote != null) {
                        notesList.set(notePosition, existingNote);
                        Log.d(TAG, "onActivityResult: ExistingNoteEdited: " + existingNote.toString());
                        notesUpdated = true;
                        Collections.sort(notesList);
                        updateNotesCount();
                        notesAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Log.d(TAG, "onActivityResult: Existing NoteEdited: " + resultCode);
            }
        }
    }

    // convertStringToDate
    private Date convertStringToDate(String input){
        try{
            if ( input.trim().length() > 0) {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                formatter.applyPattern("EEE, d MMM yyyy HH:mm:ss");
                return formatter.parse(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // convertDateToString
    private String convertDateToString(Date time) {
        try {
            if (time != null) {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance();
                formatter.applyPattern("EEE, d MMM yyyy HH:mm:ss");
                return formatter.format(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Load Notes from JSON file
    private Notes loadNotes() {
        Log.d(TAG, "loadFile: Loading JSON File");
        try {
            InputStream is = getApplicationContext().
                    openFileInput(getString(R.string.file_name));

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, getString(R.string.encoding)));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            Log.d(TAG, "loadFile: checking for JSON Array");
            JSONObject jsonObject = new JSONObject(sb.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("MultiNotesList");

            if (jsonArray != null ){
                if( jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject noteJson = jsonArray.getJSONObject(i);
                        if (noteJson != null) {
                            notesList.add(new Notes( noteJson.getString("title"),
                                                     convertStringToDate(noteJson.getString("time")),
                                                     noteJson.getString("desc") )
                                        );
                        }
                    }
                }
            }

            if( notesList.size() > 0)
                Collections.sort(notesList);

            // If load is successful update the title with count
            updateNotesCount();

        } catch (FileNotFoundException e) {
            Log.d(TAG, "loadFile: File not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notes;
    }

    // Save notes when app goes in onPause() stage
    @Override
    protected void onPause() {
        // Save notes
        Log.d(TAG, "saveProduct: Saving JSON File"+ notesUpdated+ notesList.size());
        try {
            if( notesUpdated && notesList.size() >= 0) {
                FileOutputStream fos = getApplicationContext().
                        openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

                JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, getString(R.string.encoding)));
                writer.setIndent("  ");
                writer.beginObject();
                writer.name("MultiNotesList");
                writer.beginArray();

                for (Notes note : notesList) {
                    if (note != null) {
                        writer.beginObject();
                        writer.name("title").value(note.getTitle());
                        writer.name("desc").value(note.getDescription());
                        writer.name("time").value(convertDateToString(note.getTime()));
                        writer.endObject();
                    }
                }

                writer.endArray();
                writer.endObject();
                writer.flush();
                writer.close();
                notesUpdated = false;
                //Toast.makeText(getApplicationContext(), "" + notesUpdated, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    // Function to update applications app name
    private void updateNotesCount() {
        String appName = getResources().getString(R.string.app_name);
        // setting_title
        if ( notesList != null && notesList.size() > 0) {
            setTitle(appName+ " (" + notesList.size() + ")");
        } else {
            setTitle(appName);
        }
    }
}

