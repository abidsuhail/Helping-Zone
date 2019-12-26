package com.dragontelnet.helpzone.repository.activity;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

@Singleton
public class AuthActivityRepository {
    private static final String TAG = "AuthRepo";
    private FirebaseAuth mAuth;
    private Activity activity;
    private FirebaseUser user;
    private MutableLiveData<FirebaseUser> mutableFirebaseUser = new MutableLiveData<>();
    private MutableLiveData<String> mutableVerificationId = new MutableLiveData<>();
    private MutableLiveData<Boolean> mutableIsUserExist = new MutableLiveData<>();
    private MutableLiveData<String> mutableDeviceToken = new MutableLiveData<>();


    public AuthActivityRepository(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
    }

    //getting user object ,after verifying phone number
    public MutableLiveData<FirebaseUser> getLoggedInUserMutable(String phoneNumber) {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Toast.makeText(activity, "verification completed", Toast.LENGTH_SHORT).show();
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(activity, task -> {
                            if (task.isSuccessful()) {
                                user = task.getResult().getUser();
                                mutableFirebaseUser.setValue(user);
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                }
                            }
                        });
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }

                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                mutableVerificationId.setValue(verificationId);
                Toast.makeText(activity, "code sent", Toast.LENGTH_SHORT).show();
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                activity,
                mCallbacks);

        return mutableFirebaseUser;


    }

    //getting user object by passing credential object
    public MutableLiveData<FirebaseUser> getUserByCredentialMutable(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        user = task.getResult().getUser();
                        mutableFirebaseUser.setValue(user);
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                        }
                    }
                });
        return mutableFirebaseUser;
    }

    //getting verification id when verifying phone number
    public MutableLiveData<String> getVerificationIdMutable() {
        return mutableVerificationId;
    }

    //checking particular user uid in db if exists
    public MutableLiveData<Boolean> isUserExistsMutable(String uid) {

        DatabaseReference mUserRef = FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(uid);

        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: exists");
                    mutableIsUserExist.setValue(true);
                } else {
                    mutableIsUserExist.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return mutableIsUserExist;
    }

    //setting device token in db,for FCM
    public MutableLiveData<String> setDeviceTokenToDb() {
        if (CurrentFuser.getCurrentFuser() != null) {
            final DatabaseReference regCurrentUserRef = FirebaseRefs
                    .getSingleRegUserDetailsOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid());

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        HashMap<String, Object> tokenMap = new HashMap<>();
                        tokenMap.put("device_token", token);//setting fcm token to db
                        regCurrentUserRef.updateChildren(tokenMap);
                        mutableDeviceToken.setValue(token);
                    });
        }
        return mutableDeviceToken;
    }


}
