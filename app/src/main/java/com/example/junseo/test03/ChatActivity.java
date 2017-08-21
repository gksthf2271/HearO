package com.example.junseo.test03;

/**
 * Created by KHR on 2017-08-09.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import android.annotation.TargetApi;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {
    TextToSpeech tts;

    private String CHAT_NAME;
    private String USER_NAME;
    private String DDR;
    private ListView chat_view;
    private EditText chat_edit;
    private Button chat_send;
    Button Cancel10;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        // 위젯 ID 참조
        chat_view = (ListView) findViewById(R.id.chat_view);
        chat_edit = (EditText) findViewById(R.id.chat_edit);
        chat_send = (Button) findViewById(R.id.chat_sent);
        Cancel10 = (Button) findViewById(R.id.Cancel10);
        // 로그인 화면에서 받아온 채팅방 이름, 유저 이름 저장
        final Intent intent = getIntent();

        CHAT_NAME = intent.getStringExtra("chatName");
        USER_NAME = intent.getStringExtra("userName");

        // 채팅 방 입장
        openChat(CHAT_NAME);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0,i);


        final DatabaseReference chatdatabaseReference = FirebaseDatabase.getInstance().getReference().child("USER").child(id).child("chat"); // 채팅 레퍼런스

        // 메시지 전송 버튼에 대한 클릭 리스너 지정
        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = chat_view.getFirstVisiblePosition();
                if (chat_edit.getText().toString().equals(""))
                    return;
                String text = chat_edit.getText().toString();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                //http://stackoverflow.com/a/29777304
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(text);
                } else {
                    ttsUnder20(text);
                }

                final DatabaseReference chatpushedPostRefkey = chatdatabaseReference.push();
                final String chatkey = chatpushedPostRefkey.getKey();
                final ChatDTO chat = new ChatDTO(USER_NAME, chat_edit.getText().toString()); //ChatDTO를 이용하여 데이터를 묶는다.
/*                databaseReference.child("chat").child(CHAT_NAME).push().setValue(chat); // 데이터 푸쉬*/

                chatdatabaseReference.child(CHAT_NAME).child(chatkey).setValue(chat); // 데이터 푸쉬

                chat_edit.setText(""); //입력창 초기화
                chat_view.smoothScrollToPosition(position);

            }
        });
        Cancel10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatdatabaseReference.child(CHAT_NAME).removeValue();
                DatabaseReference chatpushedPostRefkey = chatdatabaseReference.push();
                String chatkey = chatpushedPostRefkey.getKey();
                ChatDTO chat = new ChatDTO("HearO","보낼 메시지를 입력해주세요.");
                chatdatabaseReference.child(CHAT_NAME).child(chatkey).setValue(chat);
                Log.e("LOG","removemessage");

                finish();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }
    private void addMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
        adapter.add(chatDTO.getUserName() + " : " + chatDTO.getMessage());
    }

    private void removeMessage(DataSnapshot dataSnapshot, ArrayAdapter<String> adapter) {
        ChatDTO chatDTO = dataSnapshot.getValue(ChatDTO.class);
        adapter.remove(chatDTO.getUserName() + " : " + chatDTO.getMessage());
    }

    private void openChat(String chatName) {
        // 리스트 어댑터 생성 및 세팅
        final ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        chat_view.setAdapter(adapter);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0,i);
        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("USER").child(id).child("chat").child(chatName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addMessage(dataSnapshot, adapter);
                Log.e("LOG", "s:"+s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {

                Log.e("LOG","removemessage");
                removeMessage(dataSnapshot, adapter);





            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}