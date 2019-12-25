package com.dragontelnet.helpzone.model;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dragontelnet.helpzone.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TriggerViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView circleImageView;
    public TextView userName, distance, date, time;
    public ImageButton playBtn;
    //public MediaPlayer player;

    @BindView(R.id.help_requests_layout_dp)
    CircleImageView helpRequestsCircleDp;

    @BindView(R.id.help_requests_layout_username)
    TextView helpRequestsUsername;

    @BindView(R.id.help_requests_layout_distance)
    TextView helpRequestsDistance;

    @BindView(R.id.help_requests_layout_trigger_btn)
    ImageButton helpRequestsPlayBtn;

    @BindView(R.id.help_requests_layout_date)
    TextView helpRequestsDate;

    @BindView(R.id.help_requests_layout_time)
    TextView helpRequestsTime;

    public TriggerViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        circleImageView = helpRequestsCircleDp;
        userName = helpRequestsUsername;
        distance = helpRequestsDistance;
        playBtn = helpRequestsPlayBtn;
        date = helpRequestsDate;
        time = helpRequestsTime;
        //player=null;
    }

}
