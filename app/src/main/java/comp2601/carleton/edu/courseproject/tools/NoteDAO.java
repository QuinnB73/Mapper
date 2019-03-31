package comp2601.carleton.edu.courseproject.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import comp2601.carleton.edu.courseproject.models.NoteModel;

/**
 * Created by quinnbudan on 2017-03-29.
 */

public class NoteDAO {
    // Database fields
    private static final String TAG = "NoteDAO";
    private SQLiteDatabase database;
    private NoteDBHelper noteDbHelper;
    private String[] columns = {NoteDBHelper.COLUMN_ID, NoteDBHelper.COLUMN_TITLE, NoteDBHelper.COLUMN_NOTE,
                                NoteDBHelper.COLUMN_DATE, NoteDBHelper.COLUMN_LAT, NoteDBHelper.COLUMN_LON};

    public NoteDAO(Context context){
        noteDbHelper = new NoteDBHelper(context);
    }

    // open SQLite databsae
    public void open() throws SQLException {
        database = noteDbHelper.getWritableDatabase();
    }

    // close SQLite databse
    public void close(){
        database.close();
    }

    // Insert new note into DB
    public NoteModel createNote(NoteModel noteModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteDBHelper.COLUMN_TITLE, noteModel.getTitle());
        contentValues.put(NoteDBHelper.COLUMN_NOTE, noteModel.getNote());
        contentValues.put(NoteDBHelper.COLUMN_DATE, noteModel.getTimestamp());
        contentValues.put(NoteDBHelper.COLUMN_LAT, noteModel.getLat());
        contentValues.put(NoteDBHelper.COLUMN_LON, noteModel.getLon());

        long insertId = database.insert(NoteDBHelper.TABLE_NOTES, null, contentValues);
        Cursor cursor = database.query(NoteDBHelper.TABLE_NOTES, columns, NoteDBHelper.COLUMN_ID + " = "
                + insertId, null, null, null, null);
        cursor.moveToFirst();
        NoteModel newNote = cursorToNote(cursor);
        cursor.close();
        return newNote;
    }

    // Delete note from DB
    public void deleteNote(NoteModel noteModel){
        long id = noteModel.getId();
        database.delete(NoteDBHelper.TABLE_NOTES, NoteDBHelper.COLUMN_ID + " = " + id, null);
        Log.d(TAG, "Note with id: " + id + " deleted.");
    }

    // Return a list of all notes in DB
    public List<NoteModel> getAllNotes(){
        List<NoteModel> notes = new ArrayList<>();

        Cursor cursor = database.query(NoteDBHelper.TABLE_NOTES, columns, null, null, null, null, null);
        cursor.moveToFirst();

        // loop through the cursor
        while(!cursor.isAfterLast()){
            NoteModel note = cursorToNote(cursor);
            notes.add(note);
            cursor.moveToNext();
        }
        cursor.close();

        return notes;
    }

    // Create NoteModel from cursor
    private NoteModel cursorToNote(Cursor cursor){
        NoteModel noteModel = new NoteModel();
        noteModel.setId(cursor.getLong(0));
        noteModel.setTitle(cursor.getString(1));
        noteModel.setNote(cursor.getString(2));
        noteModel.setTimestamp(cursor.getString(3));
        noteModel.setLat(cursor.getDouble(4));
        noteModel.setLon(cursor.getDouble(5));
        return noteModel;
    }
}
