package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class TrackorArchiveDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogInterface.OnClickListener listener = new TrackorArchiveDialogClickListener();
        builder.setTitle(R.string.archive_dialog_title)
               .setPositiveButton(android.R.string.ok, listener)
               .setNegativeButton(android.R.string.cancel, listener);

        return builder.create();
    }

    private class TrackorArchiveDialogClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog instanceof AlertDialog) {
                Log.d(TAG, "Clicked on " + dialog + " button " + which);
                dialog.dismiss();
            } else {
                Log.d(TAG, "Unknown dialog, " + dialog);
            }
        }
    }
}