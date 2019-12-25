package com.dragontelnet.helpzone.firebase;

import com.dragontelnet.helpzone.MyConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRefs {

    public static DatabaseReference getRootRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getNearbyUsersDistancesOfUidNodeRef(String uid) {
        return getRootRef()
                .child(MyConstants.USERS_LOC_NODE)
                .child(uid)
                .child(MyConstants.NEARBY_USERS_LOC_DETAILS_NODE);
    }

    public static DatabaseReference getAllUsersLocNodeRef() {
        return getRootRef().child(MyConstants.ALL_LOC_NODE);
    }

    public static DatabaseReference getAllRegUsersNodeRef() {
        return getRootRef().child(MyConstants.REGISTERED_USERS_NODE);
    }

    public static DatabaseReference getSingleRegUserDetailsOfUidNodeRef(String uid) {
        return getAllRegUsersNodeRef().child(uid);
    }

    public static DatabaseReference getAllTriggersNodeRef() {
        return getRootRef()
                .child(MyConstants.TRIGGERS_NODE);
    }

    public static DatabaseReference getSingleUserTriggersOfUidNodeRef(String uid) {
        return getAllTriggersNodeRef().child(uid);
    }

    public static DatabaseReference getTrustedContactsNodeRef() {
        return getRootRef().child(MyConstants.TRUSTED_CONTACTS_NODE);
    }
}
