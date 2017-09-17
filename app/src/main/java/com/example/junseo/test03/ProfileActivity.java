package com.example.junseo.test03;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.FragmentTransaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Junseo on 2017-06-07.
 */

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {



    private static final String TAG = "ProfileActivity";
    //firebase auth object
    private FirebaseAuth firebaseAuth;
    private final int FRAGMENT1 = 1;
    private Button buttonResign;
    //view objects
    private TextView textViewUserEmail;
    private Button buttonLogout;
    private TextView textivewDelete;
    private Button Cancel1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initializing views
        textViewUserEmail = (TextView) findViewById(R.id.textviewUserEmail);
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        textivewDelete = (TextView) findViewById(R.id.textviewDelete);
        Cancel1 = (Button) findViewById(R.id.Cancel1);
        buttonResign = (Button) findViewById(R.id.buttonResign);
        //initializing firebase authentication object
        firebaseAuth = FirebaseAuth.getInstance();
        //유저가 로그인 하지 않은 상태라면 null 상태이고 이 액티비티를 종료하고 로그인 액티비티를 연다.
        if(firebaseAuth.getCurrentUser() == null) {
            finish(); startActivity(new Intent(this, MainActivity.class));
        }
        buttonResign.setOnClickListener(this);
        //유저가 있다면, null이 아니면 계속 진행
        FirebaseUser user = firebaseAuth.getCurrentUser();

        //textViewUserEmail의 내용을 변경해 준다.
        assert user != null;
        textViewUserEmail.setText("반갑습니다."+user.getDisplayName()+"님\n"+ user.getEmail()+"으로 로그인 하였습니다.");

        //logout button event
        buttonLogout.setOnClickListener(this);
        textivewDelete.setOnClickListener(this);

        Cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    private void callFragment(int fragment_no){

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch(fragment_no){
            case 1:
                ResignFragment1 fragment1 = new ResignFragment1();
                transaction.replace(R.id.fragment_container, fragment1); // 프래그먼트 컨테이너에 fragment1을 담는다.
                transaction.commit();
        }
    }
    //layout
    @Override
    public void onClick(View view) {

        if(view == buttonResign) {
            callFragment(FRAGMENT1);
        }
        if (view == buttonLogout) {
            firebaseAuth.signOut(); finish(); startActivity(new Intent(this, MainActivity.class));
        }

        //회원탈퇴를 클릭하면 회원정보를 삭제한다. 삭제전에 컨펌창을 하나 띄우자.
        if(view == textivewDelete) { AlertDialog.Builder alert_confirm = new AlertDialog.Builder(ProfileActivity.this);
            alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener()
            {
                @Override public void onClick(DialogInterface dialogInterface, int i)
                {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.delete() .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override public void onComplete(@NonNull Task<Void> task)
                    { Toast.makeText(ProfileActivity.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                });
                }
            });
            alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener()
            {
                @Override public void onClick(DialogInterface dialogInterface, int i)
                { Toast.makeText(ProfileActivity.this, "취소", Toast.LENGTH_LONG).show();
                }
            });
            alert_confirm.show();
        }
    }
}

