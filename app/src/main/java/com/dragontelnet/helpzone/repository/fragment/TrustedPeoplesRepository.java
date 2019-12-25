package com.dragontelnet.helpzone.repository.fragment;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.TrustedContact;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class TrustedPeoplesRepository {
    private MutableLiveData<Boolean> trustedContactsMut = new MutableLiveData<>();
    private MutableLiveData<TrustedContact> trustedContactsMutable = new MutableLiveData<>();

    public TrustedPeoplesRepository() {

    }

    public MutableLiveData<Boolean> setTrustedContactsToDb(String phtv1Str, String phtv2Str, String phtv3Str, String phtv4Str) {
        DatabaseReference trustedRef = FirebaseRefs.getTrustedContactsNodeRef()
                .child(CurrentFuser.getCurrentFuser().getUid());

        TrustedContact trustedContact = new TrustedContact(phtv1Str, phtv2Str, phtv3Str, phtv4Str);

        trustedRef.updateChildren(trustedContact.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            trustedContactsMut.setValue(true);
                        } else {
                            trustedContactsMut.setValue(false);
                        }
                    }
                });

        return trustedContactsMut;
    }

    public MutableLiveData<TrustedContact> getTrustedContactsMutable() {
        DatabaseReference trustedRef = FirebaseRefs.getTrustedContactsNodeRef()
                .child(CurrentFuser.getCurrentFuser().getUid());

        trustedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TrustedContact contact = dataSnapshot.getValue(TrustedContact.class);
                trustedContactsMutable.setValue(contact);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return trustedContactsMutable;
    }
}
