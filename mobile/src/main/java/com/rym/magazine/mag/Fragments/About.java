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



public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Inbox");

        Button buttonChangeText = (Button) view.findViewById(R.id.buttonFragmentStarred);

        final TextView textViewStarredFragment = (TextView) view.findViewById(R.id.textViewStarredFragment);

        buttonChangeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textViewStarredFragment.setTextSize(40);
                textViewStarredFragment.setText("This is the Starred Fragment");
                textViewStarredFragment.setTextColor(getResources().getColor(R.color.color_accent));

            }
        });

        return view;
    }

}
