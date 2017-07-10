package com.example.junseo.test03;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Junseo on 2017-06-27.
 */
// 추후 구글 스토어에서 파싱하여 버전 업데이트 하는 방식 구상이 필요


public class VersionActivity extends AppCompatActivity {


    private Button buttonUpdate;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);


        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                // 업데이트 파싱 부분
                // 만약 최신버전일 경우 토스트
                Toast toast = Toast.makeText(VersionActivity.this, "최신 버전 입니다.", Toast.LENGTH_SHORT );
                toast.show();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
