package com.example.junseo.test03;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.junseo.test03.arduino.BluetoothSerial;

public class BluetoothService {

    Context context;

    public BluetoothService(Context context){
        this.context= context;
    }

    // Debugging
    private static final String TAG = "BluetoothService";

    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Handler mHandler;

    static final int kMsgConnectBluetooth = 1;
    static final int kMsgReadBluetooth = 2;
    static final int kMsgDisconnectedBluetooth = 3;
    private BluetoothSocket socket_;
    private OutputStream output_stream_ = null;



    //
    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        // BluetoothAdapter 얻기
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    public BluetoothService() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");


    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private ReadThread read_thread_;
    private BluetoothDevice device_;
    private ReadHandler read_handler_;
    private Listener listener_;

    private int mState;

    // ���¸� ��Ÿ���� ���� ����
    private static final int STATE_NONE = 0; // we're doing nothing
    private static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    private static final int STATE_CONNECTED = 3; // now connected to a remote
    // device



    /**
     * Check the Bluetooth support
     *
     * @return boolean
     */
    //블루투스 지원 여부 확인 함수
    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;

        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    /**
     * Check the enabled Bluetooth (블루투스 활성화 요청)
     */
    public void enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled()) {
            // 기기의 블루투스 상태가 On인 경우
            Log.d(TAG, "Bluetooth Enable Now");

            // Next Step
            scanDevice();
        } else {
            // 기기의 블루수트 상태가 off인 경우
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Available device search
     */
    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    /**
     * after scanning and get device info
     *
     * @param data
     */
    public void getDeviceInfo(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    // Bluetooth ���� set
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    // Bluetooth 상태 get
    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // ConnectThread 초기화 device의 모든 연결 제거
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);

        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // ConnectedThread 초기화
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    // 모든 thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     *  Write data. Synchronous
     * @param bytes data to send.
     */
    public void Write(byte[] bytes) {
        if (output_stream_ == null) {
            return;
        }
        try {
            output_stream_.write(bytes);
            Log.d("KHS 패킷 -> ", String.valueOf(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (socket_ != null) {
            try {
                socket_.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (read_thread_ != null) {
            try {
                read_thread_.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            read_thread_ = null;
        }
    }

    // ���� ����������
    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    // ������ �Ҿ��� ��
    private void connectionLost() {
        setState(STATE_LISTEN);

    }




    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

			/*
			 * / // Get a BluetoothSocket to connect with the given
			 * BluetoothDevice try { // MY_UUID is the app's UUID string, also
			 * used by the server // code tmp =
			 * device.createRfcommSocketToServiceRecord(MY_UUID);
			 *
			 * try { Method m = device.getClass().getMethod(
			 * "createInsecureRfcommSocket", new Class[] { int.class }); try {
			 * tmp = (BluetoothSocket) m.invoke(device, 15); } catch
			 * (IllegalArgumentException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } catch
			 * (InvocationTargetException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); }
			 *
			 * } catch (NoSuchMethodException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } } catch (IOException e) { } /
			 */

            // ����̽� ������ �� BluetoothSocket ����
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
            // 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
            btAdapter.cancelDiscovery();
            String mText;


            // BluetoothSocket 연결 시도
            try {
                // BluetoothSocket 연결 시도에 대한 return 값은 success 또는 exception이다.
                mmSocket.connect();
                Log.d(TAG, "Connect Success");
                Toast.makeText(context, "연결이 완료되었습니다.", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                connectionFailed(); // 연결 실패시 불러오는 메소드
                Log.d(TAG, "Connect Fail");

                // socket을 닫는다.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // 연결중? 혹은 연결 대기상태인 메소드를 호출한다.
                BluetoothService.this.start();
                return;
            }

            // ConnectThread 클래스를 reset한다.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread를 시작한다.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket�� inputstream �� outputstream�� ��´�.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
                    bytes = mmInStream.read(buffer);

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
         * @param buffer
         * The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                // 값을 쓰는 부분(값을 보낸다)
                mmOutStream.write(buffer);

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
    public void askConnect(BluetoothDevice device, Listener listener) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
        device_ = device;
        listener_ = listener;
        read_handler_ = new ReadHandler();
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public interface Listener {
        void onConnect(BluetoothDevice device);
        void onRead(BluetoothDevice device, byte[] data, int len);
        void onDisconnect(BluetoothDevice device);
    }

    private class ReadThread extends Thread {
        private final InputStream input_stream_;

        public ReadThread() {
            InputStream tmpIn = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket_.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            input_stream_ = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = input_stream_.read(buffer);
                    // TODO: To prevent memory allocation each time,
                    // it may need a byte queue and synchronization.
                    byte[] fragment = Arrays.copyOf(buffer, bytes);

                    // Send the obtained bytes to the UI activity
                    read_handler_.obtainMessage(kMsgReadBluetooth, bytes, -1, fragment)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
            // Notify the end of connection.
            read_handler_.obtainMessage(kMsgDisconnectedBluetooth, 0, 0, 0).sendToTarget();
        }
    }

    protected class ReadHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case kMsgConnectBluetooth:
                    listener_.onConnect(device_);
                    break;
                case kMsgReadBluetooth:
                    listener_.onRead(device_, (byte[])msg.obj, msg.arg1);
                    break;
                case kMsgDisconnectedBluetooth:
                    listener_.onDisconnect(device_);
                    break;
            }
        }
    }
}