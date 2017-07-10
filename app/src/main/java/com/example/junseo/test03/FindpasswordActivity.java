package com.example.junseo.test03;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Junseo on 2017-06-07.
 */

public class FindpasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FindpasswordActivity";

    private EditText editTextUserEmail;
    private Button buttonFind;
    private TextView textviewMessage;
    private ProgressDialog progressDialog;
    //define firebase object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        editTextUserEmail = (EditText) findViewById(R.id.editTextUserEmail);
        buttonFind = (Button) findViewById(R.id.buttonFind);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        buttonFind.setOnClickListener(this);
    }

    @Override public void onClick(View view) {
        if(view == buttonFind){ progressDialog.setMessage("처리중입니다. 잠시 기다려 주세요...");
            progressDialog.show();
            //비밀번호 재설정 이메일 보내기
            String emailAddress = editTextUserEmail.getText().toString().trim();
            firebaseAuth.sendPasswordResetEmail(emailAddress) .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(
                        @NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(FindpasswordActivity.this, "이메일을 보냈습니다.", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        Toast.makeText(FindpasswordActivity.this, "메일 보내기 실패!", Toast.LENGTH_LONG).show(); }
                    progressDialog.dismiss();
                }
            });
        }
    }
}
