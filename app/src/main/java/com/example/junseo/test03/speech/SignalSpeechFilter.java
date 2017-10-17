package com.example.junseo.test03.speech;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Recognize the signal speech, which is a speech that voice command starts with.
 *
 * It delivers only the speeches that start with signal speech, to the next SpeechListener.
 */
public class SignalSpeechFilter implements SpeechListener {
    private String TAG = SignalSpeechFilter.class.getSimpleName();
    private SpeechListener speech_listener_;
    private String signal_speech_;


    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    final DatabaseReference pushedPostRefkey = databaseReference.push();
    final String key = pushedPostRefkey.getKey();
    private FirebaseAuth firebaseAuth;
    /**
     * Create instance from setting file.
     * @param file_path setting file that contains signal speech.
     * @param speech_listener listener that will be notified after filtering.
     * @return new SignalSpeechFilter object.
     */
    public static SignalSpeechFilter createFromFile(String file_path,
                                                    SpeechListener speech_listener) {
        return null;
    }

    public SignalSpeechFilter(SpeechListener speech_listener, String signal_speech) {
        speech_listener_ = speech_listener;
        signal_speech_ = signal_speech;
        // add a space to make it easier to compare with speech string.
        signal_speech_ += " ";
    }

    /**
     * Set signal speech.
     * @param signal_speech new signal speech string.
     * @return old signal speech string.
     */
    public String setSignalSpeech(String signal_speech) {
        String old = signal_speech_;
        signal_speech_ = signal_speech;
        return old;
    }

    @Override
    public void onSpeechRecognized(ArrayList<String> recognitions) {
        ArrayList<String> result_without_signal = new ArrayList<>();
            for (String speech : recognitions) {
                if (speech.startsWith(signal_speech_)) {
                    String signal_removed = speech.substring(signal_speech_.length());
                    result_without_signal.add(signal_removed);
                } else {
                    Log.d(TAG, "filtered: " + speech);
                    final DatabaseReference pushedPostRefkey = databaseReference.push();
                    final String key = pushedPostRefkey.getKey();
                    firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    final String email = user.getEmail();
                    int i = email.indexOf("@");
                    String id = email.substring(0,i);
                    final String userid = user.getUid();
                    long now = System.currentTimeMillis();
                    // 현재시간을 date 변수에 저장한다.
                    Date date = new Date(now);
                    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E요일");
                    // nowDate 변수에 값을 저장한다.
                    String speechtime = sdfNow.format(date);

                    databaseReference.child("huser").child(id).child("machine").child(key).child("dafault_val").setValue(speech);


                }
        }

        // notify with signal string.
        speech_listener_.onSpeechRecognized(result_without_signal);
    }
}
