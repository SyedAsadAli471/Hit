package com.app.hit.ui.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.app.hit.R;
import com.app.hit.model.response.GetRecordListMainResponse;
import com.app.hit.model.response.GetRecordListResponse;
import com.app.hit.model.response.RecordData;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.util.CommonUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarLogsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarLogsFragment extends Fragment implements OnChartValueSelectedListener {
    private String userId;
    float currentYValue, currentXValue;


    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    String[] daysCal = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    String[] month = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    private boolean moveToLastEntry = true;

    private APIInterface apiInterface;
    LinearLayout selectDate;
    TextView selectDateTxt, currentDateTxt, maxValueTxt, lowForceTxt, highForceTxt,thresholdTxt,concussionTxt;
    ImageView back;
    LineChart mChart;
    LinearLayout maxForceLayout;


    GetRecordListResponse data;
    Calendar currentDate;

    int page = 1;
    List<RecordData> dataList;

    public CalendarLogsFragment() {
        // Required empty public constructor
    }

    public static CalendarLogsFragment newInstance(String param1, String param2) {
        CalendarLogsFragment fragment = new CalendarLogsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("PLAYER_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_log_calendar, container, false);
        selectDate = view.findViewById(R.id.date_layout);
        selectDateTxt = view.findViewById(R.id.date_txt);
        currentDateTxt = view.findViewById(R.id.current_date_txt);
        back = view.findViewById(R.id.back);
        mChart = (LineChart) view.findViewById(R.id.impact_graph);
        maxValueTxt = view.findViewById(R.id.max_force_value);
        maxForceLayout = view.findViewById(R.id.max_force_layout);
        lowForceTxt = view.findViewById(R.id.impact_one_value);
        highForceTxt = view.findViewById(R.id.impact_two_value);
        thresholdTxt = view.findViewById(R.id.threshold_txt);
        concussionTxt = view.findViewById(R.id.impact_three_value);

        CommonUtils.showProgressBar(getContext());
        callAPI(view);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        return view;

    }

    public void callAPI(View view) {
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<GetRecordListMainResponse> call = apiInterface.getRecordList(false);
        call.enqueue(new Callback<GetRecordListMainResponse>() {
            @Override
            public void onResponse(Call<GetRecordListMainResponse> call, Response<GetRecordListMainResponse> response) {
                Log.i("response", response.body() + "");
                data = response.body().getResponse();
                if (dataList == null) {
                    dataList = new ArrayList<>();
                }
                for (int i = 0; i < data.getData().size(); i++) {
                    dataList.add(data.getData().get(i));
                }
                CommonUtils.hideProgressBar();
                init(view);
                Log.i("response", response.body() + "");


            }

            @Override
            public void onFailure(Call<GetRecordListMainResponse> call, Throwable t) {
                CommonUtils.hideProgressBar();
                call.cancel();
            }
        });
    }

    public void init(View view) {
        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -100);

        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 100);
        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(view, R.id.calendarView)
                .datesNumberOnScreen(9)
                .range(startDate, endDate)
                .configure()
                .formatTopText("EEE")
                .showBottomText(false)
                .end()
                .build();

        horizontalCalendar.goToday(true);
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //do something
                Log.i("selected", "" + date);
                currentDate = date;
                int dayOfMonth = (currentDate.get(Calendar.DAY_OF_MONTH)) + 1;
                int dayOfWeek = (currentDate.get(Calendar.DAY_OF_WEEK) - 1);
                currentDateTxt.setText(days[dayOfWeek] + " "
                        + dayOfMonth + " " + month[(currentDate.get(Calendar.MONTH))] + ", " + currentDate.get(Calendar.YEAR));
                drawGraph(true);
            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.DAY_OF_MONTH, day);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.YEAR, year);
