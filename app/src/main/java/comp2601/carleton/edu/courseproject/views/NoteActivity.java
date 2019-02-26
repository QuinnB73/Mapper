package comp2601.carleton.edu.courseproject.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.text.DecimalFormat;

import comp2601.carleton.edu.courseproject.R;
import comp2601.carleton.edu.courseproject.models.NoteModel;

/**
 * Created by quinnbudan on 2017-04-07.
 */

public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteActivity";
    private TextView title;
    private TextView note;
    private TextView metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        title = (TextView)findViewById(R.id.title_text_view);
        note = (TextView)findViewById(R.id.note_text_view);
        metadata = (TextView)findViewById(R.id.meta_note_text);
        displayNoteModel((NoteModel)getIntent().getSerializableExtra(MainActivity.INTENT_NOTE_KEY));
    }

    private void displayNoteModel(NoteModel noteModel){
        if(noteModel == null){
            return;
        }
        title.setText(noteModel.getTitle());
        note.setText(noteModel.getNote());

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        metadata.setText("Taken on: " + noteModel.getTimestamp() + "\nAt coordinates: " +
                         decimalFormat.format(noteModel.getLat()) + ", " +
                         decimalFormat.format(noteModel.getLon()));
    }
}
