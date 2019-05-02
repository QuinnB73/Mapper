package comp2601.carleton.edu.courseproject.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import comp2601.carleton.edu.courseproject.Interfaces.MapClickDelegate;
import comp2601.carleton.edu.courseproject.models.NoteModel;
import comp2601.carleton.edu.courseproject.views.MainActivity;

/**
 * Created by quinnbudan on 2017-04-04.
 */

// onClick listeners for the Google Maps
public class MapClickListener implements GoogleMap.OnMapLongClickListener,
        GoogleMap.OnInfoWindowClickListener{
    private static final String TAG = "MapLongClickListener";
    private static final double REGUALR_BOUND = 0.0005;
    private static final double MED_ZOOMED_IN_BOUND = 0.00005;
    private static final double FULL_ZOOMED_IN_BOUND = 0.00005;

    private Context context;
    private MapClickDelegate mapClickDelegate;
    private HashMap<Marker, NoteModel> markerNoteModels;
    private HashMap<Marker, String> markerFiles;
    private ArrayList<Marker> markers;
    private GoogleMap googleMap;

    public MapClickListener(Context context, GoogleMap map){
        this.context = context;
        googleMap = map;
    }

    // LongClick == delete marker
    @Override
    public void onMapLongClick(LatLng latLng) {
        float currZoomLevel = googleMap.getCameraPosition().zoom;
        double bound = REGUALR_BOUND; // <= level 16
        Log.e(TAG, "MAX: "+googleMap.getMaxZoomLevel() + ", CURR: "+currZoomLevel);
        if(currZoomLevel >= googleMap.getMaxZoomLevel()-2){ // >= level 19
            bound = FULL_ZOOMED_IN_BOUND;
        } else if (currZoomLevel > googleMap.getMaxZoomLevel() - 4){ // > level 16 < level 19
            bound = MED_ZOOMED_IN_BOUND;
        }
        LatLng lbound = new LatLng(latLng.latitude - bound, latLng.longitude - bound);
        LatLng ubound = new LatLng(latLng.latitude + bound, latLng.longitude + bound);
        LatLngBounds bounds = new LatLngBounds(lbound, ubound);

        for(final Marker marker: markers){
            if(bounds.contains(marker.getPosition())){
                //Log.e(TAG, "Found it");
                if(marker.getTitle().equals(MarkerHelper.PICTURE_MARKER_CONSTANT)){
                    // delete picture
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Delete Picture");
                    dialog.setMessage("Are you sure you want to delete this picture? " +
                            "This cannot be undone.");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // delete note
                            if(!markerFiles.containsKey(marker)){ // can't proceed
                                return;
                            }
                            deletePictureFromExtDir(markerFiles.get(marker));
                            markerFiles.remove(marker);
                            mapClickDelegate.removePictureMarker(marker);

                            // remove marker
                            markers.remove(marker);
                            marker.remove();
                        }
                    });
                    dialog.setNegativeButton("No", null);
                    dialog.show();
                } else {
                    // delete note
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Delete Note");
                    dialog.setMessage("Are you sure you want to delete this note? " +
                            "This cannot be undone.");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // delete note
                            if(!markerNoteModels.containsKey(marker)){ // can't proceed
                                return;
                            }
                            deleteNoteFromDB(markerNoteModels.get(marker));
                            markerNoteModels.remove(marker);
                            mapClickDelegate.removeNoteMarker(marker);
                            // remove marker
                            markers.remove(marker);
                            marker.remove();
                        }
                    });
                    dialog.setNegativeButton("No", null);
                    dialog.show();
                }
                break;
            }
        }
    }

    // Notify info window clicked to delegate
    @Override
    public void onInfoWindowClick(Marker marker) {
        if(markerNoteModels.containsKey(marker)){ // is note marker
            mapClickDelegate.clickedOnNoteWindow(markerNoteModels.get(marker));
        } else { // is picture marker
            mapClickDelegate.clickedOnPictureWindow(markerFiles.get(marker));
        }
    }

    // Delete from external memory
    private void deletePictureFromExtDir(String path){
        File file = new File(path);
        if(file.exists()){
            if(file.delete()){
                Log.d(TAG, file.toString() + " successfully deleted.");
                ImageHelper imageHelper = new ImageHelper(context);
                imageHelper.updateAndroidGallery(path);
            } else {
                Log.d(TAG, file.toString() + " could not be deleted (Maybe incorrect path?)");
                Toast.makeText(context, "Error! Couldn't delete photo!", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Delete from SQLite database
    private void deleteNoteFromDB(NoteModel noteModel){
        NoteDAO dao = new NoteDAO(context);
        try{
            dao.open();
            dao.deleteNote(noteModel);
            dao.close();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Error! Couldn't delete note!", Toast.LENGTH_LONG).show();
        }
    }

    // all three of these should be called if any external change
    public void updateMarkers(ArrayList<Marker> markers){
        this.markers = markers;
    }

    public void updateMarkerNoteModels(HashMap<Marker, NoteModel> markerNoteModels){
        this.markerNoteModels = markerNoteModels;
    }

    public void updateMarkerFiles(HashMap<Marker, String> markerFiles){
        this.markerFiles = markerFiles;
    }

    public void setMapClickDelegate(MapClickDelegate mapClickDelegate){
        this.mapClickDelegate = mapClickDelegate;
    }

    public void updateNoteMarker(Marker marker, NoteModel noteModel){
        if(markerNoteModels == null){
            markerNoteModels = new HashMap<>();
        }
        if(markers == null){
            markers = new ArrayList<>();
        }
        markerNoteModels.put(marker, noteModel);
        markers.add(marker);
    }

    public void updatePictureMarker(Marker marker, String path){
        if(markerFiles == null){
            markerFiles = new HashMap<>();
        }
        if(markers == null){
            markers = new ArrayList<>();
        }
        markerFiles.put(marker, path);
        markers.add(marker);
    }
}
