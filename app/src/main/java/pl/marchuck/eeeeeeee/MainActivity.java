package pl.marchuck.eeeeeeee;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.BluetoothScope;
import com.example.WhenDetected;

/**
 * @author Lukasz Marczak
 * @since 11.09.16.
 */
@BluetoothScope
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @WhenDetected("mac_address")
    public void doSth() {
        Log.i(TAG, "doSth: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }


}
