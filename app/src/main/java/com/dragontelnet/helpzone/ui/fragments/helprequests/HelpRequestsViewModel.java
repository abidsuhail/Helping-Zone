package com.dragontelnet.helpzone.ui.fragments.helprequests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.repository.fragment.HelpRequestsRepository;

public class HelpRequestsViewModel extends ViewModel {

    private HelpRequestsRepository repository;

    public HelpRequestsViewModel() {
        repository = new HelpRequestsRepository();
    }

    public LiveData<Boolean> isRequestsExists() {
        return repository.isRequestsExists();
    }
}
