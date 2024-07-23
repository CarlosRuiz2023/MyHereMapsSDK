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
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hackaprende.myheremapssdk.interfaces.GeoCoordinatesCallback;
import com.hackaprende.myheremapssdk.R;
import com.here.sdk.core.CustomMetadataValue;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.Camera;
import com.here.sdk.mapviewlite.MapImage;
import com.here.sdk.mapviewlite.MapImageFactory;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapMarkerImageStyle;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.PixelFormat;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.TextQuery;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class RoutingExample {
    // TAG de la clase
    private static final String TAG = RoutingExample.class.getName();
    // Definición del contexto
    private Context context;
    // Definición de la vista del mapa
    private MapViewLite mapView;
    // Definición de la lista de marcadores de mapa
    private final List<MapMarker> mapMarkerList = new ArrayList<>();
    // Definición de la lista de polilíneas de mapa
    private final List<MapPolyline> mapPolylines = new ArrayList<>();
    // Definición del motor de rutas
    private RoutingEngine routingEngine;
    // Definición de las coordenadas de inicio y destino
    private GeoCoordinates startGeoCoordinates;
    private GeoCoordinates destinationGeoCoordinates;
    // Definición de los widgets de entrada de texto
    EditText direccion1;
    EditText direccion2;
    // Definición del motor de búsqueda
    private SearchEngine searchEngine;
    // Definición de la clase SearchExample
    private SearchExample searchExample;
    // Definición de la cámara del mapa
    private final Camera camara;
    // Define la duración de la animación de zoom en milisegundos
    final long zoomAnimationDuration = 1000L;
    // Constructor de la clase
    public RoutingExample(Context context, MapViewLite mapView, SearchExample searchExample) {
        // Inicialización del contexto,la vista del mapa y la clase de searchExample
        this.context = context;
        this.mapView = mapView;
        this.searchExample = searchExample;
        // Creamos una Activity mediante el contexto
        Activity activity = (Activity) context;
        // Inicialización de los widgets de entrada de texto
        direccion1 = activity.findViewById(R.id.direccion1);
        direccion2 = activity.findViewById(R.id.direccion2);
        // Inicialización de la cámara del mapa
        camara = mapView.getCamera();
        try {
            // Inicialización del motor de rutas
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            // Manejo de la excepción de inicialización
            throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
        try {
            // Inicialización del motor de búsqueda
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            // Manejo de la excepción de inicialización
            throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
        }
    }

    public void addRoute() {
        // Limpia los marcadores y las rutas anteriores
        clearMap();
        searchExample.clearMap();
        // Obtenemos el texto de los widgets de entrada de texto
        String address1 = direccion1.getText().toString();
        String address2 = direccion2.getText().toString();
        // Verificamos que los textos no estén vacíos
        if (address1.length()>0) {
            if (address2.length()>0) {
                // First search for the start coordinates
                searchInViewport1(address1, new GeoCoordinatesCallback() {
                    @Override
                    public void onCoordinatesFound(GeoCoordinates startCoordinates, GeoCoordinates destinationCoordinates) {
                        // If start coordinates are found, search for the destination coordinates
                        if (startCoordinates != null) {
                            // Then search for the destination coordinates
                            searchInViewport2(address2, new GeoCoordinatesCallback() {
                                @Override
                                public void onCoordinatesFound(GeoCoordinates startCoordinates, GeoCoordinates destinationCoordinates) {
                                    // Both coordinates are now available, proceed with routing
                                    if (destinationCoordinates != null) {
                                        // Create Waypoints
                                        Waypoint startWaypoint = new Waypoint(startCoordinates);
                                        Waypoint destinationWaypoint = new Waypoint(destinationCoordinates);
                                        // Create a list of Waypoints
                                        List<Waypoint> waypoints = new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));
                                        // Calculate the route
                                        routingEngine.calculateRoute(
                                                waypoints,
                                                new CarOptions(),
                                                new CalculateRouteCallback() {
                                                    @Override
                                                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                                                        // Check if route calculation was successful
                                                        if (routingError == null) {
                                                            // Get the first route from the list
                                                            Route route = routes.get(0);
                                                            // Show route details
                                                            showRouteDetails(route);
                                                            // Show route on map
                                                            showRouteOnMap(route);
                                                            // Calcular la extensión de la ruta
                                                            GeoBox geoBox = calculateRouteGeoBox(route);
                                                            // Calcular el centro del GeoBox
                                                            GeoCoordinates center = calculateGeoBoxCenter(geoBox);
                                                            // Configurar la cámara para que se ajuste a la extensión de la ruta
                                                            camara.setTarget(center);
                                                            // Ajustar el nivel de zoom para que toda la ruta sea visible en el mapa
                                                            /*camara.setZoomLevel(adjustZoomLevel(geoBox));*/
                                                            animateZoom(adjustZoomLevel(geoBox));
                                                        } else {
                                                            // Show error message
                                                            showDialog("Error mientras se calculaba la ruta:", routingError.toString());
                                                        }
                                                    }
                                                });
                                    } else {
                                        // Show error message
                                        showDialog("Error", "No se localizaron las coordenadas de la direccion 2 (Sea mas especifico).");
                                    }
                                }
                            });
                        } else {
                            // Show error message
                            showDialog("Error", "No se localizaron las coordenadas de la direccion 1 (Sea mas especifico).");
                        }
                    }
                });
            } else {
                // Show error message
                Toast.makeText(context, "La direccion2 esta vacia", Toast.LENGTH_SHORT).show();
            }
        }else{
            // Show error message
            Toast.makeText(context, "La direccion1 esta vacia", Toast.LENGTH_SHORT).show();
        }
    }
    private void showRouteDetails(Route route) {
        // Tomamos el tiempo y la distancia de la ruta
        long estimatedTravelTimeInSeconds = route.getDuration().getSeconds();
        int lengthInMeters = route.getLengthInMeters();
        // Creamos un string para mostrar los detalles de la ruta
        String routeDetails =
                "Tiempo: " + formatTime(estimatedTravelTimeInSeconds)
                + ", Distancia: " + formatLength(lengthInMeters);
        // Mostramos los detalles de la ruta
        showDialog("Detalles de la ruta", routeDetails);
    }

    private String formatTime(long sec) {
        // Convertimos los segundos en horas y minutos
        long hours = sec / 3600;
        long minutes = (sec % 3600) / 60;
        // Devolvemos el string formateado
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    private String formatLength(int meters) {
        // Validamos si los metros son mayores a 1000
        if (meters > 1000) {
            // Declaramos las formulas que se usaran en sus conversiones
            int kilometers = meters / 1000;
            int remainingMeters = meters % 1000;
            // Devolvemos el string formateado
            return String.format(Locale.getDefault(), "%02d.%02d km", kilometers, remainingMeters);
        }
        // Si no, solo devolvemos los metros
        int kilometers = meters;
        // Devolvemos el string formateado
        return String.format(Locale.getDefault(), "%02d m", kilometers);
    }

    private void showRouteOnMap(Route route) {
        // Tomamos el GeoPolyline de la ruta
        GeoPolyline routeGeoPolyline = route.getGeometry();
        // Creamos un MapPolylineStyle para personalizar la apariencia de la polilínea
        MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
        // Establecemos el color y el ancho de la polilínea
        mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
        // Establecemos el ancho de la polilínea
        mapPolylineStyle.setWidthInPixels(10);
        // Creamos un MapPolyline para mostrar la polilínea en el mapa
        MapPolyline routeMapPolyline = new MapPolyline(routeGeoPolyline, mapPolylineStyle);
        // Agregamos la polilínea al mapa
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        // Agregamos la polilínea al listado de polilíneas
        mapPolylines.add(routeMapPolyline);
        // Tomamos los puntos de inicio y fin de la ruta
        GeoCoordinates startPoint =
                route.getSections().get(0).getDeparturePlace().mapMatchedCoordinates;
        GeoCoordinates destination =
                route.getSections().get(route.getSections().size() - 1).getArrivalPlace().mapMatchedCoordinates;
        // Draw a circle to indicate starting point and destination.
        addCircleMapMarker(startPoint, R.drawable.green_dot);
        addCircleMapMarker(destination, R.drawable.red_dot);
        // Log maneuver instructions per route section.
        List<Section> sections = route.getSections();
        // Iteramos sobre las secciones de la ruta
        for (Section section : sections) {
            // Mostramos las instrucciones de manejo de la ruta
            logManeuverInstructions(section);
        }
        // Iniciar la simulación de navegación
        // TODO:
    }

    private void logManeuverInstructions(Section section) {
        // Tomamos las instrucciones de manejo de la sección de la ruta
        List<Maneuver> maneuverInstructions = section.getManeuvers();
        // Iteramos sobre las instrucciones de manejo de la sección
        for (Maneuver maneuverInstruction : maneuverInstructions) {
            // Mostramos las instrucciones de manejo de la sección
            ManeuverAction maneuverAction = maneuverInstruction.getAction();
            // Tomamos las coordenadas de la ubicación de la instrucción de manejo
            GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
            // Mostramos las coordenadas de la ubicación de la instrucción de manejo
            String maneuverInfo = maneuverInstruction.getText()
                    + ", Action: " + maneuverAction.name()
                    + ", Location: " + maneuverLocation.toString();
            // Mostramos las instrucciones de manejo de la sección
            Log.d(TAG, maneuverInfo);
        }
    }

    /*public void addWaypoints() {
        if (startGeoCoordinates == null || destinationGeoCoordinates == null) {
            showDialog("Error", "Please add a route first.");
            return;
        }

        // Inserting stopover waypoints.
        Waypoint waypoint1 = new Waypoint(createRandomGeoCoordinatesInViewport());
        Waypoint waypoint2 = new Waypoint(createRandomGeoCoordinatesInViewport());
        List<Waypoint> waypoints = new ArrayList<>(Arrays.asList(new Waypoint(startGeoCoordinates),
                waypoint1, waypoint2, new Waypoint(destinationGeoCoordinates)));

        routingEngine.calculateRoute(
                waypoints,
                new CarOptions(),
                new CalculateRouteCallback() {
                    @Override
                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                        if (routingError == null) {
                            Route route = routes.get(0);
                            showRouteDetails(route);
                            showRouteOnMap(route);

                            // Draw a circle to indicate the location of the stopover waypoints.
                            addCircleMapMarker(waypoint1.coordinates, R.drawable.red_dot);
                            addCircleMapMarker(waypoint2.coordinates, R.drawable.red_dot);
                        } else {
                            showDialog("Error while calculating a route:", routingError.toString());
                        }
                    }
                });
    }*/

    public void clearMap() {
        // Limpia los marcadores y las rutas anteriores
        clearWaypointMapMarker();
        clearRoute();
    }

    private void clearWaypointMapMarker() {
        // Limpia los marcadores dewaypoints
        for (MapMarker mapMarker : mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        // Limpia la lista de marcadores
        mapMarkerList.clear();
    }

    private void clearRoute() {
        // Limpia las rutas anteriores
        for (MapPolyline mapPolyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        }
        // Limpia la lista de rutas
        mapPolylines.clear();
    }

    /*private GeoCoordinates createRandomGeoCoordinatesInViewport() {
        GeoBox geoBox = mapView.getCamera().getBoundingBox();
        GeoCoordinates northEast = geoBox.northEastCorner;
        GeoCoordinates southWest = geoBox.southWestCorner;

        double minLat = southWest.latitude;
        double maxLat = northEast.latitude;
        double lat = getRandom(minLat, maxLat);

        double minLon = southWest.longitude;
        double maxLon = northEast.longitude;
        double lon = getRandom(minLon, maxLon);

        return new GeoCoordinates(lat, lon);
    }*/

    /*private double getRandom(double min, double max) {
        return min + Math.random() * (max - min);
    }*/

    private void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates);
        mapMarker.addImage(mapImage, new MapMarkerImageStyle());
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerList.add(mapMarker);
    }

    private void showDialog(String title, String message) {
        // Create a new AlertDialog builder
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        // Set the title and message for the AlertDialog
        builder.setTitle(title);
        // Set the message for the AlertDialog
        builder.setMessage(message);
        // Show the AlertDialog
        builder.show();
    }

    private void searchInViewport1(String queryString, GeoCoordinatesCallback callback) {
        // Obtenemos el GeoBox del mapa a nivel internacional
        GeoBox viewportGeoBox = getMapViewGeoBox();
        // Creamos un TextQuery.Area para el GeoBox
        TextQuery.Area queryArea = new TextQuery.Area(viewportGeoBox);
        // Creamos un TextQuery para la búsqueda
        TextQuery query = new TextQuery(queryString, queryArea);
        // Creamos las opciones de búsqueda
        SearchOptions searchOptions = new SearchOptions();
        // Establecemos el idioma y el número máximo de resultados
        searchOptions.languageCode = LanguageCode.EN_US;
        // Definimos el número máximo de resultados a buscar
        searchOptions.maxItems = 30;
        // Ejecutamos la búsqueda
        searchEngine.search(query, searchOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                // Verificamos si hay un error en la búsqueda
                if (searchError != null) {
                    // Si hay un error, mostramos un mensaje de error
                    showDialog("Busqueda", "Error: Una de las direcciones no se encontro.");
                    // Llamamos al método de devolución de llamada para notificar el error
                    callback.onCoordinatesFound(null, destinationGeoCoordinates); // Notify even on error
                    return;
                }
                // Si no hay un error, verificamos si se encontraron resultados
                if (list != null && !list.isEmpty()) {
                    // Si se encontraron resultados, obtenemos el primer resultado
                    Place firstPlace = list.get(0);
                    // Obtenemos las coordenadas de los resultados
                    startGeoCoordinates = firstPlace.getGeoCoordinates();
                    // Llamamos al método de devolución de llamada para notificar las coordenadas
                    callback.onCoordinatesFound(startGeoCoordinates, destinationGeoCoordinates);
                } else {
                    // Si no se encontraron resultados, llamamos al método de devolución de llamada para notificar las coordenadas
                    callback.onCoordinatesFound(null, destinationGeoCoordinates);
                }
            }
        });
    }

    private void searchInViewport2(String queryString, GeoCoordinatesCallback callback) {
        // Obtenemos el GeoBox del mapa a nivel internacional
        GeoBox viewportGeoBox = getMapViewGeoBox();
        // Creamos un TextQuery.Area para el GeoBox
        TextQuery.Area queryArea = new TextQuery.Area(viewportGeoBox);
        // Creamos un TextQuery para la búsqueda
        TextQuery query = new TextQuery(queryString, queryArea);
        // Creamos las opciones de búsqueda
        SearchOptions searchOptions = new SearchOptions();
        // Establecemos el idioma y el número máximo de resultados
        searchOptions.languageCode = LanguageCode.EN_US;
        // Definimos el número máximo de resultados a buscar
        searchOptions.maxItems = 30;
        // Ejecutamos la búsqueda
        searchEngine.search(query, searchOptions, new SearchCallback() {
            @Override
            public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                // Verificamos si hay un error en la búsqueda
                if (searchError != null) {
                    // Si hay un error, mostramos un mensaje de error
                    showDialog("Search", "Error: " + searchError.toString());
                    // Llamamos al método de devolución de llamada para notificar el error
                    callback.onCoordinatesFound(startGeoCoordinates, null); // Notify even on error
                    return;
                }
                // Si no hay un error, verificamos si se encontraron resultados
                if (list != null && !list.isEmpty()) {
                    // Si se encontraron resultados, obtenemos el primer resultado
                    Place firstPlace = list.get(0);
                    // Obtenemos las coordenadas de los resultados
                    destinationGeoCoordinates = firstPlace.getGeoCoordinates();
                    // Llamamos al método de devolución de llamada para notificar las coordenadas
                    callback.onCoordinatesFound(startGeoCoordinates, destinationGeoCoordinates);
                } else {
                    // Si no se encontraron resultados, llamamos al método de devolución de llamada para notificar las coordenadas
                    callback.onCoordinatesFound(startGeoCoordinates, null);
                }
            }
        });
    }

    private static class SearchResultMetadata implements CustomMetadataValue {
        // Definimos una variable de tipo Place para almacenar el resultado de la búsqueda
        public final Place searchResult;
        // Constructor de la clase
        public SearchResultMetadata(Place searchResult) {
            this.searchResult = searchResult;
        }
        @NonNull
        @Override
        public String getTag() {
            // Devolvemos el nombre del tag
            return "SearchResult Metadata";
        }
    }
    private GeoBox getMapViewGeoBox() {
        //return mapView.getCamera().getBoundingBox();
        // GeoBox mundial
        GeoBox mundialGeoBox = new GeoBox(
                new GeoCoordinates(-90, -180),
                new GeoCoordinates(90, 180)
        );
        return mundialGeoBox;
    }
    /*private void addPoiMapMarker(GeoCoordinates geoCoordinates) {
        MapMarker mapMarker = createPoiMapMarker(geoCoordinates);
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkerList.add(mapMarker);
    }

    private MapMarker createPoiMapMarker(GeoCoordinates geoCoordinates) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), R.drawable.poi);
        MapMarker mapMarker = new MapMarker(geoCoordinates);
        MapMarkerImageStyle mapMarkerImageStyle = new MapMarkerImageStyle();
        mapMarkerImageStyle.setAnchorPoint(new Anchor2D(0.5F, 1));
        mapMarker.addImage(mapImage, mapMarkerImageStyle);
        return mapMarker;
    }*/
    private GeoBox calculateRouteGeoBox(Route route) {
        // Tomamos los puntos de inicio y fin de la ruta
        List<GeoCoordinates> coordinates = route.getGeometry().vertices;
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        // Iteramos sobre los puntos de inicio y fin de la ruta
        for (GeoCoordinates coord : coordinates) {
            if (coord.latitude < minLat) {
                minLat = coord.latitude;
            }
            if (coord.latitude > maxLat) {
                maxLat = coord.latitude;
            }
            if (coord.longitude < minLon) {
                minLon = coord.longitude;
            }
            if (coord.longitude > maxLon) {
                maxLon = coord.longitude;
            }
        }
        // Devolvemos el GeoBox calculado
        return new GeoBox(
                new GeoCoordinates(minLat, minLon),
                new GeoCoordinates(maxLat, maxLon)
        );
    }
    private GeoCoordinates calculateGeoBoxCenter(GeoBox geoBox) {
        // Calcular el centro del GeoBox
        double centerLat = (geoBox.northEastCorner.latitude + geoBox.southWestCorner.latitude) / 2;
        double centerLon = (geoBox.northEastCorner.longitude + geoBox.southWestCorner.longitude) / 2;
        // Devolver el centro del GeoBox
        return new GeoCoordinates(centerLat, centerLon);
    }
    private double adjustZoomLevel(GeoBox geoBox) {
        // Ejemplo de cálculo basado en la latitud y longitud del GeoBox
        double latDiff = geoBox.northEastCorner.latitude - geoBox.southWestCorner.latitude;
        double lonDiff = geoBox.northEastCorner.longitude - geoBox.southWestCorner.longitude;
        // La siguiente línea es un ejemplo y necesitarás ajustarla según cómo tu API maneje el zoom
        double maxDiff = Math.max(latDiff, lonDiff);
        // Calcular el nivel de zoom basado en el tamaño del GeoBox
        double zoomLevel = (Math.log(360 / maxDiff) / Math.log(2))-0.5;
        // Suponiendo que tu API de mapas tiene un método para ajustar el nivel de zoom basado en una GeoBox
        return zoomLevel;
    }
    // Método para animar el zoom suavemente
    private void animateZoom(double targetZoomLevel) {
        // Toma el nivel de zoom actual
        double currentZoomLevel = camara.getZoomLevel();
        // Genera una animación de valor para el nivel de zoom del actual al que deberia llegar segun la animacion
        ValueAnimator animator = ValueAnimator.ofFloat((float) currentZoomLevel, (float) targetZoomLevel);
        // Establece la duración de la animación en milisegundos
        animator.setDuration(zoomAnimationDuration);
        // Añadimos un oyente en la animación para actualizar del nivel de zoom
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Toma el valor actual de la animación
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                // Establece el nuevo nivel de zoom
                camara.setZoomLevel(animatedValue);
            }
        });
        // Inicia la animación
        animator.start();
    }
}
