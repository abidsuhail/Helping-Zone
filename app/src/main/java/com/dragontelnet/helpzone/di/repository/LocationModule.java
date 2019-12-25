package com.dragontelnet.helpzone.di.repository;


import android.content.Context;

import com.dragontelnet.helpzone.repository.MyLiveLocationListener;
import com.dragontelnet.helpzone.repository.MyStaticLastLocation;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LocationModule {

    Context context;

    public LocationModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    MyLiveLocationListener providesMyLiveLocationListener() {
        return new MyLiveLocationListener(context);
    }

    @Singleton
    @Provides
    MyStaticLastLocation providesMyStaticLastLocation() {
        return new MyStaticLastLocation(context);
    }

}
