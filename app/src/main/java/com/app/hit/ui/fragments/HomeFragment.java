package com.app.hit.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.hit.R;
import com.app.hit.adapter.PlayerListAdapter;
import com.app.hit.model.response.GetRecordListMainResponse;
import com.app.hit.model.response.GetRecordListResponse;
import com.app.hit.model.response.GetUserListResponse;
import com.app.hit.model.response.RecordData;
import com.app.hit.model.response.User;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    ImageView settings, logs, news;
    RelativeLayout playBtn, profileBtn, defaultLayout, shopLayout;
    TextView defaultDescTxt, impactMaxTxt, primaryNameTxt, primaryThresholdTxt;
    LinearLayout topLayout, bottomLayout, footer;
    private APIInterface apiInterface;

    List<User> data;

    List<Integer> forceList;

    public HomeFragment() {
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
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        footer = view.findViewById(R.id.footer);
        defaultLayout = view.findViewById(R.id.default_layout);
        defaultDescTxt = view.findViewById(R.id.default_desc_txt);
        topLayout = view.findViewById(R.id.top_layout);
        bottomLayout = view.findViewById(R.id.bottom_layout);
        profileBtn = view.findViewById(R.id.profile_layout);
        shopLayout = view.findViewById(R.id.shop_layout);
        settings = view.findViewById(R.id.setting);
        news = view.findViewById(R.id.news);
        logs = view.findViewById(R.id.logs);
        playBtn = view.findViewById(R.id.play_layout);
        impactMaxTxt = view.findViewById(R.id.impact_txt);
        primaryNameTxt = view.findViewById(R.id.name_txt);
        primaryThresholdTxt = view.findViewById(R.id.threshold_txt);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        apiInterface = APIClient.getClient().create(APIInterface.class);
//
//        Call<GetUserListResponse> call = apiInterface.getUserList(false);
//        call.enqueue(new Callback<GetUserListResponse>() {
//            @Override
//            public void onResponse(Call<GetUserListResponse> call, Response<GetUserListResponse> response) {
//                Log.i("response", response.body() + "");
//                List<User> userList = response.body().getGetUserListData().getData();
//                String uniqueID = UUID.randomUUID().toString();
//                boolean isProfileCompleted = false;
//                for (int i = 0; i < userList.size(); i++) {
//                    if (!uniqueID.equalsIgnoreCase(userList.get(i).getDeviceType())) {
//                        isProfileCompleted = true;
//                        break;
//                    }
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<GetUserListResponse> call, Throwable t) {
//                call.cancel();
//            }
//        });

        boolean isProfileCompleted = false;
        if (Prefs.getInstance(getContext()).getString("IS_FIRST") != null) {
            if (!Prefs.getInstance(getContext()).getString("IS_FIRST").equalsIgnoreCase(""))
                isProfileCompleted = true;
        }
        if (isProfileCompleted) {
            topLayout.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.VISIBLE);
            footer.setVisibility(View.VISIBLE);
            settings.setVisibility(View.VISIBLE);
            CommonUtils.showProgressBar(getContext());
            callAPI();
        } else {
            defaultLayout.setVisibility(View.VISIBLE);
            defaultDescTxt.setVisibility(View.VISIBLE);
        }

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment userFragment = new ProfileListFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container, userFragment, "user_list");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        defaultLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment userFragment = new ProfileFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container, userFragment, "profile");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                Fragment fragment = new SettingsFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "settings");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SelectModeFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "select_mode");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                Fragment fragment = new LogBookFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "logbook");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                Fragment fragment = new NewsFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "news");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        shopLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Feature coming soon", Toast.LENGTH_SHORT).show();
                Fragment fragment = new ShopFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "shop");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    public void callAPI() {
        apiInterface = APIClient.getClient().create(APIInterface.class);

        Call<GetUserListResponse> call = apiInterface.getUserList(false);
        call.enqueue(new Callback<GetUserListResponse>() {
            @Override
            public void onResponse(Call<GetUserListResponse> call, Response<GetUserListResponse> response) {
                Log.i("response", response.body() + "");
                List<User> userList = response.body().getGetUserListData().getData();
                data = new ArrayList<>();
                for (int i = 0; i < userList.size(); i++) {
                    data.add(userList.get(i));
                }
                List<User> filteredList = filterListWithDeviceType(data);
//                primaryNameTxt.setText("" + filteredList.get(0).getName());

                String primaryUserId = Prefs.getInstance(getContext()).getString("IS_PRIMARY", "0");
                User primaryUser = null;
                if (filteredList.size() > 0) {
                    for (int j = 0; j < filteredList.size(); j++) {
                        if (filteredList.get(j).getId().equalsIgnoreCase(primaryUserId)) {
                            primaryUser = filteredList.get(j);
                        }
                    }
                    primaryNameTxt.setText("" + primaryUser.getName());
                    primaryThresholdTxt.setText("Threshold: " + primaryUser.getDevicedetail() + " G");
                }
                callRecordAPI(primaryUser);
            }

            @Override
            public void onFailure(Call<GetUserListResponse> call, Throwable t) {
                CommonUtils.hideProgressBar();
                call.cancel();
            }
        });
    }

    public void callRecordAPI(User primaryUser) {
        apiInterface = APIClient.getClient().create(APIInterface.class);

        Call<GetRecordListMainResponse> call = apiInterface.getRecordList(false);
        call.enqueue(new Callback<GetRecordListMainResponse>() {
            @Override
            public void onResponse(Call<GetRecordListMainResponse> call, Response<GetRecordListMainResponse> response) {
                Log.i("response", response.body() + "");
                GetRecordListResponse recordResponse = response.body().getResponse();
                getMaxGforce(primaryUser, recordResponse.getData());
                CommonUtils.hideProgressBar();
            }

            @Override
            public void onFailure(Call<GetRecordListMainResponse> call, Throwable t) {
                CommonUtils.hideProgressBar();
                call.cancel();
            }
        });
    }

    public void getMaxGforce(User primaryUser, List<RecordData> recordList) {

        String userId = primaryUser.getId();
        double num = Double.parseDouble(primaryUser.getDevicedetail().equalsIgnoreCase("") ? "0" : primaryUser.getDevicedetail());

        forceList = new ArrayList<>();
        for (int j = 0; j < recordList.size(); j++) {
            if (recordList.get(j).getUserId().equalsIgnoreCase(userId)) {
                RecordData recordData = recordList.get(j);
                addGforcePoints(recordData.getGforce());
            }
        }
        if (forceList.size() > 0) {
            int maxThresholdImpact = 0;
            int maxForce = Collections.max(forceList);
            for (int k = 0; k < forceList.size(); k++) {
                if (forceList.get(k) > num) {
                    maxThresholdImpact++;
                }
            }
            impactMaxTxt.setText("" + maxThresholdImpact);
        } else {
            impactMaxTxt.setText("" + 0);

        }
    }


    public void addGforcePoints(List<List<Integer>> record) {
        for (int i = 0; i < record.size(); i++) {
            List<Integer> arr = record.get(i);
            for (int j = 0; j < arr.size(); j++) {
                forceList.add(arr.get(j));
            }
        }

    }

    public List<User> filterListWithDeviceType(List<User> data) {
        List<User> filteredList = new ArrayList<>();
        String deviceId = android.provider.Settings.Secure.getString(getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getDeviceType().equalsIgnoreCase(deviceId)) {
                filteredList.add(data.get(i));
            }
        }
        return filteredList;
    }

}