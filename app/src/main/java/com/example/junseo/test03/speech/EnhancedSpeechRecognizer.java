package com.example.junseo.test03.speech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;


import com.example.junseo.test03.STTActivity;
import com.example.junseo.test03.STTList;
import com.example.junseo.test03.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Responsible for improving recognition service and hiding complex logic.
 *
 * By default, SpeechRecognizer ends in 5 seconds after its starting.
 * So this class runs the recognizer over and over when it meets the conditions below.
 * - When speech recognized,
 * - When duplicated start() had called while it was listening to speech.
 *
 * It mutes the sounds at the begin and end of speech recognition, which is automatically generated
 * by the library and is unable to mute.
 */
@SuppressWarnings("serial")
public class EnhancedSpeechRecognizer implements RecognitionListener, Serializable {
    private static final int kMsgRecognizerStart = 1;
    private static final int kMsgRecognizerStop = 2;
    private static final String TAG = EnhancedSpeechRecognizer.class.getSimpleName();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth;
    // Max speech value from SpeechRecognizer
    public static final float kSpeechMinValue = -2.12f;
    // Min speech value from SpeechRecognizer
    public static final int kSpeechMaxValue = 10;

    private SpeechRecognizer speech_recog_;
    // Intent fo speech recognizer
    private Intent intent_;
    private Activity parent_activity_;
    private SpeechListener speech_listener_;
    private Listener listener_;
    // Whether start() is called during the speech listening..
    private boolean duplicated_listening_ = false;
    private boolean speech_recognized_ = false;
    private final SoundController sound_controllor_;
    private final Messenger server_messenger_ = new Messenger(new IncomingHandler(this));
    public static Context mContext;

    public interface Listener {
        // Start of speech recognition.
        void onStart();
        // End of speech recognition.
        void onStop();
        // Notifying sound level.
        void onSoundChanged(float rmsdB);
    }

    public EnhancedSpeechRecognizer(Context context, Listener listener,
                                    SpeechListener speech_listener) {
        mContext = context;
        listener_ = listener;
        speech_listener_ = speech_listener;

        sound_controllor_ = new SoundController(mContext);

        // set intent for speech recognizer
        intent_ = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent_.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getPackageName());
        intent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent_.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        sound_controllor_.soundOff();
    }

    // Start speech recognizing
    public void start() {
        Log.i(TAG, "start listening");
        if (speech_recog_ != null) {
            Log.i(TAG, "duplicated listening");
            duplicated_listening_ = true;
            return;
        }

        speech_recog_ = SpeechRecognizer.createSpeechRecognizer(mContext);
        speech_recog_.setRecognitionListener(this);
        speech_recog_.startListening(intent_);

        listener_.onStart();
        listener_.onSoundChanged(kSpeechMinValue);

    }

    // Stop speech recognizing
    public void stop() {
        Log.i(TAG, "stop listening");
        if (speech_recog_ != null) {
            speech_recog_.cancel();
            speech_recog_.destroy();
            speech_recog_ = null;
        }

        listener_.onStop();
        // reset sound level.
        listener_.onSoundChanged(kSpeechMinValue);
    }

    // Start speech recognizing asynchronously.
    public void asyncStart() {
        if (speech_recog_ != null) {
            speech_recog_.cancel();
            speech_recog_.destroy();
            speech_recog_ = null;
        }

        Message message = Message.obtain(null, kMsgRecognizerStart);
        try {
            server_messenger_.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Should be called before app terminates
    public void destroy() {
        if (speech_recog_ != null) {
            speech_recog_.cancel();
            speech_recog_.destroy();
            speech_recog_ = null;
        }
        sound_controllor_.soundOff();
    }

    /*
        Implements RecognitionListener interface below
    */

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech");
        speech_recognized_ = true;
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // it's too noisy
        //Log.d(TAG, "onRmsChanged: " + rmsdB);
        listener_.onSoundChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        //Log.d(TAG, String.valueOf(CommandSpeechFilter.s_arSpeech.get(0)));
    }

    @Override
    public void onError(int error) {
        Log.d(TAG, "onError for speech: " + error);

        // If there were duplicated start requests or speech recognized,
        // start listening again.
        if (duplicated_listening_ || speech_recognized_) {
            duplicated_listening_ = false;
            speech_recognized_ = false;
            asyncStart();
        } else {
            stop();
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "onResult");

        ArrayList<String> results_in_arraylist =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        // notifies the speeches each by each.
        for (String speech : results_in_arraylist) {

            Log.d(TAG, "match: " + speech);         // 여기서 이값을 리스트형식으로 출력해야하고 클릭시 파이어베이스에 다른 형태로 저장해야된다
        }



        speech_listener_.onSpeechRecognized(results_in_arraylist);

        // When speech detected, start the recognition again since in this case
        // there will be a high chance that people try speech recognition more times.

        asyncStart();

        duplicated_listening_ = false;
        speech_recognized_ = false;



    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.d(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "onEvent");
    }

    private static class IncomingHandler extends Handler {
        private WeakReference<EnhancedSpeechRecognizer> target_;

        IncomingHandler(EnhancedSpeechRecognizer target) {
            target_ = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            final EnhancedSpeechRecognizer target = target_.get();

            switch (msg.what) {
                case kMsgRecognizerStart:
                    Log.d(TAG, "message start listening");
                    // turn off beep sound
                    target.start();
                    break;

                case kMsgRecognizerStop:
                    Log.d(TAG, "message stop recognizer");
                    target.stop();
                    break;
            }
        }
    }

    // System sound on / off controller.
    private class SoundController {
        private boolean is_on = true;
        private AudioManager audio_manager_;

        public SoundController(Context app_context) {
            audio_manager_ = (AudioManager)app_context.getSystemService(Context.AUDIO_SERVICE);
        }

        public void soundOn() {
            if (!is_on) {
                audio_manager_.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                is_on = true;
            }
        }

        public void soundOff() {
            if (is_on) {
                audio_manager_.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                is_on = false;
            }
        }
    }
}