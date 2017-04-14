package comp2601.carleton.edu.courseproject.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import comp2601.carleton.edu.courseproject.R;

/**
 * Created by quinnbudan on 2017-04-04.
 */

// Custom info window for Google Maps
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    Context context;
    HashMap<Marker, Bitmap> markerImages;

    public InfoWindowAdapter(Context context, HashMap<Marker, Bitmap> markerImages){
        this.context = context;
        this.markerImages = markerImages;
    }

    @Override
    public View getInfoWindow(Marker marker){
        return null; // use default info window view
    }

    @Override
    public View getInfoContents(Marker marker){
        View v = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);

        String snippet = marker.getSnippet();
        String title = marker.getTitle();

        TextView markerTitle = (TextView)v.findViewById(R.id.marker_title);
        TextView markerText = (TextView)v.findViewById(R.id.marker_text);
        ImageView markerImage = (ImageView)v.findViewById(R.id.marker_image);

        markerTitle.setText(title);
        markerText.setText(snippet);
        if(markerImages != null && markerImages.containsKey(marker)){
            markerImage.setImageBitmap(markerImages.get(marker));
        }

        return v;
    }

    // should be called every time an image is read from a file and a marker is created
    public void updateMarkerImageMap(HashMap<Marker, Bitmap> markerImages){
        this.markerImages = markerImages;
    }
}
