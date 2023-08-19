package com.app.hit.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.hit.R;
import com.app.hit.ui.GraphActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectModeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectModeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    ImageView back;
    RelativeLayout indPlayLayout,teamPlayLayout;

    public SelectModeFragment() {
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
    public static SelectModeFragment newInstance(String param1, String param2) {
        SelectModeFragment fragment = new SelectModeFragment();
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
            mParam3 = getArguments().getString("PLAYER_ID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_mode, container, false);
        indPlayLayout = view.findViewById(R.id.card_ind_play);
        teamPlayLayout = view.findViewById(R.id.card_team_play);
        back = view.findViewById(R.id.back);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        indPlayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new PlayerListFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "impact_tracking");
                transaction.addToBackStack(null);
                transaction.commit();
//                Intent intent = new Intent(getContext(), GraphActivity.class);
//                intent.putExtra("DEVICE_ADDRESS",mParam1);
//                intent.putExtra("PLAYER_ID",mParam3);
//                getContext().startActivity(intent);
            }
        });

        teamPlayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new PlayerListFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(R.id.container, fragment, "impact_tracking");
                transaction.addToBackStack(null);
                transaction.commit();
//                Fragment calendarFragment = new CalendarLogsFragment();
//                FragmentManager manager = getFragmentManager();
//                Bundle args = new Bundle();
//                args.putString("PLAYER_ID",mParam3);
//                calendarFragment.setArguments(args);
//                FragmentTransaction transaction = manager.beginTransaction();
//                transaction.replace(R.id.container,calendarFragment,"log_calendar");
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });



    }
}