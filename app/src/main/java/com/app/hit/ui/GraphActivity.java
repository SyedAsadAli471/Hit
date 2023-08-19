package com.app.hit.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.hit.R;
import com.app.hit.model.ImpactPoint;
import com.app.hit.model.request.GForceBody;
import com.app.hit.model.response.SendGForceResponse;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.service.BLEService;
import com.app.hit.ui.fragments.CalendarLogsFragment;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GraphActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private APIInterface apiInterface;

    private LineChart mChart;
    TextView statusTxt, maxValueTxt, lowValueTxt, midValueTxt, highValueTxt;
    ImageView back;
    Button startBtn, stopBtn;
    LinearLayout maxForceLayout;

    private boolean moveToLastEntry = true;
    Button finish;
    public static final String TAG = "GraphActivity";

    float currentYValue, currentXValue;
    float maxValue = 0;
    int lowForceCount = 0, midForceCount = 0, highForceCount = 0;
    boolean isGraphStarted = false;

    private BLEService mBluetoothLeService;

    String deviceAddress = "";

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();
            mBluetoothLeService.isGraph = true;
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            } else {
                if (mBluetoothLeService.connectedDeviceMap != null && mBluetoothLeService.connectedDeviceMap.size() > 0) {
                    if (mBluetoothLeService.connectedDeviceMap.containsKey(deviceAddress)) {
                        List<ImpactPoint> data = mBluetoothLeService.connectedDevicesData.get(deviceAddress);
                        if (data.size() > 0) {
                            startBtn.setClickable(false);
                            startBtn.setBackgroundResource(R.drawable.save_button_grey_rounded);
                            stopBtn.setClickable(true);
                            stopBtn.setBackgroundResource(R.drawable.save_button_blue_rounded);
                            startPlottingGraph(data);
                            isGraphStarted = true;
                        }
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private boolean isFinished;
    private String userId;
    private String maxThreshold;
    double maxThresholdValue;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_graph);

        mChart = (LineChart) findViewById(R.id.impact_graph);
        back = findViewById(R.id.back);
        statusTxt = findViewById(R.id.status);
        startBtn = findViewById(R.id.start_btn);
        stopBtn = findViewById(R.id.stop_btn);
        maxValueTxt = findViewById(R.id.max_force_value);
        lowValueTxt = findViewById(R.id.low_force);
        midValueTxt = findViewById(R.id.mid_force);
        highValueTxt = findViewById(R.id.high_force);
        maxForceLayout = findViewById(R.id.max_force_layout);

        stopBtn.setClickable(false);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        initGraph();
        currentXValue++;
        addGraphEntry();

        if (getIntent().getExtras() != null) {
            deviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");
            userId = getIntent().getStringExtra("PLAYER_ID");
            maxThreshold = getIntent().getStringExtra("PLAYER_MAX_THRESHOLD");
            maxThresholdValue = Double.parseDouble(maxThreshold.equalsIgnoreCase("") ? "0" : maxThreshold);
            userName = getIntent().getStringExtra("PLAYER_NAME");
            if (deviceAddress != null && deviceAddress.equals("")) {
//                Toast.makeText(getApplicationContext(), getString(R.string.device_token_empty), Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothLeService.connectedDeviceMap != null) {
                    if (mBluetoothLeService.connectedDeviceMap.size() > 0) {
                        if (mBluetoothLeService.connectedDeviceMap.containsKey(deviceAddress)) {

                        } else {
                            mBluetoothLeService.connect(deviceAddress);
                        }
                    } else {
                        mBluetoothLeService.connect(deviceAddress);
                    }
                } else {
                    mBluetoothLeService.connect(deviceAddress);
                }
                startThread();
                isGraphStarted = true;
                startBtn.setClickable(false);
                startBtn.setBackgroundResource(R.drawable.save_button_grey_rounded);
                stopBtn.setClickable(true);
                stopBtn.setBackgroundResource(R.drawable.save_button_blue_rounded);
                statusTxt.setText("Device Connection : Scanning");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBtn.setClickable(true);
                startBtn.setBackgroundResource(R.drawable.save_button_blue_rounded);
                stopBtn.setClickable(false);
                stopBtn.setBackgroundResource(R.drawable.save_button_grey_rounded);
                statusTxt.setText("Device Connection : Disconnected");
                isGraphStarted = false;
                if (mBluetoothLeService.connectedDeviceMap.containsKey(deviceAddress)) {
                    BluetoothGatt mBtGatt = mBluetoothLeService.connectedDeviceMap.get(deviceAddress);
                    mBluetoothLeService.forceDisconnect(deviceAddress, mBtGatt);
                    mChart.clearValues();
                    stopThread();
                    currentXValue = 0;
                } else {

                }
                showConcussionDialog();

//                mBluetoothLeService.connect(deviceAddress);
            }
        });

        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                moveToLastEntry = false;
            }


            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public void initGraph() {
        currentXValue = 0;

        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
//        l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);

        XAxis xAxis = mChart.getXAxis();
//        xl.setTypeface(mTfLight);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelCount(12);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
// xAxis.setTypeface(tf);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(getResources().getColor(R.color.primary, null));
        xAxis.setDrawAxisLine(false);
//        xl.setSpaceBetweenLabels(1);
        xAxis.setAxisMaximum(60f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setLabelCount(10);
//        leftAxis.setTypeface(mTfLight);
        yAxis.setTextColor(Color.TRANSPARENT);
        yAxis.setTextSize(0);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawTopYLabelEntry(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawAxisLine(true);
        yAxis.setGridColor(getResources().getColor(R.color.primary, null));
        yAxis.enableGridDashedLine(10f, 10f, 0f);


        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private void addGraphEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            if (isGraphStarted) {
                if (currentYValue != 0) {
                    if (maxValue < currentYValue) {
                        maxValue = currentYValue;
                        maxValueTxt.setText(maxValue + " G");
                        if (maxValue > 60) {
                            maxForceLayout.setBackgroundResource(R.drawable.bg_red_stroke);
                            maxValueTxt.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else if (maxValue > 30 && maxValue < 61) {
                            maxForceLayout.setBackgroundResource(R.drawable.bg_yellow_stroke);
                            maxValueTxt.setTextColor(getResources().getColor(R.color.yellow));
                        } else {
                            maxForceLayout.setBackgroundResource(R.drawable.bg_green_stroke);
                            maxValueTxt.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                        }
                    }
                    if (currentYValue > 60) {
                        highForceCount++;
                        highValueTxt.setText("" + highForceCount);
                    } else if (currentYValue > 30 && currentYValue < 61) {
                        midForceCount++;
                        midValueTxt.setText("" + midForceCount);
                    } else {
                        lowForceCount++;
                        lowValueTxt.setText("" + lowForceCount);
                    }
                }
            }

            data.addEntry(new Entry(currentXValue, currentYValue), 0);
            if (currentYValue != 0)
                currentYValue = 0;
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);


            if (moveToLastEntry) {
                // move to the latest entry
                mChart.moveViewToX(data.getEntryCount());
            }


            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
//        if(currentXValue==60){
//            mChart.clearValues();
//            feedMultiple();
//            currentXValue = 0;
//        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setDrawCircles(false);
        set.setCircleColor(Color.TRANSPARENT);
        set.setLineWidth(2f);
        set.setCircleRadius(0f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        return set;
    }

    private Thread thread;

    private void startPlottingGraph(List<ImpactPoint> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getImpactValue() < 70) {
                currentXValue = data.get(i).getImpactTime();
                currentYValue = data.get(i).getImpactValue();
                addGraphEntry();
            }
        }
        startPlottingGraph();
    }

    public void startThread() {
        isFinished = false;
    }


    public void stopThread() {
        isFinished = true;
    }

    private void startPlottingGraph() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Log.e(TAG, "compare " + currentYValue + " : " + maxThresholdValue);
                if(currentYValue > maxThresholdValue)
                    showNotification();
                currentXValue++;
                addGraphEntry();
                if (currentXValue == 60) {
                    mChart.clearValues();
                    initGraph();
                    startPlottingGraph();
//                    thread.start();
                }

            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {

                for (int i = 0; i < 60; i++) {
                    if (isFinished)
                        break;

                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        if (thread != null)
            thread.interrupt();
//        disconnect();
    }

    @Override
    public void onBackPressed() {
        stopThread();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mGattUpdateReceiver);
//        if (thread != null) {
//            thread.interrupt();
//        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_DESCRIPTOR_WRITTEN);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);

        return intentFilter;
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
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                startPlottingGraph();
                Log.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED" + action);
//                checkAndSetGattServices(mBluetoothLeService.getSupportedGattServices());

            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                int messageReceived = intent.getIntExtra(BLEService.EXTRA_DATA, 0);
                Log.e(TAG, "ACTION_DATA_AVAILABLE " + action);
                Log.e(TAG, "IMPACT_VALUE messageReceived " + messageReceived);
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                currentYValue = messageReceived;
//                displayData(messageReceived);
            }
        }
    };

    private void updateConnectionState(String type) {
        runOnUiThread(() -> {
            switch (type) {
                case "service_not_exists":
                    startBtn.setClickable(true);
                    startBtn.setBackgroundResource(R.drawable.save_button_blue_rounded);
                    stopBtn.setClickable(false);
                    stopBtn.setBackgroundResource(R.drawable.save_button_grey_rounded);
                    statusTxt.setText("Device Connection : Service not exists");
                    break;
                case "write_uuid_not_exists":
                case "notify_uuid_not_exists":
                case "disconnected":
                    stopBtn.performClick();
                    statusTxt.setText("Device Connection : Disconnected");
                    break;
                case "connected":
                    statusTxt.setText("Device Connection : Connected");
                    Prefs.getInstance(this).setString(deviceAddress, userId);
                    break;
                case "written_descriptor":
//                    statusTxt.setText(getString(R.string.connecting_to) + " " + ssid);
//                    break;
            }
        });
    }

    public void showConcussionDialog() {
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);

        Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.concussion_dialog);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setLayout(width, height);
        alertDialog.setCancelable(true);
        alertDialog.show();

        LinearLayout yesBtn = alertDialog.findViewById(R.id.yes_layout);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtils.showProgressBar(GraphActivity.this);
                sendGForceData(deviceAddress);
                alertDialog.dismiss();
            }
        });

        LinearLayout noBtn = alertDialog.findViewById(R.id.no_layout);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Prefs.getInstance(getApplicationContext()).setString("IS_CONCUSSION", userId);
                finish();
            }
        });
