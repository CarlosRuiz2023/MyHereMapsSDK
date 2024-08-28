package com.hackaprende.myheremapssdk.clases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hackaprende.myheremapssdk.R;
import com.hackaprende.myheremapssdk.adaptadores.PointAdapter;
import com.hackaprende.myheremapssdk.adaptadores.PolygonAdapter;
import com.hackaprende.myheremapssdk.bd.GeoCoordinateDatabaseHelper;
import com.hackaprende.myheremapssdk.bd.PolygonDatabaseHelper;
import com.hackaprende.myheremapssdk.modelos.PointWithId;
import com.hackaprende.myheremapssdk.modelos.PolygonWithId;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapView;

import java.util.ArrayList;
import java.util.List;

public class ControlPointsExample {
    public List<GeoCoordinates> points = new ArrayList<>();
    public List<PointWithId> pointsWithIds = new ArrayList<>();
    public MapMarker mapMarker =null;
    private GeoCoordinateDatabaseHelper dbHelper;
    public PointAdapter adapter;
    // Declaración de la variable dialog
    private AlertDialog dialog;
    private RecyclerView recyclerView;
    private MapView mapView;
    private Context context;
    private LayoutInflater layoutInflater;

    public ControlPointsExample(Context context, MapView mapView, RecyclerView recyclerView, LayoutInflater layoutInflater) {
        this.context = context;
        this.mapView = mapView;
        this.recyclerView = recyclerView;
        this.layoutInflater = layoutInflater;
        // Crea una instancia del helper dela base de datos
        dbHelper = new GeoCoordinateDatabaseHelper(context);
        // Recupera la lista de polígonos de la base de datos
        pointsWithIds = dbHelper.getAllCoordinates();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if(pointsWithIds.size()>0){
            for (PointWithId point : pointsWithIds) {
                points.add(point.punto);
            }
            // INICIALIZAMOS EL ADAPTADOR
            adapter = new PointAdapter(pointsWithIds,mapMarker,mapView,context,dbHelper);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Método para agregar un marcador en el mapa en las coordenadas especificadas.
     *
     * @param geoCoordinates Las coordenadas donde se agregará el marcador.
     */
    private void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    public void showSavePointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_point, null);
        builder.setView(dialogView);

        // Obtén las referencias a los elementos del layout
        final EditText input = dialogView.findViewById(R.id.polygon_name_input);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton saveButton = dialogView.findViewById(R.id.save_button);

        // Configura los listeners de los botones
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el diálogo
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String polygonName = input.getText().toString();
                // Guarda el polígono en la base de datos
                dbHelper.saveCoordinate(mapMarker.getCoordinates(), polygonName);
                pointsWithIds = dbHelper.getAllCoordinates();
                points = new ArrayList<>();
                for (PointWithId point : pointsWithIds) {
                    points.add(point.punto);
                }
                adapter = new PointAdapter(pointsWithIds,mapMarker,mapView,context,dbHelper);
                recyclerView.setAdapter(adapter);
                mapView.getMapScene().removeMapMarker(mapMarker);
                mapMarker=null;
                dialog.dismiss(); // Cierra el diálogo
            }
        });

        // Muestra el diálogo
        dialog = builder.create();
        dialog.show();
    }
    public void startGestures(){
        // Configurar el listener de clics en el mapa
        mapView.getGestures().setTapListener(mapViewPoint-> {
            // Obtener las coordenadas geográficas del punto.
            GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(mapViewPoint);
            if(mapMarker!=null) mapView.getMapScene().removeMapMarker(mapMarker);
            // Agregar un marcador en la coordenada clicada
            addMapMarker(geoCoordinates, R.drawable.red_dot);
        });
    }
    public void cleanPoint(){
        // Eliminar cualquier mapMarker existente en el Adaptador
        if(mapMarker!=null) mapView.getMapScene().removeMapMarker(mapMarker);
        if(adapter.selectedMarker!=null)mapView.getMapScene().removeMapMarker(adapter.selectedMarker);
    }
}
