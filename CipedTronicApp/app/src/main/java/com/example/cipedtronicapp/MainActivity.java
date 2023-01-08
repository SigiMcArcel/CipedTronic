package com.example.cipedtronicapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.cipedtronicapp.mcu.mcu.CipedTronicMCU;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.example.cipedtronicapp.ui.main.SectionsPagerAdapter;
import com.example.cipedtronicapp.databinding.ActivityMainBinding;
import com.example.cipedtronicapp.ui.main.PageViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    String _BluetothAddress = "";
    int _LastPosition = 0;
    private PageViewModel _VModel;
    private CipedTronicMCU _CipedTronicMCU;
    int granted = 0;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(0);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(listener);

        int PERMISSION_ALL = 1;
        String[] permissions =
            {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_SCAN,
            };
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_ALL);
        }


        _VModel = new ViewModelProvider(this).get(PageViewModel.class);
            _VModel.connectDevice();

    }

    private ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onDestroy()
    {
        Log.w("Main Activity","destroy");
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //CipedTronicMCU.getInstance().destroy();
        Log.w("Main Activity","stop");

    }
}

