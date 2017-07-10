package com.example.junseo.test03;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<Position> position;
    private LayoutInflater inflater;

    //class Constructor
    public HelpAdapter (Context mContext, ArrayList<Position> position) {

        this.mContext = mContext;
        this.position = position;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getGroupCount() {
        return position.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return position.get(groupPosition).players.size();
    }

    //get position
    @Override
    public Object getGroup(int groupPosition) {
        return position.get(groupPosition);
    }

    //this is where we get the information of player
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return position.get(groupPosition).players.get(childPosition);
    }

    //position ID
    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    //where to get player's id
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //get parent row
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.parent_list, null);
        }

        //get position
        Position position = (Position) getGroup(groupPosition);

        //set positionName
        String positionName = position.position;

        TextView textView = (TextView) convertView.findViewById(R.id.position_tv);
        textView.setText(positionName);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.indicator);
        if(isExpanded){
            imageView.setImageResource(R.drawable.arrowdown);
        } else {
            imageView.setImageResource(R.drawable.arrowup);
        }

        convertView.setBackgroundColor(Color.LTGRAY);
        return convertView;
    }

    //get child_list.xml (View)
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        //inflate the layout
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.child_list, null);
        }

        String child = (String) getChild(groupPosition, childPosition);

        //set the child name
        TextView name = (TextView) convertView.findViewById(R.id.name_tv);
        //get the imageView
        ImageView img = (ImageView) convertView.findViewById(R.id.playerpic);

        name.setText(child);

        //get position name
        String positionName = (String) getGroup(groupPosition).toString();
        if (positionName == "pitcher") {
            if (child == "고원준") {
               // img.setImageResource(R.drawable.wonjun_ko);
            } else if (child == "Brooks Raley") {
               // img.setImageResource(R.drawable.books_raley);
            } else if (child == "박세웅") {
              //  img.setImageResource(R.drawable.sewoong_park);
            }
        } else if (positionName == "infield") {
            if (child == "문규현") {
                //img.setImageResource(R.drawable.kyuhyun_moon);
            } else if (child == "박종윤") {
               // img.setImageResource(R.drawable.jongyun_park);
            }
        } else if (positionName == "catcher") {
            if (child == "강민호") {
               // img.setImageResource(R.drawable.minho_kang);
            } else if (child == "안중열") {
              //  img.setImageResource(R.drawable.jungyul_ahn);
            }
        } else if (positionName == "outfield") {
            if (child == "Jim Adduci") {
               // img.setImageResource(R.drawable.jim_adduci);
            } else if (child == "손아섭") {
               // img.setImageResource(R.drawable.ahsup_son);
            }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}

