package us.forkloop.trackor;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class HelpFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "inflating help fragment to " + container);
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

}
