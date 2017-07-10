package com.example.junseo.test03;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private Button btnDone;
    private Button btnCancel;
    private FirebaseAuth firebaseAuth;
    private TextView textviewMessage;
    DatabaseReference myId;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myId = database.getReference("ID");

        //만일 로그인 중이라면 회원가입 창을 종료하고 메뉴 액티비티로 이동한다.
        if(firebaseAuth.getCurrentUser() != null){

            finish();
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
        }

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordConfirm = (EditText) findViewById(R.id.etPasswordConfirm);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        textviewMessage = (TextView) findViewById(R.id.textviewMessage);


        // 비밀번호 일치 검사
        etPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = etPassword.getText().toString();
                String confirm = etPasswordConfirm.getText().toString();

                if( password.equals(confirm) ) {
                    etPassword.setBackgroundColor(Color.GREEN);
                    etPasswordConfirm.setBackgroundColor(Color.GREEN);
                } else {
                    etPassword.setBackgroundColor(Color.RED);
                    etPasswordConfirm.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                myId.setValue(email);

                // 이메일 입력 확인
                if (email.length() == 0) {
                    Toast.makeText(RegistActivity.this, "Email을 입력하세요!", Toast.LENGTH_SHORT).show();
                    etEmail.requestFocus();
                    return;
                }

                // 비밀번호 입력 확인
                if (password.length() == 0) {
                    Toast.makeText(RegistActivity.this, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    etPassword.requestFocus();
                    return;
                }

                // 비밀번호 확인 입력 확인
                if (password.length() == 0) {
                    Toast.makeText(RegistActivity.this, "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show();
                    etPasswordConfirm.requestFocus();
                    return;
                }

                // 비밀번호 일치 확인
                if (!etPassword.getText().toString().equals(etPasswordConfirm.getText().toString())) {
                    Toast.makeText(RegistActivity.this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                    etPassword.setText("");
                    etPasswordConfirm.setText("");
                    etPassword.requestFocus();
                    return;
                }
                // 파이어베이스 이메일, 패스워드 저장
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegistActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // 업무가 완료 되었을 경우, 메인 메뉴로 이동한다.
                                if (task.isSuccessful()) {
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), MenuActivity.class));

                                } else {
                                    textviewMessage.setText("에러유형\n - 이미 등록된 이메일  \n -암호 최소 6자리 이상 \n - 서버에러");
                                    Toast.makeText(RegistActivity.this, "등록 에러!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                Intent result = new Intent();
                result.putExtra("email", etEmail.getText().toString());

                // 자신을 호출한 Activity로 데이터를 보낸다.
                setResult(RESULT_OK, result);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

