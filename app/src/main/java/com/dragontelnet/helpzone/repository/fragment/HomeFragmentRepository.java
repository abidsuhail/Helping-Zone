package com.dragontelnet.helpzone.repository.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.dragontelnet.helpzone.MyConstants;
import com.dragontelnet.helpzone.RadomKeyGenerator;
import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.PeopleLoc;
import com.dragontelnet.helpzone.model.Trigger;
import com.dragontelnet.helpzone.model.TrustedContact;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import javax.inject.Singleton;

import static com.dragontelnet.helpzone.RadomKeyGenerator.getRandomKey;

@Singleton
public class HomeFragmentRepository {

    private static final String TAG = "HomeFragmentRepository";
    private static boolean isChildEventListener;
    private Context context;
    //private MutableLiveData<LatLng> locationChangingMutableLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();
    private MutableLiveData<TrustedContact> trustedContactsMutable = new MutableLiveData<>();
    private long noOfPeopleFCMSent = 0;
    private Location peopleLocation;
    private int count;
    private long nodeCount;
    private MediaRecorder recorder;
    private String finalAudioUrl;
    private File audioFile;
    private int peoplesAround;
    private ChildEventListener triggerChildListener;
    private DatabaseReference triggersRef;

    public HomeFragmentRepository(Context context) {
        this.context = context;
    }


