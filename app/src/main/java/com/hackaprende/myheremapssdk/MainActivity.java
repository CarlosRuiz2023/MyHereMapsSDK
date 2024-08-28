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

package com.hackaprende.myheremapssdk;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hackaprende.myheremapssdk.adaptadores.PolygonAdapter;
import com.hackaprende.myheremapssdk.bd.PolygonDatabaseHelper;
import com.hackaprende.myheremapssdk.clases.AvoidZonesExample;
import com.hackaprende.myheremapssdk.clases.CameraExample;
import com.hackaprende.myheremapssdk.clases.ControlPointsExample;
import com.hackaprende.myheremapssdk.clases.MapObjectsExample;
import com.hackaprende.myheremapssdk.clases.TrafficExample;
import com.hackaprende.myheremapssdk.clases.SearchExample;
import com.hackaprende.myheremapssdk.interfaces.ReverseGeocodingCallback;
import com.hackaprende.myheremapssdk.modelos.PointWithId;
import com.hackaprende.myheremapssdk.modelos.PolygonWithId;
import com.hackaprende.myheremapssdk.permisos.PermissionsRequestor;
import com.here.sdk.core.Anchor2D;
import com.here.sdk.core.Color;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCoordinatesUpdate;
import com.here.sdk.core.GeoOrientation;
import com.here.sdk.core.GeoOrientationUpdate;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.Point2D;
import com.here.sdk.core.engine.SDKBuildInformation;
import com.here.sdk.core.engine.SDKNativeEngine;
import com.here.sdk.core.engine.SDKOptions;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.LineCap;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapCameraAnimation;
import com.here.sdk.mapview.MapCameraAnimationFactory;
import com.here.sdk.mapview.MapError;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapMeasure;
import com.here.sdk.mapview.MapMeasureDependentRenderSize;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapScene;
import com.here.sdk.mapview.MapScheme;
import com.here.sdk.mapview.MapView;
import com.hackaprende.myheremapssdk.clases.RoutingExample;
import com.here.sdk.mapview.RenderSize;
import com.here.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    // TAG DE LA CLASE MainActivity
    private static final String TAG = MainActivity.class.getSimpleName();
    // INICIALIZACION DE LAS VARIABLES TIPO SearchExample, RoutingExample, TrafficExample, CameraExample y MapObjectsExample
    private SearchExample searchExample;
    private RoutingExample routingExample;
    private TrafficExample trafficExample;
    private CameraExample cameraExample;
    private MapObjectsExample mapObjectsExample;
    // INICIALIZACION DE LA VARIABLE TIPO PermissionsRequestor PARA CONTROLAR LOS PERMISOS
    private PermissionsRequestor permissionsRequestor;
    // INICIALIZACION DE LA VARIABLE TIPO MapViewLite PARA EL MAPA
    private MapView mapView;
    // INICIALIZACION DE LA VARIABLE TIPO MapScheme PARA EL ESTILO DEL MAPA POR DEFECTO
    private MapScheme style = MapScheme.NORMAL_DAY;
    // INICIALIZACION DE LA VARIABLE TIPO Camera PARA EL CONTROL DE LA CAMARA
    private MapCamera camara;
    // INICIALIZACION DE LAS VARIABLES TIPO TextInputEditText
    private TextInputEditText direccion1,direccion2,searchEditText;
    // INICIALIZACION DE LA VARIABLE TIPO BottomNavigationView PARA EL CONTROL DE LA BARRA DE NAVEGACION
    private BottomNavigationView bottomNavigation;
    // INICIALIZACION DE LAS VARIABLES TIPO LinearLayout
    private LinearLayout searchLayout,routeLayout,navigatorLayout,datos,llCameraTargetDot,zonasLayout,puntosLayout;
    // INICIALIZACION DE LAS VARIABLES TIPO MaterialButton
    private MaterialButton btnSearch,btnDraw,btnZona;
    // INICIALIZACION DE LAS VARIABLES TIPO TextInputLayout
    private TextInputLayout tilsearch,tildireccion1,tildireccion2;
    // DEFINE UNA VARIABLE TIPO BOOLEAN PARA CONTROLAR SI ES LA PRIMERA HUBICACION DEL USUARIO
    private boolean isFirstLocationUpdate = true,isActiveTraffic = false;
    // DEFINE LA DURACION DEL TIEMPO DE ANIMACION
    final long zoomAnimationDuration = 1000L;
    // INICIALIZACION DE LA VARIABLE TIPO int PARA EL CONTADOR DE ESTILOS
    private int styleCounter=0;
    private RecyclerView recyclerView, recyclerViewPuntos;

    private AvoidZonesExample avoidZonesExample;
    private ControlPointsExample controlPointsExample;

    // INICIALIZACION DE LA VARIABLE TIPO SDKOPTIONS PARA CONTROLAR LOS CREDENCIALES
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // INICIALIZAMOS EL SDK DE HERE
        initializeHERESDK();
        // INICIALIZAMOS LA VISTA
        setContentView(R.layout.activity_main);
        // VERIFICAMOS LA VERSION DEL SDK
        Log.d("", "HERE SDK version: " + SDKBuildInformation.sdkVersion().versionName);
        // TOMAMOS EL MAPVIEW DEL LAYOUT
        mapView = findViewById(R.id.mapView);
        // INICIALIZAMOS EL MAPVIEW MEDIANTE SU FUNCION onCreate
        mapView.onCreate(savedInstanceState);
        // TOMAMOS LA CAMARA ACTUAL DEL MAPA
        camara = mapView.getCamera();
        // SELECCIONAMOS EL ZOOM
        double distanceInMeters = 7000000;
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, distanceInMeters);
        // LA POSICIONAMOS A CIERTAS COORDENADAS
        camara.lookAt(new GeoCoordinates(21.09914, -101.57485), mapMeasureZoom);
        /* Asi se hacia anteriormente en el Lite
        camara.setTarget(new GeoCoordinates(21.09914, -101.57485));
        camara.setZoomLevel(3);*/
        // ASOCIAMOS VARIOS DE LOS COMPONENTES DE LA VISTA AL CONTROLADOR MEDIANTE SU ID
        searchEditText = findViewById(R.id.searchEditText);
        direccion1 = findViewById(R.id.direccion1);
        tildireccion1=findViewById(R.id.tildireccion1);
        tildireccion2=findViewById(R.id.tildireccion2);
        datos = findViewById(R.id.datos);
        llCameraTargetDot = findViewById(R.id.llCameraTargetDot);
        direccion2 = findViewById(R.id.direccion2);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        searchLayout = findViewById(R.id.searchLayout);
        routeLayout = findViewById(R.id.routeLayout);
        btnSearch = findViewById(R.id.btnSearch);
        btnDraw = findViewById(R.id.btnDraw);
        tilsearch = findViewById(R.id.tilsearch);
        navigatorLayout = findViewById(R.id.navigatorLayout);
        zonasLayout = findViewById(R.id.zonasLayout);
        btnZona = findViewById(R.id.btnZona);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewPuntos = findViewById(R.id.recyclerViewPuntos);
        puntosLayout = findViewById(R.id.puntosLayout);
        avoidZonesExample = new AvoidZonesExample(this,mapView,recyclerView,getLayoutInflater());
        controlPointsExample = new ControlPointsExample(this,mapView,recyclerViewPuntos,getLayoutInflater());

        // ASIGNAMOS FUNCIONAMIENTO EN CASO DE TOCAR EL ICONO DEL TextInputLayout DE LA DIRECCION 1
        tildireccion1.setEndIconOnClickListener(view1->{
            try {
                // CREAMOS UNA VARIABLE TIPO LOCATION REQUEST PARA CONTROLAR LA UBICACION
                LocationRequest locationRequest = new LocationRequest();
                // Configurar la frecuencia de actualización de ubicación
                locationRequest.setInterval(3000);
                // Configurar la frecuencia mínima de actualización de ubicación
                locationRequest.setFastestInterval(3000);
                // Configurar la prioridad de ubicación
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                // Solicitar ubicación en tiempo real
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // Procesar el resultado de ubicación
                        super.onLocationResult(locationResult);
                        // REMOVER LA UBICACION DE LA UBICACION
                        LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                        // VERIFICAMOS SI HAY ALGUNAS UBICACIONES
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            // TOMAMOS LA ULTIMA UBICACION
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            // TOMAMOS LAS COORDENADAS DE LA UBICACION
                            double latitud = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitud = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            // CREAMOS UNA VARIABLE TIPO GEO COORDINATES PARA CONTROLAR LAS COORDENADAS
                            GeoCoordinates geoCoordinates = new GeoCoordinates(latitud, longitud);
                            // CREAMOS UNA VARIABLE TIPO REVERSE GEOCODING CALLBACK PARA CONTROLAR EL RESULTADO
                            searchExample.geocodificacionInversa(geoCoordinates, new ReverseGeocodingCallback() {
                                @Override
                                public void onResult(String addressText) {
                                    // VERIFICAMOS SI HAY ALGUNAS DIRECCIONES
                                    if (addressText != null) {
                                        // Procesa la dirección obtenida y la muestra
                                        localizar(new View(getApplicationContext()));
                                        direccion1.setText(addressText);
                                    } else {
                                        // Maneja el error
                                        Log.e("Prueba", "Error al obtener la dirección.");
                                    }
                                }
                            });
                        }
                    }
                }, Looper.myLooper());
            }catch (Exception e) {
                // TODO
            }
        });
        // ASIGNAMOS FUNCIONAMIENTO EN CASO DE TOCAR EL ICONO DEL TextInputLayout DE LA DIRECCION 2
        tildireccion2.setEndIconOnClickListener(view1->{
            try {
                // CREAMOS UNA VARIABLE TIPO LOCATION REQUEST PARA CONTROLAR LA UBICACION
                LocationRequest locationRequest = new LocationRequest();
                // Configurar la frecuencia de actualización de ubicación
                locationRequest.setInterval(3000);
                // Configurar la frecuencia mínima de actualización de ubicación
                locationRequest.setFastestInterval(3000);
                // Configurar la prioridad de ubicación
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                // Solicitar ubicación en tiempo real
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // Procesar el resultado de ubicación
                        super.onLocationResult(locationResult);
                        // REMOVER LA UBICACION DE LA UBICACION
                        LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                        // VERIFICAMOS SI HAY ALGUNAS UBICACIONES
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            // TOMAMOS LA ULTIMA UBICACION
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            // TOMAMOS LAS COORDENADAS DE LA UBICACION
                            double latitud = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitud = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            // CREAMOS UNA VARIABLE TIPO GEO COORDINATES PARA CONTROLAR LAS COORDENADAS
                            GeoCoordinates geoCoordinates = new GeoCoordinates(latitud, longitud);
                            // CREAMOS UNA VARIABLE TIPO REVERSE GEOCODING CALLBACK PARA CONTROLAR EL RESULTADO
                            searchExample.geocodificacionInversa(geoCoordinates, new ReverseGeocodingCallback() {
                                @Override
                                public void onResult(String addressText) {
                                    // VERIFICAMOS SI HAY ALGUNAS DIRECCIONES
                                    if (addressText != null) {
                                        // Procesa la dirección obtenida y la muestra
                                        localizar(new View(getApplicationContext()));
                                        direccion2.setText(addressText);
                                    } else {
                                        // Maneja el error
                                        Log.e("Prueba", "Error al obtener la dirección.");
                                    }
                                }
                            });
                        }
                    }
                }, Looper.myLooper());
            }catch (Exception e) {
                // TODO
            }
        });
        // ASIGNAMOS FUNCIONAMIENTO AL BottomNavigationView PARA CONTROLAR LA BARRA DE NAVEGACION
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // TOMAMOS EL TITULO DEL ITEM SELECCIONADO
                String title = ""+item.getTitle();
                // VALIDAMOS POR TITULO DEL ITEM SELECCIONADO
                switch (title) {
                    case "Buscar":
                        avoidZonesExample.cleanPolygon();
                        controlPointsExample.cleanPoint();
                        // CAMBIAMOS EL COMENTARIO DEL TextInputLayout DE LA BUSUQEDA
                        tilsearch.setHelperText("Direccion.");
                        // CAMBIAMOS EL TEXTO DE MUESTRA DEL TextInputLayout DE LA BUSUQEDA
                        tilsearch.setHint("Buscar...");
                        // REMOVEMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.removeCameraObserver();
                        searchExample.setTapGestureHandler();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        btnDraw.setVisibility(View.GONE);
                        routeLayout.setVisibility(View.GONE);
                        datos.setVisibility(View.GONE);
                        llCameraTargetDot.setVisibility(View.GONE);
                        // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DE LA BUSQUEDA
                        btnSearch.setVisibility(View.VISIBLE);
                        searchLayout.setVisibility(View.VISIBLE);
                        navigatorLayout.setVisibility(View.GONE);
                        zonasLayout.setVisibility(View.GONE);
                        puntosLayout.setVisibility(View.GONE);
                        break;
                    case "Enrutar":
                        avoidZonesExample.cleanPolygon();
                        controlPointsExample.cleanPoint();
                        // REMOVEMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.removeCameraObserver();
                        trafficExample.setTapGestureHandler();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        searchLayout.setVisibility(View.GONE);
                        datos.setVisibility(View.GONE);
                        llCameraTargetDot.setVisibility(View.GONE);
                        if(routingExample.getRoute()!=null){
                            navigatorLayout.setVisibility(View.VISIBLE);
                        }else{
                            // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DE LA RUTA
                            routeLayout.setVisibility(View.VISIBLE);
                        }
                        zonasLayout.setVisibility(View.GONE);
                        puntosLayout.setVisibility(View.GONE);
                        break;
                    case "Dibujar":
                        avoidZonesExample.cleanPolygon();
                        controlPointsExample.cleanPoint();
                        // CAMBIAMOS EL COMENTARIO DEL TextInputLayout DEL CIRCULO
                        tilsearch.setHelperText("Radio (mtrs).");
                        // CAMBIAMOS EL TEXTO DE MUESTRA DEL TextInputLayout DEL CIRCULO
                        tilsearch.setHint("Crear");
                        // REMOVEMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.removeCameraObserver();
                        searchExample.setTapGestureHandler();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        routeLayout.setVisibility(View.GONE);
                        btnSearch.setVisibility(View.GONE);
                        datos.setVisibility(View.GONE);
                        llCameraTargetDot.setVisibility(View.GONE);
                        // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DEL CIRCULO
                        btnDraw.setVisibility(View.VISIBLE);
                        searchLayout.setVisibility(View.VISIBLE);
                        navigatorLayout.setVisibility(View.GONE);
                        zonasLayout.setVisibility(View.GONE);
                        puntosLayout.setVisibility(View.GONE);
                        break;
                    case "Puntero":
                        avoidZonesExample.cleanPolygon();
                        controlPointsExample.cleanPoint();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        routeLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);
                        // AÑADIMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.addCameraObserver();
                        cameraExample.setTapGestureHandler();
                        // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DEL PUNTERO
                        llCameraTargetDot.setVisibility(View.VISIBLE);
                        datos.setVisibility(View.VISIBLE);
                        navigatorLayout.setVisibility(View.GONE);
                        zonasLayout.setVisibility(View.GONE);
                        puntosLayout.setVisibility(View.GONE);
                        break;
                    case "Zonas":
                        avoidZonesExample.cleanPolygon();
                        controlPointsExample.cleanPoint();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        routeLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);
                        // AÑADIMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.removeCameraObserver();
                        // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DEL PUNTERO
                        llCameraTargetDot.setVisibility(View.GONE);
                        datos.setVisibility(View.GONE);
                        navigatorLayout.setVisibility(View.GONE);
                        zonasLayout.setVisibility(View.VISIBLE);
                        avoidZonesExample.startGestures();
                        puntosLayout.setVisibility(View.GONE);
                        break;
                    case "Puntos":
                        avoidZonesExample.cleanPolygon();
                        // ESCONDEMOS ALGUNOS COMPONENTES
                        routeLayout.setVisibility(View.GONE);
                        searchLayout.setVisibility(View.GONE);
                        // AÑADIMOS EL CONTROL DE LA CLASE CameraExample PARA EL CONTROL DEL PUNTERO
                        cameraExample.removeCameraObserver();
                        // MOSTRAMOS LOS COMPONENTES ESPECIFICAMENTE DEL PUNTERO
                        llCameraTargetDot.setVisibility(View.GONE);
                        datos.setVisibility(View.GONE);
                        navigatorLayout.setVisibility(View.GONE);
                        zonasLayout.setVisibility(View.GONE);
                        puntosLayout.setVisibility(View.VISIBLE);
                        controlPointsExample.startGestures();
                        break;
                }
                return true;
            }
        });
        // VERIFICAMOS LOS PERMISOS
        handleAndroidPermissions();
    }

    // FUNCION QUE INICIALIZA EL SDK DE HERE
    private void initializeHERESDK() {
        // Set your credentials for the HERE SDK.
        /*String accessKeyID = "NbXZJlNGStoRBT4zK9hUZA";
        String accessKeySecret = "Ddh7JAr2xprqZITKpef1ipe0EXEAtEt7sP4SgjkSWtBSL3dhkyBgwPXn1z5wkZmbBUbitlQMDnnkirDd2ZBzpg";*/
        String accessKeyID = "NelTCYuCjtoWMisV8QyHyw";
        String accessKeySecret = "5MR6njdMDh_GwgfaZPLEuo_psaA41KWyqY-uGjEZ2deOSvzZ4U4kB7vlZga97yV05IJr18K2Zt_rdKieWzPmbw";
        // CREAMOS UNA VARIABLE TIPO SDKOPTIONS PARA CONTROLAR LOS CREDENCIALES
        SDKOptions options = new SDKOptions(accessKeyID, accessKeySecret);
        try {
            Context context = this;
            // Initialize the HERE SDK bajo este Context y con estas Opciones.
            SDKNativeEngine.makeSharedInstance(context, options);
        } catch (InstantiationErrorException e) {
            // Handle initialization error.
            throw new RuntimeException("Initialization of HERE SDK failed: " + e.error.name());
        }
    }

    // FUNCION QUE CONTROLARA LOS PERMISOS
    private void handleAndroidPermissions() {
        // Create a new PermissionsRequestor instance
        permissionsRequestor = new PermissionsRequestor(this);
        // VALIDAMOS QUE LOS PERMISOS ESTEN ACTIVADOS
        if (permissionsRequestor.areAllPermissionsGranted()) {
            // Permissions are already granted, do something
            loadMapScene();
        } else {
            // Request permissions
            permissionsRequestor.request(new PermissionsRequestor.ResultListener() {
                @Override
                public void permissionsGranted() {
                    // CARGAMOS EL MAPA
                    loadMapScene();
                }

                @Override
                public void permissionsDenied() {
                    // Show a dialog to explain why the permissions are needed
                    //showPermissionDeniedDialog();
                    Log.e(TAG, "Permissions denied by user.");
                }
            });
        }
    }

    // FUNCION QUE MUESTRA EL DIALOGO DE PERMISOS DENEGADOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Forward the result to the PermissionsRequestor instance
        permissionsRequestor.onRequestPermissionsResult(requestCode, grantResults);
    }

    // FUNCION QUE CARGA EL MAPA
    private void loadMapScene() {
        // Load a scene from the SDK to render the map with a map style.
        mapView.getMapScene().loadScene(MapScheme.NORMAL_DAY, new MapScene.LoadSceneCallback() {
            @Override
            public void onLoadScene(@Nullable MapError errorCode) {
                // Check for errors
                if (errorCode == null) {
                    // INICIALIZAMOS CADA UNA DE LAS INSTANCIAS NECESARIAS DEL APLICATIVO
                    mapObjectsExample = new MapObjectsExample(mapView);
                    trafficExample = new TrafficExample(MainActivity.this, mapView);
                    searchExample = new SearchExample(MainActivity.this, mapView);
                    routingExample = new RoutingExample(MainActivity.this, mapView,searchExample);
                    cameraExample = new CameraExample(MainActivity.this, mapView,searchExample);
                } else {
                    // Handle loading error
                    Log.d(TAG, "onLoadScene failed: " + errorCode.toString());
                }
            }
        });
    }

    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE BUSQUEDA
    public void searchExampleButtonClicked(View view) {
        // Llama al método searchExampleButtonClicked de la instancia de searchExample
        searchExample.onSearchButtonClicked();
    }

    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE GENERAR CIRCULO
    public void geocodeAnAddressButtonClicked(View view) {
        // TOMAMOS EL TEXTO DEL TextInputEditText
        String searchTerm = searchEditText.getText().toString();
        // VALIDAMOS QUE EL TEXTO NO ESTE VACIO
        if(searchTerm.length()>0){
            try {
                // PARSEAMOS EL TEXTO A FLOAT
                float radius = Float.parseFloat(searchEditText.getText().toString());
                // VERIFICAMOS QUE EL RADIO NO PASE DE LOS 50,000 METROS
                if(radius<=50000){
                    // Obtener las coordenadas del centro del MapView
                    GeoCoordinates geoCoordinates = getGeoCoordinatesFromCenterOfMapView();
                    // LIMPIAMOS EL MAPA
                    mapObjectsExample.clearMap();
                    // CREAMOS EL CIRCULO MANDANDO EL GEO COORDINATES Y EL RADIO
                    //mapObjectsExample.showMapCircle(geoCoordinates, radius);
                    mapObjectsExample.showMapCircle(geoCoordinates,radius);
                }else{
                    // MANDAMOS UN MENSAJE DE ERROR
                    Toast.makeText(this, "El radio debe ser menor a 50000 m.", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e) {
                // MANDAMOS UN MENSAJE DE ERROR
                Toast.makeText(this, "Por favor, introduce un numero valido", Toast.LENGTH_SHORT).show();
            }
        }else{
            // MANDAMOS UN MENSAJE DE ERROR
            Toast.makeText(this, "El radio esta vacio", Toast.LENGTH_SHORT).show();
        }
    }
    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE AÑADIR RUTA
    public void addRouteButtonClicked(View view) {
        List<GeoCoordinates> puntos = new ArrayList<>();
        for(PointWithId pointWithId:controlPointsExample.pointsWithIds){
            puntos.add(pointWithId.punto);
        }
        // Llama al método addRouteButtonClicked de la instancia de routingExample
        routingExample.addRoute(avoidZonesExample.poligonos,puntos);
    }
    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DEL TRAFICO
    public void viewTrafficButtonClicked(View view) {
        // VERIFICAMOS SI ESTA ACTIVADO EL TRAFICO
        if(!isActiveTraffic){
            // ACTIVAMOS EL TRAFICO
            trafficExample.enableAll();
            // CAMBIAMOS EL ESTADO
            isActiveTraffic = true;
        }else{
            // DESACTIVAMOS EL TRAFICO
            trafficExample.disableAll();
            // CAMBIAMOS EL ESTADO
            isActiveTraffic = false;
        }
    }
    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE RESTABLECER MAPA
    public void clearMapButtonClicked(View view) {
        // LIMPIAMOS EL MAPA EN TODAS LAS INTANCIAS
        searchExample.clearAll();
        mapObjectsExample.clearMap();
        if(routingExample.getRoute() != null){
            routingExample.detach();
        }
        routingExample.clearMap();
        navigatorLayout.setVisibility(View.GONE);
        Menu menu = bottomNavigation.getMenu();
        MenuItem menuItem = menu.findItem(bottomNavigation.getSelectedItemId());
        String itemName = menuItem.getTitle().toString();
        if(itemName.equalsIgnoreCase("Enrutar")){
            routeLayout.setVisibility(View.VISIBLE);
        }
        // LIMPIAMOS LOS TEXTOS DE LOS TextInputEditText
        searchEditText.setText("");
        direccion1.setText("");
        direccion2.setText("");
        // RESTABLECEMOS LA POSICION DE LA CAMARA
        double distanceInMeters = 7000000;
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, distanceInMeters);
        // LA POSICIONAMOS A CIERTAS COORDENADAS
        camara.lookAt(new GeoCoordinates(21.09914, -101.57485), mapMeasureZoom);
        camara.setOrientationAtTarget(new GeoOrientationUpdate(new GeoOrientation(0.0,0.0)));
        // RESTAURAMOS LAS VARIABLES GLOBALES DE LA CLASE
        isFirstLocationUpdate = true;
        isActiveTraffic = false;
        // DESACTIVAMOS EL TRAFICO
        trafficExample.disableAll();
        // Eliminar cualquier polígono existente en el mapa
        avoidZonesExample.cleanPolygon();
        // Eliminar cualquier punto existente en el mapa
        controlPointsExample.cleanPoint();
    }

    // FUNCION IMPLEMENTADA POR LA CLASE AppCompatActivity
    @Override
    protected void onPause() {
        // PAUSAMOS EL MAPA
        mapView.onPause();
        super.onPause();
    }

    // FUNCION IMPLEMENTADA POR LA CLASE AppCompatActivity
    @Override
    protected void onResume() {
        // RESUMO EL MAPA
        mapView.onResume();
        super.onResume();
    }

    // FUNCION IMPLEMENTADA POR LA CLASE AppCompatActivity
    @Override
    protected void onDestroy() {
        // DESTRUIMOS EL MAPA
        mapView.onDestroy();
        // DESTRUIMOS EL SDK DE HERE
        disposeHERESDK();
        super.onDestroy();
    }

    // FUNCION QUE DESTRUYE EL SDK DE HERE
    private void disposeHERESDK() {
        // Free HERE SDK resources before the application shuts down.
        // Usually, this should be called only on application termination.
        // Afterwards, the HERE SDK is no longer usable unless it is initialized again.
        SDKNativeEngine sdkNativeEngine = SDKNativeEngine.getSharedInstance();
        if (sdkNativeEngine != null) {
            sdkNativeEngine.dispose();
            // For safety reasons, we explicitly set the shared instance to null to avoid situations,
            // where a disposed instance is accidentally reused.
            SDKNativeEngine.setSharedInstance(null);
        }
    }
    // FUNCION QUE DEVUELVE LAS GEO COORDINATES DEL CENTRO DEL MAPA SEGUN LA CAMARA ACTUAL
    private GeoCoordinates getGeoCoordinatesFromCenterOfMapView() {
        // Obtener las coordenadas del centro del MapView
        int mapViewWidth = mapView.getWidth();
        int mapViewHeight = mapView.getHeight();
        // Calcula el punto central del MapView
        Point2D centerPoint = new Point2D(mapViewWidth / 2.0, mapViewHeight / 2.0);
        // Convierte el punto de la vista a coordenadas geográficas
        return mapView.getCamera().getState().targetCoordinates;
    }
    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE CAMBIAR ESTILO
    public void changeStyle(View view) {
        String filename="";
        // AUMENTAMOS EL CONTADOR DE ESTILOS
        styleCounter++;
        // VERIFICAMOS EL CONTADOR DE ESTILOS NO SALGA DEL RANGO
        if(styleCounter==7)styleCounter=0;
        // VALIDAMOS POR EL CONTADOR DE ESTILOS
        switch (styleCounter) {
            case 0:
                // CAMBIAMOS EL ESTILO
                style = MapScheme.NORMAL_DAY;
                break;
            case 1:
                // CAMBIAMOS EL ESTILO
                style = MapScheme.NORMAL_NIGHT;
                break;
            case 2:
                // CAMBIAMOS EL ESTILO A NULO
                style = null;
                // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                filename = "custom-dark-style-neon-rds.json";
                break;
            case 3:
                // CAMBIAMOS EL ESTILO A NULO
                style = null;
                // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                filename = "Day.json";
                break;
            case 4:
                // CAMBIAMOS EL ESTILO A NULO
                style = null;
                // TOMAMOS EL NOMBRE DEL ARCHIVO JSON
                filename = "prueba.json";
                break;
            case 5:
                // CAMBIAMOS EL ESTILO
                style = MapScheme.HYBRID_DAY;
                break;
            case 6:
                // CAMBIAMOS EL ESTILO A NULO
                style = MapScheme.SATELLITE;
                break;
        }
        // TOMAMOS LOS ASSETS DEL PROYECTO
        AssetManager assetManager = this.getAssets();
        try {
            // CARGAMOS EL ARCHIVO JSON
            assetManager.open(filename);
        } catch (Exception e) {
            // MANDAMOS UN MENSAJE DE ERROR
            Log.e("Error", e.getMessage());
        }
        // VERIFICAMOS SI EL ESTYLO ES NULO
        if(style==null){
            // CARGAMOS EL ESTILO POR DEFECTO
            mapView.getMapScene().loadScene(""+filename, new MapScene.LoadSceneCallback() {
                @Override
                public void onLoadScene(@Nullable MapError errorCode) {
                    if (errorCode == null) {
                    } else {
                        // Style loading failed
                    }
                }
            });
        }else{
            // CARGAMOS ALGUNO DE LOS ESTILOS PREDETERMINADOS DE LA SDK
            mapView.getMapScene().loadScene(style, new MapScene.LoadSceneCallback() {
                @Override
                public void onLoadScene(@Nullable MapError errorCode) {
                    if (errorCode == null) {
                        // VERIFICAMOS SI ESTA ACTIVADO EL TRAFICO
                        if(isActiveTraffic) {
                            // ACTIVAMOS EL TRAFICO
                            trafficExample.enableAll();
                        }
                    } else {
                        // Style loading failed
                    }
                }
            });
        }

    }
    // FUNCION QUE SE EJECUTA AL PRESIONAR EL BOTON DE LOCALIZAR
    public void localizar(View view) {
        try {
            // CREAMOS UNA VARIABLE TIPO LOCATION REQUEST PARA CONTROLAR LA UBICACION
            LocationRequest locationRequest = new LocationRequest();
            // Configurar la frecuencia de actualización de ubicación
            locationRequest.setInterval(3000);
            // Configurar la frecuencia mínima de actualización de ubicación
            locationRequest.setFastestInterval(3000);
            // Configurar la prioridad de ubicación
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            // Solicitar ubicación en tiempo real
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // Procesar el resultado de ubicación
                    super.onLocationResult(locationResult);
                    // REMOVER LA UBICACION DE LA UBICACION
                    LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                    // VERIFICAMOS SI HAY ALGUNAS UBICACIONES
                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        // TOMAMOS LA ULTIMA UBICACION
                        int latestLocationIndex = locationResult.getLocations().size() - 1;
                        // TOMAMOS LAS COORDENADAS DE LA UBICACION
                        double latitud = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                        double longitud = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                        // CREAMOS UNA VARIABLE TIPO GEO COORDINATES PARA CONTROLAR LAS COORDENADAS
                        GeoCoordinates geoCoordinates = new GeoCoordinates(latitud, longitud);
                        // Actualiza el marcador en el mapa
                        searchExample.addPoiMapMarkerUbicacion(geoCoordinates);
                        // Solo actualiza la cámara en la primera actualización de ubicación
                        if (isFirstLocationUpdate) {
                            // Establece la nueva posición de la cámara
                            camara.lookAt(geoCoordinates);
                            // Zoom a nivel 13
                            flyTo(geoCoordinates);
                            // ACTUALIZA EL ESTATUS DE LA PRIMERA LOCALIZACION
                            isFirstLocationUpdate = false;
                        }
                    }
                }
            }, Looper.myLooper());
        }catch (Exception e) {
            // TODO
        }
    }
    // FUNCION QUE MUESTRA UN DIALOG PARA LOS PERMISOS QUE FALTAN DE ACEPTAR
    private void showPermissionDeniedDialog() {
        // Show a dialog to explain why the permissions are needed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title and message
        builder.setTitle("Permisos necesarios")
                .setMessage("Los permisos de ubicación son necesarios para el correcto funcionamiento de la aplicación. Por favor, habilite los permisos en la configuración de la aplicación.")
                .setPositiveButton("Configuración", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Abre la configuración de la aplicación para que el usuario pueda habilitar los permisos manualmente.
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cierra la aplicación o realiza alguna acción alternativa si se cancela la solicitud de permisos.
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    // Método para animar el zoom suavemente
    private void animateZoom(double targetZoomLevel) {
        // Obtiene el nivel de zoom actual del
        double currentZoomLevel = camara.getState().zoomLevel;
        // Crea un valor
        ValueAnimator animator = ValueAnimator.ofFloat((float) currentZoomLevel, (float) targetZoomLevel);
        // Establece la duración de la animación en milisegundos
        animator.setDuration(zoomAnimationDuration);
        // Establece el método de interpolación para la animación
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // Obtiene el valor actual de la animación
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                // Establece el nuevo nivel de zoom
                camara.setDistanceToTarget(animatedValue);
            }
        });
        // Inicia la animación
        animator.start();
    }
    private void flyTo(GeoCoordinates geoCoordinates) {
        GeoCoordinatesUpdate geoCoordinatesUpdate = new GeoCoordinatesUpdate(geoCoordinates);
        MapMeasure mapMeasureZoom = new MapMeasure(MapMeasure.Kind.DISTANCE, 1000.0);
        double bowFactor = 1;
        MapCameraAnimation animation = MapCameraAnimationFactory.flyTo(
                geoCoordinatesUpdate, mapMeasureZoom, bowFactor, Duration.ofSeconds(3));
        camara.startAnimation(animation);
    }
    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void addZona(View view) {
        // Eliminar cualquier polígono existente en el mapa
        if(avoidZonesExample.mapPolygon!=null){
            avoidZonesExample.showSavePolygonDialog();
        } else {
            Toast.makeText(this, "Genere un polígono válido antes de guardar", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Método para dibujar un polígono en el mapa usando una lista de vértices.
     *
     * @param vertices Lista de coordenadas que componen el polígono.
     */
    public void addPunto(View view) {
        // Eliminar cualquier polígono existente en el mapa
        if(controlPointsExample.mapMarker!=null){
            controlPointsExample.showSavePointDialog();
        } else {
            Toast.makeText(this, "De click en el mapa para agregar un punto", Toast.LENGTH_SHORT).show();
        }
    }
}
