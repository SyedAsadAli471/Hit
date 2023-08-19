package com.app.hit.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.hit.R;
import com.app.hit.adapter.DeviceListAdapter;
import com.app.hit.model.Device;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScanDevicesActivity extends AppCompatActivity {

    public static final String TAG = ScanDevicesActivity.class.getName();

    BluetoothLeScanner scanner;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bdDevice;
    BluetoothClass bdClass;
    private static final int GATT_INTERNAL_ERROR = 129;
    private BluetoothGatt mBtGatt;

    public static final int REQUEST_ACCESS_LOCATION = 1;
    public static final int REQUEST_GPS_LOCATION = 2;


    RecyclerView listViewDetected;
    DeviceListAdapter detectedAdapter;
    ArrayList<String> arrayListpaired;
    ArrayAdapter<String> adapter;

    static HandleSeacrh handleSeacrh;

    ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    ArrayList<Device> arrayListBluetoothDevices = null;
    TextView scanStatusTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        scanStatusTxt = findViewById(R.id.status);
        listViewDetected = findViewById(R.id.device_list_recycler_view);
        arrayListpaired = new ArrayList<String>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handleSeacrh = new HandleSeacrh();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        arrayListBluetoothDevices = new ArrayList<Device>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListpaired);
        detectedAdapter = new DeviceListAdapter(this, arrayListBluetoothDevices);
        listViewDetected.setHasFixedSize(true);
        listViewDetected.setLayoutManager(new LinearLayoutManager(this));
        listViewDetected.setAdapter(detectedAdapter);
        detectedAdapter.notifyDataSetChanged();
        checkPermissionsAndProceed();

    }

    public void checkPermissionsAndProceed(){
        if(!locationEnabled()){
            new AlertDialog.Builder(ScanDevicesActivity.this)
                    .setMessage( "GPS Enable" )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivityForResult( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS ),REQUEST_GPS_LOCATION); ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
        }else{
            if (!bluetoothAdapter.isEnabled()) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage(getResources().getString(R.string.enable_bluetooth_txt));

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                initBluetooth();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else {
                initBluetooth();
            }
        }
    }

    private boolean locationEnabled () {
        LocationManager lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE ) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        if (!gps_enabled && !network_enabled) {
            return false;
        }else{
            return  true;
        }
    }

    public void initBluetooth(){
        onBluetooth();
        startSearching();
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        scanner = bluetoothAdapter.getBluetoothLeScanner();
//
//        if (scanner != null) {
//            scanner.startScan(mScanCallback);
//            Log.i(TAG, "scan started");
//        } else {
//            Log.e(TAG, "could not get scanner object");
//        }
    }

    // Scan result callback function
    private final ScanCallback mScanCallback = new ScanCallback() {
        // Scan result notification process
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // Discovered device
            if ((null != result) && (null != result.getDevice())) {
                bdDevice = result.getDevice();
                // Connect to the device.
                mBtGatt = bdDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
            }
        }

        public void onScanFailed(int errorCode) {
            Log.i(TAG, "error code is:" + errorCode);
        }

        public void onBatchScanResults(java.util.List<android.bluetooth.le.ScanResult> results) {
            Log.i(TAG, "event linstener is called!!!!");
            Log.i(TAG, "batch result are:" + results);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "gatt connected");
                gatt.discoverServices();
            } else {
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "Services found : " + gatt.getServices().size());
            if (status == GATT_INTERNAL_ERROR) {
                Log.e(TAG, "Service discovery failed");
                disconnect();
                return;
            } else {
                final List<BluetoothGattService> services = gatt.getServices();
                Log.i(TAG, "Services found : " + services.size());
                for (int i = 0; i < services.size(); i++) {
                    Log.i(TAG, "Service name : " + services.get(i).getUuid());
                    final List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
                    Log.i(TAG, "Characteristic name : " + characteristics.get(0).getUuid());
                }
                String deviceID = "";
                if (services.size() >= 5) {
                    deviceID = "" + services.get(3).getCharacteristics().get(0).getUuid();
                }
                setListData(gatt.getDevice().getName(), gatt.getDevice().getAddress());

                // Do additional processing of the services
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            int value = characteristic != null ? characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) : 0;
            Log.i(TAG, "Characteristic value changed : " + value);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    public void disconnect() {
        mBtGatt.disconnect();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();
                Log.i(TAG,"ACTION_FOUND");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    //device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                } catch (Exception e) {
                    Log.i("Log", "Inside the exception: ");
                    e.printStackTrace();
                }
                setListData(device.getName(),device.getAddress());

            }
        }
    };

    public void setListData(String name, String address) {
        if (arrayListBluetoothDevices.size() < 1) // this checks if the size of bluetooth device is 0,then add the
        {
            scanStatusTxt.setVisibility(View.GONE);
            // device to the arraylist.
            Device data = new Device();
            String deviceName = (name != null && !name.equalsIgnoreCase("")) ? name : "N/A";
            data.setDeviceName(deviceName);
            data.setAddress(address);
            arrayListBluetoothDevices.add(data);
            detectedAdapter.notifyDataSetChanged();
        } else {
            boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
            for (int i = 0; i < arrayListBluetoothDevices.size(); i++) {
                if (address.equals(arrayListBluetoothDevices.get(i).getDeviceId())) {
                    flag = false;
                }
            }
            if (flag == true) {
                Device data = new Device();
                String deviceName = name != null && !name.equalsIgnoreCase("") ? name : "N/A";
                data.setDeviceName(deviceName);
                data.setAddress(address);
                arrayListBluetoothDevices.add(data);
                detectedAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean checkCoarseLocationPermission() {
        //checks all needed permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ACCESS_LOCATION);
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void startSearching() {
        Log.i("Log", "in the start searching method");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);
        if (checkCoarseLocationPermission()) {
            bluetoothAdapter.startDiscovery();
        }else{

        }
    }

    private void onBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Log.i("Log", "Bluetooth is Enabled");
        }
    }

    private void offBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.unregisterReceiver(myReceiver);
        finish();
    }

    class HandleSeacrh extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:

                    break;

                default:
                    break;
            }
        }
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            for (BluetoothDevice device : pairedDevice) {
                arrayListpaired.add(device.getName() + "\n" + device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    class ListItemClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
//            bdDevice = arrayListBluetoothDevices.get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The dvice : " + bdDevice.toString());
            /*
             * here below we can do pairing without calling the callthread(), we can directly call the
             * connect(). but for the safer side we must usethe threading object.
             */
//            callThread();
//            connect(bdDevice);
//            Boolean isBonded = false;
//            try {
//                isBonded = createBond(bdDevice);
//                if(isBonded)
//                {
            //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
            //adapter.notifyDataSetChanged();
//                    getPairedDevices();
//                    adapter.notifyDataSetChanged();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //connect(bdDevice);
//            Log.i("Log", "The bond is created: "+isBonded);
        }
    }

    class ListItemClickedonPaired implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            try {
                Boolean removeBonding = removeBond(bdDevice);
                if (removeBonding) {
                    arrayListpaired.remove(position);
                    adapter.notifyDataSetChanged();
                }


                Log.i("Log", "Removed" + removeBonding);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void callThread() {
        new Thread() {
            public void run() {
                Boolean isBonded = false;
                try {
                    isBonded = createBond(bdDevice);
                    if (isBonded) {
                        arrayListpaired.add(bdDevice.getName() + "\n" + bdDevice.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }//connect(bdDevice);
                Log.i("Log", "The bond is created: " + isBonded);
            }
        }.start();
    }

    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    }

    ;


    public boolean removeBond(BluetoothDevice btDevice)
            throws Exception {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }


    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private void gotoBLE() {
        Intent intent = new Intent(ScanDevicesActivity.this, GraphActivity.class);
        startActivity(intent);

    }

//    Button test;
//    final String TAG = "Bluetooth";
//    BluetoothAdapter bluetoothAdapter;
//    int status = 0;     //0 = start discovering, 1 = cancel discovering
//
//    public static final int REQUEST_ENABLE_BLUETOOTH = 11;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
//        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
//        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        checkBluetoothState();
//
//        test = findViewById(R.id.testbutton);
//        test.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (status == 0) {
//                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
//                        if (checkCoarseLocationPermission()) {
//                            Boolean result = bluetoothAdapter.startDiscovery(); //start discovering and show result of function
//                            Toast.makeText(getApplicationContext(), "Start discovery result: " + result, Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, "Start discovery: " + result);
//                            test.setText("Stop");
//                            status = 1;
//                        }
//                    } else {
//                        checkBluetoothState();
//                    }
//                } else {
//                    Log.d(TAG, "Stop");
//                    status = 0;
//                    bluetoothAdapter.cancelDiscovery();
//                    test.setText("Start");
//                }
//            }
//        });
//
////        checkCoarseLocationPermission();
//    }
//

//
//    private void checkBluetoothState() {
//        //checks if bluetooth is available and if it´s enabled or not
//        if (bluetoothAdapter == null) {
//            Toast.makeText(getApplicationContext(), "Bluetooth not available", Toast.LENGTH_SHORT).show();
//        } else {
//            if (bluetoothAdapter.isEnabled()) {
//                if (bluetoothAdapter.isDiscovering()) {
//                    Toast.makeText(getApplicationContext(), "Device is discovering...", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(getApplicationContext(), "You need to enabled bluetooth", Toast.LENGTH_SHORT).show();
//                Intent enabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enabledIntent, REQUEST_ENABLE_BLUETOOTH);
//            }
//        }
//    }
//
//    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                Log.d(TAG, "Device found: " + deviceName + "|" + deviceHardwareAddress);
//                Toast.makeText(getApplicationContext(), "FOUND: " + deviceName + "|" + deviceHardwareAddress, Toast.LENGTH_SHORT).show();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    device.createBond();
//                }else{
//                    Toast.makeText(getApplicationContext(), "API lower than 19 please upgrade", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                //report user
//                Log.d(TAG, "Started");
//                Toast.makeText(getApplicationContext(), "STARTED", Toast.LENGTH_SHORT).show();
//            }
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                //change button back to "Start"
//                status = 0;
//                final Button test = findViewById(R.id.testbutton);
//                test.setText("Start");
//                //report user
//                Log.d(TAG, "Finished");
//                Toast.makeText(getApplicationContext(), "FINISHED", Toast.LENGTH_SHORT).show();
//            }
//
//            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                final int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
//                if (extra == (BluetoothAdapter.STATE_ON)) {
//                    if (bluetoothAdapter.isDiscovering()) {
//                        bluetoothAdapter.cancelDiscovery();
//                    }
//                    Boolean b = bluetoothAdapter.startDiscovery();
//                    Toast.makeText(getApplicationContext(), "Start discovery" + b, Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    };
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//        }
//
//        unregisterReceiver(receiver);
//    }
//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GPS_LOCATION) {
            checkPermissionsAndProceed();
        }
    }
//
//
//
}

