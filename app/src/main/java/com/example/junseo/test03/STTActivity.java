package com.example.junseo.test03;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junseo.test03.arduino.ArduinoConnector;
import com.example.junseo.test03.arduino.BluetoothPairActivity;
//import com.example.junseo.test03.arduino.BluetoothService;
import com.example.junseo.test03.arduino.BluetoothSerial;
import com.example.junseo.test03.arduino.PacketParser;
//import com.example.junseo.test03.arduino.PairActivity;
import com.example.junseo.test03.service.BTCTemplateService;
import com.example.junseo.test03.speech.CommandSpeechFilter;
import com.example.junseo.test03.speech.EnhancedSpeechRecognizer;
import com.example.junseo.test03.speech.SignalSpeechFilter;
import com.example.junseo.test03.speech.SpeechListener;
import com.example.junseo.test03.utils.AppSettings;
import com.example.junseo.test03.utils.Constants;
import com.example.junseo.test03.utils.Logs;
import com.example.junseo.test03.utils.RecycleUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;


/**
 * Created by Junseo on 2017-07-04.
 */


public class STTActivity extends AppCompatActivity implements View.OnClickListener  {
    Button speakbtn = null;

    private boolean flag = false;


    private BluetoothAdapter bluetooth_;
    private static TextView txt_app_status_;
    private ProgressBar progress_bar_;
    private EnhancedSpeechRecognizer speech_recognizer_;
    private ArduinoConnector arduinoConnector_;
    private final AppStateManager app_status_manager_ = new AppStateManager();
    // The value for magnifying to display on progress bar.
    private final int kSpeechMagnifyingValue = 100;
    private Button Cancel3;


    // Context, System
    public static Context mContext;
    private BTCTemplateService mService;
    private ActivityHandler mActivityHandler;

    //블루투스 상태
    private TextView txtState;
    private ImageView mImageBT;
    private RelativeLayout BlMonitoring;

    //다이얼
    AlertDialog.Builder ad;

    DialogInterface mPopupDlg = null;

    // Refresh timer
    private Timer mRefreshTimer = null;


    private static final String TAG = STTActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);
        Cancel3 = (Button) findViewById(R.id.Cancel3);

        speakbtn = (Button) findViewById(R.id.speakbtn);

        //블루투스 setup view
        txtState = (TextView)findViewById(R.id.status_text);
        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        BlMonitoring = (RelativeLayout)findViewById(R.id.bl_monitoring);
        mActivityHandler = new ActivityHandler(); // 블루투스 상태 핸들러


        progress_bar_ = (ProgressBar)findViewById(R.id.progressBarSpeech);
        progress_bar_.setMax(normalizeSpeechValue(EnhancedSpeechRecognizer.kSpeechMaxValue));   //
        txt_app_status_ = (TextView) findViewById(R.id.textViewSpeachResult);
        updateStatusUIText(app_status_manager_.getStatus());




        //블루투스 브로드캐스트 리시버
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        //registerReceiver(mBluetoothStateReceiver, stateFilter);


        speech_recognizer_ = buildSpeechRecognizer();       // 여기까지 화면구성
        arduinoConnector_ = new ArduinoConnector(arduino_listener_);    //아두이노 리스너 객체 생성
        Cancel3.setOnClickListener(new View.OnClickListener() { //뒤로가기버튼
            @Override
            public void onClick(View v) {
                //unbindService(mServiceConn);
                finish();
            }
        });

        //standby 다이얼로그
        ad = new AlertDialog.Builder(this);

        ad.setTitle("알림");       // 제목 설정
        ad.setMessage("음성인식이 중단되었습니다 다시 시작하시겠습니까?");   // 내용 설정

        ad.setPositiveButton("다시시작",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"다시시작 클릭");
                dialog.dismiss();     //닫기
                speech_recognizer_.start();

            }
        });
        ad.setNegativeButton("닫기",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"닫기 클릭");
                dialog.dismiss();     //닫기
                //09.08
                speech_recognizer_.stop();
                finish();

            }
        });
        doStartService();

        mContext=this;
    }

    @Override
    public void onClick(View view){

        switch (view.getId()){


            case R.id.speakbtn:
                speech_recognizer_.start();
                break;
        }

    }
    EnhancedSpeechRecognizer buildSpeechRecognizer() {


        //Build listener chain in reverse order of event deliver order.


        CommandSpeechFilter cmd_filter = new CommandSpeechFilter(speech_listener_);
        // Add commands that it will listen for.
        final Resources rs = getResources();
        cmd_filter.addPattern(rs.getString(R.string.command_lighton),
                rs.getString(R.string.command_lighton_variant));
        cmd_filter.addPattern(rs.getString(R.string.command_lightoff),
                rs.getString(R.string.command_lightoff_variant));

        SignalSpeechFilter signal_filter = new SignalSpeechFilter(cmd_filter, // connect cmd_filter.
                rs.getString(R.string.speech_singal));

        return new EnhancedSpeechRecognizer(this, speech_recognizer_listener_,
                signal_filter); // connect signal_filter.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mBluetoothStateReceiver);
        //arduinoConnector_.destroy();
        speech_recognizer_.destroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();


/*    //09.08
        if(flag == true) {
            speech_recognizer_.destroy();
            speech_recognizer_.start();
            flag = false;
        }*/
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        //09.08
        //speech_recognizer_.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            BluetoothDevice device = intent.getParcelableExtra("device");
            arduinoConnector_.connect(device);
            Log.d(TAG,"BLUETOOTH CONNECT");

            Intent result_intent = new Intent(getApplicationContext(), MenuActivity.class);
            result_intent.putExtra("device",device);
            setResult(RESULT_OK, result_intent);

        }
    }

