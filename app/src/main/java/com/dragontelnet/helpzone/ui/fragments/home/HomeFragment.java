package com.dragontelnet.helpzone.ui.fragments.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dragontelnet.helpzone.MySharedPrefs;
import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.model.TrustedContact;
import com.dragontelnet.helpzone.service.MyBackgroundService;
import com.dragontelnet.helpzone.service.MyBackgroundServiceViewModel;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import butterknife.OnTouch;

@Singleton
public class HomeFragment extends Fragment {

    private static final int RECORD_VOICE_REQ_CODE = 1;
    private static final int SEND_SMS_REQ_CODE = 2;
    private static final String TAG = "HomeFragment";

    @BindView(R.id.trigger_all_btn)
    Button sendBtn;

    @BindString(R.string.zero_peoples)
    String zeroPeopleStrRes;

    @BindString(R.string.load_peoples)
    String loadPeopleStrRes;

    @BindView(R.id.send_voice_msg_btn)
    Button sendVoiceMsgBtn;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.send_text_msg_btn)
    Button sendTextMsgBtn;

    @BindView(R.id.stop_service_btn)
    Button stopServiceBtn;

    private Location myLoc;
    private View root;
    private Observer<HashMap<String, GeoLocation>> hashMapObserver;
    private FloatingActionButton fab;
    private int peoplesAround;
    private int i = 6;
    private Handler handler;
    private Runnable runnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showFabIcon();

        //onClick fab icon,start observing locations HashMap
        fab.setOnClickListener(v -> startObservingHashMap());

        //getting peoples count
        getPeoplesCount();

        //checking service running status
        getServiceViewModel().getServiceStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRunning) {
                if (isRunning) {
                    Log.d(TAG, "onChanged: service is running");

                    stopServiceBtn.setText("STOP SERVICE");
                    stopServiceBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopBackgroundService();
                        }
                    });

                } else {
                    Log.d(TAG, "onChanged: service stopped");
                    stopServiceBtn.setText("START SERVICE");
                    stopServiceBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startBackgroundService();
                        }
                    });
                }
            }
        });

    }

    private MyBackgroundServiceViewModel getServiceViewModel() {
        return ViewModelProviders.of(this).get(MyBackgroundServiceViewModel.class);
    }

    private void showFabIcon() {
        if (getActivity() != null) {
            fab = getActivity().findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_refresh);
            fab.show();
        }
    }

    private void stopRecordingAudio() {
        getViewModel().stopRecordingAudio(getActivity(), peoplesAround);
        if (handler != null && runnable != null) {
            sendVoiceMsgBtn.setText("Record Voice Message to send to nearby devices");
            handler.removeCallbacks(runnable);
            i = 6;
        }
    }

    private void startRecordingAudio() {
        if (peoplesAround != 0) {
            getViewModel().startRecordingAudio(getActivity());
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    i--;
                    if (i < 0) {
                        i = 0;
                    }
                    sendVoiceMsgBtn.setText(i + "\nRecording......");
                    handler.postDelayed(this, 1000);
                }
            };
            handler.postDelayed(runnable, 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRecordingAudio();
    }

    @OnLongClick(R.id.send_text_msg_btn)
    public void onSendTextClickedBtn() {

        if (checkSendSmsPermission()) {
            getViewModel().getAndSetStaticLastLocationLiveData().observe(this, new Observer<LatLng>() {
                @Override
                public void onChanged(LatLng latLng) {
                    String helpTextMsg = String.format("Help me i need your help, my location is http://maps.google.com/?q=%f,%f", latLng.latitude, latLng.longitude);
                    configureTrustedContacts(helpTextMsg);
                    getViewModel().getAndSetStaticLastLocationLiveData().removeObservers(HomeFragment.this);
                }
            });
        } else {
            //now ask permission
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQ_CODE);
        }


    }

    private boolean checkSendSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        } else {
            //sdk is less than 23 i.e not marshmallow
            return true;
        }
    }

    @OnLongClick(R.id.trigger_all_btn)
    public void onClickedTriggerAllBtn() {
        getViewModel().sendFCMToAllNearbyDevices(false, "");
    }

    @OnTouch(R.id.send_voice_msg_btn)
    public void onClickedRecordVoiceBtn(View view, MotionEvent motionEvent) {

        if (checkRecordVoicePermission()) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //button pressed
                startRecordingAudio();
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                //button released
                stopRecordingAudio();

            }
        } else {
            //now ask permission
            requestPermissions(new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, RECORD_VOICE_REQ_CODE);

        }

    }

    private boolean checkRecordVoicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    ||
                    ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ||
                    ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            //sdk is less than 23 i.e not marshmallow
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RECORD_VOICE_REQ_CODE:

                break;
            case SEND_SMS_REQ_CODE:

                break;
        }
    }

    private void stopBackgroundService() {
        if (getActivity() != null) {
            getActivity().stopService(new Intent(getActivity(), MyBackgroundService.class));
            Toast.makeText(getActivity(), "service stopped...", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBackgroundService() {
        if (getActivity() != null) {
            getActivity().startService(new Intent(getActivity(), MyBackgroundService.class));
            Toast.makeText(getActivity(), "service started...", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureTrustedContacts(String helpTextMsg) {

        if (MySharedPrefs.getTrustedNumbersSharedPrefs().getBoolean("init", false)) {
            //trusted contacts is available in local shared prefs
            TrustedContact trustedContact = getOfflineTrustedContacts();
            if (trustedContact != null) {
                showSmsAlertDialog(trustedContact, helpTextMsg);
            }

        } else {
            //trusted contact is not available in offline shared prefs
            getViewModel().getTrustedContactsLiveData().observe(this, new Observer<TrustedContact>() {
                @Override
                public void onChanged(TrustedContact trustedContact) {
                    if (trustedContact != null) {
                        showSmsAlertDialog(trustedContact, helpTextMsg);
                    }
                    getViewModel().getTrustedContactsLiveData().removeObservers(HomeFragment.this);
                }
            });

        }
    }

    private void sendTextSMS(TrustedContact trustedContact, String helpTextMsg) {

        //Getting intent and PendingIntent instance
        PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);
        //Get the SmsManager instance and call the sendTextMessage method to send message
        SmsManager sms = SmsManager.getDefault();
        int count = 0;
        if (trustedContact.getPhone1() != null && !trustedContact.getPhone1().equals("")) {
            // "" means no. not set
            sms.sendTextMessage(trustedContact.getPhone1(), null, helpTextMsg, pi, null);
            count = count + 1;
        }

        if (trustedContact.getPhone2() != null && !trustedContact.getPhone2().equals("")) {
            sms.sendTextMessage(trustedContact.getPhone2(), null, helpTextMsg, pi, null);
            count = count + 1;
        }

        if (trustedContact.getPhone3() != null && !trustedContact.getPhone3().equals("")) {
            sms.sendTextMessage(trustedContact.getPhone3(), null, helpTextMsg, pi, null);
            count = count + 1;

        }
        if (trustedContact.getPhone4() != null && !trustedContact.getPhone4().equals("")) {
            sms.sendTextMessage(trustedContact.getPhone4(), null, helpTextMsg, pi, null);
            count = count + 1;
        }
        Toast.makeText(getActivity(), "messages sent to " + count + " trusted users", Toast.LENGTH_SHORT)
                .show();
    }


    private TrustedContact getOfflineTrustedContacts() {
        TrustedContact trustedContact = new TrustedContact();
        trustedContact.setPhone1(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone1", ""));
        trustedContact.setPhone2(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone2", ""));
        trustedContact.setPhone3(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone3", ""));
        trustedContact.setPhone4(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone4", ""));
        return trustedContact;
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }

    private void getPeoplesCount() {
        final TextView peoplesCountTv = root.findViewById(R.id.peoples_count_tv);

        //listening to nearby users onLocationChanged(){keyExit,keyEnter etc}
        hashMapObserver = stringGeoLocationHashMap -> {

            peoplesAround = stringGeoLocationHashMap.size() - 1; //getting nearby peoples count
            //getting size of hash map which was putted in onKeyEnter,onExit etc.
            for (final Map.Entry<String, GeoLocation> entry : stringGeoLocationHashMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    if (!entry.getKey().equals(getCurrentUser().getUid())) {
                        //not pushing my location to distance node
                        //get entry except my entry to add child nodes to "nearby_users_loc_details" parent node
                        if (myLoc != null) {
                            getViewModel().setUserLocDetailsToDbLiveData(entry, myLoc);
                        }

                    }
                }
            }

            //if by mistake peoples count is <0 (i.e negative),so handle it
            //doing size()-1 because i don't want to include myself(i am also in count)
            if ((peoplesAround >= 0)) {
                peoplesCountTv.setText("" + (stringGeoLocationHashMap.size() - 1));

                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }

                //peoples are non zero,then enable all buttons
                if (!(peoplesAround == 0)) {
                    if (!sendBtn.isEnabled() || !sendVoiceMsgBtn.isEnabled() || !sendTextMsgBtn.isEnabled()) {
                        enableRoundBtn();
                    }
                }
            }

        };

        disableRoundBtn();

        //start observing nearby users location HashMap details(uid & lat lng)
        startObservingHashMap();
    }

    private void disableRoundBtn() {
        sendBtn.setEnabled(false);
        sendBtn.setBackgroundResource(R.drawable.round_button_disabled);

        sendVoiceMsgBtn.setEnabled(false);
        sendVoiceMsgBtn.setBackgroundResource(R.drawable.round_button_disabled);

    }

    private void enableRoundBtn() {
        sendBtn.setEnabled(true);
        sendBtn.setBackgroundResource(R.drawable.round_button);

        sendVoiceMsgBtn.setEnabled(true);
        sendVoiceMsgBtn.setBackgroundResource(R.drawable.round_button);

    }

    private void startObservingHashMap() {

        //getting my non-live last known location
        getViewModel().getAndSetStaticLastLocationLiveData().observe(this, latLng -> {

            //now updating myLoc value with current location live location
            //myLoc i.e my location is used calculate distance with other location
            getViewModel().getLiveLocationLiveData().observe(this, location ->
            {

                //now myLoc is updating when i change my location
                myLoc = location;
            });

            //observe only ONE time
            //setting observer for getting location hash map
            //starting to calculate distance w.r.t updated myLoc
            getViewModel().getLocationsHashMapLiveData()
                    .observe(HomeFragment.this, hashMapObserver);

        });

    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        if (!isHidden) {
            showFabIcon();
        }
        super.onHiddenChanged(isHidden);
    }


    private HomeFragmentViewModel getViewModel() {
        return ViewModelProviders.of(this)
                .get(HomeFragmentViewModel.class);
    }

    private void showSmsAlertDialog(TrustedContact trustedContact, String helpTextMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("SMS charges may apply,are you sure want to continue?")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send msg
                        sendTextSMS(trustedContact, helpTextMsg);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RECORD_VOICE_REQ_CODE:

                break;
            case SEND_SMS_REQ_CODE:

                break;
        }
    }
}