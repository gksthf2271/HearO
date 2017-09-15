package com.example.junseo.test03;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by Junseo on 2017-09-14.
 */

public class ResignFragment1 extends Fragment {
    public ResignFragment1() {
        // Required empty public constructor
    }
    private Button reDone;
    private Button reCancel;
    private EditText etName;
    private EditText reetPassword;
    private EditText reetPasswordConfirm;
    private TextView myEmail;
    // private String name;
    private String email;
    //    private String password;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = firebaseAuth.getCurrentUser();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resign, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        reDone = (Button) view.findViewById(R.id.reDone);
        reCancel = (Button) view.findViewById(R.id.reCancel);
        myEmail = (TextView) view.findViewById(R.id.myEmail);
        etName = (EditText) view.findViewById(R.id.etName);
        reetPassword = (EditText) view.findViewById(R.id.reetPassword);
        reetPasswordConfirm = (EditText) view.findViewById(R.id.reetPasswordConfirm);

        myEmail.setText(user.getEmail());
        reetPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = reetPassword.getText().toString();
                String confirm = reetPasswordConfirm.getText().toString();

                if (password.equals(confirm)) {
                    reetPassword.setBackgroundColor(Color.GREEN);
                    reetPasswordConfirm.setBackgroundColor(Color.GREEN);
                } else {
                    reetPassword.setBackgroundColor(Color.RED);
                    reetPasswordConfirm.setBackgroundColor(Color.RED);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });
        reDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = etName.getText().toString();
                email = myEmail.getText().toString();
                final String password = reetPassword.getText().toString();
                if (name.length() == 0) {
                    Toast.makeText(getActivity(), "이름을 입력하세요!", Toast.LENGTH_SHORT).show();
                    etName.requestFocus();
                    return;
                }
                // 비밀번호 입력 확인
                if (password.length() == 0) {
                    Toast.makeText(getActivity(), "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    reetPassword.requestFocus();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(getActivity(), "비밀번호는 6자 이상!", Toast.LENGTH_SHORT).show();
                    reetPassword.requestFocus();
                    return;
                }
                // 비밀번호 확인 입력 확인
                if (password.length() == 0) {
                    Toast.makeText(getActivity(), "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show();
                    reetPasswordConfirm.requestFocus();
                    return;
                }
                // 비밀번호 일치 확인
                if (!password.equals(reetPasswordConfirm.getText().toString())) {
                    Toast.makeText(getActivity(), "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                    reetPassword.setText("");
                    reetPasswordConfirm.setText("");
                    reetPassword.requestFocus();
                    return;
                }

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                user.updateProfile(profileUpdates).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "유저 이름이 정상적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            Log.e("유저 이름 성공","test:");
                        }
                    }
                });

                /*user.updatePassword(password).addOnCompleteListener(getActivity(),new OnCompleteListener<Void>(){
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "패스워드가 성공적으로 바뀌었습니다!", Toast.LENGTH_SHORT).show();
                            Log.e("패스워드 성공1","test:");
                        }
                        Log.e("패스워드 성공2","test:" + password );


                    }
                }
                );*/

                user.updatePassword(password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.e("패스워드 성공2","test:" + password );

                                }
                            }
                        });
            }
        });
        //reCancel.setOnClickListener();
    }

}