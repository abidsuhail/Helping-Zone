package com.dragontelnet.helpzone.ui.activity.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dragontelnet.helpzone.MySharedPrefs;
import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.di.MyDaggerInjection;
import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.service.MyBackgroundService;
import com.dragontelnet.helpzone.ui.activity.registration.RegistrationDetailsActivity;
import com.dragontelnet.helpzone.ui.fragments.helprequests.HelpRequestsFragment;
import com.dragontelnet.helpzone.ui.fragments.home.HomeFragment;
import com.dragontelnet.helpzone.ui.fragments.home.HomeFragmentViewModel;
import com.dragontelnet.helpzone.ui.fragments.map.MapsFragment;
import com.dragontelnet.helpzone.ui.fragments.peoples.PeoplesDetailsFragment;
import com.dragontelnet.helpzone.ui.fragments.trustedpeoples.TrustedPeoplesFragment;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String HOMEF_TAG = "homef";
    public static final String MAPSF_TAG = "mapsf";
    public static final String PEOPLESF_TAG = "peoplesf";
    public static final String TRUSTEDF_TAG = "trustedf";
    public static final String HELPREQUESTSF_TAG = "helprequestsf";
    public static final int LOCATION_PERMISSION_REQ_CODE = 1;
    public static final int APPLICATION_DETAILS_SETTINGS_REQ_CODE = 2;
    private static final String TAG = "MainActivity";

    @Inject
    public MapsFragment mapsFragment;

    @Inject
    public HomeFragment homeFragment;

    @Inject
    public PeoplesDetailsFragment peoplesFragment;

    @Inject
    public TrustedPeoplesFragment trustedFragment;

    @Inject
    public HelpRequestsFragment helpRequestsFragment;

  /*  @Inject
    public MyLiveLocationListener myLiveLocationListener;*/

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.drawer_layout)

    DrawerLayout drawer;
    private TextView navUsername;
    private AlertDialog.Builder builder;
    private TextView navPhone;
    private ImageView navImage;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: in");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        ButterKnife.bind(this);

        setDaggerInjection();

        initUi();
        askPermission();
        getViewModel().getUserDetails(CurrentFuser.getCurrentFuser().getUid()).observe(this, user -> {
            navUsername.setText(user.getUserName());
            navPhone.setText(user.getPhone());
            if (!user.getImageUrl().equals("")) {
                Picasso.get().load(user.getImageUrl()).into(navImage);
            }
        });

        getHomeFragmentViewModel()
                .getLocationsHashMapLiveData()
                .observe(this, new Observer<HashMap<String, GeoLocation>>() {
                    @Override
                    public void onChanged(HashMap<String, GeoLocation> locationsHashMap) {
                        getViewModel().setNearbyPeoplesCount(locationsHashMap.size() - 1);
                    }
                });
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Log.d(TAG, "askPermission: in shouldShowRequestPermissionRationale");
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQ_CODE);
                    // openSettingsAlert();


                } else {
                    //user click never ask for permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQ_CODE);
                }
            } else {
                //permission granted , now start service and fragment
                if (builder != null) {
                    Dialog dialog = builder.create();
                    dialog.dismiss();
                }
                startServiceAndFragments();

            }
        } else {
            //SDK_INT < M
            startServiceAndFragments();

        }
    }

    private HomeFragmentViewModel getHomeFragmentViewModel() {
        return ViewModelProviders.of(this)
                .get(HomeFragmentViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getHomeFragmentViewModel().setTriggerListener();

    }

    @Override
    protected void onStop() {
        super.onStop();
        getHomeFragmentViewModel().removeTriggerListener();
        Log.d(TAG, "onStop: in");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: in");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQ_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //access granted , start service and fragments
                    startServiceAndFragments();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Location Permission denied",
                            Toast.LENGTH_SHORT).show();
                    openSettingsAlert();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void startServiceAndFragments() {
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
        if (builder != null) {
            Dialog dialog = builder.create();
            dialog.dismiss();
        }
        fragmentManager
                .beginTransaction()
                .add(R.id.nav_host_fragment, homeFragment, HOMEF_TAG)
                .commit();

        startBackgroundService();

    }

    private void startBackgroundService() {
        Intent intent = new Intent(this, MyBackgroundService.class);
        startService(intent);
    }


    private void openSettingsAlert() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Grant location permission,else app won't work")
                .setTitle("Location Permission")
                .setPositiveButton("Open Settings", (dialog, which) -> {

                    //opening settings
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, APPLICATION_DETAILS_SETTINGS_REQ_CODE);

                }).setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APPLICATION_DETAILS_SETTINGS_REQ_CODE) {
            askPermission();
        }
    }

    private MainActivityViewModel getViewModel() {
        return ViewModelProviders.of(this).get(MainActivityViewModel.class);
    }

    private void setDaggerInjection() {
      /*  MyDaggerInjection.setRepoComponent(this);//only set in MainActivity

        MyDaggerInjection.setFragmentsComponent();*/
        MyDaggerInjection.setRepoComponent(getApplicationContext());
        MyDaggerInjection.setFragmentsComponent();
        MyDaggerInjection.getFragmentsComponent().inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void initUi() {
        setSupportActionBar(toolbar);
        setNavDrawer();
    }

    private void setNavDrawer() {
        View headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.nav_header_name);
        navPhone = headerView.findViewById(R.id.nav_header_phone);
        navImage = headerView.findViewById(R.id.nav_header_dp);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:

                showHomeFragment();

                break;
            case R.id.nav_map:

                showMapFragment();
                break;
            case R.id.nav_people_details:
                showPeopleDetailsFragment();
                break;

            case R.id.nav_trusted_peoples:
                trustedPeopleFragment();
                break;

            case R.id.nav_help_requests:
                showHelpReqFragment();
                break;

            case R.id.nav_settings:
                Intent intent = new Intent(this, RegistrationDetailsActivity.class);
                intent.putExtra("editing", true);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                stopService(new Intent(this, MyBackgroundService.class));
                removeMyLocAndSignOut();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void removeMyLocAndSignOut() {

        getViewModel().isRemoveLocSuccessful().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccessful) {
                if (isSuccessful) {
                    MySharedPrefs.getStartActivitySharedPrefs().edit().clear().apply();//clearing activity db user info values
                    MySharedPrefs.getTrustedNumbersSharedPrefs().edit().clear().apply();//clearing trusted numbers db user info values
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    getViewModel().isRemoveLocSuccessful().removeObservers(MainActivity.this);
                }
            }
        });
    }
    private void showHomeFragment() {
        if (fragmentManager.findFragmentByTag(HOMEF_TAG) != null) {
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(HOMEF_TAG))
                    .commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.nav_host_fragment, homeFragment, HOMEF_TAG)
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(MAPSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MAPSF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(PEOPLESF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(PEOPLESF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(TRUSTEDF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(TRUSTEDF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG))
                    .commit();
        }
    }

    private void showMapFragment() {
        if (fragmentManager.findFragmentByTag(MAPSF_TAG) != null) {
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(MAPSF_TAG))
                    .commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.nav_host_fragment, mapsFragment, MAPSF_TAG)
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HOMEF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HOMEF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(PEOPLESF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(PEOPLESF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(TRUSTEDF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(TRUSTEDF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG))
                    .commit();
        }
    }

    private void showHelpReqFragment() {
        if (fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG) != null) {
            fragmentManager.beginTransaction()
                    .show(fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG))
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.nav_host_fragment, helpRequestsFragment, HELPREQUESTSF_TAG)
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HOMEF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HOMEF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(MAPSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MAPSF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(PEOPLESF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(PEOPLESF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(TRUSTEDF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(TRUSTEDF_TAG))
                    .commit();
        }
    }

    private void trustedPeopleFragment() {
        if (fragmentManager.findFragmentByTag(TRUSTEDF_TAG) != null) {
            fragmentManager.beginTransaction()
                    .show(fragmentManager.findFragmentByTag(TRUSTEDF_TAG))
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.nav_host_fragment, trustedFragment, TRUSTEDF_TAG)
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HOMEF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HOMEF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(MAPSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MAPSF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(PEOPLESF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(PEOPLESF_TAG))
                    .commit();
        }

        if (fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG))
                    .commit();
        }
    }

    private void showPeopleDetailsFragment() {
        if (fragmentManager.findFragmentByTag(PEOPLESF_TAG) != null) {
            fragmentManager.beginTransaction()
                    .show(fragmentManager.findFragmentByTag(PEOPLESF_TAG))
                    .commit();
        } else {
            fragmentManager.beginTransaction()
                    .add(R.id.nav_host_fragment, peoplesFragment, PEOPLESF_TAG)
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HOMEF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HOMEF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(MAPSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(MAPSF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(TRUSTEDF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(TRUSTEDF_TAG))
                    .commit();
        }
        if (fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG) != null) {
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(HELPREQUESTSF_TAG))
                    .commit();
        }
    }
}
