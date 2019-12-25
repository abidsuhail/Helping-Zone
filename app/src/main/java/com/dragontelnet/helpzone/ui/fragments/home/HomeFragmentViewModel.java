package com.dragontelnet.helpzone.ui.fragments.home;

import android.app.Activity;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.model.TrustedContact;
import com.dragontelnet.helpzone.repository.MyLiveLocationListener;
import com.dragontelnet.helpzone.repository.MyStaticLastLocation;
import com.dragontelnet.helpzone.repository.fragment.HomeFragmentRepository;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class HomeFragmentViewModel extends ViewModel {

    @Inject
    public HomeFragmentRepository homeRepository;

    @Inject
    public MyLiveLocationListener myLiveLocationRepo;

    @Inject
    public MyStaticLastLocation myStaticLocationRepo;

    public HomeFragmentViewModel() {
        //MyDaggerInjection.setRepoComponent().inject(this);
        MyDaggerInjection.getRepoComponent().inject(this);
    }

    public LiveData<LatLng> getAndSetStaticLastLocationLiveData() {
        return myStaticLocationRepo.getStaticLastLocation();
    }

    public LiveData<Location> getLiveLocationLiveData() {
        return myLiveLocationRepo.getLiveLocation();
    }

    public LiveData<Boolean> setUserLocDetailsToDbLiveData(Map.Entry<String, GeoLocation> entry, Location myLoc) {
        return homeRepository.setUserLocDetailsToDb(entry, myLoc);
    }

    public LiveData<TrustedContact> getTrustedContactsLiveData() {
        return homeRepository.getTrustedContactsMutable();
    }

    public void setTriggerListener() {
        homeRepository.triggersListener();
    }

    public void removeTriggerListener() {
        homeRepository.removeTriggerListener();
    }

    public void sendFCMToAllNearbyDevices(boolean isAudio, String audioLink) {
        homeRepository.sendFCMToAllNearbyDevices(isAudio, audioLink);
    }

    public void startRecordingAudio(Activity activity) {
        homeRepository.startRecordingAudio(activity);
    }

    public void stopRecordingAudio(Activity activity, int peoplesAround) {
        homeRepository.stopRecordingAudio(activity, peoplesAround);
    }

    public LiveData<HashMap<String, GeoLocation>> getLocationsHashMapLiveData() {
        return myLiveLocationRepo.getLiveLocationHashMap();
    }

}