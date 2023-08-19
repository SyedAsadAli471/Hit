package com.app.hit.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.app.hit.R;
import com.app.hit.adapter.ProfileListAdapter;
import com.app.hit.model.response.GetUserListResponse;
import com.app.hit.model.response.User;
import com.app.hit.network.APIClient;
import com.app.hit.network.APIInterface;
import com.app.hit.util.CommonUtils;
import com.app.hit.util.Prefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private APIInterface apiInterface;
    RecyclerView recyclerView;
    ProfileListAdapter adapter;
    Button addProfileBtn;
    ImageView back;
    int page = 1;
    List<User> data;

    public ProfileListFragment() {
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
    public static ProfileListFragment newInstance(String param1, String param2) {
        ProfileListFragment fragment = new ProfileListFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_list, container, false);
        recyclerView = view.findViewById(R.id.user_list_recycler_view);
        addProfileBtn = view.findViewById(R.id.add_profile_btn);
        back = view.findViewById(R.id.back);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CommonUtils.showProgressBar(getContext());
        callAPI();
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });


        addProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment profileFragment = new ProfileFragment();
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container, profileFragment, "profile");
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
                CommonUtils.hideProgressBar();
                setList();
            }

            @Override
            public void onFailure(Call<GetUserListResponse> call, Throwable t) {
                CommonUtils.hideProgressBar();
                call.cancel();
            }
        });
    }

    public void setList() {
        Prefs.getInstance(getContext()).setUserList("PLAYERS_LIST", data);
        List<User> filteredList = filterListWithDeviceType(data);
        adapter = new ProfileListAdapter(getContext(), filteredList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
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