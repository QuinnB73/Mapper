package comp2601.carleton.edu.courseproject.models;

import android.support.annotation.NonNull;

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

    @NonNull
    @Override
    public String toString(){
        return title + "\n" + note + "\n" + timestamp + "\n" + lat + "\n" + lon;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this) {
            return true;
        }

        if(obj instanceof NoteModel){
            NoteModel compNote = (NoteModel)obj;
            return this.id == compNote.getId() &&
                    this.title.equals(getTitle()) &&
                    this.note.equals(compNote.getNote()) &&
                    this.timestamp.equals(getTimestamp()) &&
                    this.lat == compNote.getLat() &&
                    this.lon == compNote.getLon();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash += 73 * hash + id;
        hash += 73 * hash + title.hashCode();
        hash += 73 * hash + note.hashCode();
        hash += 73 * hash + timestamp.hashCode();
        hash += 73 * hash + lat;
        hash += 73 * hash + lon;
        return hash;
    }
}
