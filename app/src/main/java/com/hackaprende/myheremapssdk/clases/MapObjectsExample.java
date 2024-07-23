/*
 * Copyright (C) 2019-2024 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.hackaprende.myheremapssdk.clases;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.hackaprende.myheremapssdk.R;
import com.here.sdk.core.GeoCircle;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.Camera;
import com.here.sdk.mapviewlite.MapCircle;
import com.here.sdk.mapviewlite.MapCircleStyle;
import com.here.sdk.mapviewlite.MapPolygon;
import com.here.sdk.mapviewlite.MapPolygonStyle;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapScene;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.PixelFormat;

import java.util.ArrayList;

public class MapObjectsExample {

    /*private static final GeoCoordinates BERLIN_GEO_COORDINATES = new GeoCoordinates(52.51760485151816, 13.380312380535472);*/
    // Definimos una variable tipo MapScene
    private final MapScene mapScene;
    // Definimos una variable tipo Camera
    private final Camera mapCamera;
    // Definimos una variable tipo MapPolyline
    private MapPolyline mapPolyline;
    // Definimos una variable tipo MapPolygon
    private MapPolygon mapPolygon;
    // Definimos una variable tipo MapCircle
    private MapCircle mapCircle;
    // Definimos una variable tipo Context
    private Context context;
    // Definimos una variable tipo EditText
    EditText searchEditText;
    // Definimos al constructor
    public MapObjectsExample(MapViewLite mapView, Context context) {
        // Generamos una Activity mediante el contexto
        Activity activity = (Activity) context;
        // Inicializamos las variables
        this.searchEditText = activity.findViewById(R.id.searchEditText);
        this.mapScene = mapView.getMapScene();
        this.mapCamera = mapView.getCamera();
        this.context = context;
    }

    /*public void showMapPolyline() {
        clearMap();
        // Move map to expected location.
        mapCamera.setTarget(BERLIN_GEO_COORDINATES);
        mapCamera.setZoomLevel(13.0);

        mapPolyline = createPolyline();
        mapScene.addMapPolyline(mapPolyline);
    }*/

    /*public void showMapPolygon() {
        clearMap();
        // Move map to expected location.
        mapCamera.setTarget(BERLIN_GEO_COORDINATES);
        mapCamera.setZoomLevel(13.0);

        mapPolygon = createPolygon();
        mapScene.addMapPolygon(mapPolygon);
    }*/

    public void showMapCircle(GeoCoordinates geoCoordinates, float radius) {
        // Creamos el circulo
        this.mapCircle = createMapCircle(geoCoordinates,radius);
        // Agregamos el circulo al mapa
        this.mapScene.addMapCircle(mapCircle);
    }

    public void clearMap() {
        // Eliminamos las lineas
        if (mapPolyline != null) {
            mapScene.removeMapPolyline(mapPolyline);
        }
        // Eliminamos el poligono
        if (mapPolygon != null) {
            mapScene.removeMapPolygon(mapPolygon);
        }
        // Eliminamos el circulo
        if (mapCircle != null) {
            mapScene.removeMapCircle(mapCircle);
        }
    }

    /*private MapPolyline createPolyline() {
        ArrayList<GeoCoordinates> coordinates = new ArrayList<>();
        coordinates.add(new GeoCoordinates(52.53032, 13.37409));
        coordinates.add(new GeoCoordinates(52.5309, 13.3946));
        coordinates.add(new GeoCoordinates(52.53894, 13.39194));
        coordinates.add(new GeoCoordinates(52.54014, 13.37958));

        GeoPolyline geoPolyline;
        try {
            geoPolyline = new GeoPolyline(coordinates);
        } catch (InstantiationErrorException e) {
            // Less than two vertices.
            return null;
        }

        MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
        mapPolylineStyle.setWidthInPixels(20);
        mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
        MapPolyline mapPolyline = new MapPolyline(geoPolyline, mapPolylineStyle);

        return mapPolyline;
    }*/

    /*private MapPolygon createPolygon() {
        ArrayList<GeoCoordinates> coordinates = new ArrayList<>();
        coordinates.add(new GeoCoordinates(52.53032, 13.37409));
        coordinates.add(new GeoCoordinates(52.5309, 13.3946));
        coordinates.add(new GeoCoordinates(52.53894, 13.39194));
        coordinates.add(new GeoCoordinates(52.54014, 13.37958));

        GeoPolygon geoPolygon;
        try {
            geoPolygon = new GeoPolygon(coordinates);
        } catch (InstantiationErrorException e) {
            // Less than three vertices.
            return null;
        }

        MapPolygonStyle mapPolygonStyle = new MapPolygonStyle();
        mapPolygonStyle.setFillColor(0x00908AA0, PixelFormat.RGBA_8888);
        MapPolygon mapPolygon = new MapPolygon(geoPolygon, mapPolygonStyle);

        return mapPolygon;
    }*/

    private MapCircle createMapCircle(GeoCoordinates geoCoordinates,float radiusInMeters) {
        // Creamos el GeoCircle para el circulo
        GeoCircle geoCircle = new GeoCircle(geoCoordinates, radiusInMeters);
        // Creamos el estilo
        MapCircleStyle mapCircleStyle = new MapCircleStyle();
        // Establecemos el color
        mapCircleStyle.setFillColor(0xFF5722A0, PixelFormat.RGBA_8888);
        // Creamos el circulo
        MapCircle mapCircle = new MapCircle(geoCircle, mapCircleStyle);
        // Devolvemos el circulo
        return mapCircle;
    }
}
