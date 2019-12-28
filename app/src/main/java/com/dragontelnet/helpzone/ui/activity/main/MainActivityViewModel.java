package com.dragontelnet.helpzone.ui.activity.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.model.User;
import com.dragontelnet.helpzone.repository.UserDetailsFetcher;
import com.dragontelnet.helpzone.repository.activity.MainActivityRepository;

import javax.inject.Inject;

public class MainActivityViewModel extends ViewModel {
    @Inject
    public MainActivityRepository repository;

    @Inject
    public UserDetailsFetcher userDetailsRepo;

    public MainActivityViewModel() {
        MyDaggerInjection.getRepoComponent().inject(this);
    }

    public LiveData<User> getUserDetails(String uid) {
        //return repository.getUserDetailsMutable();

        return userDetailsRepo.getUserFromDb(uid);
    }

    public void setNearbyPeoplesCount(int peoplesCount) {
        repository.setNearbyPeoplesCount(peoplesCount);
    }

    public LiveData<Boolean> isRemoveLocSuccessful() {
        return repository.isLocRemovedSuccessfull();
    }
}
