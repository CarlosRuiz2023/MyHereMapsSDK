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

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hackaprende.myheremapssdk.R;
import com.hackaprende.myheremapssdk.interfaces.ReverseGeocodingCallback;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.CustomMetadataValue;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.gestures.GestureState;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.MapViewBase;
import com.here.sdk.mapview.PickMapItemsResult;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.TextQuery;
import com.here.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class SearchExample {
    // Define la etiqueta para el registro de logs
    private static final String LOG_TAG = SearchExample.class.getName();
    // Define el contexto de la aplicación
    private final Context context;
    // Define el mapa
    private MapView mapView;
    // Define la cámara
    private final MapCamera camera;
    // Define la lista de MapMarker para ubicaciones buscadas por el usuario
    public final List<MapMarker> mapMarkerList = new ArrayList<>();
    // Define la lista de MapMarker para ubicaciones precionadas por el usuario
    public final List<MapMarker> mapMarkerList1 = new ArrayList<>();
    // Define la lista de MapMarker para ubicaciones del usuario
    public final List<MapMarker> mapMarkerList2 = new ArrayList<>();
    // Define el buscador de lugares
    private final SearchEngine searchEngine;
    // Define el campo de búsqueda
    EditText searchEditText;
    // Define la duración de la animación de zoom en milisegundos
    final long zoomAnimationDuration = 1000L;
    // Define el constructor de la clase
    public SearchExample(Context context, MapView mapView) {
        // Inicializa el contexto de la aplicación
        this.context = context;
        // Inicializa el mapa
        this.mapView = mapView;
        // Generamos la Activity mediante el contexto
        Activity activity = (Activity) context;
        // Inicializa el campo de búsqueda mediante su id
        searchEditText = activity.findViewById(R.id.searchEditText);
        // Inicializa la cámara
        camera = mapView.getCamera();
        try {
            // Inicializa el buscador de lugares
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            // Maneja la excepción de instancia
            throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
        }
        // Establece los gestores de gesto
        setTapGestureHandler();
        setLongPressGestureHandler();
        // Mostrar mensaje de ayuda
        Toast.makeText(context,"Mantenga presionado el mapa para obtener la dirección de esa posición usando codificación geográfica inversa.", Toast.LENGTH_LONG).show();
    }

    public void onSearchButtonClicked() {
        // Search for "Pizza" and show the results on the map.
        searchExample();
        // Search for auto suggestions and log the results to the console.
        //autoSuggestExample();
    }

    private void searchExample() {
        // Obtener el texto ingresado en el campo de búsqueda
        String searchTerm = searchEditText.getText().toString();
        // Validar que el texto no esté vacío
        if(searchTerm.length()>0){
            try {
                // Validamos que searchTerm no sea un número
                float radiusInMeters = Float.parseFloat(searchTerm);
                Toast.makeText(context, "Por favor, introduce una direccion valida", Toast.LENGTH_SHORT).show();
            }catch (Exception e) {
                // Seguir con la busqueda
                searchInViewport(searchTerm);
            }
        }else{
            // Mostrar mensaje de error
            Toast.makeText(context, "La direccion esta vacia", Toast.LENGTH_SHORT).show();
        }
    }

    public void setTapGestureHandler() {
        // Establece el gesto de tap en el mapa
        mapView.getGestures().setTapListener(touchPoint -> pickMapMarker(touchPoint));
    }

    // Establece el gesto de largo presionado en el mapa
    private void setLongPressGestureHandler() {
        // Establece el gesto de largo presionado en el mapa
        mapView.getGestures().setLongPressListener((gestureState, touchPoint) -> {
            // Verificar el estado del gesto
            if (gestureState == GestureState.BEGIN) {
                // Obtener las coordenadas de la posición del toque
                GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(touchPoint);
                //GeoCoordinates geoCoordinates = mapView.getCamera().viewToGeoCoordinates(touchPoint);
                // Mostrar las coordenadas en un diálogo
                getAddressForCoordinates(geoCoordinates);
            }
        });
    }

    private void getAddressForCoordinates(GeoCoordinates geoCoordinates) {
        // Reverse geocode the coordinates.
        SearchOptions reverseGeocodingOptions = new SearchOptions();
        // Establece el idioma en inglés
        reverseGeocodingOptions.languageCode = LanguageCode.EN_GB;
        // Establece el número máximo de resultados a obtener
        reverseGeocodingOptions.maxItems = 1;
        // Realiza la búsqueda
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                // If error is null, list is guaranteed to be not empty.
                if (searchError != null) {
                    showDialog("Reverse geocoding", "Error: " + searchError.toString());
                    return;
                }
                // Add new marker for each search result on map.
                Metadata metadata = new Metadata();
                metadata.setCustomValue("key_search_result", new SearchResultMetadata(list.get(0)));
                addPoiMapMarkerPuntero(list.get(0).getGeoCoordinates(), metadata);
                // Obtener los datos de la primera ubicación
                String estado = list.get(0).getAddress().state;
                String ciudad = list.get(0).getAddress().city;
                String colonia = list.get(0).getAddress().district;
                String codigo_postal = list.get(0).getAddress().postalCode;
                String calle = list.get(0).getAddress().street;
                String numero = list.get(0).getAddress().houseNumOrName;
                // Mostrar los datos en un diálogo
                showAddressDialog(estado, ciudad, colonia, codigo_postal, calle, numero);
            }
        });
    }

    public void geocodificacionInversa(GeoCoordinates geoCoordinates, ReverseGeocodingCallback callback) {
        // Reverse geocode the coordinates.
        SearchOptions reverseGeocodingOptions = new SearchOptions();
        // Establece el idioma en inglés
        reverseGeocodingOptions.languageCode = LanguageCode.EN_GB;
        // Establece el número máximo de resultados a obtener
        reverseGeocodingOptions.maxItems = 1;
        // Realiza la búsqueda
        searchEngine.search(geoCoordinates, reverseGeocodingOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                // If error is null, list is guaranteed to be not empty.
                if (searchError != null) {
                    showDialog("Reverse geocoding", "Error: " + searchError.toString());
                    callback.onResult(null);
                    return;
                }
                // If error is null, list is guaranteed to be not empty.
                String addressText = list.get(0).getAddress().addressText;
                callback.onResult(addressText);
            }
        });
    }

    private void pickMapMarker(final Point2D point2D) {
        // Establece el radio en metros
        float radiusInPixel = 2;
        // Obtener las coordenadas del punto del toque
        mapView.pickMapItems(point2D, radiusInPixel, new MapViewBase.PickMapItemsCallback() {
            @Override
            public void onPickMapItems(@Nullable PickMapItemsResult pickMapItemsResult) {
                // Verificar si se ha seleccionado un MapMarker
                if (pickMapItemsResult == null) {
                    return;
                }
                // Obtener el MapMarker seleccionado
                MapMarker topmostMapMarker = pickMapItemsResult.getMarkers().get(1);
                // Verificar si el MapMarker es nulo
                if (topmostMapMarker == null) {
                    return;
                }
                // Obtener la metadata del MapMarker
                Metadata metadata = topmostMapMarker.getMetadata();
                // Verificar si la metadata es nula
                if (metadata != null) {
                    // Obtener el valor personalizado de la metadata
                    CustomMetadataValue customMetadataValue = metadata.getCustomValue("key_search_result");
                    // Verificar si el valor personalizado es nulo
                    if (customMetadataValue != null) {
                        // Obtener el objeto SearchResultMetadata
                        SearchResultMetadata searchResultMetadata = (SearchResultMetadata) customMetadataValue;
                        // Obtener el título y las coordenadas del lugar
                        String title = searchResultMetadata.searchResult.getTitle();
                        // Obtener las coordenadas del lugar
                        double latitude = searchResultMetadata.searchResult.getGeoCoordinates().latitude;
                        double longitude = searchResultMetadata.searchResult.getGeoCoordinates().longitude;
                        // Mostrar el título y las coordenadas en un diálogo
                        showDialog("Direccion del lugar: ",title+"\n"+"Coordenadas: "+latitude+","+longitude);
                        return;
                    }
                }
                // Mostrar un diálogo con las coordenadas del MapMarker seleccionado
                showDialog("Picked Map Marker",
                        "Coordenadas Geometricas: " +
                                topmostMapMarker.getCoordinates().latitude + ", " +
                                topmostMapMarker.getCoordinates().longitude);
            }
        });
    }

    private void searchInViewport(String queryString) {
        // Clear the map.
        clearMap();
        // Get the viewport GeoBox.
        GeoBox viewportGeoBox = getMapViewGeoBox();
        // Create a TextArea.
        TextQuery.Area queryArea = new TextQuery.Area(viewportGeoBox);
        // Create a TextQuery.
        TextQuery query = new TextQuery(queryString,queryArea);
        // Search for the query.
        SearchOptions searchOptions = new SearchOptions();
        // Set the language to English.
        searchOptions.languageCode = LanguageCode.EN_US;
        // Set the maximum number of results to 30.
        searchOptions.maxItems = 30;
        // Perform the search.
        searchEngine.search(query, searchOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                // If error is not null, show an error message.
                if (searchError != null) {
                    showDialog("Busqueda", "Error: Lugar no encontrado");
                    return;
                }
                // If error is null, list is guaranteed to be not empty.
                // Add new marker for each search result on map.
                for (Place searchResult : list) {
                    Metadata metadata = new Metadata();
                    metadata.setCustomValue("key_search_result", new SearchResultMetadata(searchResult));
                    addPoiMapMarker(searchResult.getGeoCoordinates(), metadata);
                }
                // Obtener las coordenadas de la primera ubicación de la lista
                Place firstPlace = list.get(0);
                GeoCoordinates targetCoordinates = firstPlace.getGeoCoordinates();
                // Move map to expected location.
                flyTo(targetCoordinates);
                // Establecer las coordenadas de la primera ubicación como objetivo de la cámara
                //camera.setTarget(targetCoordinates);
                /*camera.setZoomLevel(14); // Opcional: ajustar el nivel de zoom según sea necesario*/
                //animateZoom(14);
            }
        });
    }

    private static GeoBox viewportGeoBox(GeoBox viewportGeoBox) {
        // Get the viewport GeoBox.
        return viewportGeoBox;
    }

    public static class SearchResultMetadata implements CustomMetadataValue {
        // Define el objeto Place
        public final Place searchResult;
        // Define el constructor de la clase
        public SearchResultMetadata(Place searchResult) {
            this.searchResult = searchResult;
        }
        @NonNull
        @Override
        public String getTag() {
            return "SearchResult Metadata";
        }
    }

    private void addPoiMapMarkerPuntero(GeoCoordinates geoCoordinates, Metadata metadata) {
        // Clear the map.
        clearMap1();
        // Add new marker for each search result on map.
        MapMarker mapMarker = createPoiMapMarker1(geoCoordinates);
        // Set the metadata for the MapMarker
        mapMarker.setMetadata(metadata);
        // Add the MapMarker to the map
        mapMarkerList1.add(mapMarker);
        // Add the MapMarker to the list1 of map markers
        mapView.getMapScene().addMapMarker(mapMarker);
    }

    public void addPoiMapMarkerUbicacion(GeoCoordinates geoCoordinates) {
        // Clear the map.
        clearMap2();
        // Add new marker for each search result on map.
        MapMarker mapMarker = createPoiMapMarker2(geoCoordinates);
        // Add the MapMarker to the map
        mapView.getMapScene().addMapMarker(mapMarker);
        // Add the MapMarker to the list2 of map markers
        mapMarkerList2.add(mapMarker);
    }

    private void addPoiMapMarker(GeoCoordinates geoCoordinates, Metadata metadata) {
        // Create a MapMarker
        MapMarker mapMarker = createPoiMapMarker(geoCoordinates);
        // Set the metadata for the MapMarker
        mapMarker.setMetadata(metadata);
        // Add the MapMarker to the map
        mapView.getMapScene().addMapMarker(mapMarker);
        // Add the MapMarker to the list of map markers
        mapMarkerList.add(mapMarker);
    }

    private MapMarker createPoiMapMarker(GeoCoordinates geoCoordinates) {
        // Create a MapMarker
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi);
        MapMarker mapMarker = new MapMarker(geoCoordinates,mapImage);
        //MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
        //mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1));
        //mapMarker.addImage(mapImage, mapMarkerImageStyle);
        return mapMarker;
    }

    private MapMarker createPoiMapMarker1(GeoCoordinates geoCoordinates) {
        // Create a MapMarker
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi1);
        MapMarker mapMarker = new MapMarker(geoCoordinates,mapImage);
        //MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
        //mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1));
        //mapMarker.addImage(mapImage, mapMarkerImageStyle);
        return mapMarker;
    }

    public MapMarker createPoiMapMarker2(GeoCoordinates geoCoordinates) {
        // Create a MapMarker
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi2);
        MapMarker mapMarker = new MapMarker(geoCoordinates,mapImage);
        //MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
        //mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1));
        //mapMarker.addImage(mapImage, mapMarkerImageStyle);
        return mapMarker;
    }

    private GeoCoordinates getMapViewCenter() {
        // Get the center of the map.
        return mapView.getCamera().getState().targetCoordinates;
    }

    private GeoBox getMapViewGeoBox() {
        //return mapView.getCamera().getBoundingBox();
        // Obtener la bounding box del mapa a nivel global
        GeoBox mundialGeoBox = new GeoBox(
                new GeoCoordinates(-90, -180),
                new GeoCoordinates(90, 180)
        );
        return mundialGeoBox;
    }

    public void clearMap(){
        // Eliminar los MapMarker del mapa uno por uno
        for (MapMarker mapMarker : mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        // Limpiar la lista
        mapMarkerList.clear();
    }
    public void clearAll(){
        // Eliminar los MapMarker del mapa uno por uno
        clearMap();
        clearMap1();
        clearMap2();
    }

    public void clearMap1(){
        // Eliminar los MapMarker del mapa uno por uno
        for (MapMarker mapMarker : mapMarkerList1) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        // Limpiar la lista
        mapMarkerList1.clear();
    }

    public void clearMap2(){
        // Eliminar los MapMarker del mapa uno por uno
        for (MapMarker mapMarker : mapMarkerList2) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        // Limpiar la lista
        mapMarkerList2.clear();
    }

    private void showDialog(String title, String message) {
        // Crear el diálogo
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        // Establecer el título y el mensaje
        builder.setTitle(title);
        builder.setMessage(message);
        // Mostrar el diálogo
        builder.show();
    }
    private void showAddressDialog(String estado, String ciudad, String colonia, String codigo_postal, String calle, String numero) {
        // Crear el diálogo
        Dialog dialog = new Dialog(context);
        // Establecer el contenido del diálogo
        dialog.setContentView(R.layout.dialog_address);
        // Establecer el título del diálogo
        dialog.setTitle("Dirección");
        // Obtener los elementos del diálogo
        TextView textViewEstado = dialog.findViewById(R.id.textViewEstado);
        TextView textViewCiudad = dialog.findViewById(R.id.textViewCiudad);
        TextView textViewColonia = dialog.findViewById(R.id.textViewColonia);
        TextView textViewCodigoPostal = dialog.findViewById(R.id.textViewCodigoPostal);
        TextView textViewCalle = dialog.findViewById(R.id.textViewCalle);
        TextView textViewNumero = dialog.findViewById(R.id.textViewNumero);
        // Establecer los valores en los elementos del diálogo
        textViewEstado.setText("Estado: " + estado);
        textViewCiudad.setText("Ciudad: " + ciudad);
        textViewColonia.setText("Colonia: " + colonia);
        textViewCodigoPostal.setText("Código Postal: " + codigo_postal);
        textViewCalle.setText("Calle: " + calle);
        textViewNumero.setText("Número: " + numero);
        // Mostrar el diálogo
        dialog.show();
    }

    // Método para animar el zoom suavemente
    private void animateZoom(double targetZoomLevel) {
        // Obtener el nivel de zoom actual
        double currentZoomLevel = camera.getState().zoomLevel;
        // Crear un objeto ValueAnimator para animar el zoom
        ValueAnimator animator = ValueAnimator.ofFloat((float) currentZoomLevel, (float) targetZoomLevel);
        // Establecer la duración de la animación en milisegundos
        animator.setDuration(zoomAnimationDuration);
        // Establecer el listener para el valor animado
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Obtener el valor actual del valor animado
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                // Establecer el nivel de zoom
                camera.setDistanceToTarget(animatedValue);
            }
        });
        // Iniciar la animación
        animator.start();
    }
    private void flyTo(GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, 5000.0);
        double bowFactor = 1;
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(
                geoCoordinatesUpdate, mapMeasureZoom, bowFactor, Duration.ofSeconds(3));
        camera.startAnimation(animation);
    }
}
