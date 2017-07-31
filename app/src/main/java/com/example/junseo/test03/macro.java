package com.example.junseo.test03;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Belal on 2/26/2017.
 */
@IgnoreExtraProperties
public class macro {
    private String artistId;
    private String artistName;
   // private String artistGenre;

    public macro(){
        //this constructor is required
    }

    public macro(String artistId, String artistName) {
        this.artistId = artistId;
        this.artistName = artistName;
      //  this.artistGenre = artistGenre;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }
/*
    public String getArtistGenre() {
        return artistGenre;
    }*/
}
