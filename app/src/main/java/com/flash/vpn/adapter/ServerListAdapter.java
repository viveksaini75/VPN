package com.flash.vpn.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flash.vpn.EncryptData;
import com.flash.vpn.R;
import com.flash.vpn.interfaces.OnItemClickListener;
import com.flash.vpn.model.Server;

import java.util.ArrayList;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.MyViewHolder> {

    private String image;
    private ArrayList<Server> serverList;
    private Context mContext;
    private OnItemClickListener listener;
    private Server server;
    private SharedPreferences SharedAppDetails;
    private int lastPosition = -1;

    public ServerListAdapter(ArrayList<Server> serverList, Server server, Context context) {
        this.serverList = serverList;
        this.mContext = context;
        this.listener = (OnItemClickListener) context;
        this.server = server;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.server_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
       /* holder.serverCountry.setText(serverList.get(position).getCountry());
        Glide.with(mContext)
                .load(serverList.get(position).getFlagUrl())
                .into(holder.iv_flag);
*/
        server = serverList.get(position);
        image = server.getCountry();

        switch (image) {
            case "Japan":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_japan);
                holder.serverCountry.setText("Japan");
                break;
            case "India":
                holder.iv_flag.setImageResource(R.drawable.india);
                holder.serverCountry.setText("India");
                break;
            case "Russia":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_russia);
                holder.serverCountry.setText("Russia");
                break;
            case "Thailand":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_thailand);
                holder.serverCountry.setText("Thailand");
                break;
            case "United States":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_united_states);
                holder.serverCountry.setText("United States");
                break;
            case "Singapore":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_singapore);
                holder.serverCountry.setText("Singapore");
                break;
            case "Canada":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_canada);
                holder.serverCountry.setText("Canada");
                break;
            case "China":
                holder.iv_flag.setImageResource(R.drawable.ic_china);
                holder.serverCountry.setText("China");
                break;
            case "South Korea":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_south_korea);
                holder.serverCountry.setText("South Korea");
                break;
            case "Viet Nam":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_vietnam);
                holder.serverCountry.setText("Viet Nam");
                break;
            case "Brazil":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_brazil);
                holder.serverCountry.setText("Brazil");
                break;
            case "England":
                holder.iv_flag.setImageResource(R.drawable.ic_flag_England);
                holder.serverCountry.setText("England");
                break;
            default:
                holder.iv_flag.setImageResource(R.drawable.ic_flag_unknown_mali);
                holder.serverCountry.setText("Unknown");
                break;

        }
        setAnimation(holder.itemView, position);

        holder.serverItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    listener.onItemClick(position);

                } catch (Exception e) {
                }
            }
        });

    }

    private void setAnimation(View viewToAnimate, int position)
    {
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return serverList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout serverItemLayout;
        ImageView iv_flag;
        TextView serverCountry;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            serverItemLayout = itemView.findViewById(R.id.serverItemLayout);
            iv_flag = itemView.findViewById(R.id.iconImg);
            serverCountry = itemView.findViewById(R.id.countryTv);

        }

    }

}


