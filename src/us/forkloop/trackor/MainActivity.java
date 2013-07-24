package us.forkloop.trackor;

import us.forkloop.trackor.util.SystemUiHider;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //XXX Switch to ListActivity for simplicity
        //setContentView(R.layout.activity_main);

        String[] trackings = {"UPS", "Fedex", "USPS"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trackings);
        setListAdapter(adapter);
    }

}
