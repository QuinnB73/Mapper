package comp2601.carleton.edu.courseproject.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by quinnbudan on 2017-03-29.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";

    private static final String DATABASE_NAME = "mapper.db";
    private static final int    DATABASE_VERSION = 1;

    // SQL create statements TODO: CHANGE THIS
    private static final String DATABASE_CREATE = "create table " + TABLE_NOTES + "( " +
            COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TITLE + " text not null, "
            + COLUMN_NOTE + " text not null, " + COLUMN_DATE + " text not null, "
            + COLUMN_LAT + " real not null, " + COLUMN_LON + " real not null);";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database){
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        Log.d(TAG, "Upgrading DB from version " + oldVersion + " to " + newVersion +
                ", this will destroy all data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(database);
    }
}
