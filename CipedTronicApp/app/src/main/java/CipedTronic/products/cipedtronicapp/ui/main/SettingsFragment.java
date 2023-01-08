package CipedTronic.products.cipedtronicapp.ui.main;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.EditTextPreference;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.example.cipedtronicapp.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Log.w("SettingsFragment FRAGMENT", "onCreatePreferences");
        EditTextPreference pref = (EditTextPreference) findPreference("pulse_per_revolution");
        if (pref != null) {
            pref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_NORMAL));
        }
        pref = (EditTextPreference) findPreference("wheel_radius");
        if (pref != null) {
            pref.setOnBindEditTextListener(( EditText editText) ->
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL) );

        }


    }

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