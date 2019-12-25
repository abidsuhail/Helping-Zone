package com.dragontelnet.helpzone.di.repository;

import com.dragontelnet.helpzone.service.MyService;
import com.dragontelnet.helpzone.ui.activity.main.MainActivityViewModel;
import com.dragontelnet.helpzone.ui.fragments.home.HomeFragmentViewModel;
import com.dragontelnet.helpzone.ui.fragments.map.MapFragmentViewModel;
import com.dragontelnet.helpzone.ui.fragments.peoples.PeoplesDetailsFragmentViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ReposModule.class, LocationModule.class, UserFromDbModule.class})
public interface ReposComponent {

    void inject(MapFragmentViewModel mapFragmentViewModel);

    void inject(HomeFragmentViewModel homeFragmentViewModel);

    void inject(PeoplesDetailsFragmentViewModel peoplesDetailsFragmentViewModel);

    void inject(MyService myService);

    void inject(MainActivityViewModel mainActivityViewModel);
}
