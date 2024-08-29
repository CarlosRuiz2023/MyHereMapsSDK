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
import android.media.MediaRouter2;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hackaprende.myheremapssdk.interfaces.GeoCoordinatesCallback;
import com.hackaprende.myheremapssdk.R;
import com.here.sdk.core.Color;
import com.here.sdk.core.CountryCode;
import com.here.sdk.core.CustomMetadataValue;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.LocationListener;
import com.here.sdk.core.Metadata;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapView;
import com.here.sdk.mapview.RenderSize;
import com.here.sdk.navigation.EventText;
import com.here.sdk.navigation.EventTextListener;
import com.here.sdk.navigation.LocationSimulator;
import com.here.sdk.navigation.LocationSimulatorOptions;
import com.here.sdk.navigation.VisualNavigator;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.CarOptions;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.PaymentMethod;
import com.here.sdk.routing.RoadFeatures;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteRailwayCrossing;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.SectionNotice;
import com.here.sdk.routing.SegmentReference;
import com.here.sdk.routing.Span;
import com.here.sdk.routing.Toll;
import com.here.sdk.routing.TollFare;
import com.here.sdk.routing.TrafficSpeed;
import com.here.sdk.routing.TravelDirection;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.routing.ZoneCategory;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.TextQuery;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private MapView mapView;
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
    LinearLayout navigatorLayout, routeLayout;
    Route route;
    // Definición del motor de búsqueda
    private SearchEngine searchEngine;
    private LocationSimulator locationSimulator;
    private VisualNavigator visualNavigator;
    // Definición de la clase SearchExample
    private SearchExample searchExample;
    private NavigationExample navigationExample;
    // Definición de la cámara del mapa
    private final MapCamera camara;
    // Define la duración de la animación de zoom en milisegundos
    final long zoomAnimationDuration = 1000L;
    private TextView messageView;
    private boolean isCameraTrackingEnabled = true;
    private Button releaseCameraButton, stopSimulationButton;
    private ImageView turnIcon;

    // Constructor de la clase
    public RoutingExample(Context context, MapView mapView, SearchExample searchExample) {
        // Inicialización del contexto,la vista del mapa y la clase de searchExample
        this.context = context;
        this.mapView = mapView;
        this.searchExample = searchExample;
        // Creamos una Activity mediante el contexto
        Activity activity = (Activity) context;
        // Inicialización de los widgets de entrada de texto
        direccion1 = activity.findViewById(R.id.direccion1);
        direccion2 = activity.findViewById(R.id.direccion2);
        navigatorLayout = activity.findViewById(R.id.navigatorLayout);
        routeLayout = activity.findViewById(R.id.routeLayout);
        messageView = activity.findViewById(R.id.message_view);
        releaseCameraButton = activity.findViewById(R.id.releaseCameraButton);
        stopSimulationButton = activity.findViewById(R.id.stopSimulationButton);
        turnIcon = activity.findViewById(R.id.turnIcon);
        // Inicialización de la cámara del mapa
        camara = mapView.getCamera();
        try {
            // Inicialización del motor de rutas
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            // Manejo de la excepción de inicialización
            //throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
        try {
            // Inicialización del motor de búsqueda
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            // Manejo de la excepción de inicialización
            //throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
        }
        try {
            // Inicialización del motor de búsqueda
            navigationExample = new NavigationExample(context, mapView, messageView, turnIcon, this);
        } catch (Exception e) {
            // Manejo de la excepción de inicialización
            //throw new RuntimeException("Initialization of SearchEngine failed: " + e.name());
        }
        releaseCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTrackingButtonClicked();
            }
        });
        stopSimulationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detach();
            }
        });
    }

    public Route getRoute() {
        return route;
    }

    public void toggleTrackingButtonClicked() {
        if (isCameraTrackingEnabled) {
            navigationExample.stopCameraTracking();
            isCameraTrackingEnabled = false;
        } else {
            navigationExample.startCameraTracking();
            isCameraTrackingEnabled = true;
        }
    }

    public void detach() {
        clearMap();
        // Disables TBT guidance (if running) and enters tracking mode.
        navigationExample.stopNavigation(false);
        navigationExample.stopRendering();
        // Hides the navigator layout.
        navigatorLayout.setVisibility(View.GONE);
        // Shows the route layout.
        routeLayout.setVisibility(View.VISIBLE);
    }

    public void addRoute(List<MapPolygon> poligonos, List<GeoCoordinates> puntos) {
        // Limpia los marcadores y las rutas anteriores
        clearMap();
        searchExample.clearMap();
        // Obtenemos el texto de los widgets de entrada de texto
        String address1 = direccion1.getText().toString();
        String address2 = direccion2.getText().toString();
        // Verificamos que los textos no estén vacíos
        if (address1.length() > 0) {
            if (address2.length() > 0) {
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
                                        List<Waypoint> waypoints = new ArrayList<>();
                                        if (puntos.size() > 0) {
                                            waypoints.add(startWaypoint);
                                            List<GeoCoordinates> ruta = new ArrayList<>(Arrays.asList(startCoordinates, destinationCoordinates));
                                            GeoPolyline routeLine = null;
                                            try {
                                                routeLine = new GeoPolyline(ruta);
                                            } catch (InstantiationErrorException e) {
                                                //throw new RuntimeException(e);
                                            }
                                            // Calcula el punto intermedio
                                            GeoCoordinates midPoint = new GeoCoordinates(
                                                    (startCoordinates.latitude + destinationCoordinates.latitude) / 2,
                                                    (startCoordinates.longitude + destinationCoordinates.longitude) / 2
                                            );
                                            // ...
                                            double routeLength = startCoordinates.distanceTo(destinationCoordinates);
                                            int intermediatePointsCount = (int) Math.round(routeLength / 5000);
                                            double threshold = routeLength * 0.5; // 50% de la longitud de la ruta
                                            if(threshold > 10000){
                                                threshold = 10000;
                                            }

                                            // Obtiene puntos intermedios
                                            List<GeoCoordinates> intermediatePoints = getIntermediatePoints(routeLine, intermediatePointsCount ,routeLength); // Divide la ruta en 3 segmentos

                                            // Crea una lista de pares (waypoint, distancia)
                                            List<Pair<Waypoint, Double>> waypointDistances = new ArrayList<>();
                                            for (GeoCoordinates punto : puntos) {
                                                addCircleMapMarker(punto, R.drawable.red_dot);
                                                double distanceToStart = punto.distanceTo(startGeoCoordinates);
                                                double distanceToDestination = punto.distanceTo(destinationGeoCoordinates);
                                                if (intermediatePoints.size() > 0){
                                                    for (GeoCoordinates intermediatePoint : intermediatePoints) {
                                                        double distanceToIntermediate = punto.distanceTo(intermediatePoint);
                                                        if (distanceToIntermediate <= threshold || distanceToStart <= threshold || distanceToDestination <= threshold) {
                                                            waypointDistances.add(new Pair<>(new Waypoint(punto), distanceToStart));
                                                            break; // Si el punto está cerca de un punto intermedio, no es necesario verificar los demás
                                                        }
                                                    }
                                                }else{
                                                    if (distanceToStart <= threshold || distanceToDestination <= threshold) {
                                                        waypointDistances.add(new Pair<>(new Waypoint(punto), distanceToStart));
                                                        break; // Si el punto está cerca de un punto intermedio, no es necesario verificar los demás
                                                    }
                                                }
                                            }
                                            // Ordena la lista de pares por distancia al punto intermedio
                                            waypointDistances.sort((p1, p2) -> Double.compare(p1.second, p2.second));

                                            // Agrega los waypoints ordenados a la lista final
                                            for (Pair<Waypoint, Double> pair : waypointDistances) {
                                                waypoints.add(pair.first);
                                            }
                                            waypoints.add(destinationWaypoint);
                                        } else {
                                            // Create a list of Waypoints
                                            waypoints = new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));
                                        }
                                        // A route handle is required for the DynamicRoutingEngine to get updates on traffic-optimized routes.
                                        CarOptions routingOptions = new CarOptions();
                                        routingOptions.routeOptions.enableRouteHandle = true;
                                        // Definir las zonas de evitación
                                        // GEOPOLYGON
                                        if (poligonos.size() > 0) {
                                            List<GeoPolygon> avoidanceZones = new ArrayList<>();
                                            for (MapPolygon polygonZone : poligonos) {
                                                avoidanceZones.add(polygonZone.getGeometry());
                                            }
                                            // Agrega las zonas de evitación a las opciones de enrutamiento
                                            routingOptions.avoidanceOptions.avoidPolygonAreas = avoidanceZones;
                                        }
                                        /*// GEOBOX
                                        List<GeoBox> avoidanceZones = new ArrayList<>();

                                        // Define la zona de evitación rectangular (ajusta las coordenadas según sea necesario)
                                        GeoCoordinates topLeft = new GeoCoordinates(21.126205, -101.598454);
                                        GeoCoordinates bottomRight = new GeoCoordinates(21.100375, -101.573880);
                                        GeoBox avoidanceZone = new GeoBox(topLeft, bottomRight);

                                        // Crea una lista de coordenadas para el MapPolygon
                                        List<GeoCoordinates> polygonCoordinates = new ArrayList<>();
                                        polygonCoordinates.add(topLeft);
                                        polygonCoordinates.add(new GeoCoordinates(topLeft.latitude, bottomRight.longitude));
                                        polygonCoordinates.add(bottomRight);
                                        polygonCoordinates.add(new GeoCoordinates(bottomRight.latitude, topLeft.longitude));

                                        // Crea un GeoPolygon a partir de las coordenadas
                                        GeoPolygon geoPolygon = null;
                                        try {
                                            geoPolygon = new GeoPolygon(polygonCoordinates);
                                        } catch (InstantiationErrorException e) {
                                            Log.e("Prueba", "Error al crear la zona de evitación: " + e.error.name());
                                        }

                                        // Crea un MapPolygon con un color de relleno y un ancho de borde
                                        Color fillColor = new Color(0.8f, 0.0f, 0.0f, 0.5f); // Rojo semi-transparente
                                        MapPolygon mapPolygon = new MapPolygon(geoPolygon, fillColor);

                                        // Agrega el MapPolygon a la escena del mapa
                                        mapView.getMapScene().addMapPolygon(mapPolygon);

                                        // Agrega las zonas de evitación a las opciones
                                        avoidanceZones.add(avoidanceZone);

                                        // Agrega las zonas de evitación a las opciones de enrutamiento
                                        routingOptions.avoidanceOptions.avoidBoundingBoxAreas = avoidanceZones;*/
                                        // GEOPOLYGON
                                        /*List<GeoPolygon> avoidanceZones = new ArrayList<>();

                                        // Define la zona de evitación con forma de polígono (ajusta las coordenadas según sea necesario)
                                        List<GeoCoordinates> polygonZone = new ArrayList<>();
                                        GeoCoordinates punto1 = new GeoCoordinates(21.121899, -101.592983);
                                        GeoCoordinates punto2 = new GeoCoordinates(21.114376, -101.589591);
                                        GeoCoordinates punto3 = new GeoCoordinates(21.103029, -101.576860);
                                        polygonZone.add(punto1);
                                        polygonZone.add(punto2);
                                        polygonZone.add(punto3);
                                        GeoPolygon polygonZone1 = null;
                                        try {
                                            polygonZone1 = new GeoPolygon(polygonZone);
                                        } catch (InstantiationErrorException e) {
                                            Log.e("Prueba", "Error al crear la zona de evitación: " + e.error.name());
                                        }
                                        // Crea un objeto MapPolygon con uncolor de relleno y un ancho de borde
                                        Color fillColor = new Color(0.8f, 0.0f, 0.0f, 0.5f); // Rojo semi-transparente
                                        MapPolygon mapPolygon = new MapPolygon(polygonZone1, fillColor);

                                        // Agrega el MapPolygon a la escena del mapa
                                        mapView.getMapScene().addMapPolygon(mapPolygon);
                                        avoidanceZones.add(polygonZone1);

                                        // Agrega las zonas de evitación a las opciones de enrutamiento
                                        routingOptions.avoidanceOptions.avoidPolygonAreas.add(polygonZone1);*/
                                        // ROADFEAUTURES
                                        /**
                                         * CARPOOL_LANE: Carriles para vehículos compartidos.
                                         * CONTROLLED_ACCESS_HIGHWAY: Autopistas de acceso controlado.
                                         * DIRT_ROAD: Caminos de tierra.
                                         * FERRY: Transbordadores.
                                         * HIGHWAY: Autopistas.
                                         * PARK: Parques.
                                         * RAMP: Rampas de acceso o salida de autopistas.
                                         * ROUNDABOUT: Rotondas.
                                         * TOLL_ROAD: Carreteras de peaje.
                                         * TUNNEL: Túneles.
                                         * URBAN: Áreas urbanas.
                                         * */
                                        /*List<RoadFeatures> roadFeaturesToAvoid = new ArrayList<>();
                                        roadFeaturesToAvoid.add(RoadFeatures.FERRY);
                                        roadFeaturesToAvoid.add(RoadFeatures.U_TURNS);
                                        roadFeaturesToAvoid.add(RoadFeatures.SEASONAL_CLOSURE);

                                        // Agrega las características de carretera a las opciones de evitación
                                        routingOptions.avoidanceOptions.roadFeatures = roadFeaturesToAvoid;*/
                                        // ZONECATEGORY
                                        /*List<ZoneCategory>zoneCategoriesToAvoid = new ArrayList<>();
                                        zoneCategoriesToAvoid.add(ZoneCategory.ENVIRONMENTAL);
                                        // ... agrega más categorías de zona que deseas evitar ...

                                        // Agrega las categorías de zona a las opciones de evitación
                                        routingOptions.avoidanceOptions.zoneCategories = zoneCategoriesToAvoid;*/
                                        // COUNTRYCODE
                                        /*List<CountryCode> countriesToAvoid = new ArrayList<>();
                                        countriesToAvoid.add(CountryCode.US);
                                        countriesToAvoid.add(CountryCode.CA);
                                        // ... agrega más países que deseas evitar ...

                                        // Agrega los países a las opciones de evitación
                                        routingOptions.avoidanceOptions.countries = countriesToAvoid;*/
                                        // SEGMENT REFERENCES
                                        /*// Define el nombre de la carretera que deseas evitar
                                        String carreteraAEvita = "Eje Metropolitano Leon Silao"; // Reemplaza con el nombre de la carretera que deseas evitar

                                        // Crea una consulta de texto
                                        TextQuery textQuery = new TextQuery(carreteraAEvita,new TextQuery.Area(new GeoCoordinates(21.107097,-101.581266))); // Ajusta las coordenadas a la ubicación aproximada de la carretera

                                        // Crea opciones de búsqueda
                                        SearchOptions searchOptions = new SearchOptions();
                                        searchOptions.maxItems=1; // Solo necesitamos el primer resultado

                                        // Realiza la búsqueda
                                        searchEngine.search(textQuery, searchOptions, (searchError, items) -> {
                                            if (searchError != null) {
                                                // Maneja el error de búsqueda
                                                Log.e("Error", "Error al buscar la carretera: " + searchError.name());
                                                return;
                                            }

                                            if (items != null && !items.isEmpty()) {
                                                // Obtiene el primer resultado de la búsqueda
                                                Place place = items.get(0);
                                                Log.e("Prueba", "La dirección encontrada es: "+place.getAddress().addressText);

                                                // Obtiene los IDs de segmento de la geometría de la carretera
                                                List<SegmentReference> segmentReferences = obtenerSegmentReferences(place);
                                                routingOptions.avoidanceOptions.segments = segmentReferences;
                                            }
                                        });*/
                                        // Calculate the route
                                        routingEngine.calculateRoute(
                                                waypoints,
                                                routingOptions,
                                                new CalculateRouteCallback() {
                                                    @Override
                                                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                                                        // Check if route calculation was successful
                                                        if (routingError == null) {
                                                            // Get the first route from the list
                                                            route = routes.get(0);
                                                            // Show route on map
                                                            showRouteOnMap(route);
                                                            // Show route details
                                                            showRouteDetails(route, false);
                                                            //logRouteRailwayCrossingDetails(route);
                                                            //logRouteSectionDetails(route);
                                                            //logRouteViolations(route);
                                                            //logTollDetails(route);
                                                            // Calcular la extensión de la ruta
                                                            GeoBox geoBox = calculateRouteGeoBox(route);
                                                            // Calcular el centro del GeoBox
                                                            GeoCoordinates center = calculateGeoBoxCenter(geoBox);
                                                            // Configurar la cámara para que se ajuste a la extensión de la ruta
                                                            //camara.setTarget(center);
                                                            camara.lookAt(geoBox, new GeoOrientationUpdate(center.latitude, center.longitude));
                                                            // Ajustar el nivel de zoom para que toda la ruta sea visible en el mapa
                                                            //camara.setZoomLevel(adjustZoomLevel(geoBox));
                                                            //animateZoom(adjustZoomLevel(geoBox));
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
        } else {
            // Show error message
            Toast.makeText(context, "La direccion1 esta vacia", Toast.LENGTH_SHORT).show();
        }
    }

    public void addRoute(GeoCoordinates startGeoCoordinates, GeoCoordinates destinationGeoCoordinates) {
        // Limpia los marcadores y las rutas anteriores
        clearMap();
        // Disables positioning.
        navigationExample.stopNavigation(false);
        isCameraTrackingEnabled = true;
        // Create Waypoints
        Waypoint startWaypoint = new Waypoint(startGeoCoordinates);
        Waypoint destinationWaypoint = new Waypoint(destinationGeoCoordinates);
        // Create a list of Waypoints
        List<Waypoint> waypoints = new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));
        // A route handle is required for the DynamicRoutingEngine to get updates on traffic-optimized routes.
        CarOptions routingOptions = new CarOptions();
        routingOptions.routeOptions.enableRouteHandle = true;
        // Calculate the route
        routingEngine.calculateRoute(
                waypoints,
                routingOptions,
                new CalculateRouteCallback() {
                    @Override
                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                        if (routingError == null) {
                            route = routes.get(0);
                            // Show route on map
                            showRouteOnMap(route);
                            // Show route details
                            //showRouteDetails(route, false);
                            //logRouteRailwayCrossingDetails(route);
                            //logRouteSectionDetails(route);
                            //logRouteViolations(route);
                            //logTollDetails(route);
                            // Calcular la extensión de la ruta
                            GeoBox geoBox = calculateRouteGeoBox(route);
                            // Calcular el centro del GeoBox
                            GeoCoordinates center = calculateGeoBoxCenter(geoBox);
                            // Configurar la cámara para que se ajuste a la extensión de la ruta
                            //camara.setTarget(center);
                            camara.lookAt(geoBox, new GeoOrientationUpdate(center.latitude, center.longitude));
                            navigationExample.startNavigation(route, false, isCameraTrackingEnabled);
                        } else {
                            Toast.makeText(context, "No se pudo recalcular la ruta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logRouteRailwayCrossingDetails(Route route) {
        for (RouteRailwayCrossing routeRailwayCrossing : route.getRailwayCrossings()) {
            // Coordinates of the route offset
            GeoCoordinates routeOffsetCoordinates = routeRailwayCrossing.coordinates;
            // Index of the corresponding route section. The start of the section indicates the start of the offset.
            int routeOffsetSectionIndex = routeRailwayCrossing.routeOffset.sectionIndex;
            // Offset from the start of the specified section to the specified location along the route.
            double routeOffsetInMeters = routeRailwayCrossing.routeOffset.offsetInMeters;

            Log.d(TAG, "A railway crossing of type " + routeRailwayCrossing.type.name() +
                    "is situated " +
                    routeOffsetInMeters + " m away from start of section: " +
                    routeOffsetSectionIndex);
        }
    }

    private void logRouteSectionDetails(Route route) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < route.getSections().size(); i++) {
            Section section = route.getSections().get(i);

            Log.d(TAG, "Route Section : " + (i + 1));
            Log.d(TAG, "Route Section Departure Time : "
                    + dateFormat.format(section.getDepartureLocationTime().localTime));
            Log.d(TAG, "Route Section Arrival Time : "
                    + dateFormat.format(section.getArrivalLocationTime().localTime));
            Log.d(TAG, "Route Section length : " + section.getLengthInMeters() + " m");
            Log.d(TAG, "Route Section duration : " + section.getDuration().getSeconds() + " s");
        }
    }

    // An implementation may decide to reject a route if one or more violations are detected.
    private void logRouteViolations(Route route) {
        for (Section section : route.getSections()) {
            for (Span span : section.getSpans()) {
                List<GeoCoordinates> spanGeometryVertices = span.getGeometry().vertices;
                // This route violation spreads across the whole span geometry.
                GeoCoordinates violationStartPoint = spanGeometryVertices.get(0);
                GeoCoordinates violationEndPoint = spanGeometryVertices.get(spanGeometryVertices.size() - 1);
                for (int index : span.getNoticeIndexes()) {
                    SectionNotice spanSectionNotice = section.getSectionNotices().get(index);
                    // The violation code such as "VIOLATED_VEHICLE_RESTRICTION".
                    String violationCode = spanSectionNotice.code.toString();
                    //Log.d(TAG, "The violation " + violationCode + " starts at " + toString(violationStartPoint) + " and ends at " + toString(violationEndPoint) + " .");
                }
            }
        }
    }

    private void logTollDetails(Route route) {
        for (Section section : route.getSections()) {
            // The spans that make up the polyline along which tolls are required or
            // where toll booths are located.
            List<Span> spans = section.getSpans();
            List<Toll> tolls = section.getTolls();
            if (!tolls.isEmpty()) {
                Log.d(TAG, "Attention: This route may require tolls to be paid.");
            }
            for (Toll toll : tolls) {
                Log.d(TAG, "Toll information valid for this list of spans:");
                Log.d(TAG, "Toll system: " + toll.tollSystem);
                Log.d(TAG, "Toll country code (ISO-3166-1 alpha-3): " + toll.countryCode);
                Log.d(TAG, "Toll fare information: ");
                for (TollFare tollFare : toll.fares) {
                    // A list of possible toll fares which may depend on time of day, payment method and
                    // vehicle characteristics. For further details please consult the local
                    // authorities.
                    Log.d(TAG, "Toll price: " + tollFare.price + " " + tollFare.currency);
                    for (PaymentMethod paymentMethod : tollFare.paymentMethods) {
                        Log.d(TAG, "Accepted payment methods for this price: " + paymentMethod.name());
                    }
                }
            }
        }
    }

    private void showRouteDetails(Route route, boolean isSimulated) {
        long estimatedTravelTimeInSeconds = route.getDuration().getSeconds();
        int lengthInMeters = route.getLengthInMeters();

        String routeDetails =
                "Travel Time: " + formatTime(estimatedTravelTimeInSeconds)
                        + ", Length: " + formatLength(lengthInMeters);

        showStartNavigationDialog("Route Details", routeDetails, route);
    }

    private void showStartNavigationDialog(String title, String message, Route route) {
        String buttonTextSimulated = "Simulated";
        String buttonTextDevice = "Device";
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonTextSimulated, (dialog, which) -> {
                    routeLayout.setVisibility(View.GONE);
                    navigatorLayout.setVisibility(View.VISIBLE);
                    navigationExample.startNavigation(route, true, isCameraTrackingEnabled);
                })
                .setNeutralButton(buttonTextDevice, (dialog, which) -> {
                    routeLayout.setVisibility(View.GONE);
                    navigatorLayout.setVisibility(View.VISIBLE);
                    navigationExample.startNavigation(route, false, isCameraTrackingEnabled);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Acción al presionar el botón "Cancelar" (opcional)
                    dialog.dismiss(); // Cierra el diálogo
                })
                .show();
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
        // Show route as polyline.
        GeoPolyline routeGeoPolyline = route.getGeometry();
        float widthInPixels = 20;
        Color polylineColor = new Color(0, (float) 0.56, (float) 0.54, (float) 0.63);
        MapPolyline routeMapPolyline = null;

        try {
            routeMapPolyline = new MapPolyline(routeGeoPolyline, new MapPolyline.SolidRepresentation(
                    new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                    polylineColor,
                    LineCap.ROUND));
        } catch (MapPolyline.Representation.InstantiationException e) {
            Log.e("MapPolyline Representation Exception:", e.error.name());
        } catch (MapMeasureDependentRenderSize.InstantiationException e) {
            Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
        }

        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        // Optionally, render traffic on route.
        //showTrafficOnRoute(route);

        GeoCoordinates startPoint =
                route.getSections().get(0).getDeparturePlace().mapMatchedCoordinates;
        GeoCoordinates destination =
                route.getSections().get(route.getSections().size() - 1).getArrivalPlace().mapMatchedCoordinates;

        // Draw a circle to indicate starting point and destination.
        addCircleMapMarker(startPoint, R.drawable.green_dot);
        addCircleMapMarker(destination, R.drawable.red_dot);

        // Log maneuver instructions per route section.
        List<Section> sections = route.getSections();
        for (Section section : sections) {
            logManeuverInstructions(section);
        }
    }

    // This renders the traffic jam factor on top of the route as multiple MapPolylines per span.
    private void showTrafficOnRoute(Route route) {
        if (route.getLengthInMeters() / 1000 > 5000) {
            Log.d(TAG, "Skip showing traffic-on-route for longer routes.");
            return;
        }

        for (Section section : route.getSections()) {
            for (Span span : section.getSpans()) {
                TrafficSpeed trafficSpeed = span.getTrafficSpeed();
                Color lineColor = getTrafficColor(trafficSpeed.jamFactor);
                if (lineColor == null) {
                    // We skip rendering low traffic.
                    continue;
                }
                float widthInPixels = 10;
                MapPolyline trafficSpanMapPolyline = null;
                try {
                    trafficSpanMapPolyline = new MapPolyline(span.getGeometry(), new MapPolyline.SolidRepresentation(
                            new MapMeasureDependentRenderSize(RenderSize.Unit.PIXELS, widthInPixels),
                            lineColor,
                            LineCap.ROUND));
                } catch (MapPolyline.Representation.InstantiationException e) {
                    Log.e("MapPolyline Representation Exception:", e.error.name());
                } catch (MapMeasureDependentRenderSize.InstantiationException e) {
                    Log.e("MapMeasureDependentRenderSize Exception:", e.error.name());
                }

                mapView.getMapScene().addMapPolyline(trafficSpanMapPolyline);
                mapPolylines.add(trafficSpanMapPolyline);
            }
        }
    }

    @Nullable
    private Color getTrafficColor(Double jamFactor) {
        if (jamFactor == null || jamFactor < 4) {
            return null;
        } else if (jamFactor >= 4 && jamFactor < 8) {
            return Color.valueOf(1, 1, 0, 0.63f); // Yellow
        } else if (jamFactor >= 8 && jamFactor < 10) {
            return Color.valueOf(1, 0, 0, 0.63f); // Red
        }
        return Color.valueOf(0, 0, 0, 0.63f); // Black
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
        route = null;
        isCameraTrackingEnabled = true;
        // Limpia la lista de rutas
        mapPolylines.clear();
    }

    private void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
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
        // Añadimos un margen (ajusta el valor según tus necesidades)
        double margin = 0.2; // Ejemplo: 0.01 grados
        minLat -= margin;
        maxLat += margin;
        minLon -= margin;
        maxLon += margin;
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
        double zoomLevel = (Math.log(360 / maxDiff) / Math.log(2)) - 0.5;
        // Suponiendo que tu API de mapas tiene un método para ajustar el nivel de zoom basado en una GeoBox
        return zoomLevel;
    }

    // Método para animar el zoom suavemente
    private void animateZoom(double targetZoomLevel) {
        // Toma el nivel de zoom actual
        double currentZoomLevel = camara.getState().zoomLevel;
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
                camara.setDistanceToTarget(animatedValue);
            }
        });
        // Inicia la animación
        animator.start();
    }

    private List<GeoCoordinates> getIntermediatePoints(GeoPolyline routeLine, int numSegments,double routeLength) {
        List<GeoCoordinates> intermediatePoints = new ArrayList<>();
        List<GeoCoordinates> vertices = routeLine.vertices;
        double segmentLength = routeLength / numSegments;

        double distanceCovered = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            GeoCoordinates start = vertices.get(i);
            GeoCoordinates end = vertices.get(i + 1);
            double segmentDistance = start.distanceTo(end);

            while (distanceCovered + segmentDistance >= segmentLength) {
                double ratio = (segmentLength - distanceCovered) / segmentDistance;
                double lat = start.latitude + ratio * (end.latitude - start.latitude);
                double lon = start.longitude + ratio * (end.longitude - start.longitude);
                intermediatePoints.add(new GeoCoordinates(lat, lon));

                distanceCovered = 0;
                segmentLength += segmentLength; // Avanza al siguiente segmento
            }

            distanceCovered += segmentDistance;
        }

        return intermediatePoints;
    }
}
