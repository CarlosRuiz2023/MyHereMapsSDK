package com.hackaprende.myheremapssdk.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hackaprende.myheremapssdk.modelos.PolygonWithId;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapPolygon;

import java.util.ArrayList;
import java.util.List;

public class PolygonDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "polygons.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_POLYGONS = "polygons";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VERTICES = "vertices";

    public PolygonDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_POLYGONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_VERTICES + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Maneja actualizaciones de la base de datos si es necesario
    }

    // Guarda un polígono en la base de datos
    public void savePolygon(MapPolygon polygon, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_VERTICES, serializePolygon(polygon));
        db.insert(TABLE_POLYGONS, null, values);
        db.close();
    }

    // Recupera todos los polígonos de la base de datos
    public List<PolygonWithId> getAllPolygons() {
        List<PolygonWithId> polygons = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_POLYGONS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String verticesString = cursor.getString(cursor.getColumnIndex(COLUMN_VERTICES));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                List<GeoCoordinates> vertices = deserializePolygon(verticesString);
                GeoPolygon geometry = null;
                try {
                    geometry = new GeoPolygon(vertices);
                } catch (InstantiationErrorException e) {
                    //throw new RuntimeException(e);
                }
                Color fillColor = Color.valueOf(0, 0.56f, 0.54f, 0.63f); // RGBA
                MapPolygon polygon = new MapPolygon(geometry,fillColor);
                polygons.add(new PolygonWithId(id, polygon,name));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return polygons;
    }

    // Serializa un MapPolygon en una cadena de texto
    private String serializePolygon(MapPolygon polygon) {
        List<GeoCoordinates> vertices = polygon.getGeometry().vertices;
        StringBuilder verticesString = new StringBuilder();
        for (GeoCoordinates vertex : vertices) {
            verticesString.append(vertex.latitude).append(",").append(vertex.longitude).append(";");
        }
        return verticesString.toString();
    }

    // Deserializa un polígono desde una cadena de texto
    private List<GeoCoordinates> deserializePolygon(String verticesString) {
        List<GeoCoordinates> vertices = new ArrayList<>();
        String[] vertexStrings = verticesString.split(";");
        for (String vertexString : vertexStrings) {
            String[] coords = vertexString.split(",");
            if (coords.length == 2) {
                double latitude = Double.parseDouble(coords[0]);
                double longitude = Double.parseDouble(coords[1]);
                vertices.add(new GeoCoordinates(latitude, longitude));
            }
        }
        return vertices;
    }

    // Guarda o actualiza un polígono en la base de datos
    public void savePolygon(int id, MapPolygon polygon, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_VERTICES, serializePolygon(polygon));

        if (id == 0) { // Nuevo polígono
            db.insert(TABLE_POLYGONS, null, values);
        } else { // Actualizar polígono existente
            db.update(TABLE_POLYGONS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        }

        db.close();
    }

    // Obtiene un polígono por su ID
    public MapPolygon getPolygonById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_POLYGONS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});

        MapPolygon polygon = null;
        if (cursor.moveToFirst()) {
            String verticesString = cursor.getString(cursor.getColumnIndex(COLUMN_VERTICES));
            List<GeoCoordinates> vertices = deserializePolygon(verticesString);
            GeoPolygon geometry = null;
            try {
                geometry = new GeoPolygon(vertices);
            } catch (InstantiationErrorException e) {
                //throw new RuntimeException(e);
            }
            Color fillColor = Color.valueOf(0, 0.56f, 0.54f, 0.63f); // RGBA
            polygon = new MapPolygon(geometry,fillColor);
        }

        cursor.close();
        db.close();
        return polygon;
    }
    // Elimina un polígono de la base de datos por su ID
    public void deletePolygon(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POLYGONS, COLUMN_ID + " = ?",new String[]{String.valueOf(id)});
        db.close();
    }
}
