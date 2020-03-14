package com.example.treasurehunt.Models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation {

    private Users user;
    private GeoPoint geoPoint;
    private @ServerTimestamp Date time;

    public UserLocation(Users user, GeoPoint geoPoint, Date time) {
        this.user = user;
        this.geoPoint = geoPoint;
        this.time = time;
    }

    public UserLocation () {}

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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "user=" + user +
                ", geoPoint=" + geoPoint +
                ", time=" + time +
                '}';
    }
}
