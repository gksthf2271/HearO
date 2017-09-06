package com.example.junseo.test03;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Belal on 2/26/2017.
 */

public class MacroList extends ArrayAdapter<macro> {
    private Activity context;
    List<macro> macros;

    public MacroList(Activity context, List<macro> macros) {
        super(context, R.layout.layout_macro_list, macros);
        this.context = context;
        this.macros = macros;
    }

    // 리스트 뷰 아이템 가져오는 부분
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_macro_list, null, true);

        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);

        macro macro = macros.get(position);
        textViewName.setText(macro.getmacroName());


        return listViewItem;
    }
}