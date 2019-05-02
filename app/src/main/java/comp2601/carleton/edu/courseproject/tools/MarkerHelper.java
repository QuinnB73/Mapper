package comp2601.carleton.edu.courseproject.tools;

import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import comp2601.carleton.edu.courseproject.models.NoteModel;
import comp2601.carleton.edu.courseproject.views.MainActivity;

public class MarkerHelper {
    private static final String TAG = "MarkerHelper";
    private static final double SHIFT_DISTANCE = 0.00025;
    public static final String PICTURE_MARKER_CONSTANT = "PICTURE";

    /**
     * Read images from storage
     *
     * @param activity The {@code MainActivity}, used to get application context and to register the
     *                 {@code MainActivity} as a subscriber to the {@code DecodeImageTask} instances
     */
    public void readImagesFromStorage(MainActivity activity){
        File[] allFiles;
        File dir = new File(Environment.getExternalStorageDirectory(), "Mapper Images");
        List<String> filePathsFromExtDir = new ArrayList<>();
        if(dir.isDirectory()){
            allFiles = dir.listFiles();
            if(allFiles == null){
                Log.e(TAG, "Files not there!");
                return;
            }
            for(int i = 0; i < allFiles.length; i++){
                filePathsFromExtDir.add(allFiles[i].getPath());
            }
            for(int i = 0; i < filePathsFromExtDir.size(); i++){
                DecodeImgTask decodeImgTask = new DecodeImgTask(activity.getApplicationContext());
                decodeImgTask.subscribe(activity);
                decodeImgTask.execute(filePathsFromExtDir.get(i));
            }
        }
    }

    /**
     * Read notes from the SQLite DB
     * @param activity The {@code MainActivity}, used for application context
     */
    public void readNotesFromDB(MainActivity activity){
        NoteDAO dao = new NoteDAO(activity.getApplicationContext());
        List<NoteModel> notesFromDb = new ArrayList<>();

        try{
            dao.open();
            notesFromDb.addAll(dao.getAllNotes());
            dao.close();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        for(int i = 0; i < notesFromDb.size(); i++){
            activity.addNoteMarker(notesFromDb.get(i));
        }
        Log.e(TAG, notesFromDb.toString());
    }

    /**
     * Build a note {@code MarkerOptions} object from a {@code NoteModel} object
     *
     * @param noteModel The {@code NoteModel} instance to build the {@code MarkerOptions} object from
     * @param markerPositions The current positions of markers on the map. Used to handle overlapping
     *                        markers.
     * @return {@code MarkerOptions} object
     */
    public MarkerOptions buildNoteMarkerOption(NoteModel noteModel, List<LatLng> markerPositions){
        LatLng latLng = new LatLng(noteModel.getLat(), noteModel.getLon());
        latLng = findNewLatLng(latLng, markerPositions);

        return new MarkerOptions().position(latLng).title(noteModel.getTitle())
                .snippet(noteModel.getNote() + "\n" + noteModel.getTimestamp())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
    }

    /**
     * Build an image {@code MarkerOptions} object from a {@code HashMap} of image metadata
     * @param metadata The {@code HashMap} of metadata used to build the {@code MarkerOptions} object
     * @param markerPositions The current positions of markers on the map. Used to handle overlapping
     *                        markers.
     * @return {@code MarkerOptions} object
     */
    public MarkerOptions buildImageMarkerOption(HashMap<String, String> metadata, List<LatLng> markerPositions){
        LatLng latLng;

        if(!metadata.get(DecodeImgTask.LON).equals("NULL") &&
                !metadata.get(DecodeImgTask.LAT).equals("NULL")){
            String lonDMS = metadata.get(DecodeImgTask.LON);
            String latDMS = metadata.get(DecodeImgTask.LAT);
            String lonDir = metadata.get(DecodeImgTask.LON_DIR);
            String latDir = metadata.get(DecodeImgTask.LAT_DIR);

            double lon = ImageHelper.DMStoDec(lonDMS, lonDir);
            double lat = ImageHelper.DMStoDec(latDMS, latDir);

            Log.d(TAG, "PARSED LAT: " + lat + " LON: " + lon);
            latLng = new LatLng(lat, lon);
        } else {
            latLng = new LatLng(43.65, -79.38);
        }

        // Shift based on existing markers
        latLng = findNewLatLng(latLng, markerPositions);

        return new MarkerOptions().position(latLng).title(PICTURE_MARKER_CONSTANT)
                .snippet(metadata.get(DecodeImgTask.TIMESTAMP))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
    }

    /**
     * Find a new {@code LatLng} position based on the current positions
     * @param currPos The current position of the marker
     * @param markerPositions The positions of all the already placed markers
     *
     * @return {@code LatLng} A new LatLng position if necessary, the current position otherwise
     *
     * TODO: There is a chance that doing this "randomly" will place the marker on top of an already
     * existing marker that has been checked already. There is a better way to do this.
     */
    private LatLng findNewLatLng(LatLng currPos, List<LatLng> markerPositions){
        LatLng newLatLng = currPos;

        for(int i = 0; i < markerPositions.size(); i++){
            if(markerPositions.get(i).equals(currPos)){
                double lat = currPos.latitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                double lng = currPos.longitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                newLatLng = new LatLng(lat, lng);
            }
        }
        return newLatLng;
    }
}
