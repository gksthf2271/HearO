package com.example.junseo.test03;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Belal on 2/26/2017.
 */
@IgnoreExtraProperties
public class macro {
    private String macroId;
    private String macroName;
    // private String macroGenre;

    public macro(){
        //this constructor is required
    }

    public macro(String macroId, String macroName) {
        this.macroId = macroId;
        this.macroName = macroName;
        //  this.macroGenre = macroGenre;
    }

    public String getmacroId() {
        return macroId;
    }

    public String getmacroName() {
        return macroName;
    }
/*
    public String getmacroGenre() {
        return macroGenre;
    }*/
}
