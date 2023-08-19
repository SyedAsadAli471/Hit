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
import com.app.hit.ui.MainActivity;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;

    ImageView back,userImage;
    RelativeLayout play,logs;
    TextView nameTxt;

    public PlayerDetailFragment() {
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
    public static PlayerDetailFragment newInstance(String param1, String param2) {
        PlayerDetailFragment fragment = new PlayerDetailFragment();
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
            mParam4 = getArguments().getString("PLAYER_IMAGE");
            mParam5 = getArguments().getString("PLAYER_MAX_THRESHOLD");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player_detail, container, false);
        play = view.findViewById(R.id.play_layout);
        logs = view.findViewById(R.id.log_layout);
        back = view.findViewById(R.id.back);
        nameTxt = view.findViewById(R.id.name_txt);
        userImage = view.findViewById(R.id.user_image_upload);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameTxt.setText(mParam2);
        String imageUrl = "https://app.hitrecognition.co.uk/storage/app/users/";
        if(mParam4 !=null){
            if(!mParam4.equalsIgnoreCase(""))
                Picasso.get().load(imageUrl+ mParam4).into(userImage);
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GraphActivity.class);
                intent.putExtra("DEVICE_ADDRESS",mParam1);
                intent.putExtra("PLAYER_ID",mParam3);
                intent.putExtra("PLAYER_MAX_THRESHOLD",mParam5);
                intent.putExtra("PLAYER_NAME",mParam2);
                getContext().startActivity(intent);
            }
        });

        logs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment calendarFragment = new CalendarLogsFragment();
                FragmentManager manager = getFragmentManager();
                Bundle args = new Bundle();
                args.putString("PLAYER_ID",mParam3);
                calendarFragment.setArguments(args);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.container,calendarFragment,"log_calendar");
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });



    }
}