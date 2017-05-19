/**
 * Flym
 * <p/>
 * Copyright (c) 2012-2015 Frederic Julian
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rym.magazine.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rym.magazine.Constants;
import com.rym.magazine.R;
import com.rym.magazine.fragment.EntryFragment;
import com.rym.magazine.utils.PrefUtils;
import com.rym.magazine.utils.UiUtils;

import java.util.Timer;
import java.util.TimerTask;

public class EntryActivity extends BaseActivity {

    private EntryFragment mEntryFragment;

    //For Image switcher
    private ImageView advertBanner, Arrow;
    private String Ads;
    Boolean FirstFeedView;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mAdvert = mRootRef.child("advert2");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        mEntryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.entry_fragment);
        if (savedInstanceState == null) { // Put the data only the first time (the fragment will save its state)
            mEntryFragment.setData(getIntent().getData());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_ENTRIES_FULLSCREEN, false)) {
            setImmersiveFullScreen(true);
        }



        //Download and display Advert
        mAdvert.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ads = dataSnapshot.getValue(String.class);
                String gifUrl = Ads;
                advertBanner = (ImageView) findViewById(R.id.imageSwitcher);
                Glide.with(getApplication())
                        .load(gifUrl)
                        .into(advertBanner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("firstTimer", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        boolean firstUser = pref.getBoolean("newUser", true);
        if(firstUser){
            Toast.makeText(getApplicationContext(), "Swipe RIGHT to LEFT to view more feed", Toast.LENGTH_LONG).show();
            editor.putBoolean("newUser", false);
            editor.apply();
        }else{
        }

        TextView countdownTxt=(TextView)findViewById(R.id.countdownTxt2);
        countdownTxt.setSelected(true);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Bundle b = getIntent().getExtras();
            if (b != null && b.getBoolean(Constants.INTENT_FROM_WIDGET, false)) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
            finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mEntryFragment.setData(intent.getData());
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
    }
}