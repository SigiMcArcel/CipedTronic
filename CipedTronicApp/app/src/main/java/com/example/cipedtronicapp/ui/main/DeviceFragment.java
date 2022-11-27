package com.example.cipedtronicapp.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

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

import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class DeviceFragment extends Fragment  implements ICipedTronicMCU {

    private CipedTronicMCU _CipedTronicMCU;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentDeviceBinding binding;

    public DeviceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceFragment newInstance(String param1, String param2) {
        DeviceFragment fragment = new DeviceFragment();
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
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        _CipedTronicMCU = CipedTronicMCU.getInstance();
        _CipedTronicMCU.RegisterMCUInterface(this);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context cont = this.getContext();
        binding.buttonSearchDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CipedTronicMCU.StartScan();
                /*final ListView listv = binding.ListViewDevices;
                ArrayList<String> devices = new ArrayList<String>(

                        Arrays.asList("Geeks",
                                "for",
                                "Geeks"));
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cont, android.R.layout.simple_list_item_1,devices);
                listv.setAdapter(arrayAdapter);*/
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
                edt.putString("bluetooth_Address",ls.toString());
                prefs.getString("bluetooth_Address","");
            }


        });

    }

    public void onDeviceScanResult(List<CipedTronicDevice> devices)
    {
        final ListView listv = binding.ListViewDevices;

        ArrayAdapter<CipedTronicDevice> arrayAdapter = new ArrayAdapter<CipedTronicDevice>(this.getContext(),android.R.layout.simple_list_item_1,devices);
        listv.setAdapter(arrayAdapter);

    }
    @Override
    public void onDeviceError(String error)
    {

    }
    @Override
    public void onDataUpdate(CipedtronicData data)
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _CipedTronicMCU.DeregisterMCUInterface(this);
    }
}