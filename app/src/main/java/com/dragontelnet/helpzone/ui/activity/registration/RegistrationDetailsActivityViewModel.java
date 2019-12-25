package com.dragontelnet.helpzone.ui.activity.registration;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.model.User;
import com.dragontelnet.helpzone.repository.UserDetailsFetcher;
import com.dragontelnet.helpzone.repository.activity.AuthActivityRepository;
import com.dragontelnet.helpzone.repository.activity.RegistrationDetailsActivityRepository;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;


public class RegistrationDetailsActivityViewModel extends ViewModel {

    @Inject
    public RegistrationDetailsActivityRepository repository;

    @Inject
    public AuthActivityRepository authRepository;

    @Inject
    public UserDetailsFetcher userDetailsRepo;

    public RegistrationDetailsActivityViewModel() {
        MyDaggerInjection.getAuthRepoComponent().inject(this);

    }

    public LiveData<Boolean> writeUserToLiveData(FirebaseUser firebaseUser, String userName, String imageUrl) {
        return repository.writeUserToDbMutable(firebaseUser, userName, imageUrl);
    }

    public LiveData<String> getImageUrlLiveData(Uri localImageUri) {
        return repository.getProfilePicUrlMutable(localImageUri);
    }

    public LiveData<String> setDeviceTokenToDbLiveData() {
        //used for sending FCM
        return authRepository.setDeviceTokenToDb(); //its sets token to db and returns the token
    }

    public LiveData<User> getUserDetails(String uid) {
        return userDetailsRepo.getUserFromDb(uid);
    }
}
