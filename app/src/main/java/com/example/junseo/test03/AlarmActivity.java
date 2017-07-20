package com.example.junseo.test03;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by Junseo on 2017-06-27.
 */
// 추후 구글 스토어에서 파싱하여 버전 업데이트 하는 방식 구상이 필요


public class AlarmActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

    Switch sw = (Switch) findViewById(R.id.sw);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked){
                    Toast toast1 = Toast.makeText(AlarmActivity.this, "알람이 켜졌습니다.", Toast.LENGTH_SHORT );
                    toast1.show();
                }else {
                    Toast toast2 = Toast.makeText(AlarmActivity.this, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT);
                    toast2.show();
                }
            }
        });
    }
}
