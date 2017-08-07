package com.example.junseo.test03;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;


public class HelpActivity extends ActionBarActivity {
    private Button buttonCancel6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        buttonCancel6 = (Button) findViewById(R.id.buttonCancel6);
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.elv);

        final ArrayList<Position> position = getData();

        //create and bind to adatper
        HelpAdapter adapter = new HelpAdapter(this, position);
        elv.setAdapter(adapter);

        //set onclick listener
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(getApplicationContext(), position.get(groupPosition).players.get(childPosition), Toast.LENGTH_LONG).show();
                return false;
            }
        });
        buttonCancel6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    //add and get data for list
    private ArrayList<Position> getData() {

        Position p1 = new Position("회원 정보 관리");
        p1.players.add("주제");
        p1.players.add("주제2");
        p1.players.add("주제3");

        Position p2 = new Position("모듈 관리");
        p2.players.add("주제");
        p2.players.add("주제2");
        p2.players.add("주제3");

        Position p3 = new Position("상용구 관리");
        p3.players.add("주제");
        p3.players.add("주제2");

        Position p4 = new Position("기타");
        p4.players.add("주제");
        p4.players.add("주제2");

        ArrayList<Position> allposition = new ArrayList<>();
        allposition.add(p1);
        allposition.add(p2);
        allposition.add(p3);
        allposition.add(p4);

        return allposition;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

