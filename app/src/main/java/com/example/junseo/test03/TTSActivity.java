package com.example.junseo.test03;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Junseo on 2017-07-03.
 */

public class TTSActivity extends AppCompatActivity{

    EditText textts;
    Button Cancel4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        Cancel4 = (Button) findViewById(R.id.Cancel4);
        init();

        Cancel4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void init() {
        textts = (EditText) findViewById(R.id.tts);
    }
    // 전송 버튼을 눌렀을 경우. (여기서 모듈로 전송하는 코딩이 필요)
    public void onClick(View v) {
        //Intent intent = new Intent();


    }
}
