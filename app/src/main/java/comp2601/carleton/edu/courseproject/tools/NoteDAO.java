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
    private DBHelper dbHelper;
    private String[] columns = {DBHelper.COLUMN_ID, DBHelper.COLUMN_TITLE, DBHelper.COLUMN_NOTE,
                                DBHelper.COLUMN_DATE, DBHelper.COLUMN_LAT, DBHelper.COLUMN_LON};

    public NoteDAO(Context context){
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        database.close();
    }

    public NoteModel createNote(NoteModel noteModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.COLUMN_TITLE, noteModel.getTitle());
        contentValues.put(DBHelper.COLUMN_NOTE, noteModel.getNote());
        contentValues.put(DBHelper.COLUMN_DATE, noteModel.getTimestamp());
        contentValues.put(DBHelper.COLUMN_LAT, noteModel.getLat());
        contentValues.put(DBHelper.COLUMN_LON, noteModel.getLon());

        long insertId = database.insert(DBHelper.TABLE_NOTES, null, contentValues);
        Cursor cursor = database.query(DBHelper.TABLE_NOTES, columns, DBHelper.COLUMN_ID + " = "
                + insertId, null, null, null, null);
        cursor.moveToFirst();
        NoteModel newNote = cursorToNote(cursor);
        cursor.close();
        return newNote;
    }

    public void deleteNote(NoteModel noteModel){
        long id = noteModel.getId();
        database.delete(DBHelper.TABLE_NOTES, DBHelper.COLUMN_ID + " = " + id, null);
        Log.d(TAG, "Note with id: " + id + " deleted.");
    }

    public List<NoteModel> getAllNotes(){
        List<NoteModel> notes = new ArrayList<NoteModel>();

        Cursor cursor = database.query(DBHelper.TABLE_NOTES, columns, null, null, null, null, null);
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
