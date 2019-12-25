package com.dragontelnet.helpzone.ui.activity.auth;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.repository.activity.AuthActivityRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;

import javax.inject.Inject;


public class AuthActivityViewModel extends ViewModel {

    private static final String TAG = "AuthActivityViewModel";
    @Inject
    public AuthActivityRepository authRepository;

    public AuthActivityViewModel() {
        MyDaggerInjection.getAuthRepoComponent().inject(this);
    }

    public LiveData<FirebaseUser> getLoggedInUserLiveData(String phoneNum) {
        return authRepository.getLoggedInUserMutable(phoneNum);
    }

    public LiveData<String> getVerificationIdLiveData() {
        return authRepository.getVerificationIdMutable();
    }

    public LiveData<FirebaseUser> getUserByCredentialLiveData(PhoneAuthCredential credential) {
        return authRepository.getUserByCredentialMutable(credential);
    }

    public LiveData<Boolean> isUserExistsLiveData(String uid) {
        return authRepository.isUserExistsMutable(uid);
    }

    public LiveData<String> setDeviceTokenToDbLiveData() {
        return authRepository.setDeviceTokenToDb(); //its sets token to db and returns the token

    }
}
