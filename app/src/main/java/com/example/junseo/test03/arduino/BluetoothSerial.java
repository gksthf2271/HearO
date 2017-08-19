package com.example.junseo.test03.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.junseo.test03.MainActivity;
import com.example.junseo.test03.MenuActivity;
import com.example.junseo.test03.R;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Wrapper class for BluetoothAdapter.
 *
 * Manage connect and read thread.
 * Each instance of this has one socket and is responsible for it.
 */
public class BluetoothSerial {
    static final int kMsgConnectBluetooth = 1;
    static final int kMsgReadBluetooth = 2;
    static final int kMsgDisconnectedBluetooth = 3;

    private static final String TAG = BluetoothSerial.class.getSimpleName();
    private final BluetoothAdapter bluetooth_;
    private ConnectThread connect_thread_;
    ///08.09 수정
    //private AcceptThread accept_thread_;
    private String name = "OHOHME";
    ///
    private ReadThread read_thread_;
    private Listener listener_;
    private BluetoothSocket socket_;
    private BluetoothServerSocket Server_socket;
    private BluetoothDevice device_;
    private ReadHandler read_handler_;
    private OutputStream output_stream_ = null;

    public interface Listener {
        void onConnect(BluetoothDevice device);
        void onRead(BluetoothDevice device, byte[] data, int len);
        void onDisconnect(BluetoothDevice device);
    }

    public BluetoothSerial() {
        bluetooth_ = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Ask connect request. Asynchronous.
     * Will notify the connecting result via Listener.
     * @param device device to connect.
     * @param listener listener that will be notified events of the device.
     */
    public void askConnect(BluetoothDevice device, Listener listener) {
        if (connect_thread_ != null) {
            connect_thread_.cancel();
        }
/*        if (accept_thread_ != null){
            accept_thread_.cancel();
        }*/

        device_ = device;
        listener_ = listener;
        read_handler_ = new ReadHandler();
        connect_thread_ = new ConnectThread(device);


        //accept_thread_.start();
        connect_thread_.start();
/*
        View view = null;

        final Button button1 = (Button) view.findViewById(R.id.button1);
        //텍스트 입력 버튼/
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                connect_thread_.start();
            }
        });
*/

    }

    public boolean isConnected() {
        if (read_thread_ == null) {
            return false;
        }
        return read_thread_.isAlive();
    }
///////////////////////////////////////////////////
    //08.09 블루투스 수정


   /* private class AcceptThread extends Thread {
        public AcceptThread(BluetoothDevice device) {
            BluetoothServerSocket tmp = null;
            // The uuid that I want to connect to.
            // This value of uuid is for Serial Communication.
            // http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)
            // https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
               tmp = bluetooth_.listenUsingRfcommWithServiceRecord(name,uuid);

            } catch (Exception e) {
                e.printStackTrace();
            }
            Server_socket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            socket_ =null;

            while(true){
                try{
                    socket_ = Server_socket.accept();
                } catch (IOException e) {
                        break;
                }
                break;
            }
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d(TAG, "OHOHME Connect...");
                socket_.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                // Unable to connect; close the socket and get out
                try {
                    socket_.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }

            Log.d(TAG, "Connected");

            Message msg = read_handler_.obtainMessage(kMsgConnectBluetooth);
            read_handler_.sendMessage(msg);

            // start reading thread.
            read_thread_ = new ReadThread();
            read_thread_.start();

            try {
                output_stream_ = socket_.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // release accept object
            accept_thread_ = null;
        }

        *//** Will cancel an in-progress connection, and close the socket *//*
        public void cancel() {
            try {
                socket_.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    ///////////////////////////////////////////////////
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
        if (connect_thread_ != null) {
            connect_thread_.cancel();
            connect_thread_ = null;
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

    private class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to socket_,
            // because socket_ is final
            BluetoothSocket tmp = null;
            device_ = device;

            // The uuid that I want to connect to.
            // This value of uuid is for Serial Communication.
            // http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)
            // https://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device_.createRfcommSocketToServiceRecord(uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
            socket_ = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetooth_.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d(TAG, "Connect...");
                socket_.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
                // Unable to connect; close the socket and get out
                try {
                    socket_.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }

            Log.d(TAG, "Connected");




            Message msg = read_handler_.obtainMessage(kMsgConnectBluetooth);
            read_handler_.sendMessage(msg);

            // start reading thread.
            read_thread_ = new ReadThread();
            read_thread_.start();

            try {
                output_stream_ = socket_.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // release connect object
            connect_thread_ = null;
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                socket_.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            Log.d("HS_input_stream ", String.valueOf(input_stream_));
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
