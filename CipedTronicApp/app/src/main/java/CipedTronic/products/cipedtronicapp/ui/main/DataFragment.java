package CipedTronic.products.cipedtronicapp.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.example.cipedtronicapp.R;
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
    private TextView textViewAvgMax;
    private PageViewModel _VModel;

    private Bitmap _PictureAlarmActive;
    private Bitmap _PictureAlarmEnabledInactiv;
    private Bitmap _PictureAlarmInactiv;
    private Bitmap _PictureLoadingNotEnabled;
    private Bitmap _PictureLoadingEnabledInactiv;
    private Bitmap _PictureLoading;
    private Bitmap _PictureLowBatInactive;
    private Bitmap _PictureLowBat;
    private Bitmap _PictureMovingInactiv;
    private Bitmap _PictureMoving;
    private Bitmap _PicturePowerSaveInactive;
    private Bitmap _PicturePowerSave;


    private CipedtronicData _Data = new CipedtronicData();
    private boolean _Alarm = false;

    public DataFragment() {
        // Required empty public constructor
    }

    public static DataFragment newInstance() {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    private void updateButtonState(CipedtronicData data) {
        if(data.StateAlarmActive)
        {
            _Alarm = true;
        }
        else
        {
            _Alarm = false;
        }
    }

    private void updatePictureBar(CipedtronicData data)
    {
        if(data.StateLoadEnabled)
        {
            if(data.StateLoading) {
                binding.imageViewLoad.setImageBitmap(_PictureLoading);
            }else
            {
                binding.imageViewLoad.setImageBitmap(_PictureLoadingEnabledInactiv);
            }
        }
        else
        {
            binding.imageViewLoad.setImageBitmap(_PictureLoadingNotEnabled);
        }

        if(data.StateLowBat)
        {
            binding.imageViewBattery.setImageBitmap(_PictureLowBat);
        }
        else{
            binding.imageViewBattery.setImageBitmap(_PictureLowBatInactive);
        }

        if(data.StateMove)
        {
            binding.imageViewMove.setImageBitmap(_PictureMoving);
        }
        else {
            binding.imageViewMove.setImageBitmap(_PictureMovingInactiv);
        }

        if(data.StatePowerSaveEnabled)
        {
            binding.imageViewPowerSave.setImageBitmap(_PicturePowerSave);
        }
        else {
            binding.imageViewPowerSave.setImageBitmap(_PicturePowerSaveInactive);
        }

        if(data.StateAlarmEnabled)
        {
            if(data.StateAlarmActive){
                binding.imageViewAlarm.setImageBitmap(_PictureAlarmActive);
            }else{
                binding.imageViewAlarm.setImageBitmap(_PictureAlarmEnabledInactiv);
            }
        }
        else
        {
            binding.imageViewAlarm.setImageBitmap(_PictureAlarmInactiv);
        }


    }
    private void updateTextViewContent(CipedtronicData data)
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

        textViewAvgMax.setText("AVG " + data.AvgVelocity + " km/h   " + "Max " + data.MaxVelocity + " km/h" );

        States += "Pulses = " + data.Pulses + "\r\n";
        States += "PulsesPerSecond = " + data.PulsesPerSecond + "\r\n";
        States += "Radius = " + data.Radius + "\r\n";
        States += "TimeWasMoving = " + data.TimeWasMoving + "\r\n";
        States += "Vbat = " + data.Vbat + "\r\n";


        textViewStates.setText(States);
    }

    private void setConnect(boolean connected)
    {
        int col = Color.parseColor("#000000");
        if(connected )
        {
            col = Color.BLACK;
            binding.buttonAlarm.setEnabled(true);
            binding.buttonReset.setEnabled(true);

        }
        else
        {
            col = Color.RED;
            binding.buttonAlarm.setEnabled(false);
            binding.buttonReset.setEnabled(false);

        }

        textViewDistance.setTextColor(col);
        textViewvelocity.setTextColor(col);
        textViewAvgMax.setTextColor(col);

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

        _PictureAlarmActive = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.alarmactive);
        _PictureAlarmEnabledInactiv = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.alarmenabledinactiv);
        _PictureAlarmInactiv = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.alarminactiv);
        _PictureLoadingNotEnabled = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.loadingnotenabled);
        _PictureLoadingEnabledInactiv = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.loadingenabledinactiv);
        _PictureLoading = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.loading);
        _PictureLowBatInactive = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.lowbatinactive);
        _PictureLowBat = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.lowbat);
        _PictureMovingInactiv = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.movinginactiv);
        _PictureMoving = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.moving);
        _PicturePowerSaveInactive = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.powersaveinactive);
        _PicturePowerSave = BitmapFactory.decodeResource(hostActivity.getResources(), R.drawable.powersave);

        textViewvelocity =  binding.textViewVelocity;
        textViewDistance = binding.textViewDistance;
        textViewStates = binding.textViewInfo;
        textViewAvgMax = binding.AvgMax;

        updateTextViewContent(_Data);
        setConnect(false);
        binding.buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _VModel.resetCounters();
            }
        });

        binding.buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_Data.StatePowerSaveEnabled)
                {
                    Snackbar.make(root, "Not avaiable if Powersave is enabled ", Snackbar.LENGTH_LONG)
                            .setAction("Action ", null).show();
                }
                _VModel.setAlarmEnabled(!_Alarm);
            }
        });



        _VModel = new ViewModelProvider(getActivity()).get(PageViewModel.class);
        _VModel.getData().observe(getViewLifecycleOwner(), new Observer<CipedtronicData>() {
            @Override
            public void onChanged(@Nullable CipedtronicData data) {
                _Data = data;
                updateTextViewContent(_Data);
                updateButtonState(_Data);
                updatePictureBar(_Data);
            }
        });
        _VModel.getState().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String state) {
                if(state.equals("Connected"))
                {
                    _Data = _VModel.getData().getValue();
                    setConnect(true);
                }
               else{
                    setConnect(false);
                }
            }
        });
        _VModel.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Snackbar.make(root, "Error " + s, Snackbar.LENGTH_LONG)
                        .setAction("Action " + s, null).show();
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