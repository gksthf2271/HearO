package com.example.junseo.test03;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
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
import com.example.junseo.test03.arduino.PacketParser;
import com.example.junseo.test03.speech.CommandSpeechFilter;
import com.example.junseo.test03.speech.EnhancedSpeechRecognizer;
import com.example.junseo.test03.speech.SignalSpeechFilter;
import com.example.junseo.test03.speech.SpeechListener;

import java.util.ArrayList;

/**
 * Created by Junseo on 2017-07-04.
 */

public class STTActivity extends AppCompatActivity implements View.OnClickListener  {
    Button checkbtn = null;
    EditText editstt = null;
    Button speakbtn = null;

    private boolean flag = false;

    private BluetoothAdapter bluetooth_;
    private TextView txt_app_status_;
    private ProgressBar progress_bar_;
    private EnhancedSpeechRecognizer speech_recognizer_;
    private ArduinoConnector arduinoConnector_;
    private final AppStateManager app_status_manager_ = new AppStateManager();
    // The value for magnifying to display on progress bar.
    private final int kSpeechMagnifyingValue = 100;
    private Button Cancel3;

    //블루투스 상태
    private TextView txtState;
    private ImageView mImageBT;
    private RelativeLayout BlMonitoring;


    private BluetoothSocket socket_;

    private static final String TAG = STTActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);
        Cancel3 = (Button) findViewById(R.id.Cancel3);
        checkbtn = (Button) findViewById(R.id.checkbtn);
        editstt = (EditText) findViewById(R.id.editstt);
        speakbtn = (Button) findViewById(R.id.speakbtn);

        //블루투스 setup view
        txtState = (TextView)findViewById(R.id.status_text);
        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        BlMonitoring = (RelativeLayout)findViewById(R.id.bl_monitoring);



        progress_bar_ = (ProgressBar)findViewById(R.id.progressBarSpeech);
        progress_bar_.setMax(normalizeSpeechValue(EnhancedSpeechRecognizer.kSpeechMaxValue));   //
        txt_app_status_ = (TextView) findViewById(R.id.textViewSpeachResult);
        updateStatusUIText(app_status_manager_.getStatus());




        //블루투스 브로드캐스트 리시버
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        registerReceiver(mBluetoothStateReceiver, stateFilter);


        speech_recognizer_ = buildSpeechRecognizer();       // 여기까지 화면구성
        arduinoConnector_ = new ArduinoConnector(arduino_listener_);    //아두이노 리스너 객체 생성
        Cancel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view){

        switch (view.getId()){

            case R.id.checkbtn:

                break;

            case R.id.speakbtn:
                if(flag == true)
                {
                    speech_recognizer_.start();
                }
                break;
        }

    }
    private EnhancedSpeechRecognizer buildSpeechRecognizer() {
        /*
        Build listener chain in reverse order of event deliver order.
         */
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
        unregisterReceiver(mBluetoothStateReceiver);
        arduinoConnector_.destroy();
        speech_recognizer_.destroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //speech_recognizer_.destroy();
        //speech_recognizer_.start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        speech_recognizer_.stop();
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
            Log.d(TAG,"블루투스 연결 성공");
/*            intent = new Intent(getApplicationContext(), MenuActivity.class);
            intent.putExtra("resultCode",resultCode);
            intent.putExtra("requestCode",requestCode);
            startActivity(intent);*/
        }
    }

    // connection button listener.
    public void onPair(View v){
        Intent intent = new Intent(getApplicationContext(), BluetoothPairActivity.class);
        startActivityForResult(intent, 0);
        flag =true;
    }

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

            try {
                arduinoConnector_.send(cmd);
                Log.d(TAG,"안드로이드 -> 아두이노 데이터전달");
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
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
        Listening,    // listening speech recognition.
    }

    /**
     * Manage current app status.
     * Evaluate application status using input status.
     */
    private class AppStateManager {
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

        /**
         * Update speech recognition status.
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
        private AppState getStatus() {
            if (connected_) {

                return is_listening_ ? AppState.Listening : AppState.Standby;
                /*return AppState.Listening;*/
            } else {
                return  AppState.Disconnected;
            }
        }
    }
    //블루투스 상태변화 BroadcastReceiver
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            //블루투스 활성화
            if(state == BluetoothAdapter.STATE_ON){
                txtState.setText("블루투스 활성화");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
            }
            //블루투스 활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_ON){
                txtState.setText("블루투스 활성화 중...");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
            }
            //블루투스 비활성화
            else if(state == BluetoothAdapter.STATE_OFF){
                txtState.setText("블루투스 비활성화");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
            }
            //블루투스 비활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_OFF){
                txtState.setText("블루투스 비활성화 중...");
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
            }
            else
                mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));

        }
    };





}
