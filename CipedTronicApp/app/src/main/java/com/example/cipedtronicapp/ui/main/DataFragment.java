package com.example.cipedtronicapp.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cipedtronicapp.mcu.mcu.CipedtronicData;
import com.example.cipedtronicapp.R;
import com.example.cipedtronicapp.databinding.FragmentDataBinding;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentDataBinding binding;

    private TextView textViewvelocity;
    private TextView textViewDistance;
    private PageViewModel _VModel;


    public DataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataFragment newInstance(String param1, String param2) {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    private void setTextViewContent(String velocity,String maxVelocity,String distance)
    {
        String h = velocity + " km/h";
        SpannableString ss1=  new SpannableString(h);
        textViewvelocity.setTextSize(80);
        textViewDistance.setTextSize(80);

        ss1.setSpan(new RelativeSizeSpan(0.2f), h.length() - 4, h.length() , 0); // set size
        textViewvelocity.setText(ss1);
        h = velocity + " km";
        ss1=  new SpannableString(h);
        ss1.setSpan(new RelativeSizeSpan(0.2f), h.length() - 2, h.length() , 0); // set size
        textViewDistance.setText(ss1);
    }

    private void setDisconnect(boolean connected)
    {
        int col = Color.parseColor("#000000");
        if(connected)
        {
            col = Color.BLACK;
        }
        else
        {
            col = Color.RED;
        }

        textViewDistance.setTextColor(col);

        textViewvelocity.setTextColor(col);

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("DataFragment", "onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("DataFragment", "onCreateView");

        Context hostActivity = getActivity();
        binding = FragmentDataBinding.inflate(inflater, container, false);
        View inf = inflater.inflate(R.layout.fragment_data, container, false);

        textViewvelocity =  (TextView) inf.findViewById(R.id.textViewVelocity);
        textViewDistance =  (TextView) inf.findViewById(R.id.textViewDistance);

        setTextViewContent("0.00","0.00","0.00");
        setDisconnect(false);
        _VModel = new ViewModelProvider(this).get(PageViewModel.class);
        _VModel.getData().observe(getViewLifecycleOwner(), new Observer<CipedtronicData>() {
            @Override
            public void onChanged(@Nullable CipedtronicData data) {
                setTextViewContent(data.Velocity, data.MaxVelocity, data.MaxVelocity);
            }
        });
        _VModel.getState().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String state) {
                if(state.equals("connected"))
                {
                    setDisconnect(true);
                }
                if(state.equals("disconnected"))
                {
                    setDisconnect(false);
                }
            }
        });

        // Inflate the layout for this fragment
        return inf;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("DataFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("DataFragment", "onStop");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("DataFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("DataFragment", "onResume");
    }
}