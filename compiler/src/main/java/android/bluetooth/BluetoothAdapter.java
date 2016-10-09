package android.bluetooth;

/**
 * @author Lukasz Marczak
 * @since 11.09.16.
 */
public class BluetoothAdapter {
    public interface LeScanCallback {
         void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);
    }
}
