package comp2601.carleton.edu.courseproject.Interfaces;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quinnbudan on 2017-03-20.
 */

public interface Observer {
    void update(Bitmap bmp, HashMap<String, String> metadata, String path);
}
