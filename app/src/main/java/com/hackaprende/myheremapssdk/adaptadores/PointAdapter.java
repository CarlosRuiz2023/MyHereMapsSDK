package com.hackaprende.myheremapssdk.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.hackaprende.myheremapssdk.R;
import com.hackaprende.myheremapssdk.bd.GeoCoordinateDatabaseHelper;
import com.hackaprende.myheremapssdk.bd.PolygonDatabaseHelper;
import com.hackaprende.myheremapssdk.modelos.PointWithId;
import com.hackaprende.myheremapssdk.modelos.PolygonWithId;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.TapListener;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapItemsResult;

import java.util.ArrayList;
import java.util.List;

public class PointAdapter extends RecyclerView.Adapter<PointAdapter.PointViewHolder> {

    public static List<GeoCoordinates> coordinates = new ArrayList<>();
    private static MapView mapView = null;
    private static Context context;
    public static MapMarker selectedMarker = null;
    private static List<PointWithId> poinstWithIds = new ArrayList<>();
    private static GeoCoordinateDatabaseHelper dbHelper;
    private static int position=0;

    // Constructor para el adaptador
    public PointAdapter(List<PointWithId> poinstWithIds, MapMarker mapMarker, MapView mapView, Context context, GeoCoordinateDatabaseHelper dbHelper) {
        this.poinstWithIds = poinstWithIds;
        this.selectedMarker = mapMarker;
        this.mapView = mapView;
        this.context = context;
        this.dbHelper = dbHelper;
    }

    @Override
    public PointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflar la vista del elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.polygon_item, parent, false);
        return new PointViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(PointViewHolder holder, int position) {
        // Asigna el nombre del polígono basado en su posición
        holder.polygonNameTextView.setText(poinstWithIds.get(position).name);
    }

    @Override
    public int getItemCount() {
        return poinstWithIds.size(); // Devuelve el número total de polígonos
    }

    // Clase interna para el ViewHolder
    public static class PointViewHolder extends RecyclerView.ViewHolder {
        public TextView polygonNameTextView;
        public ImageView icon_edit, icon_delete;

        public PointViewHolder(View itemView, PointAdapter adapter) {
            super(itemView);
            // Enlaza el TextView con el layout
            polygonNameTextView = itemView.findViewById(R.id.polygon_name);
            icon_edit = itemView.findViewById(R.id.icon_edit);
            icon_delete = itemView.findViewById(R.id.icon_delete);
            // Configura el OnClickListener para el ImageView de editar
            icon_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Eliminar cualquier punto existente en el mapa
                    if(selectedMarker!=null)mapView.getMapScene().removeMapMarker(selectedMarker);
                    position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        PointWithId punto = poinstWithIds.get(position);
                        addMapMarker(punto.punto, R.drawable.red_dot);
                        mapView.getMapScene().addMapMarker(selectedMarker); // Muestra el punto
                        // Centra el mapa en el polígono y ajusta el nivel de zoom
                        mapView.getCamera().lookAt(selectedMarker.getCoordinates(), new MapMeasure(MapMeasure.Kind.ZOOM_LEVEL,15));
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
                        PointWithId punto = poinstWithIds.get(position);
                        addMapMarker(punto.punto, R.drawable.red_dot);
                        mapView.getMapScene().removeMapMarker(selectedMarker); // Elimina el polígono del mapa
                        poinstWithIds.remove(position);
                        adapter.notifyItemRemoved(position); // Llama al método desde el adaptador
                        adapter.notifyItemRangeChanged(position, poinstWithIds.size());
                        dbHelper.deleteCoordinate(punto.id);
                        selectedMarker = null;
                    }
                }
            });
        }
    }
    private static void addMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        selectedMarker=mapMarker;
    }

    private static void updatePointGeometry() {
        dbHelper.updateCoordinate(poinstWithIds.get(position).id,selectedMarker.getCoordinates(),poinstWithIds.get(position).name);
        poinstWithIds.get(position).setPunto(selectedMarker.getCoordinates());
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
                    updatePointGeometry(); // Actualiza la geometría del polígono si es necesario
                    MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.red_dot);
                    selectedMarker.setImage(mapImage);
                    mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
                }
            }
        });
    }
}

