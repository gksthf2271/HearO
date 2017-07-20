package com.example.junseo.test03;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Junseo on 2017-07-04.
 */

public class STTActivity extends AppCompatActivity implements View.OnClickListener  {
    Button checkbtn = null;
    EditText editstt = null;
    Button speakbtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);

    checkbtn = (Button) findViewById(R.id.checkbtn);
    editstt = (EditText) findViewById(R.id.editstt);
    speakbtn = (Button) findViewById(R.id.speakbtn);

    }

    @Override
    public void onClick(View view){

        switch (view.getId()){

            case R.id.checkbtn:

                break;

            case R.id.speakbtn:

                break;
        }

    }

}
