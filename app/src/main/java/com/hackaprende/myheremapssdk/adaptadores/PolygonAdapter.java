package com.hackaprende.myheremapssdk.adaptadores;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.hackaprende.myheremapssdk.MainActivity;
import com.hackaprende.myheremapssdk.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.util.Log;
import androidx.recyclerview.widget.RecyclerView;

import com.hackaprende.myheremapssdk.bd.PolygonDatabaseHelper;
import com.hackaprende.myheremapssdk.modelos.PolygonWithId;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.PickedPlace;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.Rectangle2D;
import com.here.sdk.core.Size2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.GestureState;
import com.here.sdk.gestures.PanListener;
import com.here.sdk.gestures.TapListener;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapContentResult;
import com.here.sdk.mapview.PickMapItemsResult;
import com.here.sdk.search.Place;
import com.here.sdk.search.PlaceCategory;
import com.here.sdk.search.PlaceIdSearchCallback;
import com.here.sdk.search.SearchError;

import java.util.ArrayList;
import java.util.List;

public class PolygonAdapter extends RecyclerView.Adapter<PolygonAdapter.PolygonViewHolder> {

    private static List<MapPolygon> poligonos = new ArrayList<>();
    private static List<GeoCoordinates> polygonVertices = new ArrayList<>();
    private static List<MapMarker> markers = new ArrayList<>();
    private static MapView mapView = null;
    private static MapPolygon mapPolygon = null;
    private static Context context;
    private static MapMarker selectedMarker = null;
    private static List<PolygonWithId> polygonWithIds = new ArrayList<>();
    private static PolygonDatabaseHelper dbHelper;
    private static int position=0;

    // Constructor para el adaptador
    public PolygonAdapter(List<PolygonWithId> polygonWithIds,List<GeoCoordinates> polygonVertices,List<MapMarker> markers,MapPolygon mapPolygon, MapView mapView, Context context, PolygonDatabaseHelper dbHelper) {
        this.polygonWithIds = polygonWithIds;
        this.polygonVertices = polygonVertices;
        this.markers = markers;
        this.mapPolygon = mapPolygon;
        this.mapView = mapView;
        this.context = context;
        this.dbHelper = dbHelper;
    }

