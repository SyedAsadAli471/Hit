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
import com.app.hit.ui.fragments.CalendarLogsFragment;
import com.app.hit.ui.fragments.ProfileFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ViewHolder>{

        List<User> data;
        Context context;

        // RecyclerView recyclerView;
        public ProfileListAdapter(Context context, List<User> listdata) {
            this.data = listdata;
            this.context = context;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.user_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final User user = data.get(position);
            holder.userName.setText(user.getName());
            String imageUrl = "https://app.hitrecognition.co.uk/storage/app/users/";
            if(user.getImage()!=null){
                if(!user.getImage().equalsIgnoreCase(""))
                    Picasso.get().load(imageUrl + user.getImage()).into(holder.userImage);
            }
            holder.viewBtn.setTag(position);
            holder.editBtn.setTag(position);

//            holder.userImage.setImageResource(user.getImage());
            holder.viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("USER",user);
                    args.putBoolean("IS_EDIT",false);
                    profileFragment.setArguments(args);
                    FragmentManager manager = ((MainActivity)context).getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container,profileFragment,"profile");
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
            holder.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("USER",user);
                    args.putBoolean("IS_EDIT",true);
                    profileFragment.setArguments(args);
                    FragmentManager manager = ((MainActivity)context).getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container,profileFragment,"profile");
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
            Button viewBtn,editBtn;
            public ViewHolder(View itemView) {
                super(itemView);
                this.userImage = (ImageView) itemView.findViewById(R.id.user_image);
                this.userName = (TextView) itemView.findViewById(R.id.user_name);
                this.viewBtn = (Button)itemView.findViewById(R.id.view_user_btn);
                this.editBtn = (Button)itemView.findViewById(R.id.edit_user_btn);
            }
        }
    }

