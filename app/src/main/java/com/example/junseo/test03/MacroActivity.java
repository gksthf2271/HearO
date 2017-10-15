package com.example.junseo.test03;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MacroActivity extends AppCompatActivity {



    Button button1;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(); // 기본 루트 레퍼런스
    private DatabaseReference huser = databaseReference.child("huser");
    private DatabaseReference dashboardReference = FirebaseDatabase.getInstance().getReference().child("hdashboard"); // 대쉬보드 레퍼런스
    private FirebaseAuth firebaseAuth;
    EditText editTextName;
    //  Spinner spinnerGenre;
    Button buttonAddmacro;
    private ListView listViewmacros;
    Button Cancel5;
    //a list to store all the macro from firebase database
    List<macro> macros;
    TextView textview;
    TextToSpeech tts;
    EditText macrotext;
    Button button_macro;

    //our database reference object
    DatabaseReference databasemacros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macro);
        Cancel5 = (Button) findViewById(R.id.Cancel5);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        //getting views



        editTextName = (EditText) findViewById(R.id.editTextName);
        textview= (TextView) findViewById(R.id.textView2);
        listViewmacros = (ListView) findViewById(R.id.listViewmacro);
        buttonAddmacro = (Button) findViewById(R.id.buttonAddmacro);

        //list to store macros
        macros = new ArrayList<>();

        //adding an onclicklistener to button
        buttonAddmacro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling the method addmacro()
                //the method is defined below
                //this method is actually performing the write operation
                addmacro();
            }
        });
        Cancel5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //리스트 뷰 아이템 접근 (여기서 모듈 전송)
        listViewmacros.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private void showList (final String macroName){
                textview.setText(macroName);
            }
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                macro macro = macros.get(i);

                showList(macro.getmacroName());
                String text = textview.getText().toString();
                Toast.makeText(getApplicationContext(), "전송되었습니다", Toast.LENGTH_SHORT).show();

                //http://stackoverflow.com/a/29777304
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(text);
                } else {
                    ttsUnder20(text);
                }

                firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                final String email = user.getEmail();
                final String userid = user.getUid();
                int j = email.indexOf("@");
                String id1 = email.substring(0,j);
                String id = databaseReference.push().getKey();

                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E요일");
                // nowDate 변수에 값을 저장한다.
                String macrotime = sdfNow.format(date);

                SimpleDateFormat yo = new SimpleDateFormat("E요일");
                String yoman = yo.format(date);
                    //Saving the macro
                dashboardReference.child(userid).child("macro").child(id).child(yoman).setValue(macrotime);
                huser.child(id1).child("macro_tts").child("text").setValue(text);
                huser.child(id1).child("macro_tts").child("time").setValue(macrotime);
                }
        });

        //길게 눌렀을 때 실행하기
        listViewmacros.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                macro macro = macros.get(i);
                showUpdateDeleteDialog(macro.getmacroId(), macro.getmacroName());
                return true;
            }//fff
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
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
    // 수정 혹은 삭제
    private void showUpdateDeleteDialog(final String macroId, String macroName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);

        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdatemacro);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeletemacro);

        dialogBuilder.setTitle(macroName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        //갱신 버튼
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                //  String genre = spinnerGenre.getSelectedItem().toString();
                if (!TextUtils.isEmpty(name)) {
                    updatemacro(macroId, name);
                    b.dismiss();
                }
            }
        });

        //삭제
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deletemacro(macroId);
                b.dismiss();
            }
        });
    }
    //매크로 수정
    private boolean updatemacro(String id, String name) {
        //getting the specified macro reference
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id1 = email.substring(0,i);
        DatabaseReference dR = databaseReference.child("huser").child(id1).child("macro").child(id);

        //updating macro
        macro macro = new macro(id, name);
        dR.setValue(macro);
        Toast.makeText(getApplicationContext(), "상용구가 수정되었습니다.", Toast.LENGTH_LONG).show();
        return true;
    }
    // 매크로 삭제
    private boolean deletemacro(String id) {
        //getting the specified macro reference
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id1 = email.substring(0,i);
        DatabaseReference dR = databaseReference.child("huser").child(id1).child("macro").child(id);

        //removing macro
        dR.removeValue();

        //getting the tracks reference for the specified macro

        //removing all tracks
        Toast.makeText(getApplicationContext(), "상용구가 삭제되었습니다.", Toast.LENGTH_LONG).show();

        return true;
    }
    //매크로 시작, 리스트 뷰를 가져옴.
    @Override
    protected void onStart() {
        super.onStart();

        //attaching value event listener
        //getting the specified macro reference
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id1 = email.substring(0,i);
        huser.child(id1).child("macro").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //clearing the previous macro list
                macros.clear();

                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting macro
                    macro macro = postSnapshot.getValue(macro.class);
                    //adding macro to the list
                    macros.add(macro);
                }

                //creating adapter
                MacroList macroAdapter = new MacroList(MacroActivity.this, macros);
                //attaching adapter to the listview
                listViewmacros.setAdapter(macroAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /*
    * This method is saving a new macro to the
    * Firebase Realtime Database
    * */
    //매크로 추가부분.
    private void addmacro() {
        //getting the values to save
        String name = editTextName.getText().toString().trim();

        //checking if the value is provided
        if (!TextUtils.isEmpty(name)) {
            firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            final String email = user.getEmail();
            int i = email.indexOf("@");
            String id1 = email.substring(0,i);
            //getting a unique id using push().getKey() method
            //it will create a unique id and we will use it as the Primary Key for our macro
            String id = databaseReference.push().getKey();

            //creating an macro Object
            macro macro = new macro(id, name);

            //Saving the macro
            databaseReference.child("huser").child(id1).child("macro").child(id).setValue(macro);

            //setting edittext to blank again
            editTextName.setText("");

            //displaying a success toast
            Toast.makeText(this, "상용구가 추가되었습니다.", Toast.LENGTH_LONG).show();
        } else {
            //if the value is not given displaying a toast
            Toast.makeText(this, "내용을 입력해 주세요.", Toast.LENGTH_LONG).show();
        }
    }
}
