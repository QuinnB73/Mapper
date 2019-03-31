package comp2601.carleton.edu.courseproject.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import comp2601.carleton.edu.courseproject.Interfaces.MapClickDelegate;
import comp2601.carleton.edu.courseproject.Interfaces.Observer;
import comp2601.carleton.edu.courseproject.R;
import comp2601.carleton.edu.courseproject.models.NoteModel;
import comp2601.carleton.edu.courseproject.tools.DecodeImgTask;
import comp2601.carleton.edu.courseproject.tools.ImageHelper;
import comp2601.carleton.edu.courseproject.tools.InfoWindowAdapter;
import comp2601.carleton.edu.courseproject.tools.LocationHelper;
import comp2601.carleton.edu.courseproject.tools.MapClickListener;
import comp2601.carleton.edu.courseproject.tools.NoteDAO;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, Observer, MapClickDelegate {
    // constants
    private static final String TAG = "MainActivity";
    private static final int IMG_CAPTURE_REQUEST_CODE = 1;
    private static final double SHIFT_DISTANCE = 0.00025;
    public static final int ZOOM_LEVEL = 16;
    public static final String PICTURE_MARKER_CONSTANT = "PICTURE";
    public static final String INTENT_FILE_KEY = "IMAGE_PATH";
    public static final String INTENT_NOTE_KEY = "NOTE_MODEL";

    // UI
    private GoogleMap mMap;
    private FloatingActionsMenu floatingMenu;
    private FloatingActionButton noteButton;
    private FloatingActionButton cameraButton;
    private boolean noteFramgentIsOpen = false;
    private FrameLayout fragmentContainer;

    // Instance variables
    private boolean hasPermission;
    private ImageHelper imageHelper;
    private InfoWindowAdapter infoWindowAdapter;
    private MapClickListener mapClickListener;
    private NoteFragment currentNoteFragment;
    private boolean canUseCamera = false;
    private String currentImgPath = "";
    private ArrayList<String> filePathsFromExtDir;
    private ArrayList<Marker> markers;
    private LocationManager locationManager;
    private HashMap<Marker, Bitmap> markerImages;
    private HashMap<Marker, NoteModel> markerNoteModels;
    private ArrayList<NoteModel> notesFromDb;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        hasPermission = checkPermissions();
        if(hasPermission){
            Log.e(TAG, "doing unsafe work");
            doUnsafeWork();
        }
        markerImages = new HashMap<>();
        markerNoteModels = new HashMap<>();
        imageHelper = new ImageHelper(getApplicationContext(), locationHelper);
        infoWindowAdapter = new InfoWindowAdapter(getApplicationContext(), markerImages);

        // users will either write a note or take a picture from here
        floatingMenu = findViewById(R.id.fam);
        noteButton = findViewById(R.id.note_button);
        cameraButton = findViewById(R.id.camera_button);
        fragmentContainer = findViewById(R.id.fragment_conatiner);
        setFragmentContainerHeight();

        // button for making a note
        noteButton.setIcon(android.R.drawable.ic_menu_edit);
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Write a note!", Toast.LENGTH_SHORT).show();
                currentNoteFragment = new NoteFragment();
                getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_up,
                        R.animator.slide_down).replace(R.id.fragment_conatiner,
                        currentNoteFragment).commit();
                noteFramgentIsOpen = true;
            }
        });

        // button for taking a picture
        cameraButton.setIcon(android.R.drawable.ic_menu_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUseCamera) {
                    Toast.makeText(MainActivity.this, "Take a picture!", Toast.LENGTH_SHORT).show();
                    startCamera();
                } else {
                    Toast.makeText(MainActivity.this, "Camera disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Height for NoteFragment container
    private void setFragmentContainerHeight(){
        ViewGroup.LayoutParams params = fragmentContainer.getLayoutParams();
        params.height = getTargetHeight();
        fragmentContainer.setLayoutParams(params);
    }

    // Calculate height based on screen size
    private int getTargetHeight(){ // dynamically change the size of note fragment
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        Log.e(TAG, "X: " + size.x + " Y: " + size.y);
        double screenHeight = size.y;
        Double targetHeight = (2 * screenHeight) / 3; // 67% of screen height
        return targetHeight.intValue();
    }

    // Recalculate height on configuration change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, newConfig.orientation + ", " + Configuration.ORIENTATION_LANDSCAPE);
        setFragmentContainerHeight();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(locationManager != null && locationHelper.isRequesting()) {
            locationHelper.stopRequesting(locationManager);
        }
    }

    // Start camera activity
    private void startCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // make sure that the intent can be handled
        if(intent.resolveActivity(getPackageManager()) != null){
            // Create file for the img
            File imgFile = null;
            try{
                imgFile = imageHelper.createImgFile();
            } catch (IOException e){
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            // continue if file successfully created
            if(imgFile != null){
                currentImgPath = imgFile.getPath();
                Log.d(TAG, "PATH: " + currentImgPath);
                Uri imgUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider",
                        imgFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(intent, IMG_CAPTURE_REQUEST_CODE);
            } else {
                Toast.makeText(MainActivity.this, "Error! Please try again", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    // read images from external memory
    private void readImgsFromDir(){
        File[] allFiles;
        File dir = new File(Environment.getExternalStorageDirectory(), "Mapper Images");
        if(dir.isDirectory()){
            allFiles = dir.listFiles();
            if(allFiles == null){
                Log.e(TAG, "Files not there!");
                return;
            }
            for(File file : allFiles){
                if(filePathsFromExtDir == null){
                    filePathsFromExtDir = new ArrayList<>();
                }
                filePathsFromExtDir.add(file.getPath());
            }
            for(int i = 0; i < filePathsFromExtDir.size(); i++){
                DecodeImgTask decodeImgTask = new DecodeImgTask(getApplicationContext());
                decodeImgTask.subscribe(this);
                decodeImgTask.execute(filePathsFromExtDir.get(i));
            }
        }
    }

    // read the notes from the SQLite DB
    private void readNotesFromDB(){
        NoteDAO dao = new NoteDAO(getApplicationContext());
        try{
            dao.open();
            notesFromDb = (ArrayList< NoteModel>)dao.getAllNotes();
            dao.close();
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        if(notesFromDb == null){
            notesFromDb = new ArrayList<>();
        }
        for(int i = 0; i < notesFromDb.size(); i++){
            addNoteMarker(notesFromDb.get(i));
        }
        Log.e(TAG, notesFromDb.toString());
    }

    // add a picture marker to the map
    private void addPictureMarker(Bitmap img, HashMap<String, String> metadata, String path){
        if(mMap == null){
            return;
        }
        /* DEMO CODE FOR NOW */
        // Ottawa: 45.42, -75.70
        // Toronto: 43.65, -79.38
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

        if(markers == null){
            markers = new ArrayList<>();
        }

        // shiftDistance = 0.00025 IS A GOOD MARGIN
        for(int i = 0; i < markers.size(); i++){
            if(markers.get(i).getPosition().equals(latLng)){
                double lat = latLng.latitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                double lng = latLng.longitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                latLng = new LatLng(lat, lng);
            }
        }
        MarkerOptions marker = new MarkerOptions().position(latLng).title(PICTURE_MARKER_CONSTANT);
        marker.snippet(metadata.get(DecodeImgTask.TIMESTAMP));
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

        Marker newMarker = mMap.addMarker(marker);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
        markerImages.put(newMarker, img);
        infoWindowAdapter.updateMarkerImageMap(markerImages);
        mapClickListener.updatePictureMarker(newMarker, path);
        markers.add(newMarker);
    }

    // add a note marker to the map
    private void addNoteMarker(NoteModel noteModel){
        Log.d(TAG, "addNoteMarker");
        if(mMap == null){
           return;
        }
        LatLng latLng = new LatLng(noteModel.getLat(), noteModel.getLon());
        if(markers == null){
            markers = new ArrayList<>();
        }

        // shiftDistance = 0.00025 IS A GOOD MARGIN
        for(int i = 0; i < markers.size(); i++){
            if(markers.get(i).getPosition().equals(latLng)){
                double lat = latLng.latitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                double lng = latLng.longitude + (Math.random() * SHIFT_DISTANCE - SHIFT_DISTANCE);
                latLng = new LatLng(lat, lng);
            }
        }

        MarkerOptions marker = new MarkerOptions().position(latLng).title(noteModel.getTitle())
                .snippet(noteModel.getNote() + "\n" + noteModel.getTimestamp())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        Marker newMarker = mMap.addMarker(marker);
        markerNoteModels.put(newMarker, noteModel);
        markers.add(newMarker);
        mapClickListener.updateNoteMarker(newMarker, noteModel);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapClickListener = new MapClickListener(MainActivity.this, mMap);
        mapClickListener.setMapClickDelegate(this);
        mMap.setInfoWindowAdapter(infoWindowAdapter);
        mMap.setOnMapLongClickListener(mapClickListener);
        mMap.setOnInfoWindowClickListener(mapClickListener);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if(hasPermission) {
            readImgsFromDir(); // will create picture markers in update callback
            readNotesFromDB(); // will create note markers
        }
    }

    // If return from camera activity, add new marker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == IMG_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK){
            Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_LONG).show();
            imageHelper.geoTagPicture(currentImgPath);
            imageHelper.updateAndroidGallery(currentImgPath);

            // get metadata and pop up new marker
            DecodeImgTask decodeImgTask = new DecodeImgTask(getApplicationContext());
            decodeImgTask.subscribe(this);
            decodeImgTask.execute(currentImgPath);
        }
    }

    // Check if notefragment open, if so close it, otherwise, do default implementation
    @Override
    public void onBackPressed(){
        if(noteFramgentIsOpen && currentNoteFragment != null){
            getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_up,
                    R.animator.slide_down).remove(currentNoteFragment).commit();
            noteFramgentIsOpen = false;
        } else {
            super.onBackPressed();
        }
    }

    public void updateNote(NoteModel noteModel){
        if(notesFromDb == null){
            notesFromDb = new ArrayList<>();
        }
        notesFromDb.add(noteModel);
        addNoteMarker(noteModel);
    }

    // check and request all permissions required for this app
    private boolean checkPermissions(){
        ArrayList<String> permissions = new ArrayList<>();

        // camera permission
        if(checkSelfPermission(android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.CAMERA);
        } else {
            canUseCamera = true;
        }

        // write to external storage permission
        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //coarse location permission
        if(checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // fine location permission
        if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // internet permission
        if(checkSelfPermission(android.Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.INTERNET);
        }

        // record audio permission
        if(checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED){
            permissions.add(android.Manifest.permission.RECORD_AUDIO);
        }

        Log.e(TAG, "PERMISSIONS: " + permissions.toString());
        if(!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), 1);
            return false;
        } else {
            return true;
        }
    }

    // Result of asking for permissions, if any declined, close the app
    // (Maybe find friendlier way of doing this???)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        boolean didKillApp = false;

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == IMG_CAPTURE_REQUEST_CODE){
            for (String permission: permissions){
                if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "Closing app due to insufficient permissions.");
                    Toast.makeText(MainActivity.this, "These permissions are required for this" +
                            "app to work.", Toast.LENGTH_LONG).show();
                    didKillApp = true;
                    killApp();
                    finishAndRemoveTask();
                    break;
                }
            }
            if(!didKillApp){
                hasPermission = true;
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                locationHelper = new LocationHelper();
                locationHelper.startRequesting(locationManager);
            }
        }
    }

    // Close app
    private void killApp(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAndRemoveTask();
            }
        }, 1000);
    }

    @Override
    public void update(Bitmap bitmap, HashMap<String, String> metadata, String path){
        addPictureMarker(bitmap, metadata, path);
    }

    // location work
    private void doUnsafeWork(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationHelper = new LocationHelper();
        locationHelper.startRequesting(locationManager);
    }

    public LocationHelper getLocationHelper(){
        return locationHelper;
    }

    /* MapClickDelegate methods -- the click listener already removes markers
        from the map, DB, and memory
    */
    @Override
    public void removeNoteMarker(Marker marker) {
        markerNoteModels.remove(marker);
        markers.remove(marker);
    }

    @Override
    public void removePictureMarker(Marker marker) {
        markerImages.remove(marker);
        markers.remove(marker);
    }

    @Override
    public void clickedOnNoteWindow(NoteModel noteModel) {
        // start note activity
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(INTENT_NOTE_KEY, noteModel);
        startActivity(intent);
    }

    @Override
    public void clickedOnPictureWindow(String filePath) {
        // start image activity
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra(INTENT_FILE_KEY, filePath);
        startActivity(intent);
    }
}
