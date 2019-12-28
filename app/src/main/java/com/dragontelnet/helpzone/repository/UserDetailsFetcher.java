package com.dragontelnet.helpzone.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UserDetailsFetcher {

    private MutableLiveData<User> userMutable = new MutableLiveData<>();

    public MutableLiveData<User> getUserFromDb(String uid) {
        DatabaseReference userRef = FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    userMutable.setValue(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return userMutable;
    }
}
