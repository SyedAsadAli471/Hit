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
import com.app.hit.ui.fragments.CalendarLogsFragment;
import com.app.hit.ui.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LogbookListAdapter extends RecyclerView.Adapter<LogbookListAdapter.ViewHolder>{

        List<User> data;
        Context context;

        // RecyclerView recyclerView;
        public LogbookListAdapter(Context context, List<User> listdata) {
            this.data = listdata;
            this.context = context;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.log_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final User user = data.get(position);
            holder.userName.setText(user.getName());
            holder.viewBtn.setTag(position);

            String imageUrl = "https://app.hitrecognition.co.uk/storage/app/users/";
            if(user.getImage()!=null){
                if(!user.getImage().equalsIgnoreCase(""))
                    Picasso.get().load(imageUrl+ user.getImage()).into(holder.userImage);
            }
            holder.viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment calendarFragment = new CalendarLogsFragment();
                    FragmentManager manager = ((MainActivity)context).getSupportFragmentManager();
                    Bundle args = new Bundle();
                    args.putString("PLAYER_ID",user.getId());
                    calendarFragment.setArguments(args);
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container,calendarFragment,"log_calendar");
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
                this.viewBtn = (Button)itemView.findViewById(R.id.view_user_btn);
            }
        }
    }

