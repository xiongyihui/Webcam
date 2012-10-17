package cn.xiongyihui.webcam;

import java.util.Set;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {
    public final String TAG = "Webcam";
    
    ListPreference mCameraPreference;
    ListPreference mRangePreference;
    ListPreference mSizePreference;
    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.v(TAG, "Change camera");
                
                String id = (String) newValue;
                preference.setSummary(id);
                
                initialize(id);
                
                return true;
            }
        };
        
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        mCameraPreference = (ListPreference) findPreference("settings_camera");
        mSizePreference = (ListPreference) findPreference("settings_size");
        mRangePreference = (ListPreference) findPreference("settings_range");
        
        mCameraPreference.setOnPreferenceChangeListener(listener);     
        String cameraIdString = (String) mCameraPreference.getValue();
        mCameraPreference.setSummary(cameraIdString);
        Set<String> cameraIdSet = mSharedPreferences.getStringSet("camera_id_set", null);
        String[] cameraIds = cameraIdSet.toArray(new String[0]);
        mCameraPreference.setEntries(cameraIds);
        mCameraPreference.setEntryValues(cameraIds);
        
        initialize(cameraIdString);
    }
    
    private void initialize(String cameraIdString) {
        OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.v(TAG, "Change camera");
                
                preference.setSummary((String) newValue);
                
                return true;
            }
        };
        mSizePreference.setOnPreferenceChangeListener(listener);
        mRangePreference.setOnPreferenceChangeListener(listener);
        
        String sizesKey = "preview_sizes_" + cameraIdString;
        String rangesKey = "preview_ranges_" + cameraIdString;
        
        Set<String> sizeSet = mSharedPreferences.getStringSet(sizesKey, null);
        Set<String> rangeSet = mSharedPreferences.getStringSet(rangesKey, null);
        
        Log.v(TAG, sizeSet.toString());
        
        String[] sizes = sizeSet.toArray(new String[0]);
        
        mSizePreference.setEntries(sizes);
        mSizePreference.setEntryValues(sizes);
//        mSizePreference.setDefaultValue(sizes[0]);
        String size = mSizePreference.getValue();
        if (!sizeSet.contains(size)) {
            mSizePreference.setValue(sizes[0]);
        }
        mSizePreference.setSummary(mSizePreference.getValue());
        
        
        String[] ranges = rangeSet.toArray(new String[0]);
        mRangePreference.setEntries(ranges);
        mRangePreference.setEntryValues(ranges);
//        mRangePreference.setDefaultValue(ranges[0]);
        String range = mRangePreference.getValue();
        if (!rangeSet.contains(size)) {
            mRangePreference.setValue(ranges[0]);
        }
        mRangePreference.setSummary(mRangePreference.getValue());
    }
}
