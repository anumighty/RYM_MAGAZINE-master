package com.rym.magazine.mag;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rym.magazine.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class List_of_Articles extends AppCompatActivity{

    private final String URL_TO_HIT = "http://pastebin.com/raw/AKAWWjMJ";
    private TextView tvData, refresh;
    private ListView lvMovies;
    private ProgressDialog dialog;
    public LinearLayout network_error;
    public Animation advert;
    private Toolbar mToolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_all);

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

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Loading. Please wait...");

        lvMovies = (ListView)findViewById(R.id.lvMovies);
        network_error =(LinearLayout)findViewById(R.id.network_error);


        // To start fetching the data when app start, uncomment below line to start the async task.
        new JSONTask().execute(URL_TO_HIT);

        refresh=(TextView)findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                network_error=(LinearLayout)findViewById(R.id.network_error);
                network_error.setVisibility(View.GONE);
                new JSONTask().execute(URL_TO_HIT);
            }
        });


    }




    public class JSONTask extends AsyncTask<String,String, List<ArticleListModel> > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
            Gson gson = new Gson();
            final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            String articleModel_string = pref.getString("previousArticle", null);
            if(articleModel_string != null) //if we have list saved
            {
                //creating a list of YOUR_CLASS from the string response
                Type type = new TypeToken<ArrayList<ArticleListModel>>() {}.getType();
                final List<ArticleListModel> list = new Gson().fromJson(articleModel_string, type);
                //now adding the list to your arraylist
                if (list != null && list.size() > 0 ) {
                    MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.article_list, list);
                    lvMovies.setAdapter(adapter);
                    lvMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ArticleListModel articleModel = list.get(position);
                            Intent intent = new Intent(List_of_Articles.this, DetailArticle.class);
                            intent.putExtra("articleModel", new Gson().toJson(articleModel));
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                        }
                    });
                    dialog.dismiss();
                }else //ie list isn't saved yet
                {
                    //You may want to save the arraylist into Shared preference using previous method
                    dialog.show();
                }


            } else{
                dialog.show();
            }
        }


        @Override
        protected List<ArticleListModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line ="";
                while ((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray parentArray = parentObject.getJSONArray("articles");

                List<ArticleListModel> articleModelList = new ArrayList<>();

                Gson gson = new Gson();
                for(int i=0; i<parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);

                    ArticleListModel articleModel = gson.fromJson(finalObject.toString(), ArticleListModel.class);

                    articleModelList.add(articleModel);
                    final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    final SharedPreferences.Editor editor = pref.edit();
                    String companyModel_string = gson.toJson(articleModelList).toString(); //Converting it to String
                    editor.putString("previousArticle", companyModel_string); //now saving the String
                    editor.commit();
                }
                return articleModelList;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
                try {
                    if(reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return  null;
        }

        @Override
        protected void onPostExecute(final List<ArticleListModel> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result != null) {
                dialog.dismiss();
                MovieAdapter adapter = new MovieAdapter(getApplicationContext(), R.layout.article_list, result);
                lvMovies.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), "Articles updated!", Toast.LENGTH_SHORT).show();
                lvMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ArticleListModel articleModel = result.get(position);
                        Intent intent = new Intent(List_of_Articles.this, DetailArticle.class);
                        intent.putExtra("articleModel", new Gson().toJson(articleModel));
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                    }
                });
            } else {
                final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                String articleModel_string = pref.getString("previousArticle", null);
                if(articleModel_string == null) //if we have list saved {
                {
                    Toast.makeText(getApplicationContext(), "Oops! It seems your internet service is bad or not ON.", Toast.LENGTH_SHORT).show();
                    network_error =(LinearLayout)findViewById(R.id.network_error);
                    network_error.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Turn ON your internet service to update Articles", Toast.LENGTH_LONG).show();
                }


            }
        }
    }



    public class MovieAdapter extends ArrayAdapter {

        private List<ArticleListModel> articleModelList;
        private int resource;
        private LayoutInflater inflater;
        public MovieAdapter(Context context, int resource, List<ArticleListModel> objects) {
            super(context, resource, objects);
            articleModelList = objects;
            this.resource = resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if(convertView == null){
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.imageHeadCircle = (ImageView)convertView.findViewById(R.id.imageHeadCircle);
                holder.imageHeadFull = (ImageView)convertView.findViewById(R.id.imageHeadFull);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.briefDesc = (TextView)convertView.findViewById(R.id.briefDesc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            // Then later, when you want to display image
            Glide.with(getContext()).load(articleModelList.get(position).getImageHeadCircle())
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.no_image)
                    .into(holder.imageHeadCircle);

            Glide.with(List_of_Articles.this).load(articleModelList.get(position).getImageHeadFull())
                    .placeholder(R.drawable.no_image3)
                    .error(R.drawable.no_image4)
                    .into(holder.imageHeadFull);


            holder.title.setText(articleModelList.get(position).getTitle());
            holder.briefDesc.setText(articleModelList.get(position).getBriefDesc());

            return convertView;
        }


        class ViewHolder{
            private ImageView imageHeadCircle;
            private ImageView imageHeadFull;
            private TextView title;
            private TextView briefDesc;
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
            finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }


}
