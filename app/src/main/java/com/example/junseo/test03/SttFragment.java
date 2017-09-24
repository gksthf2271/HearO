package com.example.junseo.test03;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Created by Junseo on 2017-09-21.
 */

public class SttFragment extends Fragment {
    public SttFragment() {
        // Required empty public constructor
    }
    private Button cancel3;

    private ImageView mImageBT;
    private BluetoothAdapter bluetooth_;
    private TextView txt_app_status_;
    private ProgressBar progress_bar_;
    private EnhancedSpeechRecognizer speech_recognizer_;
    private ArduinoConnector arduinoConnector_;
    private final AppStateManager app_status_manager_ = new AppStateManager();
    // The value for magnifying to display on progress bar.
    private final int kSpeechMagnifyingValue = 100;

    // Context, System
    public Context mContext;
    private BTCTemplateService mService;
    private SttFragment.ActivityHandler mActivityHandler;

    //블루투스 상태
    private TextView txtState;
    private RelativeLayout BlMonitoring;

    //다이얼
    AlertDialog.Builder ad;

    DialogInterface mPopupDlg = null;

    // Refresh timer
    private Timer mRefreshTimer = null;

    private static final String TAG = SttFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stt, container, false);
        cancel3 = (Button) view.findViewById(R.id.Cancel3);
        Button speakbtn = (Button) view.findViewById(R.id.speakbtn);
        speakbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // Inflate the layout for this fragment
        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


        //블루투스 setup view
        txtState = (TextView) view.findViewById(R.id.status_text);
        mImageBT = (ImageView) view.findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        BlMonitoring = (RelativeLayout) view.findViewById(R.id.bl_monitoring);

        progress_bar_ = (ProgressBar) view.findViewById(R.id.progressBarSpeech);
        progress_bar_.setMax(normalizeSpeechValue(EnhancedSpeechRecognizer.kSpeechMaxValue));   //
        txt_app_status_ = (TextView) view.findViewById(R.id.textViewSpeachResult);
        updateStatusUIText(app_status_manager_.getStatus());

        //블루투스 브로드캐스트 리시버
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        //registerReceiver(mBluetoothStateReceiver, stateFilter);

        speech_recognizer_ = buildSpeechRecognizer();       // 여기까지 화면구성
        arduinoConnector_ = new ArduinoConnector(arduino_listener_);    //아두이노 리스너 객체 생성
        cancel3.setOnClickListener(new View.OnClickListener() { //뒤로가기버튼
            @Override
            public void onClick(View v) {
            //    getActivity().unbindService(mServiceConn);
                getView().setVisibility(View.GONE); // 프래그먼트 없앰
            }


        });
        //standby 다이얼로그
        ad = new AlertDialog.Builder(getActivity());

        ad.setTitle("알림");       // 제목 설정
        ad.setMessage("음성인식이 중단되었습니다 다시 시작하시겠습니까?");   // 내용 설정

        ad.setPositiveButton("다시시작", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "다시시작 클릭");
                dialog.dismiss();     //닫기
                speech_recognizer_.start();

            }
        });
        ad.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "닫기 클릭");
                dialog.dismiss();     //닫기
                //09.08
                speech_recognizer_.stop();
                onDestroy();

            }
        });
        doStartService();

        mContext = getActivity();
    }
