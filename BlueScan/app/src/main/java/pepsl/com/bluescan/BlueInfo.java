package pepsl.com.bluescan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;

/**
 * Created by chongming on 18-12-6.
 */

public class BlueInfo implements Comparable<BlueInfo> {
    private final static String TAG="BlueInfo";
    private String name;
    private String address;
    private ScanResult scanResult;
    private int rssi;
    protected BlueInfo(Parcel in) {
    }



    public BlueInfo(String name, String address, int rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    public BlueInfo(ScanResult scanResult) {
        this.scanResult =scanResult;
        this.name = scanResult.getDevice().getName();
        this.address = scanResult.getDevice().getAddress();
        this.rssi = scanResult.getRssi();
    }

    public ScanResult getScanResult(){
        return scanResult;
    }
    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public int getRssi(){
        return rssi;
    }

    @Override
    public int compareTo(BlueInfo o) {
        return 0;
    }
}
