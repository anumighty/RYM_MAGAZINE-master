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

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.rym.magazine.Constants;
import com.rym.magazine.R;
import com.rym.magazine.adapter.DrawerAdapter;
import com.rym.magazine.fragment.EntriesListFragment;
import com.rym.magazine.mag.MainActivity;
import com.rym.magazine.parser.OPML;
import com.rym.magazine.provider.FeedData.EntryColumns;
import com.rym.magazine.provider.FeedData.FeedColumns;
import com.rym.magazine.service.FetcherService;
import com.rym.magazine.service.RefreshService;
import com.rym.magazine.utils.PrefUtils;
import com.rym.magazine.utils.UiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_CURRENT_DRAWER_POS = "STATE_CURRENT_DRAWER_POS";

    private static final String FEED_UNREAD_NUMBER = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ')';

    private static final int LOADER_ID = 0;
    private static final int PERMISSIONS_REQUEST_IMPORT_FROM_OPML = 1;

    private EntriesListFragment mEntriesFragment;
    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
    private ListView mDrawerList;
    private DrawerAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private int mCurrentDrawerPos;
    ImageView iv;
    static int i=0;

    //For Image switcher
    private ImageView advertBanner;
    private String Ads;



    private static final int[] TOPIC_NAME = new int[]{R.string.ng_news_top_stories};

    private static final String[] TOPIC_CODES = new String[]{"x"};

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mAdvert = mRootRef.child("advert");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mEntriesFragment = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.entries_list_fragment);

        mTitle = getTitle();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLeftDrawer = findViewById(R.id.left_drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDrawerItem(position);
                if (mDrawerLayout != null) {
                    mDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(mLeftDrawer);
                        }
                    }, 50);
                }
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        if (savedInstanceState != null) {
            mCurrentDrawerPos = savedInstanceState.getInt(STATE_CURRENT_DRAWER_POS);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            // starts the service independent to this activity
            startService(new Intent(this, RefreshService.class));
        } else {
            stopService(new Intent(this, RefreshService.class));
        }
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ON_OPEN_ENABLED, false)) {
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }

        // Ask the permission to import the feeds if there is already one backup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && new File(OPML.BACKUP_OPML).exists()) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.storage_request_explanation).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_IMPORT_FROM_OPML);
                    }
                });
                builder.show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_IMPORT_FROM_OPML);
            }
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


        //Get date and increment number of views
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        String strDate = "Current Date : " + mdformat.format(calendar.getTime());
        DatabaseReference mViews = mRootRef.child("RYM Feeds Daily Views");
        DatabaseReference mViewsRef = mViews.child(strDate);
        mViewsRef.runTransaction(new Transaction.Handler() {
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

                } else {
                }
            }
        });

    }




    public void onStart(){
        super.onStart();


    }

    public void onResume(){
        super.onResume();
        //Get date and increment number of views
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        String strDate = "Current Date : " + mdformat.format(calendar.getTime());
        DatabaseReference mViews = mRootRef.child("RYM Feeds Daily Views");
        DatabaseReference mViewsRef = mViews.child(strDate);
        mViewsRef.runTransaction(new Transaction.Handler() {
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

                } else {
                }
            }
        });

    }










    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_DRAWER_POS, mCurrentDrawerPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // We reset the current drawer position
        selectDrawerItem(0);
    }

    public void onBackPressed() {
        // Before exiting from app the navigation drawer is opened
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            final SharedPreferences.Editor editor = pref.edit();
            Boolean Deeplink = pref.getBoolean("Deeplink",true);
            if (Deeplink){
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
                editor.putBoolean("Deeplink", true);
                editor.apply();
                // close this activity
                finish();
            }
            else {
                super.onBackPressed();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
                editor.putBoolean("Deeplink",true);
                editor.apply();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void onClickBack(View view) {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        Animation animFadein = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_in);
        view.startAnimation(animFadein);
        Boolean Deeplink = pref.getBoolean("Deeplink",true);
        if (Deeplink){
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
            editor.putBoolean("Deeplink", true);
            editor.apply();
            // close this activity
            finish();
        }
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
            editor.putBoolean("Deeplink", true);
            editor.apply();
        }
    }

    public void onClickEditFeeds(View view) {
                Animation animFadein = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_in);
                view.startAnimation(animFadein);
                startActivity(new Intent(this, EditFeedsListActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            }

            public void onClickAdd(View view) {
                Animation animFadein = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_in);
                view.startAnimation(animFadein);
                startActivity(new Intent(HomeActivity.this, AddGoogleNewsActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            }

            public void onClickSettings(View view) {
                Animation animFadein = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_in);
                view.startAnimation(animFadein);
                startActivity(new Intent(this, GeneralPrefsActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            }

            @Override
            protected void onPostCreate(Bundle savedInstanceState) {
                super.onPostCreate(savedInstanceState);
                // Sync the toggle state after onRestoreInstanceState has occurred.
                if (mDrawerToggle != null) {
                    mDrawerToggle.syncState();
                }
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                super.onConfigurationChanged(newConfig);
                if (mDrawerToggle != null) {
                    mDrawerToggle.onConfigurationChanged(newConfig);
                }
            }

            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                CursorLoader cursorLoader = new CursorLoader(this, FeedColumns.GROUPED_FEEDS_CONTENT_URI, new String[]{FeedColumns._ID, FeedColumns.URL, FeedColumns.NAME,
                        FeedColumns.IS_GROUP, FeedColumns.ICON, FeedColumns.LAST_UPDATE, FeedColumns.ERROR, FEED_UNREAD_NUMBER}, null, null, null
                );
                cursorLoader.setUpdateThrottle(Constants.UPDATE_THROTTLE_DELAY);
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                if (mDrawerAdapter != null) {
                    mDrawerAdapter.setCursor(cursor);
                } else {
                    mDrawerAdapter = new DrawerAdapter(this, cursor);
                    mDrawerList.setAdapter(mDrawerAdapter);

                    // We don't have any menu yet, we need to display it
                    mDrawerList.post(new Runnable() {
                        @Override
                        public void run() {
                            selectDrawerItem(mCurrentDrawerPos);
                        }
                    });
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> cursorLoader) {
                mDrawerAdapter.setCursor(null);
            }

            private void selectDrawerItem(int position) {
                mCurrentDrawerPos = position;

                Uri newUri;
                boolean showFeedInfo = true;

                switch (position) {
                    case 0:
                        newUri = EntryColumns.UNREAD_ENTRIES_CONTENT_URI;
                        break;
                    case 1:
                        newUri = EntryColumns.CONTENT_URI;
                        break;
                    case 2:
                        newUri = EntryColumns.FAVORITES_CONTENT_URI;
                        break;
                    default:
                        long feedOrGroupId = mDrawerAdapter.getItemId(position);
                        if (mDrawerAdapter.isItemAGroup(position)) {
                            newUri = EntryColumns.ENTRIES_FOR_GROUP_CONTENT_URI(feedOrGroupId);
                        } else {
                            newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(feedOrGroupId);
                            showFeedInfo = false;
                        }
                        mTitle = mDrawerAdapter.getItemName(position);
                        break;
                }

                if (!newUri.equals(mEntriesFragment.getUri())) {
                    mEntriesFragment.setData(newUri, showFeedInfo);
                }

                mDrawerList.setItemChecked(position, true);

                // First open => we open the drawer for you
                if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
                    PrefUtils.putBoolean(PrefUtils.FIRST_OPEN, false);
                    if (mDrawerLayout != null) {
                        mDrawerLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDrawerLayout.openDrawer(mLeftDrawer);
                            }
                        }, 500);
                    }

                    //for (int topic = 0; topic < TOPIC_NAME.length; topic++) {
                    //  String url;
                    //if (TOPIC_CODES[topic].equals("x")) {
                    //  url = "http://punchng.com/feed/";
                    //FeedDataContentProvider.addFeed(this, url, getString(TOPIC_NAME[topic]), true);
                    // }
                    //}

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.welcome_title)
                            .setItems(new CharSequence[]{getString(R.string.google_news_title)}, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(HomeActivity.this, AddGoogleNewsActivity.class));

                                }
                            });
                    builder.show();
                }

                // Set title & icon
                switch (mCurrentDrawerPos) {
                    case 0:
                        getSupportActionBar().setTitle(R.string.unread_entries);
                        getSupportActionBar().setIcon(R.drawable.ic_statusbar_rss);
                        break;
                    case 1:
                        getSupportActionBar().setTitle(R.string.all_entries);
                        getSupportActionBar().setIcon(R.drawable.ic_statusbar_rss);
                        break;
                    case 2:
                        getSupportActionBar().setTitle(R.string.favorites);
                        getSupportActionBar().setIcon(R.drawable.rating_important);
                        break;
                    default:
                        getSupportActionBar().setTitle(mTitle);
                        break;
                }

                // Put the good menu
                invalidateOptionsMenu();
            }

            @Override
            public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                switch (requestCode) {
                    case PERMISSIONS_REQUEST_IMPORT_FROM_OPML: {
                        // If request is cancelled, the result arrays are empty.
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            new Thread(new Runnable() { // To not block the UI
                                @Override
                                public void run() {
                                    try {
                                        // Perform an automated import of the backup
                                        OPML.importFromFile(OPML.BACKUP_OPML);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }).start();
                        }
                        return;
                    }
                }
            }
        }
