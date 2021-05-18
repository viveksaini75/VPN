package com.flash.vpn.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flash.vpn.Data;
import com.flash.vpn.EncryptData;
import com.flash.vpn.R;
import com.flash.vpn.adapter.ServerListAdapter;
import com.flash.vpn.interfaces.OnItemClickListener;
import com.flash.vpn.model.Server;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerActivity extends AppCompatActivity implements OnItemClickListener {

    private RecyclerView rvServerList;
    private ArrayList<Server> serverList;
    private ServerListAdapter serverListAdapter;
    private String StringGetConnectionURL, FileDetails;
    private SharedPreferences SharedAppDetails;
    String[] FileArray = new String[40];
    String[] CountryName = new String[40];
    String[] UserName = new String[40];
    String[] Password = new String[40];
    private Server server;
    private ImageView back, refresh;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Toolbar toolbar = findViewById(R.id.serverToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_700));

        back = findViewById(R.id.back);
        refresh = findViewById(R.id.refresh);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorScheme(R.color.blue,
                R.color.green, R.color.orange, R.color.purple);

        StringGetConnectionURL = "https://raw.githubusercontent.com/Viveksaini9898/VPN/master/app/src/main/assets/filedetails.json";

        EncryptData En = new EncryptData();
        SharedPreferences AppValues = getSharedPreferences("app_values", 0);
        String FileDetaile = En.decrypt(AppValues.getString("file_details", "NA"));

       /* if (FileDetaile.isEmpty()){
            new Task().execute();
        }else {

            loadServer();
        }*/
        new Task().execute();


        rvServerList = findViewById(R.id.serverListRv);
        rvServerList.setHasFixedSize(true);
        rvServerList.setLayoutManager(new LinearLayoutManager(this));


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Task().execute();
            }
        });


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Task().execute();
            }
        });
    }



    private void fetchServer() {

        RequestQueue queue = Volley.newRequestQueue(ServerActivity.this);
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
                EncryptData En = new EncryptData();

                try {
                    SharedAppDetails = getSharedPreferences("app_values", 0);
                    SharedPreferences.Editor Editor = SharedAppDetails.edit();
                    Editor.putString("file_details", En.encrypt(FileDetails));
                    Editor.apply();
                } catch (Exception e) {
                }

                loadServer();
            }
        });
    }

    private void loadServer() {

        serverList = new ArrayList<>();
        try {
            JSONObject json_response = new JSONObject(FileDetails);
            JSONArray jsonArray = json_response.getJSONArray("ovpn_file");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CountryName[i] = jsonObject.getString("country");
                FileArray[i] = jsonObject.getString("file");
                UserName[i] = jsonObject.getString("username");
                Password[i] = jsonObject.getString("password");
                server = new Server();
                server.setCountry(CountryName[i]);
                server.setOvpn(FileArray[i]);
                server.setOvpnUserName(UserName[i]);
                server.setOvpnUserPassword(Password[i]);
                serverList.add(server);

            }

            if (serverList != null) {
                serverListAdapter = new ServerListAdapter(serverList, server, ServerActivity.this);
                rvServerList.setAdapter(serverListAdapter);
            }

        } catch (Exception e) {
        }

    }


    @Override
    public void onItemClick(int position) {

        EncryptData En = new EncryptData();
        server = serverList.get(position);
        SharedAppDetails = getSharedPreferences("connection_data", 0);
        SharedPreferences.Editor Editor = SharedAppDetails.edit();
        Editor.putString("country", server.getCountry());
        Editor.putString("file", En.encrypt(server.getOvpn()));
        Editor.putString("username", server.getOvpnUserName());
        Editor.putString("password", server.getOvpnUserPassword());
        Editor.apply();
        finish();
    }

    private class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            fetchServer();

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mSwipeRefreshLayout.setRefreshing(false);

        }
    }

}