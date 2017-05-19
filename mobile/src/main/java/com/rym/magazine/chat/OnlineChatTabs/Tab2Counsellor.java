package com.rym.magazine.chat.OnlineChatTabs;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rym.magazine.R;
import com.rym.magazine.chat.OnlineChatTabs.CounsellorAdapter;
import com.rym.magazine.chat.model.CounsellorDataModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anumightytm on 4/24/2017.
 */
public class Tab2Counsellor extends Fragment {

    //recyclerview object
    private RecyclerView recyclerView;

    //adapter object
    private RecyclerView.Adapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<CounsellorDataModel> user;

    private String Topic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_counsellors, container, false);
        TextView CounsellorName = (TextView) rootView.findViewById(R.id.counsellorName);
        TextView CounsellorAbout = (TextView) rootView.findViewById(R.id.aboutCounsellor);
        ImageView CounsellorPics = (ImageView) rootView.findViewById(R.id.counsellorPics);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        recyclerView = (RecyclerView) rootView.findViewById(R.id.horizontal_recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManagaer);


        progressDialog = new ProgressDialog(getActivity());

        user = new ArrayList<>();

        //displaying progress dialog while fetching images
        final SharedPreferences pref = this.getActivity().getSharedPreferences("RYM_Mag_11", 0);
        final SharedPreferences.Editor editor = pref.edit();
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = mDatabase.child("CounsellorsData");
        String uploadSaved = pref.getString("counsellorData", null);
        if(uploadSaved != null) //if we have list saved
        {   progressDialog.dismiss();
            //creating a list of YOUR_CLASS from the string response
            Type type = new TypeToken<ArrayList<CounsellorDataModel>>() {
            }.getType();
            user = new Gson().fromJson(uploadSaved, type);
            //now adding the list to your arraylist
            if (user != null && user.size() > 0) {
                adapter = new CounsellorAdapter(getActivity(), user);
                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
            }
        }

        //adding an event listener to fetch values
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                progressDialog.dismiss();

                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    CounsellorDataModel upload = postSnapshot.getValue(CounsellorDataModel.class);
                    Gson gson = new Gson();
                    user.add(upload);
                    String jsonText = gson.toJson(user);
                    editor.putString("counsellorData", jsonText);
                    editor.apply();
                }
                //creating adapter
                adapter = new CounsellorAdapter(getActivity(), user);

                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        CounsellorName.setText(pref.getString("CounsellorName","")) ;
        CounsellorAbout.setText(pref.getString("CounsellorAbout","")) ;
        Glide.with(getActivity()).load(pref.getString("CounsellorPicsUrl","")).into(CounsellorPics);

        return rootView;
    }
}
