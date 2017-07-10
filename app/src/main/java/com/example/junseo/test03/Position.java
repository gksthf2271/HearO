package com.example.junseo.test03;

import java.util.ArrayList;

/**
 * Created by Junseo on 2017-06-12.
 */

public class Position {
    //Properties of Position
    public String position;
    public String image;
    public ArrayList<String> players = new ArrayList<String>();

    public Position(String position){
        this.position = position;
    }

    public String toString () {
        return position;
    }

}
