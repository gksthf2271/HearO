package com.example.junseo.test03.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.junseo.test03.MainActivity;
import com.example.junseo.test03.R;
import com.example.junseo.test03.STTActivity;

import java.util.HashMap;
import java.util.Set;

/**
 * Activity that pairs with bluetooth devices.
 *
 * Lists out the bluetooth devices and connects them.
 */
public class BluetoothPairActivity extends Activity {
    private static final String TAG = BluetoothPairActivity.class.getSimpleName();
    private ListView listview_devices_;
    private Button device_refresh_;
    private Set<BluetoothDevice> paired_devices_;
    private BluetoothAdapter bluetooth_;
    private ArrayAdapter listview_adapter_;
    private HashMap<String, BluetoothDevice> device_name_map_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_pair);

        setBackgroundColor();

        listview_devices_ = (ListView) findViewById(R.id.listViewBluetoothDevices);
        device_refresh_ = (Button) findViewById(R.id.buttonBluetoothDeviceRefresh);
        device_refresh_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateDeviceListView();
            }
        });

        bluetooth_ = BluetoothAdapter.getDefaultAdapter();

        listview_adapter_ = new ArrayAdapter(this,android.R.layout.simple_list_item_1);
        listview_devices_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = listview_devices_.getItemAtPosition(position).toString();
                Log.d(TAG, "Connect name : " + name);
                BluetoothDevice device = device_name_map_.get(name);
                if (device == null) {
                    Toast.makeText(getApplicationContext(),"Can't find the device!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Intent result_intent = new Intent(getApplicationContext(), STTActivity.class);
                result_intent.putExtra("device",device_name_map_.get(name));
                setResult(RESULT_OK, result_intent);
                finish();
            }
        });

        listview_devices_.setAdapter(listview_adapter_);
        device_name_map_ = new HashMap<>();
    }

    // Set gradient background color.
    private void setBackgroundColor() {
        View layout = findViewById(R.id.pairActivity);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0xFFF0FAFF,0xFFA3E0FF});
        gd.setCornerRadius(0f);
        layout.setBackground(gd);
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateDeviceListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver_);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_pair, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ClearDeviceList() {
        listview_adapter_.clear();
        device_name_map_.clear();
    }

    public void UpdateDeviceListView() {
        if (!bluetooth_.isEnabled()) {
            Toast.makeText(getApplicationContext(),"bluetooth is not enabled",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ClearDeviceList();
        //UpdateBondedDevices();
        DiscoverDevices();
    }

    private void UpdateBondedDevices() {
        paired_devices_ = bluetooth_.getBondedDevices();

        for(BluetoothDevice device : paired_devices_) {
            Log.d(TAG, "bonded devices : " + device.getName());
            AddDevice(device);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver_ = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "found devices : " +  device.getName());
                // Add the name and address to an array adapter to show in a ListView
                AddDevice(device);
            }
        }
    };

    private void AddDevice(BluetoothDevice device) {
        String name = device.getName() + "\n" + device.getAddress();
        if (!device_name_map_.containsKey(name)) {
            listview_adapter_.add(name);
        }
        // Even if the map already has the device, put the object again to have the new device object
        device_name_map_.put(name, device);
    }

    private void DiscoverDevices() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver_, filter); // Don't forget to unregister during onDestroy

        if (!bluetooth_.startDiscovery()) {
            Log.e(TAG, "startDiscovery failed");
        }
    }

    @Override
    public String toString() {
        String str = "Bluetooth pair Activit\n";
        for (BluetoothDevice device : device_name_map_.values()) {
            str += device.toString() + "\n";
        }
        return str;
    }
}
