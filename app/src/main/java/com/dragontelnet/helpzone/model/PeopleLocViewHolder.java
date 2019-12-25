package com.dragontelnet.helpzone.model;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dragontelnet.helpzone.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleLocViewHolder extends RecyclerView.ViewHolder {
    public TextView userName, distance;
    /*public Button triggerBtn;*/
    public CircleImageView profilePic;
    public View itemView;

    public PeopleLocViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.peoples_details_layout_username);
        distance = itemView.findViewById(R.id.peoples_details_layout_distance);
        //triggerBtn=itemView.findViewById(R.id.peoples_details_layout_trigger_btn);
        profilePic = itemView.findViewById(R.id.peoples_details_layout_dp);
        this.itemView = itemView;
    }
}
