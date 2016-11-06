package pl.marchuck.eeeeeeee;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BluetoothScope;
import com.example.WhenDetected;

import java.util.Date;

/**
 * @author Lukasz Marczak
 * @since 11.09.16.
 */
@BluetoothScope
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 0;
    private static final String variable = "";

    static class ImmutableMac {
        final String mac;

        public ImmutableMac(String mac) {
            this.mac = mac;
        }
    }

    @WhenDetected("mac_address")
    public void doSth() {
        Log.i(TAG, "doSth: ");
    }

    @WhenDetected("")
    public void doSthElse() {

    }

    MainActivityBleScanner bleScanner = new MainActivityBleScanner();

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        btAdapter.startLeScan(bleScanner);
    }

    @Override
    protected void onPause() {
        btAdapter.stopLeScan(bleScanner);

        super.onPause();
    }

    TextView textView;

    final PojoOpenHelper helper = new PojoOpenHelper(MainActivity.this);
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bleScanner.init(this);

        enableBluetoothAdapterIfNeeded();
        textView = (TextView) findViewById(R.id.textview);

        Button addBtn = (Button) findViewById(R.id.add);
        final Button searchBtn = (Button) findViewById(R.id.search);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                addNewPojoDialog();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPojoDialog();
            }
        });


        PojoOpenHelper openHelper = PojoOpenHelper.
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    void enableBluetoothAdapterIfNeeded() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    void addNewPojoDialog() {
        Log.d(TAG, "addNewPojoDialog: ");
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.form);
        final EditText uuid = (EditText) dialog.findViewById(R.id.uuid);
        final EditText age = (EditText) dialog.findViewById(R.id.age);
        final EditText number = (EditText) dialog.findViewById(R.id.number);
        final EditText message = (EditText) dialog.findViewById(R.id.message);
        Button saveBtn = (Button) dialog.findViewById(R.id.save);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _id = uuid.getText().toString();
                helper.addPojo(new Pojo(
                        _id,
                        message.getText().toString(),
                        Integer.valueOf(number.getText().toString()),
                        new Date().toString(),
                        Short.valueOf(age.getText().toString())
                ), true);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "saved " + _id, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    void searchPojoDialog() {
        Log.d(TAG, "searchPojoDialog: ");
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.search);
        final EditText number = (EditText) dialog.findViewById(R.id.number);
        Button searchBtn = (Button) dialog.findViewById(R.id.search);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String _number = number.getText().toString();
                Pojo pojo = (Pojo) helper.getPojo(PojoOpenHelper.KEY_NUMBER, _number);
                if (pojo == null) {
                    Toast.makeText(MainActivity.this, "Cannot found " + _number, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, _number + " found!", Toast.LENGTH_SHORT).show();
                }

                textView.setText(String.valueOf(pojo));
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
