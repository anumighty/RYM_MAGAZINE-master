package com.rym.magazine.mag;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.rym.magazine.R;
import com.google.gson.Gson;
import com.rym.magazine.activity.BaseActivity;
import com.rym.magazine.activity.HomeActivity;

import java.util.List;

public class DetailArticle extends BaseActivity {

    private ImageView imageHeadFull;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;
    private ImageView image5;
    private TextView content1;
    private TextView content2;
    private TextView content3;
    private TextView content4;
    private TextView content5;
    private TextView title;
    private TextView writer;
    private ProgressBar progressBar;
    private Toolbar mToolbar;
    LinearLayout Whatsapp,Facebook,Like,Comment;
    TextView likenumber, whatsappnumber, facebooknumber, commentnumber;
    public String Articlelikes, Title, Whatsappcount, Facebookcount, PrevComment;
    private boolean Articlelikebefore, commentBefore;
    ImageView LikeImage, CommentImage;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mLike = mRootRef.child("Likes");
    DatabaseReference mFacebook = mRootRef.child("Facebook");
    DatabaseReference mWhatsapp = mRootRef.child("Whatsapp");
    DatabaseReference mComments = mRootRef.child("Comments Count");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_detail);


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
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // setting up text views and stuff
        setUpUIViews();

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        // recovering data from MainActivity, sent via intent
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String json = bundle.getString("articleModel");
            ArticleListModel articleModel = new Gson().fromJson(json, ArticleListModel.class);

            // Then later, when you want to display image
            Glide.with(getApplication()).load(articleModel.getImageHeadFull())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(imageHeadFull);

            Glide.with(getApplication()).load(articleModel.getImage1())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(image1);

            Glide.with(getApplication()).load(articleModel.getImage2())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(image2);

            Glide.with(getApplication()).load(articleModel.getImage3())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(image3);

            Glide.with(getApplication()).load(articleModel.getImage4())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(image4);

            Glide.with(getApplication()).load(articleModel.getImage5())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(image5);



            title.setText(articleModel.getTitle());
            editor.putString("Title", articleModel.getTitle());
            editor.apply();
            Title = articleModel.getTitle();
            writer.setText(articleModel.getWriter());
            content1.setText(Html.fromHtml(articleModel.getContent1()));
            content2.setText(Html.fromHtml(articleModel.getContent2()));
            content3.setText(Html.fromHtml("<h2>Title</h2><br><p>Description here</p>"));
            content4.setText(Html.fromHtml(articleModel.getContent1()));
            content5.setText(Html.fromHtml(articleModel.getContent5()));

        }

        //Get the value of total likes, whatsappShare, facebookShare and Comment from server
        childListener();
        WhatsappchildListener();
        FacebookchildListener();
        CommentchildListener();

        //Check for previous WhatsappShare count
        Whatsappcount = pref.getString(Title + "-whatsapp", "0");
        whatsappnumber = (TextView) findViewById(R.id.whatsapp2);
        whatsappnumber.setText(Whatsappcount);
        //Whatsapp Share button clicked
        Whatsapp = (LinearLayout)findViewById(R.id.whatsapp);
        Whatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    String json = bundle.getString("articleModel");
                    ArticleListModel articleModel = new Gson().fromJson(json, ArticleListModel.class);
                    Animation animFadein = AnimationUtils.loadAnimation(DetailArticle.this, R.anim.fade_in);
                    Whatsapp.startAnimation(animFadein);
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Have you read the article titled " + articleModel.getTitle() + "? I have and I enjoyed it. Visit www.therymmagazine.com to download RYM MAGAZINE Android app. You will really love it.");
                    try {
                        startActivity(whatsappIntent);
                        increaseWhatsappShare();
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Whatsapp have not been installed.", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

        //Check for previous FacebookShare count
        Facebookcount = pref.getString(Title + "-facebook", "0");
        facebooknumber = (TextView) findViewById(R.id.whatsapp2);
        facebooknumber.setText(Facebookcount);
        //When Facebook Share buton is clicked
        Facebook = (LinearLayout)findViewById(R.id.facebook);
        Facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Have you read the article titled \" + articleModel.getTitle() + \"? I have and I enjoyed it. Visit www.therymmagazine.com to download RYM MAGAZINE Android app. You will really love it.");
                PackageManager pm = v.getContext().getPackageManager();
                List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
                for (final ResolveInfo app : activityList) {
                    if ((app.activityInfo.name).contains("facebook")) {
                        final ActivityInfo activity = app.activityInfo;
                        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                        shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |             Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        shareIntent.setComponent(name);
                        v.getContext().startActivity(shareIntent);
                        increaseFacebookShare();
                        break;
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Facebook have not been installed.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        //Check for previous Like count
        Articlelikes = pref.getString(Title+"-likes", "0");
        Articlelikebefore = pref.getBoolean(Title+"-likebefore", false);
        if (Articlelikebefore){
            LikeImage = (ImageView)findViewById(R.id.like1);
            LikeImage.setImageResource(R.drawable.like2);
        }
        final boolean likeArt = Articlelikebefore;
        likenumber = (TextView) findViewById(R.id.like3);
        likenumber.setText(Articlelikes);

        //When LIKE button is clicked
        Like = (LinearLayout)findViewById(R.id.like);
        Like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Animation animFadein = AnimationUtils.loadAnimation(DetailArticle.this, R.anim.fade_in);
                Like.startAnimation(animFadein);
                isConnected();
            }

        });

        //Check for previous Comment count
        commentBefore = pref.getBoolean("CommentBefore", false);
        if (commentBefore){
            CommentImage = (ImageView)findViewById(R.id.comment1);
            CommentImage.setImageResource(R.drawable.comment2);
        }
        PrevComment = pref.getString(Title + "-comment", "0");
        commentnumber = (TextView) findViewById(R.id.comment2);
        commentnumber.setText(PrevComment);
        //Comment button clicked
        Comment = (LinearLayout)findViewById(R.id.comments);
        Comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Animation animFadein = AnimationUtils.loadAnimation(DetailArticle.this, R.anim.fade_in);
                Comment.startAnimation(animFadein);
                startActivity(new Intent(DetailArticle.this, ArticleComment.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            }

        });
    }



    private void setUpUIViews() {
        imageHeadFull = (ImageView)findViewById(R.id.imageHeadFull);
        image1 = (ImageView)findViewById(R.id.image1);
        image2 = (ImageView)findViewById(R.id.image2);
        image3 = (ImageView)findViewById(R.id.image3);
        image4 = (ImageView)findViewById(R.id.image4);
        image5 = (ImageView)findViewById(R.id.image5);
        title = (TextView)findViewById(R.id.title);
        content1 = (TextView)findViewById(R.id.content1);
        content2 = (TextView)findViewById(R.id.content2);
        content3 = (TextView)findViewById(R.id.content3);
        content4 = (TextView)findViewById(R.id.content4);
        content5 = (TextView)findViewById(R.id.content5);
        writer = (TextView)findViewById(R.id.writer);

    }

    public boolean isConnected() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            String checkMB = likenumber.getText().toString();
            if (checkMB.equals("0")) {
                Toast.makeText(getApplicationContext(), "Unable to connect to RYM server, check your data", Toast.LENGTH_LONG).show();
            } else {
                Articlelikebefore = pref.getBoolean(Title+"-likebefore", false);
                final Boolean likeArt = Articlelikebefore;
                if (likeArt) {
                    Toast.makeText(getApplicationContext(), "NOTE: You can only LIKE once", Toast.LENGTH_LONG).show();
                } else {
                    incrementCounter();
                    editor.putBoolean(Title + "-likebefore", true);
                    editor.apply();
                }
            }
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Kindly turn on your internet service to LIKE", Toast.LENGTH_LONG).show();
            return false;

        }
    }

    //Do the actual liking by sending incrementing RYM server by 1
    public void incrementCounter() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        DatabaseReference mLikeCount = mLike.child(Title);
        likenumber = (TextView) findViewById(R.id.like3);
        mLikeCount.runTransaction(new Transaction.Handler() {
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
                    Toast.makeText(getApplicationContext(), "Failed to implement LIKE. Try again", Toast.LENGTH_LONG).show();

                } else {
                    childListener();
                    Articlelikes = pref.getString(Title + "-likes", "0");
                    Toast.makeText(getApplicationContext(), "Thanks, article liked. Likes: "+ Articlelikes, Toast.LENGTH_LONG).show();
                    LikeImage = (ImageView)findViewById(R.id.like1);
                    LikeImage.setImageResource(R.drawable.like2);
                    editor.putBoolean(Title+"-likebefore", true);
                    editor.apply();
                }
            }
        });
    }

    //Increament the Whastsapp share on RYM Database
    public void increaseWhatsappShare() {
        DatabaseReference mWhatsappCount = mWhatsapp.child(Title);
        mWhatsappCount.runTransaction(new Transaction.Handler() {
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
                    Toast.makeText(getApplicationContext(), "Failed to share", Toast.LENGTH_LONG).show();

                } else {
                    WhatsappchildListener();
                }
            }
        });
    }

    //Increament the Facebook share on RYM Database
    public void increaseFacebookShare() {
        DatabaseReference mFacebookCount = mFacebook.child(Title);
        mFacebookCount.runTransaction(new Transaction.Handler() {
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
                    Toast.makeText(getApplicationContext(), "Failed to share", Toast.LENGTH_LONG).show();

                } else {
                    FacebookchildListener();
                }
            }
        });
    }


    public void childListener(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        DatabaseReference mLikeCount = mLike.child(Title);
        Query queryRef = mLikeCount.orderByValue();

        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ans = (Object) snapshot.getValue();
                likenumber.setText(String.valueOf(ans));

                editor.putString(Title + "-likes", String.valueOf(ans));
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to read number of LIKES from RYM server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void WhatsappchildListener(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        DatabaseReference mWhatsappCount = mWhatsapp.child(Title);
        Query queryRef = mWhatsappCount.orderByValue();

        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ans = (Object) snapshot.getValue();
                whatsappnumber = (TextView) findViewById(R.id.whatsapp2);
                whatsappnumber.setText(String.valueOf(ans));
                editor.putString(Title + "-whatsapp", String.valueOf(ans));
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void FacebookchildListener(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        DatabaseReference mFacebookCount = mFacebook.child(Title);
        Query queryRef = mFacebookCount.orderByValue();

        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ans = (Object) snapshot.getValue();
                facebooknumber = (TextView) findViewById(R.id.facebook2);
                facebooknumber.setText(String.valueOf(ans));
                editor.putString(Title + "-facebook", String.valueOf(ans));
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void CommentchildListener(){
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        DatabaseReference mCommentCount = mComments.child(Title);
        Query queryRef = mCommentCount.orderByValue();

        queryRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object ans = (Object) snapshot.getValue();
                commentnumber = (TextView) findViewById(R.id.comment2);
                commentnumber.setText(String.valueOf(ans));
                editor.putString(Title + "-comment", String.valueOf(ans));
                editor.apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void onResume(){
        super.onResume();
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        commentBefore = pref.getBoolean("CommentBefore", false);
        if (commentBefore){
            CommentImage = (ImageView)findViewById(R.id.comment1);
            CommentImage.setImageResource(R.drawable.comment2);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DetailArticle.this.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

}
