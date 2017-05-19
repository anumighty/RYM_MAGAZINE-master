package com.rym.magazine.chat;

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

import java.util.List;

import com.rym.magazine.R;
import com.rym.magazine.chat.adapter.CircleTransform;
import com.rym.magazine.chat.model.ChatListUser;
import com.rym.magazine.chat.model.UserModel;

/**
 * Created by Anumightytm on 4/12/2017.
 */
public class MyAdapter2 extends RecyclerView.Adapter<MyAdapter2.ViewHolder> {

    private Context context;
    private List<UserModel> user;

    public MyAdapter2(Context context, List<UserModel> user) {
        this.user = user;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_images, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserModel user2 = user.get(position);

        holder.FullName.setText(user2.getName());
        holder.Counsellor.setText("C: "+user2.getCounsellor());

        Glide.with(context).load(user2.getPhoto_profile()).into(holder.profPics);




        holder.Rel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent i = new Intent(context, Chat.class);
                i.putExtra("Full_Name", user2.getName());
                i.putExtra("userPicsUrl", user2.getPhoto_profile());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView FullName, UserStatus, Counsellor;
        public ImageView profPics;
        public RelativeLayout Rel;

        public ViewHolder(View itemView) {
            super(itemView);

            FullName = (TextView) itemView.findViewById(R.id.name);
            Counsellor = (TextView) itemView.findViewById(R.id.counsellor);
            profPics = (ImageView) itemView.findViewById(R.id.profPics);
            Rel = (RelativeLayout)itemView.findViewById(R.id.rel);

        }
    }
}