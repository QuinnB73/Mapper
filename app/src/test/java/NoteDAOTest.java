import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import comp2601.carleton.edu.courseproject.models.NoteModel;
import comp2601.carleton.edu.courseproject.tools.NoteDAO;
import comp2601.carleton.edu.courseproject.tools.NoteDBHelper;

@RunWith(RobolectricTestRunner.class)
public class NoteDAOTest {
    private static final String TEST_DATA = "TEST";
    private static final double TEST_LAT  = 47.55;
    private static final double TEST_LON  = 48.55;

    private NoteDAO noteDAO;

    @Before
    public void setup(){
        noteDAO = new NoteDAO(RuntimeEnvironment.application);
    }

    @Test
    public void testAddNote() {
        NoteModel note = new NoteModel(TEST_DATA, TEST_DATA, TEST_DATA, TEST_LAT, TEST_LON);

        noteDAO.open();
        note = noteDAO.createNote(note);

        List<NoteModel> retrievedNotes = noteDAO.getAllNotes();
        Assert.assertEquals(1, retrievedNotes.size());

        NoteModel retrievedNote = retrievedNotes.get(0);
        Assert.assertEquals(note, retrievedNote);

        noteDAO.close();
    }

    @Test
    public void testDeleteNote(){
        NoteModel note = new NoteModel(TEST_DATA, TEST_DATA, TEST_DATA, TEST_LAT, TEST_LON);
        List<NoteModel> retrievedNotes;

        noteDAO.open();
        note = noteDAO.createNote(note);

        retrievedNotes = noteDAO.getAllNotes();
        Assert.assertEquals(1, retrievedNotes.size());

        noteDAO.deleteNote(note);

        retrievedNotes = noteDAO.getAllNotes();
        Assert.assertEquals(0, retrievedNotes.size());
    }

    @After
    public void tearDown(){
        NoteDBHelper noteDbHelper = new NoteDBHelper(RuntimeEnvironment.application);
        SQLiteDatabase noteDatabase = noteDbHelper.getWritableDatabase();
        noteDbHelper.clearDatabase(noteDatabase);
        noteDbHelper.close();
    }
}
