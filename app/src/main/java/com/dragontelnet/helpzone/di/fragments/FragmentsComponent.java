package com.dragontelnet.helpzone.di.fragments;


import com.dragontelnet.helpzone.ui.activity.main.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = FragmentsModule.class)
public interface FragmentsComponent {

    void inject(MainActivity mainActivity);
}
