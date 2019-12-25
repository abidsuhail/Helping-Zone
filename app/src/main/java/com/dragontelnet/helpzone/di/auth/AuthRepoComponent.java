package com.dragontelnet.helpzone.di.auth;

import com.dragontelnet.helpzone.di.repository.UserFromDbModule;
import com.dragontelnet.helpzone.ui.activity.auth.AuthActivityViewModel;
import com.dragontelnet.helpzone.ui.activity.registration.RegistrationDetailsActivityViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AuthRepoModule.class, UserFromDbModule.class})
public interface AuthRepoComponent {

    void inject(AuthActivityViewModel authActivityViewModel);

    void inject(RegistrationDetailsActivityViewModel registrationDetailsActivityViewModel);

}
