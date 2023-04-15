package CipedTronic.products.cipedtronicapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicConfiguration;
import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicMCU;

import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import CipedTronic.products.cipedtronicapp.ui.main.SectionsPagerAdapter;
import com.example.cipedtronicapp.databinding.ActivityMainBinding;

import android.content.Context;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CipedTronicMCU _CipedTronicMCU = CipedTronicMCU.getInstance();
    int PERMISSION_ALL = 1;

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {
            if (!Arrays.asList(grantResults).contains(PackageManager.PERMISSION_DENIED)) {

                CipedTronicConfiguration config = new CipedTronicConfiguration();
                SharedPreferences _Prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String _BluetothAddress = _Prefs.getString("bluetooth_Address","");
                config.BatteryLoadEnable = _Prefs.getBoolean("device_battery_load",false);
                config.PowerSave = _Prefs.getBoolean("device_power_save",false);
                config.PulsesPerRevolution = Integer.parseInt(_Prefs.getString("pulse_per_revolution","1"));
                config.WheelRadius = Integer.parseInt(_Prefs.getString("wheel_radius","1"));
                config.getFlags();
                _CipedTronicMCU.setConfiguration(config,false);
                _CipedTronicMCU.createDevice(getApplicationContext(),_BluetothAddress);
                if(_BluetothAddress.equals(""))
                {
                    return;
                }

                _CipedTronicMCU.connectDevice();

            }
        }
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

