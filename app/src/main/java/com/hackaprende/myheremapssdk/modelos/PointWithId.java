package com.hackaprende.myheremapssdk.modelos;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.mapview.MapPolygon;

public class PointWithId {
    public int id;
    public GeoCoordinates punto;
    public String name;

    public PointWithId(int id, GeoCoordinates punto, String name) {
        this.id = id;
        this.punto = punto;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoCoordinates punto() {
        return punto;
    }

    public void setPunto(GeoCoordinates punto) {
        this.punto = punto;
    }
}
