package com.example.cipedtronicapp.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.*;
import com.example.cipedtronicapp.CipedTronicDevice;
import com.example.cipedtronicapp.CipedTronicMCU;
import com.example.cipedtronicapp.CipedtronicData;
import com.example.cipedtronicapp.ICipedTronicMCU;
import com.example.cipedtronicapp.R;
import com.example.cipedtronicapp.databinding.FragmentDeviceBinding;
import com.example.cipedtronicapp.databinding.FragmentMainBinding;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class DeviceFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentDeviceBinding binding;
    private PageViewModel _VModel;

    public DeviceFragment() {
        // Required empty public constructor
    }


    public static DeviceFragment newInstance(String param1, String param2) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("DeviceFragment", "onCreate");

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w("DeviceFragment", "onCreateView");
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        _VModel = new ViewModelProvider(this).get(PageViewModel.class);
        _VModel.getScanDeResults().observe(getViewLifecycleOwner(), new Observer<List<CipedTronicDevice>>() {
            @Override
            public void onChanged(@Nullable List<CipedTronicDevice> devices) {
                final ListView listv = binding.ListViewDevices;

                ArrayAdapter<CipedTronicDevice> arrayAdapter = new ArrayAdapter<CipedTronicDevice>(getContext(),android.R.layout.simple_list_item_1,devices);
                listv.setAdapter(arrayAdapter);
                binding.buttonSearchDevice.setEnabled(true);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.w("DeviceFragment", "onViewCreated");

        Context cont = this.getContext();
        binding.buttonSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.buttonSearchDevice.setEnabled(false);
                _VModel.scanDevices();
            }
        });

        final ListView listv = binding.ListViewDevices;


         listv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView ls = (TextView) view;
                Context hostActivity = getActivity();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(hostActivity);

                SharedPreferences.Editor edt = prefs.edit();
                CipedTronicDevice dev = (CipedTronicDevice) parent.getAdapter().getItem(position);
                edt.putString("bluetooth_Address",dev.getAddress());
                edt.apply();
                edt.commit();
                String d = prefs.getString("pulseperrevolution","");
                Log.d("hjgjhg",d);
            }


        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("DeviceFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w("DeviceFragment", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("DeviceFragment", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("DeviceFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("DeviceFragment", "onResume");
    }
}