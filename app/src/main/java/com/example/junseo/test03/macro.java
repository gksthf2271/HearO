package com.example.junseo.test03;

/**
 * Created by Junseo on 2017-07-04.
 */
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by MARS on 6/11/2016.
 */
public class macro extends BaseAdapter {

    Context context;
    static ArrayList<String> name;
    static ArrayList<String> Namelist;
    SharedPreferences preferences;
    SharedPreferences preference;


    public macro(Context c, ArrayList<String> n){

        context=c;
        name=n;
    }

    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int pos) {
        return pos;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=LayoutInflater.from(context);
        if (convertView==null){
            convertView=inflater.inflate(R.layout.listview_layout,null);
        }
        TextView tvlistview= (TextView) convertView.findViewById(R.id.tvListview);
        tvlistview.setText(name.get(position));

        tvlistview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(v.getRootView().getContext()); //후... 빡칠뻔

                dlg.setTitle("삭제")
                        .setMessage("삭제하시겠습니까 ?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            // 확인 버튼 클릭시 설정

                            public void onClick(DialogInterface dialog, int whichButton) {

                                name.remove(position);
                                notifyDataSetChanged(); // 변경을 리스트에 표시

                                Namelist = new ArrayList<String>();
                                Namelist = name;

                                preferences = context.getSharedPreferences("Name_List", Context.MODE_PRIVATE);
                                Editor editor = preferences.edit();


                                for (int i = 0; i < Namelist.size(); i++) {
                                    editor.putString("Name " + i, Namelist.get(i));
                                }
                                editor.commit();

                                preference = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
                                int Num=Namelist.size();
                                Editor editors = preference.edit();
                                editors.putInt("Number", Num);
                                editors.commit();
                            }

                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {

                    // 취소 버튼 클릭시 설정

                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.cancel();

                    }
                });
                AlertDialog dialog = dlg.create();
                dialog.show();
                return true;
            }
        });

        tvlistview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "전송되었습니다.", Toast.LENGTH_SHORT).show();

            }
        });

        return convertView;
    }
}