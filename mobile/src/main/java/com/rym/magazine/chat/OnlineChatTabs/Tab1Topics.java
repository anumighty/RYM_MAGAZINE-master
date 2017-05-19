package com.rym.magazine.chat.OnlineChatTabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileInputStream;

import com.rym.magazine.chat.Chat;
import com.rym.magazine.chat.ChatList;
import com.rym.magazine.R;

/**
 * Created by Anumightytm on 4/24/2017.
 */
public class Tab1Topics extends Fragment {
    private ImageView profPics;
    private RelativeLayout Relationship;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_topics, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));


        final SharedPreferences pref = this.getActivity().getSharedPreferences("RYM_Mag_11", 0);
        //Initialize views
        TextView Fullname = (TextView)rootView.findViewById(R.id.fullname);
        TextView Talk = (TextView)rootView.findViewById(R.id.talk);
        if(pref.getBoolean("Counsellor",false)){
            Talk.setText("SOMEONE NEEDS YOUR HELP!");
        }
        profPics = (ImageView)rootView.findViewById(R.id.profPics);
        Fullname.setText(pref.getString("Surname", "Unregistered User") + " " + pref.getString("OtherName", ""));

        getImageBitmap(getContext(), "profile", "pics");

        Relationship = (RelativeLayout)rootView.findViewById(R.id.one);
        Relationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                Relationship.startAnimation(animFadein);
                if(pref.getBoolean("Counsellor",false)){
                    Intent intent = new Intent(getContext(),ChatList.class);
                    intent.putExtra("Topic", "Relationship");
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(getContext(), Chat.class);
                    intent.putExtra("Topic", "Relationship");
                    startActivity(intent);
                }
            }
        });

        viewPager = (ViewPager)getActivity().findViewById(R.id.container);
        //When profile pics is clicked, it goes to profile
        profPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                profPics.startAnimation(animFadein);
                viewPager.setCurrentItem(2);
            }
        });

        return rootView;
    }

    public Bitmap getImageBitmap(Context context,String name,String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            profPics.setImageBitmap(b);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }
}
