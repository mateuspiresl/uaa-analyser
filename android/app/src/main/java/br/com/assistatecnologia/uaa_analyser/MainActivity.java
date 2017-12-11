package br.com.assistatecnologia.uaa_analyser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ControlClient client = new ControlClient();
        Analyser analyser = new Analyser();
        
        ((ListView) super.findViewById(R.id.lv_long_delays)).setAdapter(new ArrayAdapter<>(this,
                R.layout.list_long_delays_item, UserLog.get()));
    }
}
