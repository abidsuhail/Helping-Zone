package com.dragontelnet.helpzone.repository.fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class HelpRequestsRepository {

    private MutableLiveData<User> mutableUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> requestsExists = new MutableLiveData<>();

    public HelpRequestsRepository() {
    }

    public MutableLiveData<User> getUser(String uid) {
        FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        mutableUser.setValue(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return mutableUser;
    }

    public MutableLiveData<Boolean> isRequestsExists() {
        DatabaseReference ref = FirebaseRefs
                .getSingleUserTriggersOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    requestsExists.setValue(true);
                } else {
                    requestsExists.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return requestsExists;
    }
}
