package pepsl.com.bluescan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClientService extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private final static String TAG="ClientService";
    private int mState;
    private ListView mList;
    private BlueScanAdaper adapter;
    private Button start_scan;
    private BluetoothManager mBlueManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<ScanFilter> scanFilterList = new ArrayList<>();
    private Handler mHandler;
    private boolean mScanning;
    private Button bt_ble_broadcast;
    public final static UUID MY_UUID = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    private EditText edit;
    private Button send_msg;
    private BluetoothGatt mGatt;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mList = (ListView) findViewById(R.id.scan_list);
        start_scan = (Button)findViewById(R.id.start_scan);

        edit = (EditText)findViewById(R.id.text_edit);
        send_msg = (Button) findViewById(R.id.send_msg);
        send_msg.setOnClickListener(this);

        start_scan.setOnClickListener(this);
        adapter = new BlueScanAdaper(this);
        mList.setAdapter(adapter);
        mList.setOnItemClickListener(this);
        mBlueManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBlueManager.getAdapter();
        reflectSetScanMode(bluetoothAdapter);
        mHandler = new Handler();
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.start_scan) {
            Log.i(TAG, "start blue scan!!!");
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            if (bluetoothAdapter != null) {

                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            }
            if(adapter.getCount() > 0){
                adapter.removAll();
            }
            scanLeDevice(true);
        }else if(v.getId() == R.id.send_msg){
            String msg = edit.getText().toString();
            if(msg != null && mGatt != null){
                BluetoothGattCharacteristic characteristic = mGatt.getService(ServerService.UUID_SERVICE).getCharacteristic(ServerService.UUID_SERVICE);
                mGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        ServerService.UUID_SERVICE);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                //mGatt.writeDescriptor(descriptor);

                characteristic.setValue(msg.getBytes()); //单次最多20个字节
                characteristic.addDescriptor(descriptor);
                mGatt.writeCharacteristic(characteristic);
                //mGatt.readCharacteristic(characteristic);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG,"onItemClick position = " + position);
        BlueInfo info = (BlueInfo) parent.getItemAtPosition(position);
        ScanResult scanResult = info.getScanResult();
        if(scanResult != null) {
            Log.i(TAG,"start connectGatt!!!");
            scanResult.getDevice().connectGatt(getApplicationContext(), false, bluetoothGattCallback);
        }else {
            Log.i(TAG,"scanResult is null!!!");
        }
    }

    private void reflectSetScanMode(BluetoothAdapter bluetoothAdapter){
        Class cl = null;
        try {
            cl = Class.forName("android.bluetooth.BluetoothAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Method setScanMode = null;
        try {
            setScanMode = cl.getMethod("setScanMode", Integer.TYPE);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        setScanMode.setAccessible(true);
        try {
            setScanMode.invoke(bluetoothAdapter,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                }
            }, 30000);
            mScanning = true;
            Log.i(TAG," scan mode = " + bluetoothAdapter.getScanMode());

            //匹配uuid扫描
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(String.valueOf(ServerService.UUID_SERVICE))).build();
            scanFilterList.add(filter);
            bluetoothLeScanner.startScan(scanFilterList,new ScanSettings.Builder().build(),scanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.i(TAG,"onScanResult!!! --- callbackType = " + callbackType + " 设备:" + result.getDevice().getName() + " uuid = " + result.getDevice().getUuids());
            if(result.getDevice().getName() != null) {
                adapter.add(new BlueInfo(result));
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i(TAG,"onBatchScanResults!!!\n" + results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(TAG,"onScanFailed!!! --- errorCode = " + errorCode);
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    "LIST_NAME", "current service test-1");
            currentServiceData.put("LIST_UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        "LIST_NAME", "current charaDate test-1");
                currentCharaData.put("LIST_UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                Log.i(TAG,"gattCharacteristic.getPermissions() = " + gattCharacteristic.getPermissions());
                mGatt.setCharacteristicNotification(gattCharacteristic, true);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        for(ArrayList<BluetoothGattCharacteristic> mGattCharacteristics:mGattCharacteristics){
            Log.i(TAG,"Characteristics -----> " + mGattCharacteristics.toString());
        }

        for(ArrayList<HashMap<String, String>> mGattCharacteristicsData:gattCharacteristicData){
            Log.i(TAG,"gattCharacteristicData -----> " + mGattCharacteristicsData.toString());
        }
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.i(TAG,"bluetoothGattCallback----onPhyUpdate status = " + status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.i(TAG,"bluetoothGattCallback----onPhyRead status = " + status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG,"bluetoothGattCallback----onConnectionStateChange  status = " + status + " newState = " + newState + " readRemoteRssi() = " + gatt.readRemoteRssi() + " remote_device_name = " + gatt.getDevice().getName());

            mGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices(); //启动服务发现
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG,"bluetoothGattCallback----onServicesDiscovered  status = " + status);
            displayGattServices(gatt.getServices());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG,"bluetoothGattCallback----onCharacteristicRead status = " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            try {
                String value = new String(characteristic.getValue(),"UTF-8");
                Log.i(TAG,"bluetoothGattCallback --- onCharacteristicWriteRequest value = " + value + " status = " + status);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            try {
                String value = new String(characteristic.getValue(),"UTF-8");
                Log.i(TAG,"bluetoothGattCallback --- onCharacteristicChanged value = " + value );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(TAG,"bluetoothGattCallback----onDescriptorRead status = " + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(TAG,"bluetoothGattCallback----onDescriptorWrite status = " + status);

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.i(TAG,"bluetoothGattCallback----onReliableWriteCompleted status = " + status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.i(TAG,"bluetoothGattCallback----onReadRemoteRssi rssi = " + rssi + " status = " + status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG,"bluetoothGattCallback----onMtuChanged mtu = " + mtu + " status = " + status);
        }
    };

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ClientConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ClientConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        //updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        //startServerAcceptThread();
    }
}
