package com.example.junseo.test03;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Junseo on 2017-07-03.
 */

public class TTSActivity extends AppCompatActivity{

    EditText textts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        init();
    }

    void init() {
        textts = (EditText) findViewById(R.id.tts);
    }
    // 전송 버튼을 눌렀을 경우. (여기서 모듈로 전송하는 코딩이 필요)
    public void onClick(View v) {
        //Intent intent = new Intent();


    }
}
