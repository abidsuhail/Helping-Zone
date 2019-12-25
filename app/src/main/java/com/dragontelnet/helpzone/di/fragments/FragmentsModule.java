package com.dragontelnet.helpzone.di.fragments;

import com.dragontelnet.helpzone.ui.fragments.helprequests.HelpRequestsFragment;
import com.dragontelnet.helpzone.ui.fragments.home.HomeFragment;
import com.dragontelnet.helpzone.ui.fragments.map.MapsFragment;
import com.dragontelnet.helpzone.ui.fragments.peoples.PeoplesDetailsFragment;
import com.dragontelnet.helpzone.ui.fragments.trustedpeoples.TrustedPeoplesFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentsModule {

    @Singleton
    @Provides
    HomeFragment providesHomeFragment() {
        return new HomeFragment();
    }

    @Singleton
    @Provides
    MapsFragment providesMapFragment() {
        return new MapsFragment();
    }

    @Singleton
    @Provides
    PeoplesDetailsFragment providesPeoplesDetailsFragment() {
        return new PeoplesDetailsFragment();
    }

    @Singleton
    @Provides
    TrustedPeoplesFragment providesTrustedPeoplesFragment() {
        return new TrustedPeoplesFragment();
    }

    @Singleton
    @Provides
    HelpRequestsFragment providesHelpRequestsFragment() {
        return new HelpRequestsFragment();
    }

}
