
package com.example.junseo.test03.arduino;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.io.FileDescriptor;


public class BluetoothService extends Service implements IBinder {

    ServiceThread thread;
    BluetoothSerial.ConnectThread connectThread;
    BluetoothSerial.ReadThread readThread;
    BluetoothSerial.Listener listener;
    BluetoothDevice device_;
    private static final String TAG = BluetoothService.class.getSimpleName();

    private final IBinder mBinder = new ServiceBinder();

    @Override
    public String getInterfaceDescriptor() throws RemoteException {
        return null;
    }

    @Override
    public boolean pingBinder() {
        return false;
    }

    @Override
    public boolean isBinderAlive() {
        return false;
    }

    @Override
    public IInterface queryLocalInterface(String descriptor) {
        return null;
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {

    }

    @Override
    public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {

    }

    @Override
    public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }

    @Override
    public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {

    }

    @Override
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return false;
    }

    public class ServiceBinder extends Binder {
         BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    ///시작타입 서비스 onBind는 널값을 리턴하면된다.
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    //연결타입 서비스는 onStartCommand를 재정의할필요없다.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        Log.d(TAG,"BLService Start");
        thread.start();

        return START_STICKY;
    }



    public void onDestroy() {
        thread.stopForever();       //블루투스 종료부분
        //thread.stop();
        readThread.stop();
        connectThread.stop();

        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(BluetoothService.this, BluetoothPairActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(BluetoothService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //블루투스 장치 새로운 device , 페얼이된 paired device setup view
  /*          Toast.makeText(getApplicationContext(),
                    "시작한다!", Toast.LENGTH_LONG).show();*/

                switch(msg.arg1){
                    case BluetoothSerial.kMsgConnectBluetooth:
                        Log.d(TAG,"BS_TEST_CONNECT");
                        connectThread.start();
                        //listener.onConnect();
                        break;
                    case BluetoothSerial.kMsgReadBluetooth:
                        Log.d(TAG,"BS_TEST_READ");
                        readThread.start();
                        //listener.onRead();
                        break;
                    case BluetoothSerial.kMsgDisconnectedBluetooth:
                        Log.d(TAG,"BS_TEST_DISCONNECT");
                       // listener.onDisconnect(device_,listener);
                        onDestroy();

                        break;
                }


        }
    }
}

