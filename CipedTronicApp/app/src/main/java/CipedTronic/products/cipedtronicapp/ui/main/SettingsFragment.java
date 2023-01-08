package CipedTronic.products.cipedtronicapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.cipedtronicapp.R;
import com.example.cipedtronicapp.databinding.FragmentDataBinding;
import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends PreferenceFragmentCompat {

    private PageViewModel _VModel;
    private FragmentDataBinding binding;
    View _RootView;
    EditTextPreference _PulsesPerRevolutionPref;
    EditTextPreference _WheelRadiusPref;
    EditTextPreference _BluetoothAddressPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

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
        }
        _PulsesPerRevolutionPref.setOnPreferenceChangeListener(_OnPulsesPerRevolutionPrefChange);
        _WheelRadiusPref = (EditTextPreference) findPreference("wheel_radius");
        if (_WheelRadiusPref != null) {
            _WheelRadiusPref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL) );

        }
        _WheelRadiusPref.setOnPreferenceChangeListener(_OnWheelRadiusPrefChange);
        _VModel = new ViewModelProvider(this).get(PageViewModel.class);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view,savedInstanceState);
        _RootView = view;
    }

    Preference.OnPreferenceChangeListener _OnWheelRadiusPrefChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

            float wheelradius = 0;
            try {
                wheelradius =  Float.parseFloat((String)newValue);
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

           _VModel.setWheelradius(wheelradius);
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
            _VModel.setPulsesPerRevolution(pulsesPerRevolution);
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