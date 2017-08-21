package com.example.junseo.test03;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junseo.test03.arduino.ArduinoConnector;
import com.example.junseo.test03.arduino.BluetoothPairActivity;
import com.example.junseo.test03.arduino.PacketParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmail;
    private TextView btnRegist;
    private FirebaseAuth firebaseAuth;
    private EditText etPassword;
    private ProgressDialog progressDialog;
    private Button btnLogin;
    private TextView forgotpasswordtv;
    private BluetoothAdapter bluetooth_;
    private ArduinoConnector arduinoConnector_;
    private ArduinoConnector.Listener arduino_listener_;

    private static final String TAG = MainActivity.class.getSimpleName();
    TextView textviewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setTheme(android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        firebaseAuth = FirebaseAuth.getInstance();
        //만일 로그인 중이라면 회원가입 창을 종료하고 메뉴 액티비티로 이동한다.
        if (firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
        }
        //액션바 설정하기// 타이틀 변경하기
        //getSupportActionBar().setTitle("HearO");

        //블루투스
        bluetooth_ = BluetoothAdapter.getDefaultAdapter();
        if (!bluetooth_.isEnabled()) {
            Toast.makeText(getApplicationContext(), "bluetooth is not enabled",
                    Toast.LENGTH_LONG).show();
            Intent enableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableintent, 0);

        }

        arduinoConnector_ = new ArduinoConnector(arduino_listener_);    //아두이노 리스너 객체 생성

        progressDialog = new ProgressDialog(this);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);

        etEmail = (EditText) findViewById(R.id.etEmail);
        btnRegist = (TextView) findViewById(R.id.btnRegist);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        forgotpasswordtv = (TextView) findViewById(R.id.forgotpasswordtv);

        //비밀번호 찾기
        forgotpasswordtv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), FindpasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, 1001);

            }
        });
        //로그인 버튼
        btnLogin.setOnClickListener(this);

        //회원가입 버튼
        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegistActivity.class);

                // SINGLE_TOP : 이미 만들어진게 있으면 그걸 쓰고, 없으면 만들어서 사용한다.
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // 동시에 사용 가능
                // intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // intent를 보내면서 다음 액티비티로부터 데이터를 받기 위해 식별번호(1000)을 준다.
                startActivityForResult(intent, 1000);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


/*        if (resultCode == RESULT_OK) {
            BluetoothDevice device = data.getParcelableExtra("device");
            arduinoConnector_.connect(device);
            Log.d(TAG,"블루투스 연결");
        }*/

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Toast.makeText(MainActivity.this, "회원가입을 완료했습니다!", Toast.LENGTH_SHORT).show();
            etEmail.setText(data.getStringExtra("email"));
        }


    }
    //액션 바 숨기기
    // private void hideActionBar() {
    //   ActionBar actionBar = getSupportActionBar(); if(actionBar != null) actionBar.hide(); }

    private void userLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "email을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "password를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
//logging in the user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(
                            @NonNull Task<AuthResult> task) {
                        // progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "로그인 실패!", Toast.LENGTH_LONG).show();
                            // textviewMessage.setText("로그인 실패 유형\n - password가 맞지 않습니다.\n -서버에러");
                        }
                    }
                });
    }

    public void onClick(View view) {
        if (view == btnLogin) {
            userLogin();
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        //블루투스 MainActivity 초기에 실행하기
/*        Intent intent = getIntent();
        int resultCode = intent.getExtras().getInt("resultCode");
        int requestCode = intent.getExtras().getInt("requestCode");*/

        bluetooth_ = BluetoothAdapter.getDefaultAdapter();
        if (!bluetooth_.isEnabled()) {
            Toast.makeText(getApplicationContext(), "bluetooth is not enabled",
                    Toast.LENGTH_LONG).show();
            Intent enableintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableintent, 0);
/*
            Intent intent = new Intent(getApplicationContext(), BluetoothPairActivity.class);
            startActivityForResult(intent, 0);*/
        }
    }

}



