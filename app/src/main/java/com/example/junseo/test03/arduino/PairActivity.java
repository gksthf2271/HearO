/*
package com.example.junseo.test03.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junseo.test03.R;
import com.example.junseo.test03.STTActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

*
 * Created by rlagk on 2017-09-07.



public class PairActivity extends Activity {
    private static final String TAG = PairActivity.class.getSimpleName();
    private ListView listview_devices_;
    ServiceThread thread;
    final static int BLUETOOTH_REQUEST_CODE = 100;
    private ListView listview_paired_;

    private Button device_refresh_;
    private Button device_stop_;
    private Set<BluetoothDevice> paired_devices_;
    private BluetoothAdapter bluetooth_;

    private ArrayAdapter listview_adapter_;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private HashMap<String, BluetoothDevice> device_name_map_;

    SimpleAdapter adapterPaired;
    private BluetoothService blservice_;
    private  Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bluetooth_pair);

        blservice_ = new BluetoothService();

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listview_adapter_ = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        //블루투스 장치 새로운 device , 페얼이된 paired device setup view
        listview_devices_ = (ListView) findViewById(R.id.listViewBluetoothDevices);
        listview_paired_ = (ListView) findViewById(R.id.pairedlistview);

        //Adapter1
        //  dataPaired = new ArrayList<>();
        //  adapterPaired = new SimpleAdapter(this, dataPaired, android.R.layout.simple_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1, android.R.id.text2});
        listview_paired_.setAdapter(adapterPaired);


        device_refresh_ = (Button) findViewById(R.id.buttonBluetoothDeviceRefresh);
        device_refresh_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    blservice_.UpdateDeviceListView();

            }
        });


        device_stop_ = (Button) findViewById(R.id.buttonBluetoothStop);
        device_stop_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blservice_.StopDeviceListView();
              device_stop_.setVisibility(v.GONE);
                device_refresh_.setVisibility(v.VISIBLE);

            }
        });


        bluetooth_ = BluetoothAdapter.getDefaultAdapter();


        //paired listview setup 08.20
        ListView pairedListView = (ListView) findViewById(R.id.pairedlistview);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);

        pairedListView.setOnItemClickListener(mDeviceClickListener);
        listview_devices_.setOnItemClickListener(mDeviceClickListener);


        // Get the local Bluetooth adapter
        bluetooth_ = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetooth_.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.PairedTitle).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.bluetooth_acticity_title2).toString();
            mPairedDevicesArrayAdapter.add(noDevices);

        }


        listview_devices_.setAdapter(listview_adapter_);

        device_name_map_ = new HashMap<>();

    }

   private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetooth_.cancelDiscovery();


            //String name = listview_devices_.getItemAtPosition(position).toString();
            String name = ((TextView) view).getText().toString();
            Log.d(TAG, "Connect name : " + name);
            BluetoothDevice device = device_name_map_.get(name);
            if (device == null) {
                Toast.makeText(getApplicationContext(), "Can't find the device!",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent result_intent = new Intent(getApplicationContext(), STTActivity.class);
            result_intent.putExtra("device", device_name_map_.get(name));
            setResult(RESULT_OK, result_intent);
            finish();


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if (resultCode == Activity.RESULT_OK) {
                    //GetListPairedDevice();
                }
                //블루투스 활성화 거절
                else {
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(blservice_.receiver_.onReceive(mContext,));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_pair, menu);
        return true;
    }


}


*/
