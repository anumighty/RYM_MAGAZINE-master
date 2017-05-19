package com.rym.magazine.mag;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rym.magazine.R;
import com.rym.magazine.activity.AboutActivity;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ArticleComment extends AppCompatActivity {

    Toolbar mToolbar;

    EditText nameTxt, comTxt;
    EditText locate;
    ListView lv;
    static final String TAG = "Main Acvity";
    ArrayAdapter<String> valuesAdapter;
    ArrayList<String> displayArray;
    ArrayList<String> keysArray;
    ProgressBar progressBar;
    public RelativeLayout v;
    EditText message;
    public String Title;
    Button save_profile;
    ImageView SendBtn;
    String chat;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mComments = mRootRef.child("Comments");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Showing and Enabling clicks on the Home/Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        v = (RelativeLayout) findViewById(R.id.v);

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        Title = pref.getString("Title", "");
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            lv = (ListView) findViewById(R.id.lv);
            displayArray = new ArrayList<>();
            keysArray = new ArrayList<>();

            showProgressBar();
            checkPrevComments();
            displayArray.clear();
            valuesAdapter = new ArrayAdapter<String>(this, R.layout.oct1_chat_content, displayArray);
            lv.setAdapter(valuesAdapter);
            lv.setOnItemClickListener(itemClickListener);
            DatabaseReference mCommentsSend = mComments.child(Title);
            mCommentsSend.addChildEventListener(childEventListener);


            //Show Profile Dialog OR check if users is already has a profile
            Boolean profile = pref.getBoolean("profile", false);
            if (profile==false) {
                showDialog(); //Show profile dialog if user doesn't have a profile
            }


            //Send user's message at button pressed
            SendBtn = (ImageView) findViewById(R.id.sendBtn);
            SendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation animFadein = AnimationUtils.loadAnimation(ArticleComment.this, R.anim.fade_in);
                    SendBtn.startAnimation(animFadein);
                    message = (EditText) findViewById(R.id.message);
                    chat = message.getText().toString();
                    sendChat(chat);
                    message.setText("");
                    editor.putBoolean(Title + "incomingMessage", false);
                    editor.apply();
                }
            });


    }


    //Check for previously viewed Business Discussions
    private void checkPrevComments() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString(Title+"previousComment", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        if(json != null){
            displayArray.clear();
            displayArray = gson.fromJson(json, type);
            lv = (ListView) findViewById(R.id.lv);
            valuesAdapter = new ArrayAdapter<String>(this, R.layout.oct1_chat_content, displayArray);
            lv.setAdapter(valuesAdapter);
        } else {
            showProgressBar();
        }

    }


    private void showProgressBar(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    //Display profile DIALOG
    private void showDialog() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        final Dialog d = new Dialog(this,R.style.customDialog);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setTitle("User Profile");
        d.setCancelable(false);
        d.setContentView(R.layout.comment_profile_dialog);

        nameTxt = (EditText) d.findViewById(R.id.nameEditText);
        nameTxt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        locate = (EditText) d.findViewById(R.id.location);
        save_profile = (Button) d.findViewById(R.id.save_profile);

        String name = pref.getString("Username","");
        String location = pref.getString("Userlocation", "");
        nameTxt.setText(name);
        locate.setText(location);

        save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameString = nameTxt.getText().toString();
                String locationString = locate.getText().toString();
                LinearLayout l = (LinearLayout)d.findViewById(R.id.l);
                if (("".equals(nameString.trim()) || "".equals(locationString.trim()))) {
                    Snackbar.make(l, "Ensure you fill all boxes", Snackbar.LENGTH_LONG).show();
                    return;
                }
                editor.putString("Username", nameString);
                editor.putString("Userlocation", locationString);
                editor.putBoolean("profile", true);
                editor.apply();
                Snackbar.make(v, "Successfully! You can now comment", Snackbar.LENGTH_LONG).show();
                d.dismiss();
            }
        });

        //SHOW
        d.show();
    }


    //Send user's message
    private void sendChat(final String chat){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("CommentBefore",true);
        editor.apply();

        String name = pref.getString("Username", "Unknown");
        String location = pref.getString("Userlocation", "Unknown");
        Title = pref.getString("Title", "");
        DatabaseReference mCommentsSend = mComments.child(Title);
        mCommentsSend.child(String.valueOf(formattedDate)).setValue(name + " - "+ location+":\n" + chat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                hideProgressBar();
                incrementCounter();

            }

        });

        //Automatically scroll down the listview
        lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                lv.smoothScrollToPosition(valuesAdapter.getCount());
            }
        }, 500);
    }

    public void incrementCounter() {
        DatabaseReference mComment = mRootRef.child("Comments Count");
        DatabaseReference mCommentCount = mComment.child(Title);
        mCommentCount.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(final MutableData currentData) {

                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot currentData) {
                if (firebaseError != null) {
                    Snackbar.make(v, "Failed to add COMMENT to RYM server. Try again", Snackbar.LENGTH_LONG).show();
                } else {
                }
            }
        });
    }


    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            showProgressBar();
            showNotification();
            Log.d(TAG, dataSnapshot.getKey() + ":" + dataSnapshot.getValue().toString());
            String keyAndValue = dataSnapshot.getValue().toString();
            displayArray.add(keyAndValue);
            keysArray.add(dataSnapshot.getKey());
            Gson gson = new Gson();
            String json = gson.toJson(displayArray);
            editor.putString(Title+"previousComment", json);
            editor.apply();
            updateListView();
            hideProgressBar();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            showNotification();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String deletedKey = dataSnapshot.getKey();
            int removedIndex = keysArray.indexOf(deletedKey);
            keysArray.remove(removedIndex);
            displayArray.remove(removedIndex);
            updateListView();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, dataSnapshot.getKey() +":" + dataSnapshot.getValue().toString());
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            hideProgressBar();
        }
    };

    private void showNotification(){

        //Creating a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        Intent notificationIntent = new Intent(ArticleComment.this, List_of_Articles.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ArticleComment.this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("RYM MAGAZINE");
        builder.setContentText("An article has been commented on");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private Boolean appON = false;
    public void updateListView() {

        if (appON) {
            valuesAdapter.notifyDataSetChanged();
            lv.invalidate();
            lv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lv.smoothScrollToPosition(valuesAdapter.getCount());
                }
            }, 1000);
            //Check if to play sound or not
            // If true
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            Boolean incomingMessage = pref.getBoolean("incomingMessage", false);
            if (incomingMessage) {
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        editor.putBoolean("incomingMessage", true);
                        editor.apply();
                    }
                }, 2 * 1000);


            }
            Log.d(TAG, "Length: " + displayArray.size());
        } else{
            valuesAdapter.notifyDataSetChanged();
            lv.invalidate();
            lv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    lv.smoothScrollToPosition(valuesAdapter.getCount());
                }
            }, 1000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    appON = true;
                }
            }, 5 * 1000);

        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };



    @Override
    public void onBackPressed() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("incomingMessage", false);
        editor.apply();
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.comment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
                return true;
            case R.id.comment_edit:
                showDialog();
        }

        return super.onOptionsItemSelected(item);
    }





}
