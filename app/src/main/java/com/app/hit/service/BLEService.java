package com.app.hit.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.app.hit.model.request.GForceBody;
import com.app.hit.model.response.AddUserResponse;
import com.app.hit.model.ImpactPoint;
import com.app.hit.model.response.SendGForceResponse;
import com.app.hit.model.response.User;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BLEService extends Service {

    protected static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final String TAG = "BLEService";
    public static final String DEVICES_UUID = "2B187DEA-3F94-41E0-9D41-7D18A47F38FE";
    private static final int GATT_INTERNAL_ERROR = 129;
    BluetoothLeScanner scanner;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager btManager;
    private BluetoothDevice mBtDevice;
    private BluetoothGatt mBtGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    public Map<String, BluetoothGatt> connectedDeviceMap;
    public Map<String, BLETimerTask> timerTaskMap;
    public Map<String, List<ImpactPoint>> connectedDevicesData;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.sphere.raqtan.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.sphere.raqtan.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DESCRIPTOR_WRITTEN = "com.sphere.raqtan.ACTION_DESCRIPTOR_WRITTEN";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.sphere.raqtan.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.sphere.raqtan.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.sphere.raqtan.EXTRA_DATA";
    private String mBluetoothDeviceAddress;
    int previousValue = 0;
    String currentValue = "";
    private CountDownTimer bleTimer;
    private APIInterface apiInterface;

    private Handler mHandler = new Handler();
    public Timer mTimer = null;
    public BLETimerTask bleTimerTask;
    boolean isFinished = false;

    public boolean isGraph = false;
    public boolean isConcussion = false;


    @Override
    public void onCreate() {
        super.onCreate();
        connectedDeviceMap = new HashMap<String, BluetoothGatt>();
        connectedDevicesData = new HashMap<String, List<ImpactPoint>>();
        timerTaskMap = new HashMap<String, BLETimerTask>();

    }

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private String intentAction;
    // bluetooth services callback function
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

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to GATT server.");

                if (!connectedDeviceMap.containsKey(address)) {
                    connectedDeviceMap.put(address, gatt);
                }
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Broadcast if needed
                broadcastUpdate(intentAction);
                if(isGraph)
                    Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
//                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                gatt.disconnect();
//                gatt.close();
//                gatt.getDevice().connectGatt(getApplicationContext(), false, bluetoothGattCallback);
                Log.i(TAG, "Disconnected from GATT server.");
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                if (connectedDeviceMap.containsKey(address)) {
                    BluetoothGatt bluetoothGatt = connectedDeviceMap.get(address);
                    if (bluetoothGatt != null) {
                        bluetoothGatt.close();
                        bluetoothGatt = null;
                    }
                    connectedDeviceMap.remove(address);
                    connectedDevicesData.remove(address);
                }
                // Broadcast if needed
                broadcastUpdate(intentAction);
            }

//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.i(TAG,"gatt connected");
//                gatt.discoverServices();
//            } else {
//                gatt.close();
//            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, "Services found : " + gatt.getServices().size());
            if (status == GATT_INTERNAL_ERROR) {
                Log.e(TAG, "Service discovery failed");
                disconnect(gatt);
                return;
            } else {
                intentAction = ACTION_GATT_SERVICES_DISCOVERED;
                final List<BluetoothGattService> services = gatt.getServices();
                Log.i(TAG, "Services found : " + services.size());
                for (int i = 0; i < services.size(); i++) {
                    Log.i(TAG, "Service name : " + services.get(i).getUuid());
                    final List<BluetoothGattCharacteristic> characteristics = services.get(i).getCharacteristics();
                    Log.i(TAG, "Characteristic name : " + characteristics.get(0).getUuid());
                    boolean isMatch = services.get(i).getUuid().toString().equalsIgnoreCase(DEVICES_UUID.toLowerCase());
                    if (isMatch) {
                        BluetoothGattCharacteristic characteristic = characteristics.get(0);
                        setCharacteristicNotification(gatt, characteristic, true);
                        startTimer(gatt.getDevice().getAddress());

                        //save first data point as reference
                        ImpactPoint impactPoint = new ImpactPoint();
                        impactPoint.setImpactTime(0);
                        impactPoint.setImpactValue(0);
                        List<ImpactPoint> impactPointList = new ArrayList<>();
                        impactPointList.add(impactPoint);
                        connectedDevicesData.put(gatt.getDevice().getAddress(), impactPointList);
                        broadcastUpdate(intentAction);
                    }
                }

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
            mBtGatt = gatt;
            int value = characteristic != null ? characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) : 0;
            currentValue = gatt.getDevice().getAddress() + "-" + value;
            previousValue = value;
            Log.i(TAG, "Characteristic value changed : " + value);
            broadcastUpdate(ACTION_DATA_AVAILABLE, value);
