package com.dragontelnet.helpzone.repository.fragment;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.Trigger;
import com.dragontelnet.helpzone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Singleton;

@Singleton
public class PeoplesDetailsFragmentRepository {

    private static final String TAG = "PeoplesDetailsFragmentR";
    private FirebaseUser currentUser;
    private Context context;
    private MutableLiveData<User> userNameByUidMutable = new MutableLiveData<>();
    private MutableLiveData<Boolean> peoplesExists = new MutableLiveData<>();
    private ValueEventListener userNameListener;

    public PeoplesDetailsFragmentRepository(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        this.context = context;
    }

    public MutableLiveData<User> getUserNameByUidMutable(String uid) {
        userNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("userName").exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        userNameByUidMutable.setValue(user);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        DatabaseReference mUserRef = FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(uid);
        mUserRef.addListenerForSingleValueEvent(userNameListener);
        return userNameByUidMutable;
    }

    private Trigger getTrigger(DataSnapshot peopleSnapshot) {
        //getting trigger object
        Trigger trigger = new Trigger();
        String uid = currentUser.getUid();
        String title = peopleSnapshot.child("userName").getValue().toString();
        String body = peopleSnapshot.child("distance").getValue().toString();
        trigger.setByUid(uid);
        trigger.setTitle(title);
        trigger.setBody(body);
        return trigger;
    }

    public void sendFCMToDevice(String peopleUid) {
        final DatabaseReference triggerRef = FirebaseRefs
                .getSingleUserTriggersOfUidNodeRef(peopleUid);

        DatabaseReference distanceRef = FirebaseRefs
                .getNearbyUsersDistancesOfUidNodeRef(currentUser.getUid())
                .child(peopleUid);

        distanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot peopleSnapshot) {
                if (peopleSnapshot.exists()) {
                    String triggerKey = triggerRef.push().getKey();
                    if (triggerKey != null) {
                        Trigger trigger = getTrigger(peopleSnapshot);
                        triggerRef.child(triggerKey).setValue(trigger.toMap())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Notified to user", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public MutableLiveData<Boolean> isPeoplesNearbyExists() {
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
        return peoplesExists;
    }
}
