package com.flash.vpn.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flash.vpn.Data;
import com.flash.vpn.EncryptData;
import com.flash.vpn.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;


public class WelcomeActivity extends AppCompatActivity {
    TextView tv_welcome_status, tv_welcome_app;
    String StringGetConnectionURL, FileDetails;
    SharedPreferences SharedAppDetails;
    int random;

    @Override
    public void onBackPressed() {
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.lightBlue));



        StringGetConnectionURL = "https://raw.githubusercontent.com/Viveksaini9898/VPN/master/app/src/main/assets/filedetails.json";


        tv_welcome_status = findViewById(R.id.tv_welcome_status);
        tv_welcome_app = findViewById(R.id.tv_welcome_app);


        startAnimation(WelcomeActivity.this, R.id.ll_welcome_loading, R.anim.slide_up_800, true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(WelcomeActivity.this, R.id.ll_welcome_details, R.anim.slide_up_800, true);
            }
        }, 1000);


        if (!Data.isConnectionDetails) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Task().execute();
                    }
                }, 2000);

        }

    }


    void fetchServer() {

        RequestQueue queue = Volley.newRequestQueue(WelcomeActivity.this);
        queue.getCache().clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, StringGetConnectionURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String Response) {
                        FileDetails = Response;
                        Data.isConnectionDetails = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Data.isConnectionDetails = false;
            }
        });
        queue.add(stringRequest);
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {

                final int min = 0;
                final int max = 4;
                random = new Random().nextInt((max - min) + 1) + min;
                EncryptData En = new EncryptData();
                String country = "NULL", file = "NULL", username = "NULL", password = "NULL";


                try {
                    JSONObject json_response = new JSONObject(FileDetails);
                    JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
                    JSONObject json_object = jsonArray.getJSONObject(Integer.valueOf(random));
                    country = json_object.getString("country");
                    file = json_object.getString("file");
                    username = json_object.getString("username");
                    password = json_object.getString("password");
                } catch (Exception e) {
                }


                try {
                    SharedAppDetails = getSharedPreferences("connection_data", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("country", country);
                    Editor.putString("file", En.encrypt(file));
                    Editor.putString("username", username);
                    Editor.putString("password", password);
                    Editor.apply();
                } catch (Exception e) {
                }

                try {
                    SharedAppDetails = getSharedPreferences("app_values", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("file_details", En.encrypt(FileDetails));
                    Editor.apply();
                } catch (Exception e) {
                }


                if (Data.isConnectionDetails) {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {

                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void startAnimation(Context ctx, int view, int animation, boolean show) {
        final View Element = findViewById(view);
        if (show) {
            Element.setVisibility(View.VISIBLE);
        } else {
            Element.setVisibility(View.INVISIBLE);
        }
        Animation anim = AnimationUtils.loadAnimation(ctx, animation);
        Element.startAnimation(anim);
    }

    private class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tv_welcome_status.setText("GETTING CONNECTION DETAILS");
        }

        @Override
        protected Boolean doInBackground(String... params) {

            fetchServer();

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

        }
    }

}