//            currentYValue = value;
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


    // Scan result callback function
    private final ScanCallback mScanCallback = new ScanCallback() {
        // Scan result notification process
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // Discovered device
            if ((null != result) && (null != result.getDevice())) {
                mBtDevice = result.getDevice();
                // Connect to the device.
                mBtDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
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

    public boolean initialize() {

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (btManager == null) {
            btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (btManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = btManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

//        scanner = mBluetoothAdapter.getBluetoothLeScanner();
//
//        if (scanner != null) {
//            scanner.startScan(mScanCallback);
//            Log.i(TAG, "scan started");
//        } else {
//            Log.e(TAG, "could not get scanner object");
//        }

        return true;
    }

    public boolean setCharacteristicNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, boolean enable) {
        Log.i(TAG, "notification here");
        bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
        return bluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?

    }

    public void disconnect(BluetoothGatt gatt) {
        if (gatt != null)
            gatt.disconnect();
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }


        // Previously connected device.  Try to reconnect.
        if (connectedDeviceMap.size() > 0) {
            if (connectedDeviceMap.containsKey(address)) {
                BluetoothGatt mBtGatt = connectedDeviceMap.get(address);
                Log.e(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                if (mBtGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        if (device != null) {
//            forceDisconnect();
            Log.e(TAG, "starting connection");
            BluetoothGatt mBtGatt = device.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        }
        //mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void forceDisconnect(String address, BluetoothGatt mBtGatt) {

        if (mBtGatt != null) {
            mBtGatt.disconnect();
            mBtGatt.close();
            mBtGatt = null;
            connectedDeviceMap.remove(address);
            if(isGraph){
                BLETimerTask timerTask = timerTaskMap.get(address);
                timerTask.cancel();
                timerTaskMap.remove(address);
            }
        }

    }


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, int value) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, value);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "service destroyed");
    }

    public class BLETimerTask extends TimerTask {
        int countUpTimer;
        String address = "";

        public BLETimerTask(String deviceAddress) {
            address = deviceAddress;
            countUpTimer = 1;
        }

        public String getAddress() {
            return address;
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (countUpTimer <= 60) {
                        countUpTimer++;
                        Log.i(TAG, address + " - " + countUpTimer + " - " + previousValue);
                        Log.i(TAG, "currentValue: " + currentValue);
                        if (!currentValue.equalsIgnoreCase("")) {
                            String[] values = currentValue.split("-");
                            if (address.equalsIgnoreCase(values[0])) {
                                ImpactPoint impactPoint = new ImpactPoint();
                                impactPoint.setImpactTime(countUpTimer);
                                impactPoint.setImpactValue(Integer.parseInt(values[1]));
                                List<ImpactPoint> impactPointList;
                                if (connectedDevicesData.containsKey(address)) {
                                    impactPointList = connectedDevicesData.get(address);
                                    impactPointList.add(impactPoint);
                                } else {
                                    impactPointList = new ArrayList<>();
                                    impactPointList.add(impactPoint);
                                }
                                connectedDevicesData.put(address, impactPointList);
                            }
                        }
                        currentValue = address + "-" + "0";
                        previousValue = 0;
                    } else {
                        Log.i(TAG, "counter - finish");
                        cancel();
                        connectedDevicesData.get(address).clear();
                        startTimer(address);
                    }
                }

            });
        }

        @Override
        public boolean cancel() {
            Log.i(TAG, "counter - cancel");
            sendGForceData(address);
            return super.cancel();
        }
    }

    public void startTimer(String address) {
        mTimer = new Timer();
        bleTimerTask = new BLETimerTask(address);
        mTimer.scheduleAtFixedRate(bleTimerTask, 5, 1000);
        timerTaskMap.put(address,bleTimerTask);
//        bleTimer = new BLETimer(time, 1000);
//        bleTimer.start();
    }

    public void sendGForceData(String address) {
        List<ImpactPoint> data = connectedDevicesData.get(address);
        if(data.size()<0)
            return;
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
        String userId = Prefs.getInstance(getApplicationContext()).getString(address);
        ArrayList<Integer> points = new ArrayList<>();
        JSONArray pointsJSON = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getImpactValue() != 0){
                points.add(data.get(i).getImpactValue());
                pointsJSON.put(data.get(i).getImpactValue());
            }
        }
        JSONObject main = new JSONObject();
        try {
            main.put("user_id",userId);
            main.put("Gdate",date);
            main.put("Gforce",pointsJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GForceBody body = new GForceBody();
        body.setUser_id(userId);
        body.setGdate(date);
        body.setGforce(points);
        apiInterface = APIClient.getPostClient().create(APIInterface.class);
        Call<SendGForceResponse> call = apiInterface.sendGForceData(body);
        call.enqueue(new Callback<SendGForceResponse>() {
            @Override
            public void onResponse(Call<SendGForceResponse> call, Response<SendGForceResponse> response) {
                Log.i(TAG, response.body() + "");

            }

            @Override
            public void onFailure(Call<SendGForceResponse> call, Throwable t) {
                call.cancel();
            }
        });

    }

    //    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
//
//        final Intent intent = new Intent(action);
//        if (SERVICE_WRITE_UUID.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.e(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.e(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.e(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            Log.e(TAG, "Heart rate from ElSE");
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(characteristic.getValue(), StandardCharsets.UTF_8));
//            }
//        }
//        sendBroadcast(intent);
//    }

}
