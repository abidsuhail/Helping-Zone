package com.dragontelnet.helpzone.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Singleton;

@Singleton
public class MyStaticLastLocation {
    private Context context;
    private MutableLiveData<LatLng> locationMutableLiveData = new MutableLiveData<>();

    public MyStaticLastLocation(Context context) {
        this.context = context;
    }

    public MutableLiveData<LatLng> getStaticLastLocation() {

        final GeoFire geoFire = new GeoFire(FirebaseRefs.getAllUsersLocNodeRef());
        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                        geoFire.setLocation(CurrentFuser.getCurrentFuser().getUid(), geoLocation);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        locationMutableLiveData.setValue(latLng);
                    }

                });
        return locationMutableLiveData;
    }
}