//                                horizontalCalendar.refresh();
                                horizontalCalendar.selectDate(calendar, false);
                                String monthName = new SimpleDateFormat("MMMM").format(calendar.getTime());
                                selectDateTxt.setText(monthName + ", " + year);

                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        initGraph();
        currentDate = Calendar.getInstance();
        currentDateTxt.setText(daysCal[currentDate.get(Calendar.DAY_OF_WEEK) - 1] + " "
                + currentDate.get(Calendar.DATE) + " " + month[(currentDate.get(Calendar.MONTH))] + ", " + currentDate.get(Calendar.YEAR));
        selectDateTxt.setText(month[(currentDate.get(Calendar.MONTH))]  + ", " + currentDate.get(Calendar.YEAR));

        drawGraph(false);
    }

    public void checkImpactThreshold(List<Integer> data) {
        int lowForceCount = 0, highForceCount = 0, concussionCount = 0;
        for(int i=0;i<data.size();i++){
            if(data.get(i)<0){
                concussionCount++;
            }
            if(data.get(i)>40 && data.get(i)<60){
                lowForceCount++;
            }else if(data.get(i)>60){
                highForceCount++;
            }
        }
        lowForceTxt.setText(""+lowForceCount);
        highForceTxt.setText(""+highForceCount);
        thresholdTxt.setText(highForceCount + " Threshold impact this year");
        if(concussionCount > 0){
            concussionTxt.setText("Yes");
        }else{
            concussionTxt.setText("No");
        }
    }

    public List<Integer> addGforcePoints(List<List<Integer>> record) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < record.size(); i++) {
            List<Integer> arr = record.get(i);
            for (int j = 0; j < arr.size(); j++) {
                result.add(arr.get(j));
            }
        }
        return result;
    }

    public void drawGraph(boolean isSelected) {
        mChart.clearValues();
        List<Integer> points = null;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getUserId().equalsIgnoreCase(userId)) {
                RecordData recordData = dataList.get(i);
                String[] date = recordData.getDate().split("-");
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                Calendar calendar = currentDate;
                if (isSelected) {
                    if (year == calendar.get(Calendar.YEAR)
                            && month == (calendar.get(Calendar.MONTH) + 1)
                            && day == calendar.get(Calendar.DATE) + 1) {
                        points = addGforcePoints(recordData.getGforce());
                    }
                } else {
                    if (year == calendar.get(Calendar.YEAR)
                            && month == (calendar.get(Calendar.MONTH) + 1)
                            && day == calendar.get(Calendar.DATE)) {
                        points = addGforcePoints(recordData.getGforce());
                    }
                }
            }
        }
        if (points != null) {
            checkImpactThreshold(points);
            int maxValue = Collections.max(points);
            maxValueTxt.setText(maxValue+" G");
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
//            Collections.sort(points);
//            Collections.reverse(points);
            for (int i = 0; i < points.size(); i++) {
                currentXValue = i;
                currentYValue = points.get(i);
                addGraphEntry();
            }
        } else {
            concussionTxt.setText("No");
            for (int i = 0; i <= 10; i++) {
                currentXValue = i;
                currentYValue = 0;
                addGraphEntry();
            }
        }
    }

//    private void startPlottingGraph() {
//
//        if (thread != null)
//            thread.interrupt();
//
//        final Runnable runnable = new Runnable() {
//
//            @Override
//            public void run() {
//                currentXValue++;
//                addGraphEntry();
//                if (currentXValue == 60) {
//                    mChart.clearValues();
//                    initGraph();
//                    startPlottingGraph();
////                    thread.start();
//                }
//
//            }
//        };
//
//        thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                for (int i = 0; i < 60; i++) {
//                    if (isFinished)
//                        break;
//
//                    runOnUiThread(runnable);
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//    }

    public void initGraph() {
        currentXValue = 0;
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
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
        xAxis.setLabelCount(10);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
// xAxis.setTypeface(tf);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(getContext().getResources().getColor(R.color.primary, null));
        xAxis.setDrawAxisLine(false);
//        xl.setSpaceBetweenLabels(1);
        xAxis.setAxisMaximum(10f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTfLight);
        yAxis.setTextColor(Color.TRANSPARENT);
        yAxis.setTextSize(0);
        yAxis.setAxisMaximum(100f);
        yAxis.setGridColor(getContext().getResources().getColor(R.color.primary, null));
        yAxis.setLabelCount(10);
        yAxis.setDrawTopYLabelEntry(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawGridLines(true);
        yAxis.setDrawAxisLine(false);
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}