package com.hackaprende.myheremapssdk.interfaces;

import com.here.sdk.core.GeoCoordinates;

public interface GeoCoordinatesCallback {
    void onCoordinatesFound(GeoCoordinates startCoordinates, GeoCoordinates destinationCoordinates);
}