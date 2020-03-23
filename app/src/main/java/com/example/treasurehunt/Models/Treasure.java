package com.example.treasurehunt.Models;

import com.google.firebase.firestore.GeoPoint;


public class Treasure {

    private Users user;
    private GeoPoint geoPoint;
    private String message;
    private String treasureId;

    public Treasure(Users user, GeoPoint geoPoint, String message, String treasureId) {
        this.user = user;
        this.geoPoint = geoPoint;
        this.message = message;
        this.treasureId = treasureId;
    }

    public Treasure ()
    {


    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTreasureId() {
        return treasureId;
    }

    public void setTreasureId(String treasureId) {
        this.treasureId = treasureId;
    }
}