/*    // connection button listener.
    public void onPair(View v){

        Intent intent = new Intent(getApplicationContext(), BluetoothPairActivity.class);
        startActivityForResult(intent, 0);
    }*/

    // Handles the speeches delivered by EnhancedSpeechRecognizer.
    private SpeechListener speech_listener_ = new SpeechListener() {
        @Override
        public void onSpeechRecognized(ArrayList<String> recognitions) {
            if (recognitions.isEmpty()) {
                return;
            }
            // Use only the first command.
            String cmd = recognitions.get(0);
            Toast.makeText(getApplicationContext(), cmd, Toast.LENGTH_SHORT).show();

/*            try {
                arduinoConnector_.send(cmd);
                Log.d(TAG,"안드로이드 -> 아두이노 데이터전달");
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }*/
        }
    };


/**
     * Change the value, ranged from -2.12 to 10, into new value ranged from 0 to 1212.
     * @param value speech level from SpeechRecognizer.
     * @return normalized value.
     */

    private int normalizeSpeechValue(float value) {
        return (int)((value + Math.abs(EnhancedSpeechRecognizer.kSpeechMinValue))
                * kSpeechMagnifyingValue);
    }


/**
     * Listener for speech recognition.
     */

    private EnhancedSpeechRecognizer.Listener speech_recognizer_listener_ =
            new EnhancedSpeechRecognizer.Listener() {
                @Override
                public void onStart() {
                    app_status_manager_.updateSpeechRecognitionStatus(true);
                    updateStatusUIText(app_status_manager_.getStatus());
                }

                @Override
                public void onStop() {
                    app_status_manager_.updateSpeechRecognitionStatus(false);
                    updateStatusUIText(app_status_manager_.getStatus());
                }

                @Override
                public void onSoundChanged(float rmsdB) {
                    final int increment = normalizeSpeechValue(rmsdB) - progress_bar_.getProgress();
                    progress_bar_.incrementProgressBy(increment);
                }
            };


