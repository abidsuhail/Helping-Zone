package com.dragontelnet.helpzone.ui.fragments.helprequests;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.dragontelnet.helpzone.model.Trigger;
import com.dragontelnet.helpzone.model.TriggerViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HelpRequestsFragment extends Fragment {

    private static final String TAG = "HelpRequestsFragment";
    @BindView(R.id.fragment_help_requests_rv)
    RecyclerView recyclerView;
    @BindView(R.id.msg_tv)
    TextView msgTv;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private MediaPlayer player;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_help_requests, container, false);
        ButterKnife.bind(this, view);
        player = new MediaPlayer();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        hideFabIcon();
        checkRequestsEmptiness();
        populateList();
        return view;
    }

    private void checkRequestsEmptiness() {
        getViewModel().isRequestsExists().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isReqExists) {

                if (isReqExists) {
                    //hide msg tv
                    msgTv.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    //show msg tv
                    msgTv.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void hideFabIcon() {
        if (getActivity() != null) {
            FloatingActionButton fab = getActivity().findViewById(R.id.fab);
            if (fab.isShown()) {
                fab.hide();
            }
        }
    }

    private void populateList() {
        DatabaseReference query = FirebaseRefs.getSingleUserTriggersOfUidNodeRef(CurrentFuser
                .getCurrentFuser()
                .getUid());

        FirebaseRecyclerOptions<Trigger> options =
                new FirebaseRecyclerOptions.Builder<Trigger>()
                        .setQuery(query, Trigger.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Trigger, TriggerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TriggerViewHolder holder, int position, @NonNull Trigger trigger) {


                loadUsersProfilePic(trigger.getByUid(), holder);


                if (!trigger.getAudioLink().equals("")) {

                    //if there is valid audio link
                    //get audio link and setOnClick listener

                    //visible play button
                    if (holder.playBtn.getVisibility() == View.GONE) {
                        holder.playBtn.setVisibility(View.VISIBLE);
                        holder.playBtn.setImageResource(R.drawable.ic_play);
                    }
                    holder.playBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startPlayingAudio(holder, trigger);
                        }
                    });

                } else {
                    //if there is invalid audio link

                    //hide play button
                    if (holder.playBtn.getVisibility() == View.VISIBLE) {
                        holder.playBtn.setVisibility(View.GONE);
                    }
                }

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //delete trigger
                        if (holder.getAdapterPosition() > -1) {
                            deleteTrigger(getRef(holder.getAdapterPosition()));
                        }
                        return true;
                    }
                });
                setViewsInfo(trigger, holder);
            }

            @NonNull
            @Override
            public TriggerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.help_requests_layout, parent, false);

                return new TriggerViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void deleteTrigger(DatabaseReference currentTriggerRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete Request");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //remove current specific trigger
                currentTriggerRef.removeValue();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void setViewsInfo(Trigger trigger, TriggerViewHolder holder) {
        holder.userName.setText(trigger.getTitle());
        holder.distance.setText("Is " + trigger.getBody() + "m away from you");
        holder.date.setText("Date : " + trigger.getDate());
        holder.time.setText("Time : " + trigger.getTime());
    }

    private void loadUsersProfilePic(String triggerUid, TriggerViewHolder holder) {

        //not using UserDetailsFetcher class because for list,observer is not working properly
        FirebaseRefs.getSingleRegUserDetailsOfUidNodeRef(triggerUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("imageUrl").getValue().toString().equals("")) {
                            Picasso.get().load(dataSnapshot.child("imageUrl").getValue().toString())
                                    .into(holder.circleImageView);
                        } else {
                            holder.circleImageView.setImageResource(R.drawable.ic_user_default);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void startPlayingAudio(TriggerViewHolder holder, Trigger trigger) {
        if (player == null) {
            player = new MediaPlayer();
        }
        if (!player.isPlaying()) {
            Log.d(TAG, "run: not playing");
            holder.playBtn.setImageResource(R.drawable.ic_stop);
            try {
                Log.d(TAG, "onClick: audio link " + trigger.getAudioLink());

                //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDataSource(trigger.getAudioLink());

                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException ile) {
                //Toast.makeText(getActivity(), ile.getMessage(), Toast.LENGTH_SHORT).show();
            }
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "onPrepared: prepared");
                    mp.start();

                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying(holder);
                }
            });
        } else {
            Log.d(TAG, "run: playing");
            stopPlaying(holder);
        }

    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);
        if (!isHidden) {
            hideFabIcon();
        }

    }

    private void stopPlaying(TriggerViewHolder holder) {
        if (player != null) {
            try {
                player.stop();
                player.release();
                player = null;
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        holder.playBtn.setImageResource(R.drawable.ic_play);
    }

    private HelpRequestsViewModel getViewModel() {
        return ViewModelProviders.of(this).get(HelpRequestsViewModel.class);
    }
}
