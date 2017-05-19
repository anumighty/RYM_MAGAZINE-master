package com.rym.magazine.chat.OnlineChatTabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rym.magazine.R;
import com.rym.magazine.chat.Chat;
import com.rym.magazine.chat.model.CounsellorDataModel;

import java.util.List;

/**
 * Created by Anumightytm on 5/6/2017.
 */

public class CounsellorAdapter extends RecyclerView.Adapter<com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter.ViewHolder> {

    private Context context;
    private List<CounsellorDataModel> user;

    public CounsellorAdapter(Context context, List<CounsellorDataModel> user) {
        this.user = user;
        this.context = context;
    }

    @Override
    public com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.counsellor_layout, parent, false);
        com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter.ViewHolder viewHolder = new com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter.ViewHolder holder, int position) {
        final SharedPreferences pref = context.getSharedPreferences("RYM_Mag_11", 0);
        final SharedPreferences.Editor editor = pref.edit();
        final CounsellorDataModel user2 = user.get(position);

        Glide.with(context).load(user2.getPicsUrl()).into(holder.picsTouch);

        holder.picsTouch.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                editor.putString("CounsellorName",user2.getName());
                editor.putString("CounsellorAbout",user2.getAbout());
                editor.putString("CounsellorPicsUrl",user2.getPicsUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView Pics, picsTouch;

        private ViewHolder(View itemView) {
            super(itemView);

            picsTouch = (ImageView)itemView.findViewById(R.id.counsellorPics1);

        }
    }
}