package CipedTronic.products.cipedtronicapp.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.hardware.lights.Light;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedtronicData;

import com.example.cipedtronicapp.databinding.FragmentDataBinding;
import com.google.android.material.snackbar.Snackbar;

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
    private FragmentDataBinding binding;

    private TextView textViewvelocity;
    private TextView textViewDistance;
    private TextView textViewStates;
    private PageViewModel _VModel;

    private boolean _Light = false;
    private boolean _Alarm = false;

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    private void setButtonState(CipedtronicData data) {
        if(data.StateAlarmActive)
        {
            _Alarm = true;
            binding.buttonAlarm.setBackgroundColor(Color.GREEN);
        }
        else
        {
            _Alarm = false;
            binding.buttonAlarm.setBackgroundColor(Color.GRAY);
        }
        if(data.StateLight)
        {
            _Light = true;
            binding.buttonLight.setBackgroundColor(Color.GREEN);
        }
        else
        {
            _Light = false;
            binding.buttonLight.setBackgroundColor(Color.GRAY);
        }
    }

    private void setTextViewContent(CipedtronicData data)
    {
        String States = "";
        String h = data.Velocity + " km/h";
        SpannableString ss1=  new SpannableString(h);
        textViewvelocity.setTextSize(80);
        textViewDistance.setTextSize(80);

        ss1.setSpan(new RelativeSizeSpan(0.2f), h.length() - 4, h.length() , 0); // set size
        textViewvelocity.setText(ss1);
        h = data.Distance + " km";
        ss1=  new SpannableString(h);
        ss1.setSpan(new RelativeSizeSpan(0.2f), h.length() - 2, h.length() , 0); // set size
        textViewDistance.setText(ss1);

        States += "Pulses = " + data.Pulses + "\r\nPulses per second = " + data.PulsesPerSecond + "\r\n";
        if(data.StateLight)
        {
            States += "Light On\r\n";
        }
        if(data.StateError)
        {
            States += "Error\r\n";
        }
        if(data.StateLowBat)
        {
            States += "Low Battery\r\n";
        }
        if(data.StateMove)
        {
            States += "Move\r\n";
        }
        if(data.StateRunning)
        {
            States += "Running\r\n";
        }
        if(data.StateAlarm)
        {
            States += "Alarm Active\r\n";
        }
        textViewStates.setText(States);
    }

    private void setDisconnect(boolean connected)
    {
        int col = Color.parseColor("#000000");
        if(connected)
        {
            col = Color.BLACK;
            binding.buttonAlarm.setEnabled(true);
            binding.buttonReset.setEnabled(true);
            binding.buttonLight.setEnabled(true);
        }
        else
        {
            col = Color.RED;
            binding.buttonAlarm.setEnabled(false);
            binding.buttonReset.setEnabled(false);
            binding.buttonLight.setEnabled(false);
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
        View root = binding.getRoot();


        textViewvelocity =  binding.textViewVelocity;
        textViewDistance = binding.textViewDistance;
        textViewStates = binding.textViewInfo;

        setTextViewContent(new CipedtronicData());
        setDisconnect(false);
        binding.buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _VModel.resetMCU();
            }
        });
        binding.buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _VModel.setLight(!_Light);
            }
        });
        binding.buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _VModel.setAlarm(!_Alarm);
            }
        });

        _VModel = new ViewModelProvider(getActivity()).get(PageViewModel.class);
        _VModel.getData().observe(getViewLifecycleOwner(), new Observer<CipedtronicData>() {
            @Override
            public void onChanged(@Nullable CipedtronicData data) {
                setTextViewContent(data);
                setButtonState(data);
            }
        });
        _VModel.getState().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String state) {
                if(!state.equals("Connected"))
                {
                    setDisconnect(false);
                }
               else{
                    setDisconnect(true);
                }
            }
        });
        _VModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Snackbar.make(root, s, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        // Inflate the layout for this fragment
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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