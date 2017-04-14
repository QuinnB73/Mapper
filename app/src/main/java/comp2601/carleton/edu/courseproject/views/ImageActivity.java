package comp2601.carleton.edu.courseproject.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import comp2601.carleton.edu.courseproject.Interfaces.Observer;
import comp2601.carleton.edu.courseproject.R;
import comp2601.carleton.edu.courseproject.tools.DecodeImgTask;
import comp2601.carleton.edu.courseproject.tools.ImageHelper;

public class ImageActivity extends AppCompatActivity implements Observer {
    private static final String TAG = "ImageActivity";
    private ImageView imgView;
    private TextView metadataTxt;
    private DecodeImgTask decodeImgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String imgPath = getIntent().getStringExtra(MainActivity.INTENT_FILE_KEY);
        imgView = (ImageView)findViewById(R.id.img_view);
        metadataTxt = (TextView)findViewById(R.id.metadata_view);

        decodeImgTask = new DecodeImgTask(getApplicationContext(), 760, 760);
        decodeImgTask.subscribe(this);
        decodeImgTask.execute(imgPath);
    }

    @Override // from Observer interface
    public void update(Bitmap bitmap, HashMap<String, String> metadata, String path){
        imgView.setImageBitmap(bitmap);
        metadataTxt.setText(parseMetadata(metadata));
    }

    private String parseMetadata(HashMap<String, String> metadata){
        String text = "";
        if(metadata.containsKey(DecodeImgTask.TIMESTAMP)){
            text += "Taken on: " + metadata.get(DecodeImgTask.TIMESTAMP);
        }
        if(metadata.containsKey(DecodeImgTask.LAT) && metadata.containsKey(DecodeImgTask.LON)){
            String latdms = metadata.get(DecodeImgTask.LAT);
            String londms = metadata.get(DecodeImgTask.LON);
            if(latdms.equals("NULL") && londms.equals("NULL")){ // picture not geotagged properly
                text += "\nAt coordinates: UNKNOWN, UNKNOWN";
                return text;
            }
            String latdir = metadata.get(DecodeImgTask.LAT_DIR);
            String londir = metadata.get(DecodeImgTask.LON_DIR);
            double lat = ImageHelper.DMStoDec(latdms, latdir);
            double lon = ImageHelper.DMStoDec(londms, londir);
            DecimalFormat format = new DecimalFormat("##.##");
            text += "\nAt coordinates: " + format.format(lat) + ", " + format.format(lon);
        }
        return text;
    }
}
