package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import us.forkloop.trackor.TrackorApp;
import us.forkloop.trackor.TrackorDBDelegate;
import us.forkloop.trackor.db.Tracking;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

public class TrackorAddTagDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();
    private EditText edit;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TrackorApp app = TrackorApp.getInstance(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        edit = new EditText(getActivity());
        edit.setTypeface(app.getTypeface("Gotham-Book.otf"));
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        final String tag = getArguments().getString(PullableListView.TRACKING_TAG_KEY);
        if (tag != null) {
            edit.setText(tag);
        }
        DialogInterface.OnClickListener listener = new TrackorAddTagDialogClickListener();
        builder.setTitle(R.string.add_tag_dialog_title)
                .setView(edit)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener);
        return builder.create();
    }

    private class TrackorAddTagDialogClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Bundle bundle = getArguments();
            String trackingNumber = bundle.getString(PullableListView.TRACKING_NUMBER_KEY);
            String action = bundle.getString("action");
            String tag = edit.getText().toString();
            if ("add".equals(action)) {
                Tracking tracking;
                String carrier = bundle.getString("carrier");
                Log.d(TAG, "add new tracking " + carrier + ": " + trackingNumber + ": " + tag);
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    tracking = new Tracking(carrier, trackingNumber, tag, false);
                } else {
                    tracking = new Tracking(carrier, trackingNumber, null, false);
                }
                ((TrackorDBDelegate) getActivity()).addTracking(tracking);
            } else if ("update".equals(action)) {
                Log.d(TAG, "update tracking " + ": " + trackingNumber + ": " + tag);
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ((TrackorDBDelegate) getActivity()).updateTracking(trackingNumber, tag);
                } // else nothing
            }
        }
    }

}