package CipedTronic.products.cipedtronicapp.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.cipedtronicapp.R;
import com.example.cipedtronicapp.databinding.FragmentDataBinding;
import com.google.android.material.snackbar.Snackbar;

import CipedTronic.products.cipedtronicapp.mcu.mcu.CipedTronicConfiguration;

public class SettingsFragment extends PreferenceFragmentCompat {

    private PageViewModel _VModel;
    private FragmentDataBinding binding;
    View _RootView;
    EditTextPreference _PulsesPerRevolutionPref;
    EditTextPreference _WheelRadiusPref;
    EditTextPreference _BluetoothAddressPref;
    Preference _SynchronizePref;
    SwitchPreference _BatteryLoad;
    SwitchPreference _PowerSave;
    SwitchPreference _RadiusMeasure;
    String _RootKey;
    SynchronizeDialog _Dialog = new SynchronizeDialog();
    CipedTronicConfiguration _Config = new CipedTronicConfiguration();
    void DownloadConfiguration()
    {
        _Config.BatteryLoadEnable = getPreferenceManager().getSharedPreferences().getBoolean("device_battery_load",false);
        _Config.PowerSave = getPreferenceManager().getSharedPreferences().getBoolean("device_power_save",false);
        _Config.PulsesPerRevolution = Integer.parseInt(getPreferenceManager().getSharedPreferences().getString("pulse_per_revolution","1"));
        _Config.WheelRadius = Integer.parseInt(getPreferenceManager().getSharedPreferences().getString("wheel_radius","1"));
        _Config.getFlags();
        _VModel.setConfiguration(_Config,true);
    }

    void UploadConfiguration(){
        _VModel.readConfiguration();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        _RootKey = rootKey;
        setPreferencesFromResource(R.xml.root_preferences, _RootKey);

        _BluetoothAddressPref =  (EditTextPreference) findPreference("bluetooth_Address");
        if(_BluetoothAddressPref != null)
        {
            _BluetoothAddressPref.setEnabled(false);
        }

        _PulsesPerRevolutionPref = (EditTextPreference) findPreference("pulse_per_revolution");
        if (_PulsesPerRevolutionPref != null) {
            _PulsesPerRevolutionPref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_NORMAL));
            _PulsesPerRevolutionPref.setOnPreferenceChangeListener(_OnPulsesPerRevolutionPrefChange);
        }

        _WheelRadiusPref = (EditTextPreference) findPreference("wheel_radius");
        if (_WheelRadiusPref != null) {
            _WheelRadiusPref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL) );
            _WheelRadiusPref.setOnPreferenceChangeListener(_OnWheelRadiusPrefChange);
        }

        _SynchronizePref = (Preference) findPreference("device_synchronize");
        if (_SynchronizePref != null) {
            _SynchronizePref.setOnPreferenceClickListener(_OnSynchronizePrefClick);
        }



    }

    public void updatePerfView()
    {
        setPreferenceScreen(null);
        setPreferencesFromResource(R.xml.root_preferences, _RootKey);
        _SynchronizePref = (Preference) findPreference("device_synchronize");
        if (_SynchronizePref != null) {
            _SynchronizePref.setOnPreferenceClickListener(_OnSynchronizePrefClick);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
        _RootView = view;
        _VModel = new ViewModelProvider(this).get(PageViewModel.class);
        _VModel.getConfiguration().observe(getViewLifecycleOwner(), new Observer<CipedTronicConfiguration>() {
            @Override
            public void onChanged(@Nullable CipedTronicConfiguration data) {
                Context hostActivity = getActivity();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(hostActivity);
                SharedPreferences.Editor edt = prefs.edit();
                edt.putBoolean("device_battery_load",data.BatteryLoadEnable);
                edt.apply();
                edt.commit();
                edt.putBoolean("device_power_save",data.PowerSave);
                edt.apply();
                edt.commit();
                updatePerfView();
            }
        });
    }

    Preference.OnPreferenceChangeListener _OnWheelRadiusPrefChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

            int wheelradius = 0;
            try {
                wheelradius =  Integer.parseInt((String)newValue);
            }
            catch(Exception exp)
            {
                Snackbar.make(_RootView, "Must be a number", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
            if(wheelradius == 0)
            {
                Snackbar.make(_RootView, "Could not be 0", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
            _Config.WheelRadius = wheelradius;
            _VModel.setConfiguration(_Config,false);

           return true;
        }
    };

    Preference.OnPreferenceChangeListener _OnPulsesPerRevolutionPrefChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            int pulsesPerRevolution = 0;

            try {
                pulsesPerRevolution =  Integer.parseInt((String)newValue);
            }
            catch(Exception exp)
            {
                Snackbar.make(_RootView, "Must be a number", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
            if(pulsesPerRevolution == 0)
            {
                Snackbar.make(_RootView, "Could not be 0", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return false;
            }
            _Config.PulsesPerRevolution = pulsesPerRevolution;
            _VModel.setConfiguration(_Config,false);
            return true;
        }
    };

    Preference.OnPreferenceClickListener _OnSynchronizePrefClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(@NonNull Preference preference) {

            if(_VModel.getState().getValue() != "Connected")
            {
                Snackbar.make(_RootView, "Not connected", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return true;
            }
            _Dialog.setOnSynchronizeDialogListener(new SynchronizeDialog.SynchronizeDialogListener() {
                @Override
                public void onFinishDialog(SynchronizeDialog.SynchronizeDialogResult result) {
                    switch(result)
                    {
                        case Cancel:
                        {
                            return;
                        }
                        case Upload:
                        {
                            UploadConfiguration();

                            return;
                        }
                        case Download:
                        {
                            DownloadConfiguration();
                            return;
                        }
                    }
                }
            });
            _Dialog.show( getChildFragmentManager(),"");
            return true;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("SettingsFragment", "onDestroy");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w("SettingsFragment", "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w("SettingsFragment", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w("SettingsFragment", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w("SettingsFragment", "onResume");
    }


}