package com.hackaprende.myheremapssdk.modelos;

import com.here.sdk.mapview.MapPolygon;

public class PolygonWithId {
    public int id;
    public MapPolygon polygon;
    public String name;

    public PolygonWithId(int id, MapPolygon polygon,String name) {
        this.id = id;
        this.polygon = polygon;
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

    public MapPolygon polygon() {
        return polygon;
    }

    public void setPolygon(MapPolygon polygon) {
        this.polygon = polygon;
    }
}
