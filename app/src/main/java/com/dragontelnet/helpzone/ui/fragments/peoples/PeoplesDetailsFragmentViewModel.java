package com.dragontelnet.helpzone.ui.fragments.peoples;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.repository.fragment.PeoplesDetailsFragmentRepository;

import javax.inject.Inject;

public class PeoplesDetailsFragmentViewModel extends ViewModel {

    @Inject
    PeoplesDetailsFragmentRepository repository;

    public PeoplesDetailsFragmentViewModel() {
        MyDaggerInjection.getRepoComponent().inject(this);

    }

   /* public LiveData<User> getUserNameByUidLiveData(String uid)
    {
        return repository.getUserNameByUidMutable(uid);
    }*/

    public LiveData<Boolean> isPeoplesNearbyExists() {
        return repository.isPeoplesNearbyExists();
    }

    /*public void sendFCMToDevice(String peopleUid)
    {
        repository.sendFCMToDevice(peopleUid);
    }*/

}
