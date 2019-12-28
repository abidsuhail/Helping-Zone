package com.dragontelnet.helpzone.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;


public class MyBackgroundServiceViewModel extends ViewModel {


    public LiveData<Boolean> getServiceStatus() {
        return MyBackgroundService.getServiceStatus();
    }

}
