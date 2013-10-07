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
        edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
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
            String carrier = bundle.getString("carrier");
            String tNumber = bundle.getString("tnumber");
            int rowId = bundle.getInt("rowid", -1);
            Log.d(TAG, carrier + " : " + tNumber + " : " + rowId);
            if (dialog instanceof AlertDialog) {
                Log.d(TAG, "Clicked on " + dialog + " button " + which);
                String tag = edit.getText().toString();
                Tracking tracking = new Tracking(carrier, tNumber, tag);
                if (rowId < 0) {
                    ((TrackorDBDelegate) getActivity()).addTracking(tracking);
                } // else update
                dialog.dismiss();
            } else {
                Log.d(TAG, "Unknown dialog, " + dialog);
            }
        }
    }

}