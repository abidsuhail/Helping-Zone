package com.dragontelnet.helpzone.ui.fragments.trustedpeoples;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.model.TrustedContact;
import com.dragontelnet.helpzone.repository.fragment.TrustedPeoplesRepository;

public class TrustedPeoplesViewModel extends ViewModel {

    TrustedPeoplesRepository repository;

    public TrustedPeoplesViewModel() {

        repository = new TrustedPeoplesRepository();

    }

    public LiveData<Boolean> setTrustedContactsToDb(String phtv1Str, String phtv2Str, String phtv3Str, String phtv4Str) {
        return repository.setTrustedContactsToDb(phtv1Str, phtv2Str, phtv3Str, phtv4Str);
    }

    public LiveData<TrustedContact> getTrustedContactsLiveData() {
        return repository.getTrustedContactsMutable();
    }
}
