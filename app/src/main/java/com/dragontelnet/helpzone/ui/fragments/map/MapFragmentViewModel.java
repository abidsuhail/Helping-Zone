package com.dragontelnet.helpzone.ui.fragments.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.repository.MyLiveLocationListener;
import com.dragontelnet.helpzone.repository.MyStaticLastLocation;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import javax.inject.Inject;


public class MapFragmentViewModel extends ViewModel {

/*    @Inject
    public MapFragmentRepository mapRepository;*/

    private static final String TAG = "MapFragmentViewModel";
    @Inject
    public MyLiveLocationListener locationRepository;
    @Inject
    public MyStaticLastLocation staticLocationRepository;

    public MapFragmentViewModel() {

        MyDaggerInjection.getRepoComponent().inject(this);
    }

    public LiveData<HashMap<String, GeoLocation>> getLocationsHashMap() {
        return locationRepository.getLiveLocationHashMap();
    }

    public LiveData<LatLng> getStaticLastLocationLiveData() {
        return staticLocationRepository.getStaticLastLocation();
    }
}