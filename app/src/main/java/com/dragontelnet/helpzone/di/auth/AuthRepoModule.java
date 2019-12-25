package com.dragontelnet.helpzone.di.auth;

import android.app.Activity;

import com.dragontelnet.helpzone.repository.activity.AuthActivityRepository;
import com.dragontelnet.helpzone.repository.activity.RegistrationDetailsActivityRepository;
import com.dragontelnet.helpzone.ui.activity.auth.AuthActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AuthRepoModule {

    private Activity activity;

    public AuthRepoModule(AuthActivity authActivity) {
        this.activity = authActivity;
    }

    @Singleton
    @Provides
    AuthActivityRepository providesAuthActivityRepository() {
        return new AuthActivityRepository(activity);
    }

    @Singleton
    @Provides
    RegistrationDetailsActivityRepository providesRegistrationDetailsActivityRepo() {
        return new RegistrationDetailsActivityRepository();
    }

}
