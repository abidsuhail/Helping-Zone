package com.dragontelnet.helpzone.di.repository;

import com.dragontelnet.helpzone.repository.UserDetailsFetcher;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UserFromDbModule {

    @Singleton
    @Provides
    UserDetailsFetcher providesFetchUserDetails() {
        return new UserDetailsFetcher();
    }
}
