package comp2601.carleton.edu.courseproject.views;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import comp2601.carleton.edu.courseproject.R;
import comp2601.carleton.edu.courseproject.models.NoteModel;
import comp2601.carleton.edu.courseproject.tools.DBHelper;
import comp2601.carleton.edu.courseproject.tools.LocationHelper;
import comp2601.carleton.edu.courseproject.tools.NoteDAO;

/**
 * Created by quinnbudan on 2017-03-14.
 */

public class NoteFragment extends Fragment {
    private static final String TAG = "NoteFragment";

    private LocationHelper locationHelper;
    private EditText titleEditText;
    private EditText noteEditText;

    private String title;
    private String note;
    private String date;
    private double lat;
    private double lon;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_note, container, false);
        locationHelper = ((MainActivity)getActivity()).getLocationHelper();
        titleEditText = (EditText)rootView.findViewById(R.id.title_edit_text);
        noteEditText = (EditText)rootView.findViewById(R.id.note_edit_text);
        Log.e(TAG, ""+rootView.getLayoutParams().height);
        return rootView;
    }

    private int getTargetHeight(){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        double screenHeight = 0.0;
        if (getActivity().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            screenHeight = size.y;
        } else if (getActivity().getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE){
            screenHeight = size.x;
        }
        Double targetHeight = (2 * screenHeight) / 3; // 67% of screen height
        return targetHeight.intValue();
    }

    @Override
    public void onStop(){
        title = titleEditText.getText().toString();
        note = noteEditText.getText().toString();
        if(title.equals("") && note.equals("")){ // don't save blank note
            Log.e(TAG, "Note was blank! Not saved.");
            Toast.makeText(getContext(), "Didn't save blank note!", Toast.LENGTH_LONG).show();
            super.onStop();
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date currDate = new Date();
        if(locationHelper == null) {
            Log.e(TAG, "LocationHelper was null!");
            Toast.makeText(getContext(), "Error! Couldn't save note!", Toast.LENGTH_LONG).show();
            super.onStop();
            return;
        }
        Location currLocation = locationHelper.getCurrentLocation();
        if(currLocation == null){
            Log.e(TAG, "Location was null!");
            Toast.makeText(getContext(), "Error! Couldn't save note!", Toast.LENGTH_LONG).show();
            super.onStop();
            return;
        }
        date = dateFormat.format(currDate).toString();
        lat = currLocation.getLatitude();
        lon = currLocation.getLongitude();

        NoteModel noteModel = new NoteModel(title, note, date, lat, lon);
        Log.e(TAG, "NOTEMODEL GOING IN: " + noteModel);
        NoteDAO dao = new NoteDAO(getContext());
        try {
            dao.open();
            NoteModel result = dao.createNote(noteModel);
            Log.e(TAG, "NOTE MODEL FROM DB: " + result);
            dao.close();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        // send note model to main activity for updating
        ((MainActivity)getActivity()).updateNote(noteModel);

        super.onStop();
    }
}
