package com.dragontelnet.helpzone.di;

import android.content.Context;

import com.dragontelnet.helpzone.di.auth.AuthRepoComponent;
import com.dragontelnet.helpzone.di.auth.AuthRepoModule;
import com.dragontelnet.helpzone.di.auth.DaggerAuthRepoComponent;
import com.dragontelnet.helpzone.di.fragments.DaggerFragmentsComponent;
import com.dragontelnet.helpzone.di.fragments.FragmentsComponent;
import com.dragontelnet.helpzone.di.repository.DaggerReposComponent;
import com.dragontelnet.helpzone.di.repository.LocationModule;
import com.dragontelnet.helpzone.di.repository.ReposComponent;
import com.dragontelnet.helpzone.di.repository.ReposModule;
import com.dragontelnet.helpzone.ui.activity.auth.AuthActivity;

public class MyDaggerInjection {
    private static ReposComponent reposComponent;
    private static AuthRepoComponent authComponent;
    private static FragmentsComponent fragmentsComponent;

    public static void setFragmentsComponent() {
        if (fragmentsComponent == null) {
            fragmentsComponent = DaggerFragmentsComponent.create();
        }
    }

    public static AuthRepoComponent getAuthRepoComponent() {
        return authComponent;
    }

    public static void setAuthRepoComponent(AuthActivity authActivity) {
        if (authComponent == null) {
            authComponent = DaggerAuthRepoComponent
                    .builder()
                    .authRepoModule(new AuthRepoModule(authActivity))
                    .build();
        }
    }

    public static FragmentsComponent getFragmentsComponent() {
        return fragmentsComponent;
    }

    public static ReposComponent getRepoComponent() {
        return reposComponent;
    }

    public static void setRepoComponent(Context context) {
        if (reposComponent == null) {
            reposComponent = DaggerReposComponent
                    .builder()
                    .reposModule(new ReposModule(context))
                    .locationModule(new LocationModule(context))
                    .build();
        }
    }
}
