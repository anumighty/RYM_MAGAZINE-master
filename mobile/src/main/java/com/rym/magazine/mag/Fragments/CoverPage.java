package com.rym.magazine.mag.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rym.magazine.R;
import com.rym.magazine.mag.MainActivity;


public class CoverPage extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.cover_page, container, false);

        TextView countdownTxt=(TextView)view.findViewById(R.id.countdownTxt);
        countdownTxt.setSelected(true);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");

        //Button buttonChangeText = (Button) view.findViewById(R.id.buttonFragmentInbox);

         //final TextView textViewInboxFragment = (TextView) view.findViewById(R.id.textViewInboxFragment);

        //buttonChangeText.setOnClickListener(new View.OnClickListener() {
           // @Override
            //public void onClick(View v) {

                //textViewInboxFragment.setText("This is the Inbox Fragment");
                //textViewInboxFragment.setTextColor(getResources().getColor(R.color.light_theme_color_primary_dark));

           // }
       // });




        return view;
    }

}