//        // Create an alert builder
//        AlertDialog.Builder builder
//                = new AlertDialog.Builder(this);
//        // set the custom layout
//        final View customLayout = getLayoutInflater().inflate(R.layout.concussion_dialog, null);
//        builder.setView(customLayout);
//
//        // create and show
//        // the alert dialog
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }

    public void sendGForceData(String address) {

        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DATE) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.YEAR);
//        String userId = Prefs.getInstance(getApplicationContext()).getString(address);
        ArrayList<Integer> points = new ArrayList<>();
        points.add(-1);

        GForceBody body = new GForceBody();
        body.setUser_id(userId);
        body.setGdate(date);
        body.setGforce(points);
        apiInterface = APIClient.getPostClient().create(APIInterface.class);
        Call<SendGForceResponse> call = apiInterface.sendGForceData(body);
        call.enqueue(new Callback<SendGForceResponse>() {
            @Override
            public void onResponse(Call<SendGForceResponse> call, Response<SendGForceResponse> response) {
                CommonUtils.hideProgressBar();
                Log.i(TAG, response.body() + "");
                Prefs.getInstance(getApplicationContext()).setString("IS_CONCUSSION", userId);
                finish();
            }

            @Override
            public void onFailure(Call<SendGForceResponse> call, Throwable t) {
                call.cancel();
            }
        });

    }

    public void showNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("IMPACT THRESHOLD DETECTED")
                .setContentText(userName + " Max GForce: " + maxThreshold + "G")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        final Random myRandom = new Random();
        notificationManager.notify(myRandom.nextInt(100), builder.build());
    }
}