    public MutableLiveData<Boolean> setUserLocDetailsToDb(final Map.Entry<String, GeoLocation> entry, final Location myLoc) {

        if (CurrentFuser.getCurrentFuser() != null && entry.getKey() != null) {
            FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //got user snapshot because i want current username ,now set user loc details node
                                setUserDetailsNodeHashMap(entry, myLoc, dataSnapshot);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        return isSuccessful;

    }

    private void setUserDetailsNodeHashMap(final Map.Entry<String, GeoLocation> entry, final Location myLoc, DataSnapshot dataSnapshot) {
        if (CurrentFuser.getCurrentFuser() != null && entry.getKey() != null) {
            final DatabaseReference mSinglePeopleDistanceRef = FirebaseRefs
                    .getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                    .child(entry.getKey());

            peopleLocation = new Location("");
            peopleLocation.setLatitude(entry.getValue().latitude);
            peopleLocation.setLongitude(entry.getValue().longitude);

            DecimalFormat df = new DecimalFormat("0.00");
            float peopleDistance = myLoc.distanceTo(peopleLocation);
            if (peopleDistance <= 500.0) {
                //checking if by mistake other people distance from my distance is 500<x

                String peopleDistanceRoundOff = df.format(peopleDistance); //round off distance to 2 decimal places
                PeopleLoc peopleLoc = getPeopleLoc(entry, peopleDistanceRoundOff, dataSnapshot);
                mSinglePeopleDistanceRef.updateChildren(peopleLoc.toMap())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                isSuccessful.setValue(true);
                            } else {
                                isSuccessful.setValue(false);
                            }
                        });
            }
        }
    }

    private PeopleLoc getPeopleLoc(Map.Entry<String, GeoLocation> entry, String peopleDistance, DataSnapshot dataSnapshot) {
        //creating peopleLoc object for pushing to distance ref

        PeopleLoc peopleLoc = new PeopleLoc();
        peopleLoc.setUid(entry.getKey());
        peopleLoc.setDistance(peopleDistance);
        peopleLoc.setLat(entry.getValue().latitude);
        peopleLoc.setLng(entry.getValue().longitude);
        peopleLoc.setUserName(dataSnapshot.child("userName").getValue().toString());
        return peopleLoc;
    }

    private Trigger getTrigger(DataSnapshot dataSnapshot, boolean isAudio, String audioLink) {
        //getting distance from data snapshot user loc distance node and creating obj

        Trigger trigger = new Trigger();

        if (!isAudio) {
            //not a audio
            trigger.setTitle(dataSnapshot.child("trigger_generator_user_name")
                    .getValue()
                    .toString() + " asking a help request");

            trigger.setAudioLink("");

        } else {
            //is audio
            trigger.setTitle(dataSnapshot.child("trigger_generator_user_name")
                    .getValue()
                    .toString() + " sent a voice message");

            trigger.setAudioLink(audioLink);
        }
        trigger.setBody(dataSnapshot.child("distance").getValue().toString());
        trigger.setByUid(CurrentFuser.getCurrentFuser().getUid());
        trigger.setDate(RadomKeyGenerator.getDate());
        trigger.setTime(RadomKeyGenerator.getTime());
        trigger.setTimeStamp(RadomKeyGenerator.getTimeStamp());
        return trigger;
    }

    public void sendFCMToAllNearbyDevices(boolean isAudio, String audioLink) {
        //sending fcm to all nearby devices in looping nearby loc lat lng node
        final DatabaseReference triggersRef = FirebaseRefs.getAllTriggersNodeRef();

        FirebaseRefs.getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        noOfPeopleFCMSent = dataSnapshot.getChildrenCount();
                        for (DataSnapshot userNode : dataSnapshot.getChildren()) {
                            pushTriggerToTriggerNode(userNode, triggersRef, isAudio, audioLink);
                        }
                        Toast.makeText(context, "Notifications sent to " + noOfPeopleFCMSent + " devices", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void removeTriggerListener() {
        if (triggersRef != null && triggerChildListener != null) {
            triggersRef.removeEventListener(triggerChildListener);
        }
    }

    public void triggersListener() {
        //add music also
        isChildEventListener = false;
        count = 0;
        nodeCount = 0;
        triggersRef = FirebaseRefs
                .getSingleUserTriggersOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid());

        triggersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nodeCount = dataSnapshot.getChildrenCount();
                dosomething();
                Log.d(TAG, "onDataChange: nodecount " + nodeCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void dosomething() {
        triggerChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (!isChildEventListener) {
                    count = count + 1;
                    //if entered this block then its listening first time
                    if (count == nodeCount) {
                        isChildEventListener = true;
                    }
                } else {
                    Trigger trigger = new Trigger();
                    trigger.setBody(dataSnapshot.child("body").getValue().toString());
                    trigger.setByUid(CurrentFuser.getCurrentFuser().getUid());
                    trigger.setTitle(dataSnapshot.child("title").getValue().toString());
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.d(TAG, "onChildAdded: oreo+ ");
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(500);
                        Log.d(TAG, "onChildAdded: -oreo ");
                    }

                    try {
                        //playing notification sound
                        Log.d(TAG, "onChildAdded: playing sound");
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        triggersRef.addChildEventListener(triggerChildListener);
    }

    private void pushTriggerToTriggerNode(DataSnapshot userNode, DatabaseReference triggersRef, boolean isAudio, String audioLink) {
        final String peopleUid = userNode.getKey(); //key of node in distance ref
        if (peopleUid != null) {
            DatabaseReference distanceRef = FirebaseRefs
                    .getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid())
                    .child(peopleUid);

            distanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String triggerKey = triggersRef.child(peopleUid).push().getKey();
                        Trigger trigger = getTrigger(dataSnapshot, isAudio, audioLink);

                        if (triggerKey != null) {
                            triggersRef
                                    .child(peopleUid)
                                    .child(triggerKey)
                                    .setValue(trigger.toMap());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


    public void startRecordingAudio(Activity activity) {
        String AUDIO_FILE_NAME = "helping_zone_audio.mp3";

        audioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                AUDIO_FILE_NAME);

        //not recording,then record
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setMaxDuration(6000); //max duration is 6sec
        recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                Log.d(TAG, "onInfo: in extra " + what);
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecordingAudio(activity, peoplesAround);
                }
            }
        });
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("HomeFragment", "prepare() failed");
        }
        recorder.start();

    }

    private void showAlertDialog(Activity activity, int peoplesAround) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Send Audio");
        builder.setMessage("Send this audio to " + peoplesAround + " nearby peoples?");
        builder.setCancelable(false);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadAudioToStorage(); //uploading audio via firebase storage
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete audio in future
                dialog.dismiss();
            }
        }).show();
    }

    public void stopRecordingAudio(Activity activity, int peoples) {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
                recorder = null;
                peoplesAround = peoples;
                showAlertDialog(activity, peoplesAround);
            } catch (Exception e) {
                Toast.makeText(context, "Hold for few seconds to record voice", Toast.LENGTH_SHORT).show();
                /*  Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();*/
                Log.d(TAG, "stopRecordingAudio: " + e.getMessage());
            }
        }
    }

    public MutableLiveData<TrustedContact> getTrustedContactsMutable() {
        DatabaseReference trustedRef = FirebaseRefs.getTrustedContactsNodeRef()
                .child(CurrentFuser.getCurrentFuser().getUid());

        trustedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: trusted contacts");
                TrustedContact contact = dataSnapshot.getValue(TrustedContact.class);
                trustedContactsMutable.setValue(contact);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return trustedContactsMutable;
    }

    public void uploadAudioToStorage() {
        //its keeping link of previous link
        Log.d(TAG, "uploadAudioToStorage: in");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(MyConstants.AUDIO_STORAGE_REF);

        Uri uri = Uri.fromFile(audioFile);
        Log.d(TAG, "uploadAudioToStorage: uri " + uri.getPath());
        final StorageReference filePath = storageRef.child(getRandomKey() + ".mp3");

        final UploadTask uploadTask = filePath.putFile(uri);

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
                            Toast.makeText(context, "audio uploaded success", Toast.LENGTH_SHORT).show();
                            Uri downloadUri = task.getResult();
                            finalAudioUrl = downloadUri.toString();
                            sendFCMToAllNearbyDevices(true, finalAudioUrl);
                        }
                    }
                });
            }
        });
    }

}
