package com.flash.vpn.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.flash.vpn.CheckInternetConnection;
import com.flash.vpn.EncryptData;
import com.flash.vpn.R;
import com.flash.vpn.SharedPreference;
import com.flash.vpn.databinding.FragmentMainBinding;
import com.flash.vpn.interfaces.ChangeServer;
import com.flash.vpn.model.Server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import ir.amirrajabzadeh.rayanalert.RayanAlert;

import static android.app.Activity.RESULT_OK;

public class MainFragment extends Fragment implements View.OnClickListener {

    private Server server;
    private CheckInternetConnection connection;

    private OpenVPNThread vpnThread = new OpenVPNThread();
    private OpenVPNService vpnService = new OpenVPNService();
    boolean vpnStart = false;
    private SharedPreference preference;

    private FragmentMainBinding binding;
    private InputStream inputStream;
    private String serverFile = "NULL", country = "NULL", username = "NULL", password = "NULL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);

        View view = binding.getRoot();
        initializeAll();

        return view;
    }

    /**
     * Initialize all variable and object
     */
    private void initializeAll() {
        preference = new SharedPreference(getContext());
        server = preference.getServer();

        // Update current selected server icon
        //updateCurrentServerIcon(server.getFlagUrl());
        connection = new CheckInternetConnection();
        binding.laAnimation.setAnimation(R.raw.ninjasecure);
        binding.laAnimation.playAnimation();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.vpnBtn.setOnClickListener(this);
        // binding.browser.setOnClickListener(this);

        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(getActivity().getCacheDir());

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vpnBtn:
                // Vpn is running, user would like to disconnect current connection.
                if (vpnStart) {
                    confirmDisconnect();
                } else {
                    prepareVpn();
                }
                break;
            //case R.id.browser:

        }
    }

    /**
     * Show show disconnect confirm dialog
     */
    public void confirmDisconnect() {
        RayanAlert alert = new RayanAlert(getContext());
        alert.setTitle("Disconnect")
                .setMessage("Would you like to cancel the current VPN Connection?")
                .setTextNegativeButton("No")
                .setTextPositiveButton("Yes")
                .setFocusBackgroundColorNegativeButton(Color.GREEN)
                .setBorderColorNegativeButton(Color.LTGRAY)
                .setBorderWidthNegativeButton(5)
                .setTextColorNegativeButton(Color.BLACK)
                .setTextColorPositiveButton(Color.BLACK)
                .showLoading(false)
                .show(new RayanAlert.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        stopVpn();
                    }
                });
    }

    private void prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {

                // Checking permission for network monitor
                Intent intent = VpnService.prepare(getContext());

                if (intent != null) {
                    startActivityForResult(intent, 1);
                } else startVpn();//have already permission

                // Update confection status
                status("connecting");

            } else {

                // No internet connection available
                showToast("you have no internet connection !!");
            }

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }


    public boolean stopVpn() {
        try {
            vpnThread.stop();

            status("connect");
            vpnStart = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Taking permission for network access
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Permission granted, start the VPN
            startVpn();
        } else {
            showToast("Permission Deny !! ");
        }
    }

    /**
     * Internet connection status.
     */
    public boolean getInternetStatus() {
        return connection.netCheck(getContext());
    }

    /**
     * Get service status
     */
    public void isServiceRunning() {
        setStatus(vpnService.getStatus());
    }

    /**
     * Start the VPN
     */
    private void startVpn() {
        try {
            // .ovpn file

            EncryptData En = new EncryptData();
            SharedPreferences ConnectionDetails = getContext().getSharedPreferences("connection_data", 0);
            country = ConnectionDetails.getString("country", "NA");
            serverFile = En.decrypt(ConnectionDetails.getString("file", "NA"));
            username = ConnectionDetails.getString("username", "NA");
            password = ConnectionDetails.getString("password", "NA");

            // InputStream conf = getActivity().getAssets().open(server.getOvpn());
            inputStream = new ByteArrayInputStream(serverFile.getBytes(Charset.forName("UTF-8")));

            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String config = "";
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }

            br.readLine();
            OpenVpnApi.startVpn(getContext(), config, country, username, password);

            // Update log
            binding.logTv.setText("Connecting...");
            vpnStart = true;

        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }


    public void setStatus(String connectionState) {
        if (connectionState != null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("connect");
                    vpnStart = false;
                    vpnService.setDefaultStatus();
                    binding.logTv.setText("Tap CONNECT to start :)");
                    binding.connection.setText("The connection is ready.");
                    binding.laAnimation.setAnimation(R.raw.ninjasecure);
                    binding.laAnimation.playAnimation();

                    break;
                case "CONNECTED":
                    vpnStart = true;// it will use after restart this activity
                    status("connected");
                    binding.logTv.setText("Please enjoy a safer internet");
                    binding.connection.setText("Connected " + country);
                    binding.laAnimation.setAnimation(R.raw.ninjasecure);
                    binding.laAnimation.playAnimation();
                    break;
                case "WAIT":
                    binding.logTv.setText("waiting for server connection!!");
                    binding.connection.setText("Connecting " + country);
                    binding.laAnimation.setAnimation(R.raw.boost);
                    binding.laAnimation.playAnimation();

                    break;
                case "AUTH":
                    binding.logTv.setText("server authenticating!!");
                    binding.connection.setText("Connecting " + country);
                    binding.laAnimation.setAnimation(R.raw.boost);
                    binding.laAnimation.playAnimation();
                    break;
                case "RECONNECTING":
                    status("connecting");
                    binding.logTv.setText("Reconnecting...");
                    binding.connection.setText("Connecting " + country);
                    binding.laAnimation.setAnimation(R.raw.boost);
                    binding.laAnimation.playAnimation();
                    break;
                case "NONETWORK":
                    binding.logTv.setText("No network connection");
                    binding.laAnimation.setAnimation(R.raw.boost);
                    binding.laAnimation.playAnimation();
                    break;
            }

    }


    public void status(String status) {

        if (status.equals("connect")) {
            binding.vpnBtn.setText(getContext().getString(R.string.connect));
        } else if (status.equals("connecting")) {
            binding.vpnBtn.setText(getContext().getString(R.string.connecting));
            binding.connection.setText("Connecting " + country);
            binding.laAnimation.setAnimation(R.raw.boost);
            binding.laAnimation.playAnimation();
        } else if (status.equals("connected")) {
            binding.vpnBtn.setText(getContext().getString(R.string.disconnect));

        } else if (status.equals("tryDifferentServer")) {

            binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
            binding.vpnBtn.setText("Try Different\nServer");
        } else if (status.equals("loading")) {
            binding.vpnBtn.setBackgroundResource(R.drawable.button);
            binding.vpnBtn.setText("Loading Server..");
        } else if (status.equals("invalidDevice")) {
            binding.vpnBtn.setBackgroundResource(R.drawable.button_connected);
            binding.vpnBtn.setText("Invalid Device");
        } else if (status.equals("authenticationCheck")) {
            binding.vpnBtn.setBackgroundResource(R.drawable.button_connecting);
            binding.vpnBtn.setText("Authentication \n Checking...");
        }

    }

    /**
     * Receive broadcast message
     */
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = "↓0kB/s";
                if (byteOut == null) byteOut = "↑0kB/s";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    public void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        binding.durationTv.setText("Duration: " + duration);
        // binding.lastPacketReceiveTv.setText("Packet Received: " + lastPacketReceive + " second ago");
        binding.byteInTv.setText("Download: " + byteIn);
        binding.byteOutTv.setText("Upload: " + byteOut);
    }


    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    /*public void updateCurrentServerIcon(String serverIcon) {
        Glide.with(getContext())
                .load(serverIcon)
                .into(binding.selectedServerIcon);
    }*/


    /* @Override
     public void newServer(Server server) {
         this.server = server;
         updateCurrentServerIcon(server.getFlagUrl());
         countryName = server.getCountry();

         // Stop previous connection
         if (vpnStart) {
             stopVpn();
         }

         prepareVpn();
     }
 */
    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }


    @Override
    public void onStop() {
        if (server != null) {
            preference.saveServer(server);
        }
        super.onStop();
    }
}
