package com.dragontelnet.helpzone.repository.activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.MyConstants;
import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivityRepository {

    private MutableLiveData<String> peoplesCountMutable = new MutableLiveData<>();

    public MainActivityRepository() {

    }

    public MutableLiveData<String> getNearbyPeoplesCount() {
        DatabaseReference ref = FirebaseRefs.getRootRef()
                .child(MyConstants.USERS_LOC_NODE)
                .child(CurrentFuser.getCurrentFuser().getUid())
                .child("peoples_count_around");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String peoplesCount = dataSnapshot.child("no_of_peoples").getValue().toString();
                    peoplesCountMutable.setValue(peoplesCount);
                } else {
                    peoplesCountMutable.setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return peoplesCountMutable;
    }

    public void setNearbyPeoplesCount(int peoplesCount) {
        DatabaseReference ref = FirebaseRefs.getRootRef()
                .child(MyConstants.USERS_LOC_NODE)
                .child(CurrentFuser.getCurrentFuser().getUid())
                .child("peoples_count_around");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("no_of_peoples", peoplesCount);
                ref.updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
