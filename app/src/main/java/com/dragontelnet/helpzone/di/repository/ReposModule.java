package com.dragontelnet.helpzone.di.repository;

import android.content.Context;

import com.dragontelnet.helpzone.repository.activity.MainActivityRepository;
import com.dragontelnet.helpzone.repository.fragment.HomeFragmentRepository;
import com.dragontelnet.helpzone.repository.fragment.PeoplesDetailsFragmentRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ReposModule {
    Context context;

    //this is application context
    public ReposModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    HomeFragmentRepository providesHomeRepository() {
        return new HomeFragmentRepository(context);
    }

    @Singleton
    @Provides
    PeoplesDetailsFragmentRepository providesPeoplesDetailsRepository() {
        return new PeoplesDetailsFragmentRepository(context);
    }

    @Singleton
    @Provides
    MainActivityRepository providesMainActivityRepo() {
        return new MainActivityRepository();
    }

}
