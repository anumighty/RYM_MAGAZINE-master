package com.rym.magazine.chat.OnlineChatTabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.rym.magazine.R;

import java.io.FileInputStream;

/**
 * Created by Anumightytm on 4/24/2017.
 */
public class Tab3Profile extends Fragment {
    private Bitmap bitmap,b;
    private ImageView profPics;
    private EditText surname, otherNames;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        viewPager = (ViewPager)getActivity().findViewById(R.id.container);


        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        /*profPics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadein = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
                profPics.startAnimation(animFadein);
                    Intent intent = new Intent(getContext(),FullScreenImageActivity.class);
                    intent.putExtra("Topic", "Relationship");;
                    startActivity(intent);
            }
        });*/

        final SharedPreferences pref = this.getActivity().getSharedPreferences("RYM_Mag_11", 0);
        profPics = (ImageView)rootView.findViewById(R.id.profPics);
        surname = (EditText)rootView.findViewById(R.id.surname);
        otherNames = (EditText)rootView.findViewById(R.id.other_name);
        surname.setText(pref.getString("Surname", "UnknownUser"));
        surname.setFocusable(false);
        otherNames.setText(pref.getString("OtherName", ""));
        otherNames.setFocusable(false);
        //Retrieve image and put in imageView
        getImageBitmap(getActivity(), "profile", "pics");
        return rootView;
    }

    public Bitmap getImageBitmap(Context context,String name,String extension) {
        name = name + "." + extension;
        try {
            FileInputStream fis = context.openFileInput(name);
            b = BitmapFactory.decodeStream(fis);
            profPics.setImageBitmap(b);
            fis.close();
            return b;
        } catch (Exception e) {
        }
        return null;
    }

}