/**
     * Listener for Arduino.
     */

    private ArduinoConnector.Listener arduino_listener_ = new ArduinoConnector.Listener() {
        @Override
        public void onConnect(BluetoothDevice device) {
            app_status_manager_.updateConnectionStatus(true);
            updateStatusUIText(app_status_manager_.getStatus());
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();

            // Starting recognition right after connection made.
            speech_recognizer_.start();
        }

        @Override
        public void onReaction(PacketParser.Type type, String data) {
            if (type == PacketParser.Type.ActivityDetected) {
                app_status_manager_.updateConnectionStatus(true);
                updateStatusUIText(app_status_manager_.getStatus());
                Toast.makeText(getApplicationContext(), "Reaction", Toast.LENGTH_SHORT).show();
                // There is a limitation that Android doesn't offer continuous speech recognition.
                // So only when is activity detected, speech recognition starts.
                speech_recognizer_.start();

            }
        }

        @Override
        public void onDisconnect(BluetoothDevice device) {
            app_status_manager_.updateConnectionStatus(false);
            updateStatusUIText(app_status_manager_.getStatus());
            Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    // Update Status Text on UI.
    public void updateStatusUIText(AppState state) {
        switch (state) {
            case Disconnected:
                txt_app_status_.setText(
                        getResources().getString(R.string.txtview_disconnected));

                break;
            case Standby:
                txt_app_status_.setText(getResources().getString(R.string.txtview_standby));

                //다이얼로그 띄우고 계속 음성인식을 하시겠습니까? yes면 stats = Listening 구현
                ad.show();

                break;
            case Listening:
                txt_app_status_.setText(
                        getResources().getString(R.string.txtview_listening));

                break;
        }
    }


    // Represent current application status.
    private enum AppState {
        Disconnected, // connected to Arduino.
        Standby,       // waiting until activity detected
        Listening    // listening speech recognition.
    }


/**
     * Manage current app status.
     * Evaluate application status using input status.
     */

    static class AppStateManager {
        private boolean connected_ = false;
        private boolean is_listening_ = false;


/**
         * Update Arduino connection status.
         * @param connected true if connected to Arduino.
         * @return Current app status.
         */

        public AppState updateConnectionStatus(boolean connected) {
            connected_ = connected;
            return getStatus();
        }


         /* Update speech recognition status.
         * @param is_listening true if speech recognition is working.
         * @return Current app status.
         */

        public AppState updateSpeechRecognitionStatus(boolean is_listening) {
            is_listening_ = is_listening;
            return getStatus();
        }


/**
         * Evaluate the current AppStatus.
         * @return Current AppStatus.
         */

        AppState getStatus() {
            if (connected_) {

                return is_listening_ ? AppState.Listening : AppState.Standby;

               // return AppState.Listening;

            } else {
                return  AppState.Disconnected;
            }
        }
    }
    /*//블루투스 상태변화 BroadcastReceiver
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
            int ble_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            //블루투스 활성화
            if(ble_state == BluetoothAdapter.STATE_ON){
                txtState.setText("블루투스 활성화");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
            }
            //블루투스 활성화 중
            else if(ble_state == BluetoothAdapter.STATE_TURNING_ON){
                txtState.setText("블루투스 활성화 중...");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
            }
            //블루투스 비활성화
            else if(ble_state == BluetoothAdapter.STATE_OFF){
                txtState.setText("블루투스 비활성화");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
            }
            //블루투스 비활성화 중
            else if(ble_state == BluetoothAdapter.STATE_TURNING_OFF){
                txtState.setText("블루투스 비활성화 중...");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
            }
            else
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));

        }
    };*/
    private void doStartService() {
        Logs.d(TAG, "# Activity - doStartService()");
        startService(new Intent(this, BTCTemplateService.class));
        //bindService(new Intent(this, BTCTemplateService.class), mServiceConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * Service connection
     */
/*    private ServiceConnection mServiceConn = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Logs.d(TAG, "Activity - Service connected");

            mService = ((BTCTemplateService.ServiceBinder) binder).getService();

            // Activity couldn't work with mService until connections are made
            // So initialize parameters and settings here. Do not initialize while running onCreate()
            initialize();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };*/

    /**
     * Stop the service
     */
    private void doStopService() {
        Logs.d(TAG, "# Activity - doStopService()");
        mService.finalizeService();
        stopService(new Intent(this, BTCTemplateService.class));
    }

    private void initialize() {
        Logs.d(TAG, "# Activity - initialize()");
        mService.setupService(mActivityHandler);

        // If BT is not on, request that it be enabled.
        // RetroWatchService.setupBT() will then be called during onActivityResult
        if(!mService.isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }

        // Load activity reports and display
        if(mRefreshTimer != null) {
            mRefreshTimer.cancel();
        }

        // Use below timer if you want scheduled job
        //mRefreshTimer = new Timer();
        //mRefreshTimer.schedule(new RefreshTimerTask(), 5*1000);
    }

    private void finalizeActivity() {
        Logs.d(TAG, "# Activity - finalizeActivity()");

        if(!AppSettings.getBgService()) {
            doStopService();
        } else {
        }

        // Clean used resources
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
                // Receives BT state messages from service
                // and updates BT state UI
                case Constants.MESSAGE_BT_STATE_INITIALIZED:
                    txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_init));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_LISTENING:
                    txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_wait));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTING:
                    txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_connect));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTED:
                    if(mService != null) {
                        String deviceName = mService.getDeviceName();
                        if(deviceName != null) {
                            txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                                    getResources().getString(R.string.bt_state_connected) + " " + deviceName);
                            mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
                        }
                    }
                    break;
                case Constants.MESSAGE_BT_STATE_ERROR:
                    txtState.setText(getResources().getString(R.string.bt_state_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                // BT Command status
                case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
                    txtState.setText(getResources().getString(R.string.bt_cmd_sending_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                ///////////////////////////////////////////////
                // When there's incoming packets on bluetooth
                // do the UI works like below
                ///////////////////////////////////////////////
//			case Constants.MESSAGE_READ_ACCEL_REPORT:
//				ActivityReport ar = (ActivityReport)msg.obj;
//				if(ar != null) {
//					TimelineFragment frg = (TimelineFragment) mSectionsPagerAdapter.getItem(FragmentAdapter.FRAGMENT_POS_TIMELINE);
//					frg.showActivityReport(ar);
//				}
//				break;

                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }	// End of class ActivityHandler


}