/*
    public void onClick(View view) {

        switch (view.getId()) {


            case R.id.speakbtn:

                break;
        }
    }*/

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

        return new EnhancedSpeechRecognizer(getActivity(), speech_recognizer_listener_,
                signal_filter); // connect signal_filter.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(mBluetoothStateReceiver);
        //arduinoConnector_.destroy();
        speech_recognizer_.destroy();
    }
    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        //09.08
        //speech_recognizer_.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            BluetoothDevice device = intent.getParcelableExtra("device");
            arduinoConnector_.connect(device);
            Log.d(TAG, "BLUETOOTH CONNECT");

            Intent result_intent = new Intent(getActivity().getApplicationContext(), MenuActivity.class);
            result_intent.putExtra("device", device);
            getActivity().setResult(Activity.RESULT_OK, result_intent);

        }
    }

        // connection button listener.
    public void onPair1(View v){

        Intent intent = new Intent(getActivity().getApplicationContext(), BluetoothPairActivity.class);
        startActivityForResult(intent, 0);
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
            Toast.makeText(getActivity().getApplicationContext(), cmd, Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getActivity().getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();

            // Starting recognition right after connection made.
            speech_recognizer_.start();
        }

        @Override
        public void onReaction(PacketParser.Type type, String data) {
            if (type == PacketParser.Type.ActivityDetected) {
                app_status_manager_.updateConnectionStatus(true);
                updateStatusUIText(app_status_manager_.getStatus());
                Toast.makeText(getActivity().getApplicationContext(), "Reaction", Toast.LENGTH_SHORT).show();
                // There is a limitation that Android doesn't offer continuous speech recognition.
                // So only when is activity detected, speech recognition starts.
                speech_recognizer_.start();

            }
        }
        @Override
        public void onDisconnect(BluetoothDevice device) {
            app_status_manager_.updateConnectionStatus(false);
            updateStatusUIText(app_status_manager_.getStatus());
            Toast.makeText(getActivity().getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    // Update Status Text on UI.
    public void updateStatusUIText(SttFragment.AppState state) {
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

    private static class AppStateManager {
        private boolean connected_ = false;
        private boolean is_listening_ = false;

        /**
         * Update Arduino connection status.
         *
         * @param connected true if connected to Arduino.
         * @return Current app status.
         */

        public SttFragment.AppState updateConnectionStatus(boolean connected) {
            connected_ = connected;
            return getStatus();
        }

         /* Update speech recognition status.
         * @param is_listening true if speech recognition is working.
         * @return Current app status.
         */

        public SttFragment.AppState updateSpeechRecognitionStatus(boolean is_listening) {
            is_listening_ = is_listening;
            return getStatus();
        }

        /**
         * Evaluate the current AppStatus.
         *
         * @return Current AppStatus.
         */

        SttFragment.AppState getStatus() {
            if (connected_) {

                return is_listening_ ? SttFragment.AppState.Listening : SttFragment.AppState.Standby;

                // return AppState.Listening;

            } else {
                return SttFragment.AppState.Disconnected;
            }
        }
   }

  private void doStartService() {
      Logs.d(TAG, "# Activity - doStartService()");
      getActivity().startService(new Intent(getActivity(), BTCTemplateService.class));
      getActivity().bindService(new Intent(getActivity(), BTCTemplateService.class), mServiceConn, Context.BIND_AUTO_CREATE);
  }

    /**
     * Service connection
     */
    private ServiceConnection mServiceConn = new ServiceConnection() {

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
    };
    /**
     * Stop the service
     */
    private void doStopService() {
        Logs.d(TAG, "# Activity - doStopService()");
        mService.finalizeService();
        getActivity().stopService(new Intent(getActivity(), BTCTemplateService.class));
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
        RecycleUtils.recursiveRecycle(getActivity().getWindow().getDecorView());
        System.gc();
    }

    public abstract class ActivityHandler extends Handler {
        private WeakReference<SttFragment> mFrag;

        private ActivityHandler(SttFragment aFragment){

            mFrag = new WeakReference<SttFragment>(aFragment);
        }

        @Override
        public void handleMessage(Message msg)
        {
            SttFragment theFrag = mFrag.get();
            switch(msg.what) {
                // Receives BT state messages from service
                // and updates BT state UI
                case Constants.MESSAGE_BT_STATE_INITIALIZED:
                    theFrag.txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_init));
                    theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_LISTENING:
                    theFrag.txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_wait));
                    theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTING:
                    theFrag.txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_connect));
                    theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTED:
                    if(mService != null) {
                        String deviceName = mService.getDeviceName();
                        if(deviceName != null) {
                            theFrag.txtState.setText(getResources().getString(R.string.bt_title) + ": " +
                                    getResources().getString(R.string.bt_state_connected) + " " + deviceName);
                            theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
                        }
                    }
                    break;
                case Constants.MESSAGE_BT_STATE_ERROR:
                    theFrag.txtState.setText(getResources().getString(R.string.bt_state_error));
                    theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                // BT Command status
                case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
                    theFrag.txtState.setText(getResources().getString(R.string.bt_cmd_sending_error));
                    theFrag.mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
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
