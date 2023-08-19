package com.app.hit.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.hit.R;
import com.app.hit.model.Device;
import com.app.hit.model.response.User;
import com.app.hit.ui.ScanDevicesActivity;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder>{

        List<Device> data;
        Context context;

        // RecyclerView recyclerView;
        public DeviceListAdapter(Context context, List<Device> listdata) {
            this.data = listdata;
            this.context = context;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.devices_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Device device = data.get(position);
            holder.deviceName.setText(device.getDeviceName());
            holder.deviceId.setText(" " + device.getAddress());

            holder.parent.setTag(device.getAddress());
            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<User> userList = Prefs.getInstance(context).getUserList();
                    String tag = view.getTag().toString();
                    boolean isExist = false;
                    for(int i=0;i<userList.size();i++){
                        if(userList.get(i).getDeviceId().equalsIgnoreCase(tag)){
                            isExist = true;
                            break;
                        }
                    }
//                    if(!isExist){
                        CommonUtils.showAlert(context,"Device Associated",context.getResources().getString(R.string.device_pair_success_txt),false);
                        Intent intent = new Intent();
                        intent.putExtra("SELECTED_DEVICE_NAME", holder.deviceName.getText().toString());
                        intent.putExtra("SELECTED_DEVICE_ID", holder.deviceId.getText().toString());
                        ((ScanDevicesActivity)context).setResult(3, intent );
                        ((ScanDevicesActivity)context).finish();
//                    }else{
//                        CommonUtils.showAlert(context,"Alert",context.getResources().getString(R.string.device_pair_error_txt),false);
//                    }

                }
            });

        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout parent;
            public TextView deviceName,deviceId;
            public ViewHolder(View itemView) {
                super(itemView);
                this.parent = itemView.findViewById(R.id.parent);
                this.deviceName =  itemView.findViewById(R.id.device_name);
                this.deviceId =  itemView.findViewById(R.id.device_id);
            }
        }
    }

