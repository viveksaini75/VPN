package com.flash.vpn.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.flash.vpn.R;


public class MainActivity extends AppCompatActivity {
    private FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    private Fragment fragment;
    private DrawerLayout drawer;

    public static final String TAG = "FLASHVPN";
    private String image;
    private ImageView serverImage;
    private ImageButton navBar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext() ,R.color.teal_700));

        initializeAll();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        navBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
            }
        });

        serverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });

        transaction.add(R.id.container, fragment);
        transaction.commit();

    }


    private void initializeAll() {
        drawer = findViewById(R.id.drawer_layout);
        fragment = new MainFragment();
        navBar = findViewById(R.id.navbar_right);
        serverImage = findViewById(R.id.server_image);

    }


    public void closeDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences ConnectionDetails = getSharedPreferences("connection_data", 0);
        image = ConnectionDetails.getString("country", "NA");

        switch (image) {
            case "Japan":
                serverImage.setImageResource(R.drawable.ic_flag_japan);
                break;
            case "Russia":
                serverImage.setImageResource(R.drawable.ic_flag_russia);
                break;
            case "South Korea":
                serverImage.setImageResource(R.drawable.ic_flag_south_korea);
                break;
            case "India":
                serverImage.setImageResource(R.drawable.india);
                break;
            case "Thailand":
                serverImage.setImageResource(R.drawable.ic_flag_thailand);
                break;
            case "Viet Nam":
                serverImage.setImageResource(R.drawable.ic_flag_vietnam);
                break;
            case "United States":
                serverImage.setImageResource(R.drawable.ic_flag_united_states);
                break;
            case "United Kingdom":
                serverImage.setImageResource(R.drawable.ic_flag_united_kingdom);
                break;
            case "Brazil":
                serverImage.setImageResource(R.drawable.ic_flag_brazil);
                break;
            case "China":
                serverImage.setImageResource(R.drawable.ic_china);
                break;
            case "Singapore":
                serverImage.setImageResource(R.drawable.ic_flag_singapore);
                break;
            case "france":
                serverImage.setImageResource(R.drawable.ic_flag_france);
                break;
            case "germany":
                serverImage.setImageResource(R.drawable.ic_flag_germany);
                break;
            case "Canada":
                serverImage.setImageResource(R.drawable.ic_flag_canada);
                break;
            case "Luxemburg":
                serverImage.setImageResource(R.drawable.ic_flag_luxemburg);
                break;
            case "netherlands":
                serverImage.setImageResource(R.drawable.ic_flag_netherlands);
                break;
            case "spain":
                serverImage.setImageResource(R.drawable.ic_flag_spain);
                break;
            case "finland":
                serverImage.setImageResource(R.drawable.ic_flag_finland);
                break;
            case "poland":
                serverImage.setImageResource(R.drawable.ic_flag_poland);
                break;
            case "australia":
                serverImage.setImageResource(R.drawable.ic_flag_australia);
                break;
            case "italy":
                serverImage.setImageResource(R.drawable.ic_flag_italy);
                break;
            case "England":
                serverImage.setImageResource(R.drawable.ic_flag_england);
                break;
            default:
                serverImage.setImageResource(R.drawable.ic_flag_unknown_mali);
                break;

        }

    }
}
