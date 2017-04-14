package comp2601.carleton.edu.courseproject.models;

import java.io.Serializable;

/**
 * Created by quinnbudan on 2017-03-29.
 */

public class NoteModel implements Serializable {
    private long id;
    private String title;
    private String note;
    private String timestamp;
    private double lat;
    private double lon;

    public NoteModel(){
        // do nothing
    }

    public NoteModel(String title, String note, String timestamp, double lat, double lon){
        this.title = title;
        this.note = note;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getNote(){
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }

    public String getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(String timestamp){
        this.timestamp = timestamp;
    }

    public double getLat(){
        return lat;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon){
        this.lon = lon;
    }

    @Override
    public String toString(){
        return title + "\n" + note + "\n" + timestamp + "\n" + lat + "\n" + lon;
    }
}
