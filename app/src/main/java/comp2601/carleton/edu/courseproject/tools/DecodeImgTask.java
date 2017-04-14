package comp2601.carleton.edu.courseproject.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

import comp2601.carleton.edu.courseproject.Interfaces.Observable;
import comp2601.carleton.edu.courseproject.Interfaces.Observer;

/**
 * Created by quinnbudan on 2017-03-20.
 */

// Decodes images from a file, returns a bitmap, also gets exif data
public class DecodeImgTask extends AsyncTask <String, Void, Bitmap> implements Observable{
    private ArrayList<Observer> observers;
    private HashMap<String, String> metadata;
    private Context context;
    private String path;
    private int bmpH;
    private int bmpW;

    public static String TIMESTAMP = ExifInterface.TAG_DATETIME;
    public static String TITLE = ExifInterface.TAG_IMAGE_DESCRIPTION;
    public static String LAT = ExifInterface.TAG_GPS_LATITUDE;
    public static String LON = ExifInterface.TAG_GPS_LONGITUDE;
    public static String LAT_DIR = ExifInterface.TAG_GPS_LATITUDE_REF;
    public static String LON_DIR = ExifInterface.TAG_GPS_LONGITUDE_REF;

    public DecodeImgTask(Context ctx){
        context = ctx;
        observers = new ArrayList<>();
        bmpH = 100;
        bmpW = 100;
    }

    // specify width and height of the bitmap
    public DecodeImgTask(Context ctx, int bmpH, int bmpW){
        context = ctx;
        observers = new ArrayList<>();
        this.bmpH = bmpH;
        this.bmpW = bmpW;
    }

    // background task -- decoding the image
    @Override
    protected Bitmap doInBackground(String... params){
        metadata = new HashMap<>();
        try {
            ExifInterface exifInterface = new ExifInterface(params[0]);
            path = params[0];
            String exactTimeStamp = exifInterface.getAttribute(TIMESTAMP);
            String timeStamp = parseTimeStamp(exactTimeStamp);
            String title = exifInterface.getAttribute(TITLE);
            String lat = exifInterface.getAttribute(LAT);
            String lon = exifInterface.getAttribute(LON);
            String latDir = exifInterface.getAttribute(LAT_DIR);
            String lonDir = exifInterface.getAttribute(LON_DIR);
            Log.d("DECODETASK", "LON: " + lon + " LAT: " + lat);
            metadata.put(TITLE, title != null ? title : "NULL");
            metadata.put(TIMESTAMP, timeStamp != null ? timeStamp : "NULL");
            metadata.put(LAT, lat != null ? lat : "NULL");
            metadata.put(LON, lon != null ? lon: "NULL");
            metadata.put(LAT_DIR, latDir != null ? latDir : "NULL");
            metadata.put(LON_DIR, lonDir != null ? lonDir : "NULL");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                Bitmap bitmap = Glide.with(context).load(params[0]).asBitmap().into(bmpH, bmpW).get();
                return  bitmap;
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
        notifyObservers(bitmap, metadata, path);
    }

    @Override // from Observable interface
    public void subscribe(Observer observer){
        observers.add(observer);
    }

    @Override // from Observable interface
    public void notifyObservers(Bitmap bitmap, HashMap<String, String> metadata, String path){
        for(Observer observer : observers){
            observer.update(bitmap, metadata, path);
        }
    }

    // parses the exif timestamp
    private String parseTimeStamp(String parse){
        if(parse == null){
            return "ERROR";
        }
        String year = parse.substring(0, parse.indexOf(":"));
        String monthNum = parse.substring(parse.indexOf(year) + 5,
                parse.indexOf(":", parse.indexOf(year) + 5));
        String day = parse.substring(parse.indexOf(monthNum) + 3,
                parse.indexOf(" ", parse.indexOf(monthNum) + 3));
        String time = parse.substring(parse.indexOf(" "), parse.length());
        String month = parseMonthNum(monthNum);

        String str = day + " " + month + ", " + year + " -- " + time;
        return str;
    }

    private String parseMonthNum(String monthNum){
        try {
            int num = Integer.parseInt(monthNum);
            switch (num){
                case 1: return "January";
                case 2: return "February";
                case 3: return "March";
                case 4: return "April";
                case 5: return "May";
                case 6: return "June";
                case 7: return "July";
                case 8: return "August";
                case 9: return "September";
                case 10: return "October";
                case 11: return "November";
                case 12: return "December";
                default: return "ERROR";
            }
        } catch (Exception e){
            e.printStackTrace();
            return "ERROR";
        }
    }
}
