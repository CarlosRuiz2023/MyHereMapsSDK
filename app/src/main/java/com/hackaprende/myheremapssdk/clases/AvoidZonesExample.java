package com.hackaprende.myheremapssdk.clases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hackaprende.myheremapssdk.R;
import com.hackaprende.myheremapssdk.adaptadores.PolygonAdapter;
import com.hackaprende.myheremapssdk.bd.PolygonDatabaseHelper;
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

public class AvoidZonesExample {
    public List<GeoCoordinates> polygonVertices = new ArrayList<>();
    public List<PolygonWithId> polygonWithIds = new ArrayList<>();
    public List<MapPolygon> poligonos = new ArrayList<>();
    public List<MapMarker> markers = new ArrayList<>();
    public MapPolygon mapPolygon =null;
    private PolygonDatabaseHelper dbHelper;
    public PolygonAdapter adapter;
    // Declaración de la variable dialog
    private AlertDialog dialog;
    private RecyclerView recyclerView;
    private MapView mapView;
    private Context context;
    private LayoutInflater layoutInflater;

    public AvoidZonesExample(Context context, MapView mapView, RecyclerView recyclerView,LayoutInflater layoutInflater) {
        this.context = context;
        this.mapView = mapView;
        this.recyclerView = recyclerView;
        this.layoutInflater = layoutInflater;
        // Crea una instancia del helper dela base de datos
        dbHelper = new PolygonDatabaseHelper(context);
        // Recupera la lista de polígonos de la base de datos
        polygonWithIds = dbHelper.getAllPolygons();
        if(polygonWithIds.size()>0){
            for (PolygonWithId polygonWithId : polygonWithIds) {
                poligonos.add(polygonWithId.polygon);
            }
            // INICIALIZAMOS EL ADAPTADOR
            adapter = new PolygonAdapter(polygonWithIds,polygonVertices,markers,mapPolygon,mapView,context,dbHelper);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
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
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        markers.add(mapMarker);
    }

    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    private void drawPolygon(List<GeoCoordinates> vertices) {
        // Eliminar cualquier polígono existente en el mapa
        if(mapPolygon!=null)mapView.getMapScene().removeMapPolygon(mapPolygon);

        // Crear un objeto PolygonLite con los vértices
        GeoPolygon geoPolygon=null;
        try {
            geoPolygon = new GeoPolygon(vertices);
        } catch (InstantiationErrorException e) {
            // Less than three vertices.
            //return null;
        }

        Color fillColor = Color.valueOf(1f, 0f, 0f, 0.63f); // RGBA
        mapPolygon = new MapPolygon(geoPolygon, fillColor);

        // Agregar el polígono al mapa
        mapView.getMapScene().addMapPolygon(mapPolygon);
    }
    public void showSavePolygonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Infla el layout personalizado
        View dialogView = layoutInflater.inflate(R.layout.dialog_save_polygon, null);
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
                dbHelper.savePolygon(mapPolygon, polygonName);
                polygonWithIds = dbHelper.getAllPolygons();
                poligonos = new ArrayList<>();
                for (PolygonWithId polygonWithId : polygonWithIds) {
                    poligonos.add(polygonWithId.polygon);
                }
                adapter = new PolygonAdapter(polygonWithIds,polygonVertices,markers,mapPolygon,mapView,context,dbHelper);
                recyclerView.setAdapter(adapter);
                mapView.getMapScene().removeMapPolygon(mapPolygon);
                mapPolygon=null;
                if(polygonVertices.size()>0){
                    for (MapMarker marker : markers) {
                        mapView.getMapScene().removeMapMarker(marker);
                    }
                    markers.clear();
                    polygonVertices.clear();
                }
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
            // Agregar la coordenada clicada a la lista de vértices
            polygonVertices.add(geoCoordinates);
            // Agregar un marcador en la coordenada clicada
            addMapMarker(geoCoordinates, R.drawable.red_dot);
            // Si hay al menos tres vértices, dibujar el polígono
            if (polygonVertices.size() > 2) {
                drawPolygon(polygonVertices);
            }
        });
    }
    public void cleanPolygon(){
        // Eliminar cualquier polígono existente en el Adaptador
        if(adapter.mapPolygon!=null)mapView.getMapScene().removeMapPolygon(adapter.mapPolygon);
        if(adapter.polygonVertices.size()>0){
            for (MapMarker marker : adapter.markers) {
                mapView.getMapScene().removeMapMarker(marker);
            }
            adapter.markers.clear();
            adapter.polygonVertices.clear();
        }
        // Eliminar cualquier polígono existente en esta Activity
        if(mapPolygon!=null)mapView.getMapScene().removeMapPolygon(mapPolygon);
        if(polygonVertices.size()>0){
            for (MapMarker marker : markers) {
                mapView.getMapScene().removeMapMarker(marker);
            }
            markers.clear();
            polygonVertices.clear();
        }
    }
}
