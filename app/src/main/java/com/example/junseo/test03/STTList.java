package com.example.junseo.test03;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.junseo.test03.speech.SpeechListener;

import java.util.ArrayList;

/**
 * Created by rlagk on 2017-10-07.
 */

public class STTList extends Activity implements SpeechListener {

    static final String[] LIST_MENU = {"LIST1", "LIST2", "LIST3"} ;
    private SpeechListener speech_listener_;
    private static final String TAG = STTList.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout.activity_sttlist) ;

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, LIST_MENU) ;

        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position) ;

                // TODO : use strText
            }
        }) ;

    }

    @Override
    public void onSpeechRecognized(ArrayList<String> recognitions) {

        ArrayList<String> arSttResult = new ArrayList<>();
        speech_listener_.onSpeechRecognized(arSttResult);
        for(String speech : recognitions) {
            Log.d(TAG, arSttResult.get(0));
        }
    }
}
