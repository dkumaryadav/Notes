package com.deepakyadav.multinote;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Date;

public class AddNote extends AppCompatActivity {

    private EditText noteTitle;
    private EditText noteDescription;
    private static final String TAG = "EditActivity";
    private boolean existingNote;
    private int notePos;
    private String previousTitle;
    private String previousDesc;

    // get values from intent, if existing note get previous title and desc
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        noteTitle = findViewById(R.id.addTitle);
        noteDescription = findViewById(R.id.addDesc);
        noteDescription.setMovementMethod(new ScrollingMovementMethod());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("existingNote")) {
            existingNote = intent.getBooleanExtra("existingNote", false);
            if (existingNote) {
                Notes existingNote = (Notes) intent.getSerializableExtra("Note");
                notePos = intent.getIntExtra("Note position", 0);
                if (existingNote != null) {
                    previousTitle = existingNote.getTitle();
                    previousDesc = existingNote.getDescription();
                    noteTitle.setText( previousTitle );
                    noteDescription.setText( previousDesc );
                }
            }
        }
    }

    // options menu to save note
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_note, menu);
        return true;
    }

    // Handling click events on the options menu
    // call saveNote with fromBackButton = false
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.saveNote:
                saveNote(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // call save note with back button pressed = true
    @Override
    public void onBackPressed() {
        saveNote(true);
    }

    // Function will check if the note has to be saved or not
    private void saveNote(boolean fromBackButton) {

        final String currentTitle = noteTitle.getText().toString().trim();
        final String currentDesc = noteDescription.getText().toString().trim();

        if (existingNote) { // Existing note
            if( currentTitle.length() != 0 ){ // If title entered
                if( !currentTitle.equals(previousTitle) || !currentDesc.equals(previousDesc)) {
                    if( fromBackButton ){ // save note from back button
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // If Yes is pressed save note and add details to intent
                                Intent intent = new Intent();
                                Notes note = new Notes( currentTitle,
                                                        new Date(),
                                                        currentDesc );
                                intent.putExtra("note", (Serializable)note);
                                intent.putExtra("notePos", notePos);
                                intent.putExtra("noteUpdated", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Note changes discared no need to save
                                exitWithoutSaving(false);
                            }
                        });
                        builder.setMessage("Note is not saved!\n Save '"+ currentTitle+"' note ?");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{ // save request from save button

                        Intent intent = new Intent();
                        Notes note = new Notes( currentTitle,
                                                new Date(),
                                                currentDesc);
                        intent.putExtra("note", (Serializable)note);
                        intent.putExtra("notePos", notePos);
                        intent.putExtra("noteUpdated", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                } else{
                    // Title and desc are same so exit with saving no toast
                    exitWithoutSaving(false);
                }
            } else{ // No title entered exit with TOAST
                exitWithoutSaving(true);
            }
        } else { // New Note
            if ( currentTitle.length() != 0) {
                if(fromBackButton){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent();
                            Notes note = new Notes( currentTitle, new Date(), currentDesc);
                            intent.putExtra("NEW_NOTE", (Serializable) note);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            exitWithoutSaving(false);
                        }
                    });

                    builder.setMessage("Note is not saved!\n Save '"+ currentTitle+"' note ?");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    Intent intent = new Intent();
                    Log.d(TAG, "intent created");
                    Notes note = new Notes(currentTitle, new Date(), currentDesc);
                    Log.d(TAG, "note object created");
                    intent.putExtra("NEW_NOTE", (Serializable)note);
                    Log.d(TAG, "put extra created");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else { // Title is empty exit with showing toast
                exitWithoutSaving(true);
            }
        }
    }

    // Function will navigate back to main without saving
    public void exitWithoutSaving(boolean ToastShow){
        if( ToastShow )
            Toast.makeText(this, "Title is empty not saving note!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.putExtra("noteUpdated", false);
        setResult(RESULT_OK, intent);
        finish();
    }
}
