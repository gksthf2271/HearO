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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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

    private static TextView txt_app_status_;
    private ProgressBar progress_bar_;
    private EnhancedSpeechRecognizer speech_recognizer_;
    //private final AppStateManager app_status_manager_ = new AppStateManager();
    // The value for magnifying to display on progress bar.
    private final int kSpeechMagnifyingValue = 100;
    private Button Cancel3;

    // Context, System
    public static Context mContext;

    //다이얼
    AlertDialog.Builder ad;

    Switch sw;
    public ArrayList<String> arSpeech;



    private static final String TAG = STTActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);
        Cancel3 = (Button) findViewById(R.id.Cancel3);

        speakbtn = (Button) findViewById(R.id.speakbtn);


        progress_bar_ = (ProgressBar)findViewById(R.id.progressBarSpeech);
        progress_bar_.setMax(normalizeSpeechValue(EnhancedSpeechRecognizer.kSpeechMaxValue));   //
        txt_app_status_ = (TextView) findViewById(R.id.textViewSpeachResult);

        sw = (Switch)findViewById(R.id.sw_speech);           //음성인식 스위치

        final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.mic_animation);

        arSpeech = new ArrayList<>();

        speech_recognizer_ = buildSpeechRecognizer();       // 여기까지 화면구성
        Cancel3.setOnClickListener(new View.OnClickListener() { //뒤로가기버튼
            @Override
            public void onClick(View v) {
                //unbindService(mServiceConn);
                finish();
            }
        });


        speakbtn.setOnClickListener(new View.OnClickListener() { //음성인식 버튼
            @Override
            public void onClick(View v) {
                Log.d(TAG,"클릭 테스트");
                speech_recognizer_.start();
            }
        });


        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Toast.makeText(STTActivity.this, "체크상태 = " + isChecked, Toast.LENGTH_SHORT).show();

                if(isChecked == true){
                    speech_recognizer_.start();
                    speakbtn.startAnimation(animation);
                   // Log.d(TAG, s_arSpeech.get(s_arSpeech.size()-1));
                }else {
                    speech_recognizer_.destroy();
                    speech_recognizer_.stop();
                }
            }
        });
        mContext=this;
    }

    @Override
    public void onClick(View view){


    }

    EnhancedSpeechRecognizer buildSpeechRecognizer() {


        //Build listener chain in reverse order of event deliver order.


        CommandSpeechFilter cmd_filter = new CommandSpeechFilter(speech_listener_);
        // Add commands that it will listen for.
        final Resources rs = getResources();
/*        cmd_filter.addPattern(rs.getString(R.string.command_lighton),
                rs.getString(R.string.command_lighton_variant));
        cmd_filter.addPattern(rs.getString(R.string.command_lightoff),
                rs.getString(R.string.command_lightoff_variant));*/

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

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
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


    // Handles the speeches delivered by EnhancedSpeechRecognizer.
    private SpeechListener speech_listener_ = new SpeechListener() {
        @Override
        public void onSpeechRecognized(ArrayList<String> recognitions) {
            if (recognitions.isEmpty()) {
                Toast.makeText(getApplicationContext(), "다시 말해주세요.", Toast.LENGTH_LONG).show();
                return;
            }
            // Use only the first command.
            for (int i = 0; i <= recognitions.size()-1; i++){
                arSpeech.add(recognitions.get(i));
                Log.d(TAG+"kimhansol",arSpeech.get(i));
            }

            if(arSpeech.size() > 0) {
                Log.d(TAG, "체크");
                Intent SpeechIntent = new Intent(STTActivity.this, STTList.class);
                SpeechIntent.putExtra("arSpeech", arSpeech);
                startActivity(SpeechIntent);
                arSpeech.clear();
                speech_recognizer_.stop();
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
                    //app_status_manager_.updateSpeechRecognitionStatus(true);
                    //updateStatusUIText(app_status_manager_.getStatus());
                    sw.setChecked(true);
                }

                @Override
                public void onStop() {
                    //app_status_manager_.updateSpeechRecognitionStatus(false);
                    //updateStatusUIText(app_status_manager_.getStatus());
                    sw.setChecked(false);
                }

                @Override
                public void onSoundChanged(float rmsdB) {
                    final int increment = normalizeSpeechValue(rmsdB) - progress_bar_.getProgress();
                    progress_bar_.incrementProgressBy(increment);
                }
            };




    private void finalizeActivity() {
        Logs.d(TAG, "# Activity - finalizeActivity()");

        // Clean used resources
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
        System.gc();
    }

}

