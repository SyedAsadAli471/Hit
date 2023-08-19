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
import com.app.hit.ui.fragments.SettingDetailFragment;

import java.util.List;

public class SettingListAdapter extends RecyclerView.Adapter<SettingListAdapter.ViewHolder>{

        List<User> data;
        Context context;

        // RecyclerView recyclerView;
        public SettingListAdapter(Context context, List<User> listdata) {
            this.data = listdata;
            this.context = context;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.setting_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final User user = data.get(position);
            holder.userName.setText(user.getName());
            holder.deviceName.setText("Device Name: "+ user.getDeviceName());
            holder.deviceId.setText("Device Id: "+ user.getDeviceId());
            holder.viewBtn.setTag(position);

//            holder.userImage.setImageResource(user.getImage());
            holder.viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment settingDetailFragment = new SettingDetailFragment();
                    FragmentManager manager = ((MainActivity)context).getSupportFragmentManager();
                    Bundle args = new Bundle();
                    args.putString("DEVICE_ADDRESS",user.getDeviceId());
                    args.putString("DEVICE_NAME",user.getDeviceName());
                    args.putString("PLAYER_NAME",user.getName());
                    settingDetailFragment.setArguments(args);
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.container,settingDetailFragment,"setting_detail");
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
            public TextView userName,deviceName,deviceId;
            Button viewBtn;
            public ViewHolder(View itemView) {
                super(itemView);
                this.userImage = (ImageView) itemView.findViewById(R.id.user_image);
                this.userName = (TextView) itemView.findViewById(R.id.user_name);
                this.deviceName = (TextView) itemView.findViewById(R.id.device_name);
                this.deviceId = (TextView) itemView.findViewById(R.id.device_id);
                this.viewBtn = (Button)itemView.findViewById(R.id.view_user_btn);
            }
        }
    }

