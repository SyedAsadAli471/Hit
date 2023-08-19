package com.app.hit.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.app.hit.R;
import com.app.hit.model.response.User;
import com.app.hit.ui.MainActivity;
import com.app.hit.ui.fragments.PlayerDetailFragment;
import com.app.hit.ui.fragments.PlayerListFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {

    List<User> data;
    Context context;
    PlayerListFragment mFragment;


    // RecyclerView recyclerView;
    public PlayerListAdapter(PlayerListFragment fragment, Context context, List<User> listdata) {
        this.mFragment = fragment;
        this.data = listdata;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.impact_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User user = data.get(position);
        holder.userName.setText(user.getName());
        holder.viewBtn.setText("Max G.Force : "+ user.getMaxGForce() + " G");
        holder.viewBtn.setTag(position);
        if(user.getMaxGForce()<=30){
            holder.itemView.setBackgroundResource(R.drawable.bg_green_stroke);
            holder.userName.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.viewBtn.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.viewBtn.setBackgroundResource(R.drawable.bg_green_stroke);
        }else  if(user.getMaxGForce()>30 && user.getMaxGForce()<=60){
            holder.itemView.setBackgroundResource(R.drawable.bg_yellow_stroke);
            holder.userName.setTextColor(context.getResources().getColor(R.color.yellow));
            holder.viewBtn.setTextColor(context.getResources().getColor(R.color.yellow));
            holder.viewBtn.setBackgroundResource(R.drawable.bg_yellow_stroke);
        }else if(user.getMaxGForce()>60){
            holder.itemView.setBackgroundResource(R.drawable.bg_red_stroke);
            holder.userName.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.viewBtn.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.viewBtn.setBackgroundResource(R.drawable.bg_red_stroke);
        }

        String imageUrl = "https://app.hitrecognition.co.uk/storage/app/users/";
        if(user.getImage()!=null){
            if(!user.getImage().equalsIgnoreCase(""))
                Picasso.get().load(imageUrl+ user.getImage()).into(holder.userImage);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new PlayerDetailFragment();
                Bundle args = new Bundle();
                args.putString("DEVICE_ADDRESS",user.getDeviceId());
                args.putString("PLAYER_ID",user.getId());
                args.putString("PLAYER_NAME",user.getName());
                args.putString("PLAYER_IMAGE",user.getImage());
                args.putString("PLAYER_MAX_THRESHOLD",user.getDevicedetail());
                fragment.setArguments(args);
                FragmentManager manager = ((MainActivity)context).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container, fragment, "impact_tracking");
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView userImage;
        public TextView userName;
        Button viewBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.userImage = (ImageView) itemView.findViewById(R.id.user_image);
            this.userName = (TextView) itemView.findViewById(R.id.user_name);
            this.viewBtn = (Button) itemView.findViewById(R.id.view_user_btn);
        }
    }
}

