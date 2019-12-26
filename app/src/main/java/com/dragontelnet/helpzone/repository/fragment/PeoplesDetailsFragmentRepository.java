package com.dragontelnet.helpzone.repository.fragment;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Singleton;

@Singleton
public class PeoplesDetailsFragmentRepository {

    private static final String TAG = "PeoplesDetailsFragmentR";
    private Context context;
    private MutableLiveData<Boolean> peoplesExists = new MutableLiveData<>();

    public PeoplesDetailsFragmentRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<Boolean> isPeoplesNearbyExists() {
        if (CurrentFuser.getCurrentFuser() != null) {
            DatabaseReference ref = FirebaseRefs
                    .getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid());

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        peoplesExists.setValue(true);
                    } else {
                        peoplesExists.setValue(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return peoplesExists;
    }
}
