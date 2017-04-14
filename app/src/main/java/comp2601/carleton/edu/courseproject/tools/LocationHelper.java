package comp2601.carleton.edu.courseproject.tools;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by quinnbudan on 2017-04-04.
 */

// Requests location information
public class LocationHelper implements LocationListener {
    Location currentLocation = null;
    boolean  isRequesting = false;

    public void startRequesting(LocationManager locationManager){
        String provider = locationManager.getBestProvider(new Criteria(), false);
        isRequesting = true;

        // the errors here are about permissions, the app would never make it this far
        // without the appropriate permissions
        locationManager.requestLocationUpdates(provider, 20000, 1, this);
        currentLocation = locationManager.getLastKnownLocation(provider);

    }

    public void stopRequesting(LocationManager locationManager){
        isRequesting = false;

        // the error here is about permissions, the app would never make it this far
        // without the appropriate permissions
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public Location getCurrentLocation(){
        return currentLocation;
    }

    public boolean isRequesting(){
        return isRequesting;
    }
}
