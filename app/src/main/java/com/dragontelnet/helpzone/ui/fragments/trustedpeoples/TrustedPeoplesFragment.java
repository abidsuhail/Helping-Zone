package com.dragontelnet.helpzone.ui.fragments.trustedpeoples;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TrustedPeoplesFragment extends Fragment {

    private static final String TAG = "TrustedPeoplesFragment";
    private static final int READ_CONTACTS_REQ_CODE = 1;
    private final int PICK_CONTACT1 = 1;
    private final int PICK_CONTACT2 = 2;
    private final int PICK_CONTACT3 = 3;
    private final int PICK_CONTACT4 = 4;
    @BindView(R.id.fragment_trusted_phtv1)
    EditText phtv1;
    @BindView(R.id.fragment_trusted_phtv2)
    EditText phtv2;
    @BindView(R.id.fragment_trusted_phtv3)
    EditText phtv3;
    @BindView(R.id.fragment_trusted_phtv4)
    EditText phtv4;
    private String pickedContactNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trusted_peoples, container, false);
        ButterKnife.bind(this, view);

        if (MySharedPrefs.getTrustedNumbersSharedPrefs().getBoolean("init", false)) {
            phtv1.setText(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone1", ""));
            phtv2.setText(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone2", ""));
            phtv3.setText(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone3", ""));
            phtv4.setText(MySharedPrefs.getTrustedNumbersSharedPrefs().getString("phone4", ""));
        } else {
            getViewModel().getTrustedContactsLiveData().observe(this, new Observer<TrustedContact>() {
                @Override
                public void onChanged(TrustedContact contact) {

                    if (contact != null) {
                        phtv1.setText(contact.getPhone1());
                        phtv2.setText(contact.getPhone2());
                        phtv3.setText(contact.getPhone3());
                        phtv4.setText(contact.getPhone4());

                        removeTrustedContactsObserver();
                    }

                }
            });
        }
        return view;
    }

    private void hideFabIcon() {
        if (getActivity() != null) {
            FloatingActionButton fab = getActivity().findViewById(R.id.fab);
            fab.hide();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PICK_CONTACT1:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        phtv1.setText(getPickedContactNumber(data));
                    }

                }
                break;
            case PICK_CONTACT2:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        phtv2.setText(getPickedContactNumber(data));
                    }

                }
                break;
            case PICK_CONTACT3:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        phtv3.setText(getPickedContactNumber(data));
                    }

                }
                break;
            case PICK_CONTACT4:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        phtv4.setText(getPickedContactNumber(data));
                    }

                }
                break;
        }
    }

    private String getPickedContactNumber(Intent data) {
        Uri contactData = data.getData();
        Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1")) //1 for true
            {
                Cursor phones = getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                        null, null);
                phones.moveToFirst();
                pickedContactNumber = phones.getString(phones.getColumnIndex("data1"));
                Log.d(TAG, "number is:" + pickedContactNumber);
            }
            String pickedContactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        return pickedContactNumber;
    }

    @OnClick(R.id.fragment_trusted_save_btn)
    public void onSaveTrustedNoBtnClicked() {

        String phtv1Str = phtv1.getText().toString().trim();
        String phtv2Str = phtv2.getText().toString().trim();
        String phtv3Str = phtv3.getText().toString().trim();
        String phtv4Str = phtv4.getText().toString().trim();

        //add validation later
        getViewModel().setTrustedContactsToDb(phtv1Str, phtv2Str, phtv3Str, phtv4Str)
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isWritten) {
                        if (isWritten) {
                            Toast.makeText(getActivity(), "contacts saved to db", Toast.LENGTH_SHORT).show();
                            saveToLocalDb(phtv1Str, phtv2Str, phtv3Str, phtv4Str); //now save to local DB
                            getViewModel().setTrustedContactsToDb(phtv1Str, phtv2Str, phtv3Str, phtv4Str).removeObservers(TrustedPeoplesFragment.this);
                        } else {
                            Toast.makeText(getActivity(), "error in fetching data", Toast.LENGTH_SHORT).show();
                            getViewModel().setTrustedContactsToDb(phtv1Str, phtv2Str, phtv3Str, phtv4Str).removeObservers(TrustedPeoplesFragment.this);

                        }
                    }
                });

    }

    private boolean checkAccessContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        } else {
            //sdk is less than 23 i.e not marshmallow
            return true;
        }
    }

    private void saveToLocalDb(String phtv1Str, String phtv2Str, String phtv3Str, String phtv4Str) {
        MySharedPrefs.getTrustedNumbersSharedPrefs().edit().putBoolean("init", true).apply();
        MySharedPrefs.getTrustedNumbersSharedPrefs().edit().putString("phone1", phtv1Str).apply();
        MySharedPrefs.getTrustedNumbersSharedPrefs().edit().putString("phone2", phtv2Str).apply();
        MySharedPrefs.getTrustedNumbersSharedPrefs().edit().putString("phone3", phtv3Str).apply();
        MySharedPrefs.getTrustedNumbersSharedPrefs().edit().putString("phone4", phtv4Str).apply();
    }

    private void removeTrustedContactsObserver() {
        if (getViewModel().getTrustedContactsLiveData().hasObservers()) {
            getViewModel().getTrustedContactsLiveData().removeObservers(this);
        }

    }

    private TrustedPeoplesViewModel getViewModel() {
        return ViewModelProviders.of(this).get(TrustedPeoplesViewModel.class);
    }


    @OnClick({R.id.fragment_trusted_selcont1_btn, R.id.fragment_trusted_selcont2_btn, R.id.fragment_trusted_selcont3_btn, R.id.fragment_trusted_selcont4_btn})
    public void onChooseContactsBtnClicked(View view) {
        if (checkAccessContactsPermission()) {
            //perm. is not granted
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            switch (view.getId()) {
                case R.id.fragment_trusted_selcont1_btn:
                    startActivityForResult(intent, PICK_CONTACT1);
                    break;
                case R.id.fragment_trusted_selcont2_btn:
                    startActivityForResult(intent, PICK_CONTACT2);
                    break;
                case R.id.fragment_trusted_selcont3_btn:
                    startActivityForResult(intent, PICK_CONTACT3);
                    break;
                case R.id.fragment_trusted_selcont4_btn:
                    startActivityForResult(intent, PICK_CONTACT4);
                    break;
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQ_CODE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            hideFabIcon();
        }
    }
}
