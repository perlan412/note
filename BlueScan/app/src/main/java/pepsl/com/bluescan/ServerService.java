package pepsl.com.bluescan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.media.PlaybackParams;
import android.net.wifi.aware.Characteristics;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ServerService extends Activity implements View.OnClickListener {

    private final static String TAG="ServerService";
    private int mState;
    private Button bt_ble_broadcast;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdver;
    private ServerAcceptThread mServerAcceptThread;
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final UUID UUID_SERVICE = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private BluetoothManager mBlueManager;
    private BluetoothGattServer mBlueGattServer;
    private BluetoothGattService service;
    private ListView mListView;
    private ServerPrintLogAdapter logAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_server);
        bt_ble_broadcast = (Button)findViewById(R.id.bt_ble_broadcast);
        mListView = (ListView) findViewById(R.id.print_log);
        logAdapter = new ServerPrintLogAdapter(this);
        mListView.setAdapter(logAdapter);
        bt_ble_broadcast.setOnClickListener(this);
        mBlueManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBlueManager.getAdapter();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_ble_broadcast){
            Log.i(TAG, "start ble_adver!!!");
            logAdapter.add(new LogInfo("start ble_adver!!!"));
            logAdapter.notifyDataSetChanged();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            if (bluetoothAdapter != null) {
                bluetoothLeAdver = bluetoothAdapter.getBluetoothLeAdvertiser();
                bluetoothLeAdver.startAdvertising( new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER).setTimeout(2*60*1000).setConnectable(true).build(),new AdvertiseData.Builder().addServiceUuid(ParcelUuid.fromString(String.valueOf(ServerService.UUID_SERVICE))).setIncludeDeviceName(true).build(),advertiseCallback);
                startServerAcceptThread();
            }
        }
    }

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.i(TAG,"advertiseCallback----onStartSuccess = " + settingsInEffect.toString());
            Message message = new Message();
            message.obj = "advertiseCallback----onStartSuccess = " + settingsInEffect.toString();
            mHandler.sendMessage(message);
            mBlueGattServer = mBlueManager.openGattServer(getApplicationContext(),gattServerCallback);
            service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic characteristicReadWrite = new BluetoothGattCharacteristic(UUID_SERVICE,
                    BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattDescriptor descriptor1 = new BluetoothGattDescriptor(UUID_SERVICE, BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ);
            characteristicReadWrite.addDescriptor(descriptor1);
            service.addCharacteristic(characteristicReadWrite);
            mBlueGattServer.addService(service);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.i(TAG,"advertiseCallback----errorCode = " + errorCode);
            Message message = new Message();
            message.obj = "advertiseCallback----errorCode = " + errorCode;
            mHandler.sendMessage(message);
        }
    };

    private BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG,"gattServerCallback --- onConnectionStateChange status = " + status + " newState = " + newState);
            Message message = new Message();
            message.obj = "gattServerCallback --- onConnectionStateChange status = " + status + " newState = " + newState;
            mHandler.sendMessage(message);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG,"remote_device_type = " + device.getType() + " remote_client_device_name = " + device.getName() + " device.getBondState() = " + device.getBondState() + " device.getUuids() = " + device.getUuids());
                //if(device.getBondState() == BluetoothDevice.BOND_NONE)
                //    device.createBond();
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.i(TAG,"gattServerCallback --- onServiceAdded");
            Message message = new Message();
            message.obj = "gattServerCallback --- onServiceAdded";
            mHandler.sendMessage(message);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.i(TAG,"gattServerCallback --- onCharacteristicReadRequest");
            Message message = new Message();
            message.obj = "gattServerCallback --- onCharacteristicReadRequest";
            mHandler.sendMessage(message);
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            try {
                String res = new String(value,"UTF-8");
                Log.i(TAG,"gattServerCallback --- onCharacteristicWriteRequest value = " + res);
                Message message = new Message();
                message.obj = "gattServerCallback --- onCharacteristicWriteRequest value = " + res;
                mHandler.sendMessage(message);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

            final Timer timer =  new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.i(TAG,"timer start notifyCharacteristicChanged!!!");
                    Message message = new Message();
                    message.obj = "timer start notifyCharacteristicChanged!!!";
                    mHandler.sendMessage(message);
                    characteristic.setValue("test".getBytes());
                    mBlueGattServer.notifyCharacteristicChanged(device,characteristic,false);
                    timer.purge();
                    timer.cancel();
                }
            },5000);

        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.i(TAG,"gattServerCallback --- onDescriptorReadRequest");
            Message message = new Message();
            message.obj = "gattServerCallback --- onDescriptorReadRequest";
            mHandler.sendMessage(message);
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.i(TAG,"gattServerCallback --- onDescriptorWriteRequest");
            Message message = new Message();
            message.obj = "gattServerCallback --- onDescriptorWriteRequest";
            mHandler.sendMessage(message);
            mBlueGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.i(TAG,"gattServerCallback --- onExecuteWrite");
            Message message = new Message();
            message.obj = "gattServerCallback --- onExecuteWrite";
            mHandler.sendMessage(message);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.i(TAG,"gattServerCallback --- onNotificationSent");
            Message message = new Message();
            message.obj = "gattServerCallback --- onNotificationSent";
            mHandler.sendMessage(message);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            Log.i(TAG,"gattServerCallback --- onMtuChanged");
            Message message = new Message();
            message.obj = "gattServerCallback --- onMtuChanged";
            mHandler.sendMessage(message);
        }

        @Override
        public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(device, txPhy, rxPhy, status);
            Log.i(TAG,"gattServerCallback --- onPhyUpdate");
            Message message = new Message();
            message.obj = "gattServerCallback --- onPhyUpdate";
            mHandler.sendMessage(message);
        }

        @Override
        public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
            super.onPhyRead(device, txPhy, rxPhy, status);
            Log.i(TAG,"gattServerCallback --- onPhyRead");
            Message message = new Message();
            message.obj = "gattServerCallback --- onPhyRead";
            mHandler.sendMessage(message);
        }
    };

    public synchronized void startServerAcceptThread() {
        Log.d(TAG, "start startServerAcceptThread!!");

        if (mServerAcceptThread == null) {
            mServerAcceptThread = new ServerAcceptThread(false);
            mServerAcceptThread.start();
            Log.i(TAG,"mServerAcceptThread.start()");
            Message message = new Message();
            message.obj = "mServerAcceptThread.start()";
            mHandler.sendMessage(message);
        }
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class ServerAcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public ServerAcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            Log.i(TAG,"AcceptThread ---- mSocketType = " + mSocketType);

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NAME_SECURE",
                            UUID_SERVICE);
                } else {
                    tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            "NAME_INSECURE", UUID_SERVICE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    Log.i(TAG,"mmServerSocket.accept()");
                    Message message = new Message();
                    message.obj = "mmServerSocket.accept()";
                    mHandler.sendMessage(message);
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
            }
        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String log = (String) msg.obj;
            Log.i(TAG,"handleMessage log = " + log + " logAdapter = " + logAdapter.getCount());
            logAdapter.add(new LogInfo(log));
            logAdapter.notifyDataSetChanged();
            return false;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        logAdapter.removeAll();
        mBlueGattServer.close();
    }
}
