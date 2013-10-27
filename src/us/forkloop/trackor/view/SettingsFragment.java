package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import us.forkloop.trackor.WebActivity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        setup();
    }

    private void setup() {
        PreferenceManager manager = getPreferenceManager();
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
