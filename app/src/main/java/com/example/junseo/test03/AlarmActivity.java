package com.example.junseo.test03;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * Created by Junseo on 2017-06-27.
 */
// 추후 구글 스토어에서 파싱하여 버전 업데이트 하는 방식 구상이 필요


public class AlarmActivity extends AppCompatActivity {

    private static final String TAG = AlarmActivity.class.getSimpleName(); ;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("huser"); // 기본 루트 레퍼런스
   // private DatabaseReference alert = databaseReference.child("sensor");
   // private DatabaseReference Alarm = alert.child("Alarm");

    private Button Cancel2;
    private FirebaseAuth firebaseAuth;
    private String flag = "True";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();
        int i = email.indexOf("@");
        String id = email.substring(0, i);
        final DatabaseReference User_Alarm = databaseReference.child(id).child("sensor").child("Alarm");
        Cancel2 = (Button) findViewById(R.id.Cancel2);
        Cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent alramintent = new Intent(AlarmActivity.this, MenuActivity.class);
                AlarmActivity.this.startActivity(alramintent);
            }
        });

    final Switch sw = (Switch) findViewById(R.id.sw);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked){
                    User_Alarm.setValue("True");
                    Toast toast1 = Toast.makeText(AlarmActivity.this, "알람이 켜졌습니다.", Toast.LENGTH_SHORT );
                    toast1.show();
                }else {
                    User_Alarm.setValue("False");
                    Toast toast2 = Toast.makeText(AlarmActivity.this, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT);
                    toast2.show();
                }
            }
        });
        User_Alarm.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class); // val 값을 가져온다.
                Log.d(TAG, "Value is: " + value);

                if(Objects.equals(value, flag)){
                    sw.setChecked(true);  // val 값이 true일 경우, 스위치 on 상태
                }else{
                    sw.setChecked(false); // val 값이 false일 경우, 스위치 off 상태
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
