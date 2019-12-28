package com.dragontelnet.helpzone.ui.activity.registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dragontelnet.helpzone.MySharedPrefs;
import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.model.User;
import com.dragontelnet.helpzone.ui.activity.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class RegistrationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationDetailsActi";

    @BindView(R.id.reg_iv)
    CircleImageView profilePic;

    @BindView(R.id.reg_name_et)
    EditText regNameEt;

    private String userName;
    private Uri localImageUri;
    private Observer<String> imageUrlObserver;
    private ProgressDialog progressDialog;
    private boolean isHasImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_details);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("uploading image,please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        setImageUrlObserver();//setting image observer

        if (isEditingProfile()) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            fetchUserDetails();
        }

    }

    private void fetchUserDetails() {

        getViewModel().getUserDetails(CurrentFuser.getCurrentFuser().getUid()).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                regNameEt.setText(user.getUserName());
                if (!user.getImageUrl().equals("")) {
                    Picasso.get()
                            .load(user.getImageUrl())
                            .into(profilePic);
                    isHasImage = true;
                }
            }
        });
    }

    private boolean isEditingProfile() {
        return getIntent().hasExtra("editing");
    }

    @OnClick(R.id.reg_iv)
    public void onRegProfilePicClicked() {
        //setting profile pic
        //cropping pic
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(RegistrationDetailsActivity.this);
    }


    @OnClick(R.id.reg_continue_btn)
    public void onRegContinueBtnClicked() {
        userName = regNameEt.getText().toString().trim();
        if (!TextUtils.isEmpty(userName)) {
            if (localImageUri != null) {
                //setProfilePic();
                progressDialog.show();
                getViewModel().getImageUrlLiveData(localImageUri)
                        .observe(RegistrationDetailsActivity.this, imageUrlObserver);

                localImageUri = null;
            } else {
                writeUserToDb("");
            }
        } else {
            Toast.makeText(RegistrationDetailsActivity.this, "please enter the name", Toast.LENGTH_SHORT).show();
        }
    }

    private void setImageUrlObserver() {
        imageUrlObserver = new Observer<String>() {
            @Override
            public void onChanged(String finalImageUrl) {
                Log.d(TAG, "onChanged: got image url from live data" + finalImageUrl);

                if (finalImageUrl == null) {
                    Toast.makeText(RegistrationDetailsActivity.this, "Error in uploading image", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(userName)) {
                    //if image url !=null
                    writeUserToDb(finalImageUrl);
                }
            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    localImageUri = result.getUri();
                    profilePic.setImageURI(localImageUri);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                if (result != null) {
                    Exception error = result.getError();
                    Log.d(TAG, "onActivityResult: " + error.getMessage());
                }
            }
        }
    }

    private void writeUserToDb(String finalImageUrl) {
        getViewModel().writeUserToLiveData(getCurrentUser(), userName, finalImageUrl)
                .observe(RegistrationDetailsActivity.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isWriteSuccess) {
                        if (isWriteSuccess) {
                            progressDialog.dismiss();
                            getViewModel().setDeviceTokenToDbLiveData();

                            //saving to local db
                            MySharedPrefs.getStartActivitySharedPrefs()
                                    .edit()
                                    .putBoolean("start", true)
                                    .apply();

                            if (!isEditingProfile()) {

                                //if i am new
                                startMainActivity();
                            } else {
                                //if editing my profile
                                finish();
                            }

                        } else {
                            Toast.makeText(RegistrationDetailsActivity.this, "unable to write to db", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void startMainActivity() {
        startActivity(new Intent(RegistrationDetailsActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private RegistrationDetailsActivityViewModel getViewModel() {
        return ViewModelProviders.of(this).get(RegistrationDetailsActivityViewModel.class);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
