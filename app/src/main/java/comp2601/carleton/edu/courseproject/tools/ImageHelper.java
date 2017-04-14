package comp2601.carleton.edu.courseproject.tools;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by quinnbudan on 2017-04-04.
 */

public class ImageHelper {
    private static final String TAG = "ImageHelper";
    private Context context;
    private LocationHelper locationHelper;

    public ImageHelper(Context context){
        this.context = context;
    }

    public ImageHelper(Context context, LocationHelper locationHelper){
        this.context = context;
        this.locationHelper = locationHelper;
    }

    // creates a file in the external storage directory (external memory)
    @Nullable
    public File createImgFile() throws IOException {
        String dirName = "Mapper Images";
        File customDir = new File(Environment.getExternalStorageDirectory(), dirName);
        if(!customDir.exists()){
            boolean success = customDir.mkdirs();
            if(!success){
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgFileName = "JPEG_" + timeStamp + "_";
        File imgFile = File.createTempFile(imgFileName, ".jpg", customDir);
        return imgFile;
    }

    // adds to the public android gallery so all apps can access it
    public void updateAndroidGallery(String path){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri currentUri = Uri.fromFile(f);
        Log.d(TAG, "URI: " + currentUri.toString());
        mediaScanIntent.setData(currentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    // add location exif data
    public void geoTagPicture(String path){
        if(locationHelper == null){
            Log.e("ImageHelper", "geotag failed.");
            return;
        }
        Location currentLocation = locationHelper.getCurrentLocation();
        if(currentLocation == null){ // should not happen often
            Log.e(TAG, "Could not geotag picture");
            return;
        }
        Log.e(TAG, currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
        try {
            ExifInterface exifInterface = new ExifInterface(path);

            // convert latitude to DMS, tag it; tag N/S
            if(currentLocation.getLatitude() < 0){
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            } else{
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            }
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
                    decToDMS(currentLocation.getLatitude()));

            // convert longitude to DMS, tag it; tag W/E
            if (currentLocation.getLongitude() < 0){
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            } else {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            }
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
                    decToDMS(currentLocation.getLongitude()));
            exifInterface.saveAttributes();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    // decimal latitude/longitude to DecimalMinutesSeconds latitude/longitude
    public static String decToDMS(double val){
        if (val < 0){
            val = val * -1;
        }
        Double degrees = Math.floor(val);
        Double minutes = Math.floor(60 * (val - degrees));
        Double seconds = 3600 * (val - degrees) - 60 * minutes;

        String dmsStr =  degrees.intValue() + "/1," + minutes.intValue() + "/1," + seconds.intValue() + "/1";
        Log.e(TAG, dmsStr);
        return dmsStr;
    }

    // DecimalMinutesSeconds latitude/longitude to decimal latitude/longitude
    public static Double DMStoDec(String dms, String dir){
        // ASSUMES DMS NOT DM, BUT ALL PHOTOS TAKEN BY THIS APP ARE DMS
        String[] parts = dms.split("/1,");
        Double deg = 0.0;
        Double min = 0.0;
        Double sec = 0.0;

        if (parts.length == 3) {
            deg = Double.parseDouble(parts[0]);
            min = Double.parseDouble(parts[1]);
            parts[2] = parts[2].substring(0, parts[2].indexOf("/"));
            sec = Double.parseDouble(parts[2]);
        }
        Double val = deg + (min / 60) + (sec / 3600);
        if(dir.equals("S") || dir.equals("W")){
            val = val * -1;
        }
        return val;
    }
}
