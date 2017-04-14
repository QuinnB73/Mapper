package comp2601.carleton.edu.courseproject.Interfaces;

import com.google.android.gms.maps.model.Marker;

import comp2601.carleton.edu.courseproject.models.NoteModel;

/**
 * Created by quinnbudan on 2017-04-04.
 */

public interface MapClickDelegate {
    void removePictureMarker(Marker marker);
    void removeNoteMarker(Marker marker);
    void clickedOnPictureWindow(String filePath);
    void clickedOnNoteWindow(NoteModel noteModel);
}
