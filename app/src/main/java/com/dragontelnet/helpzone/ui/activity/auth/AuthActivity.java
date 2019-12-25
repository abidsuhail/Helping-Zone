package com.dragontelnet.helpzone.ui.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dragontelnet.helpzone.MySharedPrefs;
import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.ui.activity.main.MainActivity;
import com.dragontelnet.helpzone.ui.activity.registration.RegistrationDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    @BindView(R.id.phone_et)
    EditText phneEt;

    @BindView(R.id.otp_et)
    EditText otpEt;

    private Observer<FirebaseUser> firebaseUserObserver;
    private Observer<String> verificationIdObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Log.d(TAG, "auth onCreate: in");
        ButterKnife.bind(this);
        setDaggerInjection();

        settingObservers();
    }

    private void setDaggerInjection() {
        MyDaggerInjection.setAuthRepoComponent(this);
    }

    private AuthActivityViewModel getViewModel() {
        return ViewModelProviders.of(this).get(AuthActivityViewModel.class);
    }


    @OnClick(R.id.send_otp_btn)
    public void getLoggedInUser() {
        //checking for text view emptiness
        if (!TextUtils.isEmpty(phneEt.getText().toString().trim())) {
            getViewModel().getLoggedInUserLiveData(phneEt.getText().toString())
                    .observe(AuthActivity.this, firebaseUserObserver);
        }
    }

    @OnClick(R.id.verify_otp_btn)
    public void verifyOtp() {
        if (verificationIdObserver != null) {
            getViewModel().getVerificationIdLiveData()
                    .observe(AuthActivity.this, verificationIdObserver);
        }
    }

    private void settingObservers() {
        firebaseUserObserver = new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                Log.d(TAG, "onChanged: uuiidd : " + firebaseUser.getUid());
                startSuitableActivity(firebaseUser);
            }
        };


        verificationIdObserver = new Observer<String>() {
            @Override
            public void onChanged(String verificationId) {
                Log.d(TAG, "onChanged: verification id:" + verificationId);
                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(verificationId, otpEt.getText().toString());
                getViewModel().getUserByCredentialLiveData(credential)
                        .observe(AuthActivity.this, firebaseUserObserver);
            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (getCurrentUser() != null) {
            startSuitableActivity(getCurrentUser());
        }
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser();

    }

    private void startSuitableActivity(FirebaseUser firebaseUser) {
        Log.d(TAG, "startSuitableActivity: in");

        if (MySharedPrefs.getStartActivitySharedPrefs().getBoolean("start", false)) {
            //if true means user is in db because at the time of registration it set to true to local db via
            // shared prefs.

            // start main activity
            startMainActivity();

        } else {

            //not in DB
            //checking user in DB
            getViewModel().isUserExistsLiveData(firebaseUser.getUid()).observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isUserExists) {
                    if (isUserExists) {
                        Log.d(TAG, "onChanged: isUserExists : " + isUserExists);
                        getViewModel().setDeviceTokenToDbLiveData(); //setting device token to db

                        //user exists in db
                        //saving user info in local db
                        MySharedPrefs.getStartActivitySharedPrefs()
                                .edit()
                                .putBoolean("start", true)
                                .apply();

                        startMainActivity();
                        removeObserver(firebaseUser);

                    } else {
                        Toast.makeText(AuthActivity.this, "user not exists in db", Toast.LENGTH_SHORT).show();

                        startRegistrationDetailsActivity();

                        removeObserver(firebaseUser);

                    }
                }
            });
        }


    }

    private void removeObserver(FirebaseUser firebaseUser) {
        if (getViewModel().isUserExistsLiveData(firebaseUser.getUid()).hasObservers()) {
            getViewModel().isUserExistsLiveData(firebaseUser.getUid())
                    .removeObservers(AuthActivity.this);
        }
    }


    private void startRegistrationDetailsActivity() {
        startActivity(new Intent(this, RegistrationDetailsActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

}
