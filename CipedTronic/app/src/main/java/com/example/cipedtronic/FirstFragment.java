package com.example.cipedtronic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cipedtronic.databinding.FragmentFirstBinding;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private CipedTronicMCU _CipedTronicMCU;
    private Timer timer;
    private TimerTask timerTask;
    private TextView _VelocityTexView;
    private TextView _DistanceTexView;
    private TextView _InfoTexView;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        _CipedTronicMCU = new CipedTronicMCU(getActivity());
        _CipedTronicMCU.Resume();
        startTimer();
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _VelocityTexView = (TextView)view.findViewById(R.id.textviewVelocity);
        _DistanceTexView = (TextView)view.findViewById(R.id.textViewDistance);
        _InfoTexView = (TextView)view.findViewById(R.id.textViewInfo);
        binding.buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CipedTronicMCU.ResetMCU();
            }
        });

        binding.buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 250, 250); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                StringBuilder InfoText = new StringBuilder();

                _VelocityTexView.setText(_CipedTronicMCU.getVelocity() +"\r\nKm/h");
                _DistanceTexView.setText(_CipedTronicMCU.getDistance() + "\r\nKm");

                InfoText.append("Max Velocity : ");
                InfoText.append(_CipedTronicMCU.getMaxVelocity());
                InfoText.append("\r\n");
                InfoText.append("Pulses : ");
                InfoText.append(_CipedTronicMCU.getPulses());
                InfoText.append("\r\n");

                InfoText.append("Pulses\\second: ");
                InfoText.append(_CipedTronicMCU.getPulsesPerSecond());
                InfoText.append("\r\n");


                _InfoTexView.setText(InfoText.toString());
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        _CipedTronicMCU.Resume();
    }



}