    @Override
    public PolygonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.polygon_item, parent, false);
        return new PolygonViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(PolygonViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        holder.polygonNameTextView.setText(polygonWithIds.get(position).name);
    }

    @Override
    public int getItemCount() {
        return polygonWithIds.size(); // Devuelve el número total de polígonos
    }

    // Clase interna para el ViewHolder
    public static class PolygonViewHolder extends RecyclerView.ViewHolder {
        public TextView polygonNameTextView;
        public ImageView icon_edit, icon_delete;

        public PolygonViewHolder(View itemView, PolygonAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            polygonNameTextView = itemView.findViewById(R.id.polygon_name);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            icon_delete = itemView.findViewById(R.id.icon_delete);
            // Configura el OnClickListener para el ImageView de editar
            icon_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Limpiar los poligonos existentes del mapa
                    for (PolygonWithId polygon : polygonWithIds) {
                        mapView.getMapScene().removeMapPolygon(polygon.polygon);
                    }
                    // Eliminar cualquier polígono existente en el mapa
                    if(mapPolygon!=null)mapView.getMapScene().removeMapPolygon(mapPolygon);
                    if(polygonVertices.size()>0){
                        for (MapMarker marker : markers) {
                            mapView.getMapScene().removeMapMarker(marker);
                        }
                        markers.clear();
                        polygonVertices.clear();
                    }
                    position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mapPolygon = polygonWithIds.get(position).polygon;
                        polygonVertices = mapPolygon.getGeometry().vertices;
                        for (GeoCoordinates vertex : polygonVertices) {
                            addMapMarker(vertex, R.drawable.poi);
                        }
                        mapView.getMapScene().addMapPolygon(mapPolygon); // Muestra el polígono
                        // Calcula el centro del polígono
                        GeoCoordinates center = calculatePolygonCenter(mapPolygon);

                        // Calcula la distancia diagonal del polígono
                        double diagonalDistance = calculatePolygonDiagonalDistance(mapPolygon);

                        // Calcula el nivel de zoom adecuado (ajusta este valorsegún tus necesidades)
                        double zoomLevel = 15 - Math.log(diagonalDistance / 1000) / Math.log(2);

                        MapMeasure mapMeasure = new MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, zoomLevel);
                        // Centra el mapa en el polígono y ajusta el nivel de zoom
                        mapView.getCamera().lookAt(center, mapMeasure);
                        mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
                    }
                }
            });
            // Configura el OnClickListener para el ImageView de eliminar
            icon_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PolygonWithId polygon = polygonWithIds.get(position);
                        mapView.getMapScene().removeMapPolygon(polygon.polygon); // Elimina el polígono del mapa
                        polygonWithIds.remove(position);
                        adapter.notifyItemRemoved(position); // Llama al método desde el adaptador
                        adapter.notifyItemRangeChanged(position, polygonWithIds.size());
                        dbHelper.deletePolygon(polygon.id);
                        if(polygonVertices.size()>0){
                            for (MapMarker marker : markers) {
                                mapView.getMapScene().removeMapMarker(marker);
                            }
                            markers.clear();
                            polygonVertices.clear();
                        }
                    }
                }
            });
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        markers.add(mapMarker);
    }

    private static void updatePolygonGeometry() {
        // Obtiene las nuevas coordenadas de los marcadores
        List<GeoCoordinates> newVertices = new ArrayList<>();
        Log.e("Prueba", "updatePolygonGeometry: " + markers.size());
        for (MapMarker marker : markers) {
            newVertices.add(marker.getCoordinates());
        }

        // Crea una nueva geometría de polígono con las nuevas coordenadas
        GeoPolygon newGeometry = null;
        try {
            newGeometry = new GeoPolygon(newVertices);
        } catch (InstantiationErrorException e) {
            //throw new RuntimeException(e);
        }

        // Actualiza la geometría del polígono existente
        if (mapPolygon != null) {
            mapPolygon.setGeometry(newGeometry);
        }
        dbHelper.savePolygon(polygonWithIds.get(position).id,mapPolygon,"Mi Poligono "+(polygonWithIds.size()+1));
    }

    private static void pickMapMarker(final Point2D touchPoint) {
        // Establece el radio en metros
        float radiusInPixel = 2;

        // Obtener las coordenadas del punto del toque
        mapView.pickMapItems(touchPoint, radiusInPixel, new MapViewBase.PickMapItemsCallback() {
            @Override
            public void onPickMapItems(@Nullable PickMapItemsResult pickMapItemsResult) {
                try{
                    // Verificar si se ha seleccionado un MapMarker
                    if (pickMapItemsResult == null) {
                        return;
                    }

                    // Obtener el MapMarker seleccionado
                    MapMarker topmostMapMarker = pickMapItemsResult.getMarkers().get(0);
                    Log.e("Prueba", "onPickMapItems: " + topmostMapMarker);

                    // Verificar si el MapMarker es nulo
                    if (topmostMapMarker == null) {
                        return;
                    }
                    changeMapMarker(topmostMapMarker);

                } catch (Exception e) {
                    // Manejo de excepciones
                }
            }
        });
    }
    private static void changeMapMarker(MapMarker topmostMapMarker) {

        selectedMarker = topmostMapMarker;
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.green_dot);
        selectedMarker.setImage(mapImage);

        mapView.getGestures().setTapListener(new TapListener() {
            @Override
            public void onTap(@NonNull Point2D point2D) {
                if (selectedMarker != null) { // Solo si hay un marcador seleccionado
                    // Segundo clic: Mover el marcador a la nueva posición
                    GeoCoordinates newCoordinates = mapView.viewToGeoCoordinates(point2D);
                    selectedMarker.setCoordinates(newCoordinates);
                    updatePolygonGeometry(); // Actualiza la geometría del polígono si es necesario
                    MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi);
                    selectedMarker.setImage(mapImage);
                    selectedMarker = null; // Deselecciona el marcador
                    mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
                }
            }
        });
    }

    // Funciones auxiliares
    private static GeoCoordinates calculatePolygonCenter(MapPolygon polygon) {
        double latSum = 0;
        double lonSum = 0;
        for (GeoCoordinates vertex : polygon.getGeometry().vertices) {
            latSum += vertex.latitude;
            lonSum += vertex.longitude;
        }
        return new GeoCoordinates(latSum / polygon.getGeometry().vertices.size(), lonSum / polygon.getGeometry().vertices.size());
    }

    private static double calculatePolygonDiagonalDistance(MapPolygon polygon) {
        double maxDistance = 0;List<GeoCoordinates> vertices = polygon.getGeometry().vertices;
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                double distance = vertices.get(i).distanceTo(vertices.get(j));
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;
    }
}

