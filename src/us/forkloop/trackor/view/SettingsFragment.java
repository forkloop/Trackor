package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import us.forkloop.trackor.WebActivity;
import us.forkloop.trackor.util.TrackorSyncReceiver;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";
    public static final int ALARM_REQUEST_CODE = 11;
    public static final String AUTO_SYNC_FREQ = "f";
    PreferenceManager manager;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        setup();
    }

    private void setup() {
        manager = getPreferenceManager();
        // set package version
        try {
            String app = getActivity().getString(R.string.app_name);
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            Preference preference = manager.findPreference("pref_version");
            preference.setTitle(app + " " + version);
        } catch (NameNotFoundException nnfe) {
            Log.d(TAG, "Can not set version " + nnfe.getMessage());
        }

        // add perference click listeners
        Preference tos = manager.findPreference("pref_tos");
        tos.setOnPreferenceClickListener(new PreferenceClickListener("http://forkloop.github.io/"));
        Preference pp = manager.findPreference("pref_pp");
        pp.setOnPreferenceClickListener(new PreferenceClickListener("http://forkloop.github.io/"));

        CheckBoxPreference enabledSync = (CheckBoxPreference) manager.findPreference("pref_sync");
        final ListPreference syncFreq = (ListPreference) manager.findPreference("pref_sync_freq");
        enabledSync.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                toggleAutoSync((Boolean) newValue, syncFreq);
                return true;
            }
        });
        syncFreq.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateAutoSync((String) newValue);
                return true;
            }
        });
    }

    private void toggleAutoSync(boolean status, ListPreference freqPreference) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), TrackorSyncReceiver.class);
        intent.setAction(TrackorSyncReceiver.ACTION);
        final String freq = freqPreference.getValue(); // in minutes
        intent.putExtra(AUTO_SYNC_FREQ, freq);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        if (status) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, 1000L, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void updateAutoSync(String freq) {
        Log.d(TAG, "new auto sync freq " + freq);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), TrackorSyncReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, Integer.parseInt(freq) * 60000L, pendingIntent);
    }

    private class PreferenceClickListener implements Preference.OnPreferenceClickListener {
        private final String url;

        public PreferenceClickListener(String url) {
            this.url = url;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(getActivity(), WebActivity.class);
            intent.putExtra("url", this.url);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
            return true;
        }
    }
}
