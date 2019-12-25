package com.dragontelnet.helpzone.ui.fragments.map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dragontelnet.helpzone.R;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private Marker marker;
    private Observer<HashMap<String, GeoLocation>> locationsHashMapObserver;
    private LatLng locLatLng;
    private Circle circle;
    private MarkerOptions markerOptions;
    private Observer<LatLng> staticLastLocObserver;
    //private boolean isListeningGetLocationsHashMap = false;
    private boolean isOnLocationChangeListening = false;
    private Marker staticMarker;
    private String TITLE = "";
    private FloatingActionButton fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        showFabIcon();
        initGmap();
        setStaticLocationObserver();
        setLocationsObserver();
        fab.setOnClickListener(view -> getViewModel().getStaticLastLocationLiveData()
                .observe(getActivity(), staticLastLocObserver));
        return root;
    }

    private void showFabIcon() {
        if (getActivity() != null) {
            fab = getActivity().findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_locate);
            fab.show();
        }
    }

    private void hideFabIcon() {
        if (getActivity() != null) {
            fab = getActivity().findViewById(R.id.fab);
            fab.hide();
        }
    }


    private MapFragmentViewModel getViewModel() {
        return ViewModelProviders.of(this)
                .get(MapFragmentViewModel.class);
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser();
    }

    private void initGmap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        OnMapReadyCallback callback = googleMap -> {
            mMap = googleMap;
            //getting non-live last location
            getViewModel().getStaticLastLocationLiveData().observe(getActivity(), staticLastLocObserver);
        };
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void setStaticLocationObserver() {

        staticLastLocObserver = latLng -> {

            removePreviousCircleZone();
            if (staticMarker != null) {
                staticMarker.remove();
            }

            if (!isOnLocationChangeListening) {
                TITLE = "fetching peoples locations,please wait....";
                markerOptions = new MarkerOptions().position(latLng)
                        .draggable(true)
                        .title(TITLE)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icc_offline_loc));
            } else {
                markerOptions = new MarkerOptions().position(latLng)
                        .draggable(true)
                        .title("YOU")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_myloc2));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.5f));
            staticMarker = mMap.addMarker(markerOptions);
            staticMarker.showInfoWindow();
            addHelpingZone(getCurrentUser().getUid(), latLng);
            //if (!isListeningGetLocationsHashMap) {
            //listening to locationsHashMapObserver only one time
            getViewModel().getLocationsHashMap()
                    .observe(MapsFragment.this, locationsHashMapObserver);
            //isListeningGetLocationsHashMap = true;
            //}
        };
    }

    private void setLocationsObserver() {
        locationsHashMapObserver = stringGeoLocationHashMap -> {
            isOnLocationChangeListening = true;

            //called when hash map values changed,on peopleLocation exit or enter,called when on location changed
            //removing all previous marker
            removeAll();
            removePreviousCircleZone();

            //looping through hash map filled with locations
            for (final Map.Entry<String, GeoLocation> entry : stringGeoLocationHashMap.entrySet()) {

                Log.d(TAG, "setLocationsObserver: " + entry.getKey());
                double lat = entry.getValue().latitude;
                double lng = entry.getValue().longitude;
                locLatLng = new LatLng(lat, lng);


                if (entry.getKey().equals(getCurrentUser().getUid())) {
                    //me
                    markerOptions = new MarkerOptions().position(locLatLng)
                            .draggable(true)
                            .title("YOU")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_myloc2));
                } else {

                    //other peoples
                    markerOptions = new MarkerOptions().position(locLatLng)
                            .title(entry.getKey())
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_standing));
                }


                marker = mMap.addMarker(markerOptions);

                marker.showInfoWindow();
                if (entry.getKey().equals(getCurrentUser().getUid())) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng, 15.5f));
                }

                addHelpingZone(entry.getKey(), locLatLng);
            }
        };
    }

    private void removeAll() {
        Log.d(TAG, "removeAll: in");
        mMap.clear();

    }

    private void removePreviousCircleZone() {
        if (circle != null) {
            circle.remove();
        }
    }

    private void addHelpingZone(String key, LatLng latLng) {
        if (key.equals(getCurrentUser().getUid())) {
            //show circle only to my peopleLocation only

            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng);

            circleOptions.fillColor(getResources().getColor(R.color.transparent_blue));
            circleOptions.strokeColor(getResources().getColor(R.color.transparent_blue));
            circleOptions.radius(500); // In meters
            circle = mMap.addCircle(circleOptions);
        }
    }

    @Override
    public void onHiddenChanged(boolean isHidden) {

        if (!isHidden) {
            showFabIcon();
        }
        super.onHiddenChanged(isHidden);

    }
}