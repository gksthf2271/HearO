package com.example.junseo.test03.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.junseo.test03.STTActivity;
import com.example.junseo.test03.STTList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Filters the speeches delivered by SpeechListener, using the designated speeches in advance.
 */
public class CommandSpeechFilter implements SpeechListener {
    private String TAG = CommandSpeechFilter.class.getSimpleName();
    private SpeechListener speech_listener_;
    private HashMap<String, String> pattern_speeches_ = new HashMap<>();

    private final String kVariantsDelemiter = "|";

    //firebase
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth;

    public ArrayList<String> s_arSpeech;
    /**
     * Load patterns from setting file.
     * @param file_path file that contains speech patterns.
     * @param listener listener that will be notified after filtering.
     * @return new CommandSpeechFilter object
     */
    public static CommandSpeechFilter createFromFile(String file_path, SpeechListener listener) {
        // TODO: implement this.
        throw new UnsupportedOperationException();
    }

    public CommandSpeechFilter(SpeechListener listener) {
        speech_listener_ = listener;
    }

    /**
     * Add patterns that it is listening to.
     * @param speech the speech it is listening to.
     * @param variants similar speeches that are recognized as the speech, which is for increasing
     *                 recognition accuracy.
     */
    public void addPattern(String speech, ArrayList<String> variants) {
        // push variants
        for (String variant : variants) {
            String previous = pattern_speeches_.put(variant, speech);
            if (previous != null) {
                Log.w(TAG, "pattern duplicated: " + previous);
            }
        }
        // push itself.
        pattern_speeches_.put(speech, speech);
    }

    public void addPattern(String speech, String formatted_variants) {
        StringTokenizer tokenizer = new StringTokenizer(formatted_variants, kVariantsDelemiter);
        ArrayList<String> variants = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            variants.add(tokenizer.nextToken().trim());
        }
        addPattern(speech, variants);
    }

    @Override
    public void onSpeechRecognized(ArrayList<String> recognitions) {
        ArrayList<String> commands = new ArrayList<>();
        s_arSpeech = new ArrayList<>();
        for (String speech : recognitions) {
            String pattern = pattern_speeches_.get(speech);

            if (pattern != null) {
                commands.add(pattern);
            } else {
                Log.d(TAG, "filtered: " + speech);
                //필터값 배열형태 static변수에 저장 -> STTList.class
                s_arSpeech.add(speech);
                Log.d(TAG, "s_arSpeech: " +  s_arSpeech.get(s_arSpeech.size()-1));
    /*            final DatabaseReference pushedPostRefkey = databaseReference.push();
                final String speechkey = pushedPostRefkey.getKey();
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

                databaseReference.child("huser").child(id).child("sensor").child("voice").child(speechtime).setValue(speech);
                databaseReference.child("hdashboard").child(userid).child("sensor").child("voice").child(speechtime).setValue(speech);
            */}
        }
       /* speech_listener_.onSpeechRecognized(commands);*/
        speech_listener_.onSpeechRecognized(s_arSpeech);
    }
}
