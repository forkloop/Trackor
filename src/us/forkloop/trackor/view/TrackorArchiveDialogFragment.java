package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import us.forkloop.trackor.TrackorDBDelegate;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class TrackorArchiveDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();
    private String trackingNumber;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        trackingNumber = bundle.getString(PullableListView.TRACKING_NUMBER_KEY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogInterface.OnClickListener listener = new TrackorArchiveDialogClickListener();
        builder.setTitle(R.string.archive_dialog_title)
               .setPositiveButton(android.R.string.ok, listener)
               .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    private class TrackorArchiveDialogClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                Log.d(TAG, "archiving tracking " + trackingNumber);
                ((TrackorDBDelegate) getActivity()).archiveTracking(trackingNumber);
            }
        }
    }
}