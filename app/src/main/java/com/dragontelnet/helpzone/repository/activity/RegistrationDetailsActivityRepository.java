package com.dragontelnet.helpzone.repository.activity;


import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.MyConstants;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static com.dragontelnet.helpzone.RadomKeyGenerator.getRandomKey;

public class RegistrationDetailsActivityRepository {
    private static final String TAG = "RegistrationDetailsRepo";
    private MutableLiveData<Boolean> isWriteSuccessMutable = new MutableLiveData<>();
    private MutableLiveData<String> imageUrlMutable = new MutableLiveData<>();
    private StorageReference storageRef;
    private String finalImageUrl;

    public MutableLiveData<Boolean> writeUserToDbMutable(FirebaseUser firebaseUser, String userName, String imageUrl) {
        if (firebaseUser != null) {
            User user = new User();
            user.setPhone(firebaseUser.getPhoneNumber());
            user.setUserName(userName);
            user.setUid(firebaseUser.getUid());
            user.setImageUrl(imageUrl);
            DatabaseReference mUserRef = FirebaseRefs
                    .getSingleRegUserDetailsOfUidNodeRef(firebaseUser.getUid());
            HashMap<String, Object> userMap = user.toMap();
            mUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        isWriteSuccessMutable.setValue(true);
                    } else {
                        isWriteSuccessMutable.setValue(false);
                    }

                }
            });
        }
        return isWriteSuccessMutable;
    }

    public MutableLiveData<String> getProfilePicUrlMutable(Uri localImageUri) {
        storageRef = FirebaseStorage.getInstance().getReference().child(MyConstants.PROFILE_PIC_REF);

        final StorageReference filePath = storageRef.child(getRandomKey() + ".jpg");

        final UploadTask uploadTask = filePath.putFile(localImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            finalImageUrl = downloadUri.toString();
                            imageUrlMutable.setValue(finalImageUrl);
                            Log.d(TAG, "onComplete: finalImageUrl" + finalImageUrl);
                        } else {

                        }
                    }
                });
            }
        });

        return imageUrlMutable;
    }


}
