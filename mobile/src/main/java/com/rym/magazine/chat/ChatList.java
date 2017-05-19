package com.rym.magazine.chat;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.rym.magazine.R;
import com.rym.magazine.chat.model.ChatListUser;
import com.rym.magazine.chat.model.UserModel;
import com.rym.magazine.mag.DetailArticle;

public class ChatList extends AppCompatActivity {
    Toolbar mToolbar;
    //recyclerview object
    private RecyclerView recyclerView;

    //adapter object
    private RecyclerView.Adapter adapter;

    //database reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog progressDialog;

    //list to hold all the uploaded images
    private List<UserModel> user;

    private String Topic, Surname, Name;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Showing and Enabling clicks on the Home/Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        Surname = pref.getString("Surname", "UnknownUser");
        Name = pref.getString("OtherName", "");

        Bundle bundle = getIntent().getExtras();
        Topic = bundle.getString("Topic");
        Button topic = (Button)findViewById(R.id.topic);
        topic.setText(Topic);
        editor.putString("ClickedTopic",Topic);
        editor.apply();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        progressDialog = new ProgressDialog(this);

        user = new ArrayList<>();


        //displaying progress dialog while fetching images
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = mDatabase.child("ChatTopicsUser");
        DatabaseReference mDatabase3 = mDatabase2.child(Topic);
        String uploadSaved = pref.getString("saved"+Topic+"Chat", null);
        if(uploadSaved != null) //if we have list saved
        {   progressDialog.dismiss();
            //creating a list of YOUR_CLASS from the string response
            Type type = new TypeToken<ArrayList<UserModel>>() {
            }.getType();
            final List<UserModel> list = new Gson().fromJson(uploadSaved, type);
            //now adding the list to your arraylist
            if (list != null && list.size() > 0) {
                adapter = new MyAdapter2(getApplicationContext(), list);
                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
            }
        }

        //adding an event listener to fetch values
        mDatabase3.addListenerForSingleValueEvent(new ValueEventListener() {
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("RYM_Mag_11", MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //dismissing the progress dialog
                progressDialog.dismiss();
                //iterating through all the values in database
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    UserModel upload = postSnapshot.getValue(UserModel.class);
                    user.add(upload);

                    Gson gson = new Gson();
                    String jsonText = gson.toJson(user);
                    editor.putString("saved"+Topic+"Chat", jsonText);
                    editor.putString("ClickedTopic",Topic);
                    editor.apply();
                }
                //creating adapter
                adapter = new MyAdapter2(getApplicationContext(), user);

                //adding adapter to recyclerview
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ChatList.this.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue("online");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseDatabaseReference.child(Constants.DATABASE_PATH_PROFILE+"/" +Surname+" "+Name).child("status").setValue(Calendar.getInstance().getTime().getTime()+"");
    }
}
