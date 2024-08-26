package com.hackaprende.myheremapssdk.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hackaprende.myheremapssdk.modelos.PointWithId;
import com.here.sdk.core.GeoCoordinates;

import java.util.ArrayList;
import java.util.List;

public class GeoCoordinateDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "coordinates.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_COORDINATES = "coordinates";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    public GeoCoordinateDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_COORDINATES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Maneja actualizaciones de la base de datos si es necesario
    }

    // Guarda una coordenada en la base de datos
    public void saveCoordinate(GeoCoordinates coordinate, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LATITUDE, coordinate.latitude);
        values.put(COLUMN_LONGITUDE, coordinate.longitude);
        db.insert(TABLE_COORDINATES, null, values);
        db.close();
    }

    // Recupera todas las coordenadas de la base de datos
    public List<PointWithId> getAllCoordinates() {
        List<PointWithId> pointsWithIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COORDINATES;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                pointsWithIds.add(new PointWithId(id,new GeoCoordinates(latitude, longitude),name));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return pointsWithIds;
    }

    // Elimina una coordenada de la base de datos por su ID
    public void deleteCoordinate(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COORDINATES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Obtiene una coordenada por su ID
    public PointWithId getCoordinateById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_COORDINATES + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});

        PointWithId punto = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
            punto = new PointWithId(id,new GeoCoordinates(latitude, longitude),name);
        }

        cursor.close();
        db.close();
        return punto;
    }

    // Actualiza una coordenada en la base de datos
    public void updateCoordinate(int id, GeoCoordinates coordinate, String name) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_LATITUDE, coordinate.latitude);
        values.put(COLUMN_LONGITUDE, coordinate.longitude);
        db.update(TABLE_COORDINATES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}