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

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.rym.magazine.R;
import com.rym.magazine.provider.FeedDataContentProvider;
import com.rym.magazine.utils.UiUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class AddGoogleNewsActivity extends BaseActivity {

    private static final int[] TOPIC_NAME = new int[]{R.string.google_news_top_stories, R.string.google_news_world, R.string.google_news_business,
            R.string.google_news_technology, R.string.google_news_entertainment, R.string.google_news_sports, R.string.google_news_science, R.string.google_news_health,
            R.string.ng_news_top_stories, R.string.nigeria_news_sports, R.string.nigeria_news_entertainment};

    private static final String[] TOPIC_CODES = new String[]{"null", "w", "b", "t", "e", "s", "snc", "m", "x", "y", "z"};

    private static final int[] CB_IDS = new int[]{R.id.cb_top_stories, R.id.cb_world, R.id.cb_business, R.id.cb_technology, R.id.cb_entertainment,
            R.id.cb_sports, R.id.cb_science, R.id.cb_health, R.id.ng_top_stories, R.id.ng_politics, R.id.ng_entertainment};
    private EditText mCustomTopicEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_add_google_news);
        mCustomTopicEditText = (EditText) findViewById(R.id.google_news_custom_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    public void onClickOk(View view) {
        for (int topic = 0; topic < TOPIC_NAME.length; topic++) {
            if (((CheckBox) findViewById(CB_IDS[topic])).isChecked()) {
                String url;
                if (TOPIC_CODES[topic].equals("null")) {
                    url = "http://news.google.com/news?hl=" + Locale.getDefault().getLanguage() + "&output=rss";
                }
                else if (TOPIC_CODES[topic].equals("x")) {
                    url = "http://punchng.com/feed/";
                }
                else if (TOPIC_CODES[topic].equals("y")) {
                    url = "https://news.google.com.ng/news/section?cf=all&pz=1&ned=en_ng&topic=s";
                }
                else if (TOPIC_CODES[topic].equals("z")) {
                    url = "https://news.google.com.ng/news/section?cf=all&pz=1&ned=en_ng&topic=e";
                }
                else {
                    url = "http://news.google.com/news?hl=" + Locale.getDefault().getLanguage() + "&output=rss";
                    url += "&topic=" + TOPIC_CODES[topic];
                }
                FeedDataContentProvider.addFeed(this, url, getString(TOPIC_NAME[topic]), true);
            }

        }


        String custom_topic = mCustomTopicEditText.getText().toString();
        if (!custom_topic.isEmpty()) {
            try {
                String url = "http://news.google.com/news?hl=" + Locale.getDefault().getLanguage() + "&output=rss&q=" + URLEncoder.encode(custom_topic, "UTF-8");
                FeedDataContentProvider.addFeed(this, url, custom_topic, true);
            } catch (UnsupportedEncodingException ignored) {
            }
        }

        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    public void onClickCancel(View view) {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }


}

