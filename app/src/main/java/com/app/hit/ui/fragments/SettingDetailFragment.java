package com.app.hit.ui.fragments;

import static android.content.Context.BIND_AUTO_CREATE;

import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.hit.R;
import com.app.hit.adapter.LogbookListAdapter;
import com.app.hit.model.ImpactPoint;
import com.app.hit.model.response.GetUserListResponse;
import com.app.hit.model.response.User;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.service.BLEService;
import com.app.hit.util.Prefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingDetailFragment extends Fragment {

    public static final String TAG = "Settings";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    ImageView back;
    View batteryLevel;
    TextView batteryLevelTxt, deviceNameTxt, deviceAddressTxt, nameTxt,connectionStatusTxt;
    private BLEService mBluetoothLeService;


    public SettingDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayerListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingDetailFragment newInstance(String param1, String param2) {
        SettingDetailFragment fragment = new SettingDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString("DEVICE_ADDRESS");
            mParam2 = getArguments().getString("PLAYER_NAME");
            mParam3 = getArguments().getString("DEVICE_NAME");
        }
        Intent gattServiceIntent = new Intent(getActivity(), BLEService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_detail, container, false);
        back = view.findViewById(R.id.back);
        batteryLevel = view.findViewById(R.id.battery_level);
        batteryLevelTxt = view.findViewById(R.id.battery_value_txt);
        deviceAddressTxt = view.findViewById(R.id.device_id);
        deviceNameTxt = view.findViewById(R.id.device_name);
        nameTxt = view.findViewById(R.id.user_name);
        connectionStatusTxt = view.findViewById(R.id.connection_status);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTxt.setText(mParam2);
        deviceNameTxt.setText(mParam1);
        deviceAddressTxt.setText(mParam3);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

    }

    public void calculateTimePassed() {
        long millisUntilFinished = System.currentTimeMillis() - Prefs.getInstance(getContext()).getLong("BATTERY_DATE");
        int level = Prefs.getInstance(getContext()).getInt("BATTERY_LEVEL");
        long hour = (millisUntilFinished / 3600000) % 24;
        if (hour >= 360) {
            level = 100;
            Prefs.getInstance(getContext()).setInt("BATTERY_LEVEL", level);
            Prefs.getInstance(getContext()).setLong("BATTERY_DATE", System.currentTimeMillis());
            batteryLevel.getBackground().setLevel(level * 100);
            batteryLevelTxt.setText(level + "%");
        } else if (hour >= 2) {
            level = level - 5;
            if (level < 0) {
                level = 0;
                Prefs.getInstance(getContext()).setInt("BATTERY_LEVEL", level);
                Prefs.getInstance(getContext()).setLong("BATTERY_DATE", System.currentTimeMillis());
                batteryLevel.getBackground().setLevel(0);
                batteryLevelTxt.setText(level + "%");
            } else {
                Prefs.getInstance(getContext()).setInt("BATTERY_LEVEL", level);
                Prefs.getInstance(getContext()).setLong("BATTERY_DATE", System.currentTimeMillis());
                batteryLevel.getBackground().setLevel(level * 100);
                batteryLevelTxt.setText(level + "%");
            }
        } else {
            batteryLevel.getBackground().setLevel(level * 100);
            batteryLevelTxt.setText(level + "%");
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
            mBluetoothLeService.isGraph = false;
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                getFragmentManager().popBackStack();
            } else {
                mBluetoothLeService.connect(mParam1);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_DESCRIPTOR_WRITTEN);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
        BluetoothGatt mBtGatt = mBluetoothLeService.connectedDeviceMap.get(mParam1);
        mBluetoothLeService.forceDisconnect(mParam1,mBtGatt);
        mBluetoothLeService = null;

    }

    private boolean mConnected;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
                //invalidateOptionsMenu();
                Log.e(TAG, "ACTION_GATT_CONNECTED" + action);
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("disconnected");
                //invalidateOptionsMenu();
                //clearUI();
                Log.e(TAG, "ACTION_GATT_DISCONNECTED" + action);
            }
        }
    };

    private void updateConnectionState(String type) {
        getActivity().runOnUiThread(() -> {
            switch (type) {
                case "service_not_exists":

                    break;
                case "write_uuid_not_exists":
                case "notify_uuid_not_exists":
                case "disconnected":
                    connectionStatusTxt.setText("Disconnected");
                    int level = 0;
                    Prefs.getInstance(getContext()).setInt("BATTERY_LEVEL", level);
                    Prefs.getInstance(getContext()).setLong("BATTERY_DATE", System.currentTimeMillis());
                    batteryLevel.getBackground().setLevel(0);
                    batteryLevelTxt.setText(level + "%");
                    break;
                case "connected":
                    connectionStatusTxt.setText("Connected");
                    int currentBattery = Prefs.getInstance(getContext()).getInt("BATTERY_LEVEL");
                    if (currentBattery == 0) {
                        batteryLevel.getBackground().setLevel(10000);
                        Prefs.getInstance(getContext()).setLong("BATTERY_DATE", System.currentTimeMillis());
                        Prefs.getInstance(getContext()).setInt("BATTERY_LEVEL", 100);
                        batteryLevelTxt.setText(100 + "%");
                    } else {
                        calculateTimePassed();
                    }
                    break;
                case "written_descriptor":
//                    statusTxt.setText(getString(R.string.connecting_to) + " " + ssid);
//                    break;
            }
        });
    }
}