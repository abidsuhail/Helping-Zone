package com.dragontelnet.helpzone.ui.fragments.peoples;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dragontelnet.helpzone.R;
import com.dragontelnet.helpzone.firebase.CurrentFuser;
import com.dragontelnet.helpzone.firebase.FirebaseRefs;
import com.dragontelnet.helpzone.model.PeopleLoc;
import com.dragontelnet.helpzone.model.PeopleLocViewHolder;
import com.dragontelnet.helpzone.model.User;
import com.dragontelnet.helpzone.ui.fragments.home.HomeFragmentViewModel;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.inject.Singleton;

import butterknife.BindView;
import butterknife.ButterKnife;

@Singleton
public class PeoplesDetailsFragment extends Fragment {
    private static final String TAG = "PeoplesDetailsFragment";
    @BindView(R.id.msg_tv)
    TextView msgTv;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.fragment_peoples_details_rv)
    RecyclerView recyclerView;
    private ValueEventListener userNameListener;
    private FirebaseRecyclerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: in");
        if (!isHidden()) {
            hideFabIcon();
        }
        View view = inflater.inflate(R.layout.fragment_peoples_details, container, false);
        ButterKnife.bind(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        checkPeoplesAvailability();
        populateList();

        getHomeFragmentViewModel().getLocationsHashMapLiveData().observe(this, new Observer<HashMap<String, GeoLocation>>() {
            @Override
            public void onChanged(HashMap<String, GeoLocation> stringGeoLocationHashMap) {
                //disable progress
                Log.d(TAG, "onChanged: in before removing observers");
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                adapter.startListening();

                removeObservers();
            }
        });

        //gettingCurrentUserLiveData();


        return view;
    }

    private void checkPeoplesAvailability() {
        getViewModel().isPeoplesNearbyExists().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPeopleExits) {

                if (isPeopleExits) {
                    //hide msg label
                    msgTv.setVisibility(View.GONE);
                } else {
                    //show msg label
                    msgTv.setVisibility(View.VISIBLE);

                }
            }
        });
    }

    private void hideFabIcon() {
        if (getActivity() != null) {
            FloatingActionButton fab = getActivity().findViewById(R.id.fab);
            fab.hide();
        }
    }

    private void removeObservers() {
        getHomeFragmentViewModel().getLocationsHashMapLiveData()
                .removeObservers(PeoplesDetailsFragment.this);

    }


    private PeoplesDetailsFragmentViewModel getViewModel() {
        return ViewModelProviders.of(this).get(PeoplesDetailsFragmentViewModel.class);
    }

    private HomeFragmentViewModel getHomeFragmentViewModel() {
        return ViewModelProviders.of(this).get(HomeFragmentViewModel.class);
    }

    private void populateList() {
        FirebaseRecyclerOptions<PeopleLoc> options =
                new FirebaseRecyclerOptions.Builder<PeopleLoc>()
                        .setQuery(FirebaseRefs.getNearbyUsersDistancesOfUidNodeRef(CurrentFuser.getCurrentFuser().getUid()),
                                PeopleLoc.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<PeopleLoc, PeopleLocViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PeopleLocViewHolder holder, int position, @NonNull final PeopleLoc people) {
                userNameListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final User user = dataSnapshot.getValue(User.class);
                            holder.userName.setText(user.getUserName());
                            if (!user.getImageUrl().equals("")) {
                                //have profile pic
                                Picasso.get().load(user.getImageUrl())
                                        .into(holder.profilePic);

                            } else {
                                //no profile pic set
                                holder.profilePic.setImageResource(R.drawable.ic_user_default);
                            }
                        } else {
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
            /*holder.triggerBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //clicked on trigger button via fcm
                        Log.d(TAG, "onClick: peopleUid " + people.getUid());
                        getViewModel().sendFCMToDevice(people.getUid());
                        return true;
                    }
                });*/
                FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(people.getUid())
                        .addListenerForSingleValueEvent(userNameListener);
                holder.distance.setText(people.getDistance() + " meters away");

            }

            @NonNull
            @Override
            public PeopleLocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.people_details_layout, parent, false);
                return new PeopleLocViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            hideFabIcon();
        }
    }
}
