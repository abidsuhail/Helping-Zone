package com.dragontelnet.helpzone.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.Observable;

import javax.inject.Singleton;

@Singleton
public class MyLiveLocationListener extends Observable implements LocationListener {
    private Context context;
    private GeoQuery geoQuery;
    //private GeoQueryEventListener geoQueryEventListener;
    private boolean isIn = false;
    private boolean isInOnGeoQueryReady = false;
    private boolean isInGeoQuery = false;
    private boolean isInRequestLocationUpdates = false;
    private MutableLiveData<HashMap<String, GeoLocation>> usersKeyMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Location> liveLocationMutable = new MutableLiveData<>();

    private static final String TAG = "MyLiveLocationListener";

    public MyLiveLocationListener(Context context) {
        this.context = context;
    }


    @Override
    public void onLocationChanged(Location location) {

        if (CurrentFuser.getCurrentFuser() != null) {
            //for storing uid and location
            final HashMap<String, GeoLocation> locationsHashMap = new HashMap<>();

            final GeoFire allLocsGeoFire = new GeoFire(FirebaseRefs.getAllUsersLocNodeRef());
            liveLocationMutable.setValue(location);//setting live location for observer

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            GeoLocation myGeoLocation = new GeoLocation(latitude, longitude);

            allLocsGeoFire.setLocation(CurrentFuser.getCurrentFuser().getUid(), myGeoLocation);
            if (!isIn) {
                //query only one time initially the changes its center
                geoQuery = allLocsGeoFire.queryAtLocation(myGeoLocation, 0.5); //0.5km=500meter
                isIn = true;
            }
            geoQuery.setCenter(myGeoLocation); //updating center

            GeoQueryEventListener geoQueryEventListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation peopleLoc) {
                    //updating hashmap i.e add new key value pairs(uid,geoloc)
                    //observing in HomeFragment and MapsFragment
                    locationsHashMap.put(key, peopleLoc);
                    usersKeyMutableLiveData.setValue(locationsHashMap);
                }

                @Override
                public void onKeyExited(String key) {
                    //updating hashmap i.e removing previous key value pairs(uid,geoloc)
                    //observing in HomeFragment and MapsFragment

                    //removing uid from db on exit
                    FirebaseRefs.getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                            .child(key).removeValue();
                    locationsHashMap.remove(key);
                    usersKeyMutableLiveData.setValue(locationsHashMap);
                }

                @Override
                public void onKeyMoved(String key, GeoLocation peopleLoc) {
                    //updating hashmap i.e updating/replacing previous key value pairs(uid,geoloc)
                    //observing in HomeFragment and MapsFragment
                    locationsHashMap.put(key, peopleLoc);
                    usersKeyMutableLiveData.setValue(locationsHashMap);
                }

                @Override
                public void onGeoQueryReady() {
                    if (!isInOnGeoQueryReady) {
                                    /* Initially removing previous NEARBY_USERS_LOC_DETAILS_NODE
                                    and adding new entered users*/
                        FirebaseRefs.getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                                .removeValue();
                        isInOnGeoQueryReady = true;
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                }
            };
            if (!isInGeoQuery) {
                geoQuery.addGeoQueryEventListener(geoQueryEventListener);
                isInGeoQuery = true;
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @SuppressLint("MissingPermission")
    public MutableLiveData<HashMap<String, GeoLocation>> getLiveLocationHashMap() {
        int TIME_INTERVAL = 5000; //10sec
        int DISTANCE_INTERVAL = 1; //1meter
        if (!isInRequestLocationUpdates) {
            //executed only one time
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    TIME_INTERVAL,
                    DISTANCE_INTERVAL,
                    this);
            locationManager.
                    requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            TIME_INTERVAL,
                            DISTANCE_INTERVAL,
                            this);
            isInRequestLocationUpdates = true;

            //change duration to 10sec
        }
        return usersKeyMutableLiveData;
    }


    public MutableLiveData<Location> getLiveLocation() {
        return liveLocationMutable;
    }

  /*  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onStop()
    {

        Log.d(TAG, "onStop: in");
    }*/
}
