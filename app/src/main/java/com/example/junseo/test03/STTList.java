package com.example.junseo.test03;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.junseo.test03.speech.CommandSpeechFilter;
import com.example.junseo.test03.speech.EnhancedSpeechRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rlagk on 2017-10-07.
 */

public class STTList extends Activity {

    private static final String TAG = STTList.class.getSimpleName();

    //firebase
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_sttlist) ;
        Log.d(TAG,"진입");
        final ArrayList<STTList> speech_list = (ArrayList<STTList>) getIntent().getSerializableExtra("arSpeech");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, speech_list) ;

        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long ID) {

                final DatabaseReference pushedPostRefkey = databaseReference.push();
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


                databaseReference.child("huser").child(id).child("sensor").child("voice").child(speechtime).setValue(speech_list.get(position));
                databaseReference.child("hdashboard").child(userid).child("sensor").child("voice").child(speechtime).setValue(speech_list.get(position));

                finish();
            }
        }) ;

    }
}
