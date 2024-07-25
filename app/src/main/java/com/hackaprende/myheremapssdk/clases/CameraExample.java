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

 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import androidx.annotation.NonNull;
 import androidx.annotation.Nullable;
 import androidx.appcompat.app.AlertDialog.Builder;
 import android.util.Log;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;

 import com.google.android.material.textfield.TextInputEditText;
 import com.hackaprende.myheremapssdk.MainActivity;
 import com.here.sdk.core.Anchor2D;
 import com.here.sdk.core.GeoCircle;
 import com.here.sdk.core.GeoCoordinates;
 import com.here.sdk.core.LanguageCode;
 import com.here.sdk.core.Metadata;
 import com.here.sdk.core.Point2D;
 import com.here.sdk.core.errors.InstantiationErrorException;
 import com.here.sdk.mapview.MapCamera;
 //import com.here.sdk.mapview.CameraObserver;
 import com.here.sdk.mapview.MapCameraListener;
 import com.here.sdk.mapview.MapCameraUpdate;
 //import com.here.sdk.mapview.MapCircle;
 //import com.here.sdk.mapview.MapCircleStyle;
 import com.here.sdk.mapview.MapView;
 //import com.here.sdk.mapview.PixelFormat;
 import com.hackaprende.myheremapssdk.R;
 import com.here.sdk.search.Place;
 import com.here.sdk.search.SearchCallback;
 import com.here.sdk.search.SearchEngine;
 import com.here.sdk.search.SearchError;
 import com.here.sdk.search.SearchOptions;

 import java.util.List;

 /**
  * This example shows how to use the Camera class to rotate and tilt the map programmatically, to set
  * a new transform center that influences those operations, and to move to a new location using
  * Android's Animation framework.
  * For more features of the Camera class, please consult the API Reference and the Developer's Guide.
  */
 public class CameraExample {
     // Default zoom level for the map.
     private static final float DEFAULT_ZOOM_LEVEL = 14;
    // Definimos la variable context
     private Context context;
     // Definimos la variable mapView
     private MapView mapView;
     // Definimos la variable camera
     private MapCamera camera;
     // Definimos la variable cameraAnimator
     //private CameraAnimator cameraAnimator;
     // Definimos la variable cameraTargetView
     private ImageView cameraTargetView;
     // Definimos la variable poiMapCircle
     //private MapCircle poiMapCircle;
     // Definimos la variable searchExample
     private SearchExample searchExample;
     // Definimos la variable searchEngine
     private SearchEngine searchEngine;
     // Definimos las variables de los componentes de la vista
     private TextInputEditText textViewEstado;
     private TextInputEditText textViewCiudad;
     private TextInputEditText textViewColonia;
     private TextInputEditText textViewCodigoPostal;
     private TextInputEditText textViewCalle;
     private TextInputEditText textViewNumero;

     public CameraExample(Context context, MapView mapView,SearchExample searchExample) {
         // Generamos una Activity mediante el contexto
         Activity activity = (Activity) context;
         // Asignamos los valores a las variables
         this.context = context;
         this.mapView = mapView;
         this.searchExample = searchExample;
         this.camera = mapView.getCamera();
         try {
             // Inicializamos el objeto SearchEngine
             searchEngine = new SearchEngine();
         } catch (InstantiationErrorException e) {
             // En caso de que no se pueda inicializar el objeto SearchEngine, mostramos un mensaje de error
             throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
         }
         // Inicializamos el objeto CameraAnimator
         //cameraAnimator = new CameraAnimator(camera);
         // Establecemos el listener para la animación
         //cameraAnimator.setTimeInterpolator(new AccelerateDecelerateInterpolator());
         // Asignamos cada uno de los componentes con sus respectivas variables de la clase
         textViewEstado = activity.findViewById(R.id.textViewEstado);
         textViewCiudad = activity.findViewById(R.id.textViewCiudad);
         textViewColonia = activity.findViewById(R.id.textViewColonia);
         textViewCodigoPostal = activity.findViewById(R.id.textViewCodigoPostal);
         textViewCalle = activity.findViewById(R.id.textViewCalle);
         textViewNumero = activity.findViewById(R.id.textViewNumero);
         cameraTargetView = activity.findViewById(R.id.cameraTargetDot);

         // The POI MapCircle (green) indicates the next location to move to.
         //updatePoiCircle(getRandomGeoCoordinates());

         //addCameraObserver();
         //setTapGestureHandler(mapView);
     }

     private final MapCameraListener cameraObserver = new MapCameraListener() {
         @Override
         public void onMapCameraUpdated(@NonNull MapCamera.State state) {
             // Obtenemos la nueva ubicación del mapa
             GeoCoordinates camTarget = state.targetCoordinates;

             // Creamos las opciones de búsqueda para la ubicación inversa
             SearchOptions reverseGeocodingOptions = new SearchOptions();

             // Establecemos el idioma de la búsqueda
             reverseGeocodingOptions.languageCode = LanguageCode.EN_GB;

             // Establecemos el número máximo de resultados
             reverseGeocodingOptions.maxItems = 1;

             // Hacemos la busqueda
             searchEngine.search(camTarget, reverseGeocodingOptions, new SearchCallback() {
                 @Override
                 public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                     // Verificamos si hay un error en la búsqueda
                     if (searchError != null) {
                         // Si hay un error, mostramos los datos de la ubicación vacios
                         textViewEstado.setText("");
                         textViewCiudad.setText("");
                         textViewColonia.setText("");
                         textViewCodigoPostal.setText("");
                         textViewCalle.setText("");
                         textViewNumero.setText("");
                         return;
                     }

                     // Add new marker for each search result on map.
                     for (Place searchResult : list) {
                         Metadata metadata = new Metadata();
                         metadata.setCustomValue("key_search_result", new SearchExample.SearchResultMetadata(searchResult));
                     }

                     // If error is null, list is guaranteed to be not empty.
                     // Obtener los datos de la ubicación inversa
                     String estado = list.get(0).getAddress().state;
                     String ciudad = list.get(0).getAddress().city;
                     String colonia = list.get(0).getAddress().district;
                     String codigo_postal = list.get(0).getAddress().postalCode;
                     String calle = list.get(0).getAddress().street;
                     String numero = list.get(0).getAddress().houseNumOrName;

                     // Mostrar los datos en un diálogo
                     textViewEstado.setText(estado);
                     textViewCiudad.setText(ciudad);
                     textViewColonia.setText(colonia);
                     textViewCodigoPostal.setText(codigo_postal);
                     textViewCalle.setText(calle);
                     textViewNumero.setText(numero);
                 }
             });
         }

     };

     /*public void rotateButtonClicked() {
         rotateMap(10);
     }

     public void tiltButtonClicked() {
         tiltMap(5);
     }*/

     /*public void moveToXYButtonClicked() {
         // Obtenemos una nueva ubicación aleatoria.
         GeoCoordinates geoCoordinates = getRandomGeoCoordinates();
         // Movemos el mapa a la nueva ubicación.
         //updatePoiCircle(geoCoordinates);
         // Establecemos el nuevo transform center.
         //cameraAnimator.moveTo(geoCoordinates, DEFAULT_ZOOM_LEVEL);
     }*/

     // Rotate the map by x degrees. Tip: Try to see what happens for negative values.
     /*private void rotateMap(int bearingStepInDegrees) {
         // Get the current bearing.
         double currentBearing = camera.getBearing();
         // Calculate the new bearing.
         double newBearing = currentBearing + bearingStepInDegrees;
         //By default, bearing will be clamped to the range (0, 360].
         camera.setBearing(newBearing);
     }*/

     // Tilt the map by x degrees.
     /*private void tiltMap(int tiltStepInDegrees) {
         // Get the current tilt.
         double currentTilt = camera.getTilt();
         // Calculate the new tilt.
         double newTilt = currentTilt + tiltStepInDegrees;
         //By default, tilt will be clamped to the range [0, 70].
         camera.setTilt(newTilt);
     }*/
     public void setTapGestureHandler() {
         mapView.getGestures().setTapListener(this::setTransformCenter);
     }
     public void setTapGestureHandlerRemove() {
         // Remove the tap listener
         mapView.getGestures().setTapListener(null);
     }

     // The new transform center will be used for all programmatical map transformations (like rotate and tilt)
     // and determines where the target is located in the view.
     // By default, the anchor point is located at x = 0.5, y = 0.5.
     // Note: Gestures are not affected, for example, the pinch-rotate gesture and
     // the two-finger-pan (=> tilt) will work like before.
     private void setTransformCenter(Point2D mapViewPoint) {
         // Get the current transform center.
         double normalizedX = (1F / mapView.getWidth()) * mapViewPoint.x;
         double normalizedY = (1F / mapView.getHeight()) * mapViewPoint.y;
         // Set the new transform center.
         Anchor2D transformationCenter = new Anchor2D(normalizedX, normalizedY);
         //camera.setTargetAnchorPoint(transformationCenter);
         // Reposition view on screen to indicate the new target.
         cameraTargetView.setX((float) mapViewPoint.x - cameraTargetView.getWidth() / 2);
         cameraTargetView.setY((float) mapViewPoint.y - cameraTargetView.getHeight() / 2);
         // Obtener las coordenadas geográficas del punto.
         GeoCoordinates geoCoordinates = mapView.viewToGeoCoordinates(mapViewPoint);
         // Creamos la opciones de búsqueda para la ubicación inversa.
         SearchOptions reverseGeocodingOptions = new SearchOptions();
         // Establecemos el idioma de la búsqueda.
         reverseGeocodingOptions.languageCode = LanguageCode.EN_GB;
         // Establecemos el número máximo de resultados.
         reverseGeocodingOptions.maxItems = 1;
         // Hacemos la busqueda
         searchEngine.search(geoCoordinates, reverseGeocodingOptions, new SearchCallback() {
             @Override
             public void onSearchCompleted(@Nullable SearchError searchError, @Nullable List<Place> list) {
                 // Verificamos si hay un error en la búsqueda
                 if (searchError != null) {
                     // Si hay un error, mostramos un mensaje de error
                     //showDialog("Reverse geocoding", "Error: " + searchError.toString());
                     return;
                 }
                 // Add new marker for each search result on map.
                 for (Place searchResult : list) {
                     Metadata metadata = new Metadata();
                     metadata.setCustomValue("key_search_result", new SearchExample.SearchResultMetadata(searchResult));
                 }
                 // If error is null, list is guaranteed to be not empty.
                 // Obtener los datos de la ubicación inversa
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

     /*private void updatePoiCircle(GeoCoordinates geoCoordinates) {
         // Verificamos si ya existe un MapCircle en el mapa
         if (poiMapCircle != null) {
             // Si existe, lo eliminamos del mapa
             mapView.getMapScene().removeMapCircle(poiMapCircle);
         }
         // Creamos un MapCircle nuevo
         poiMapCircle = createMapCircle(geoCoordinates, 0x00FF00A0, 80d, 1000);
         // Añadimos el MapCircle al mapa
         mapView.getMapScene().addMapCircle(poiMapCircle);
     }*/

     /*private MapCircle createMapCircle(GeoCoordinates geoCoordinates,
                                       long color, double radiusInMeters, long drawOrder) {
         // Creamos un GeoCircle
         GeoCircle geoCircle = new GeoCircle(geoCoordinates, radiusInMeters);
         // Creamos un MapCircleStyle
         MapCircleStyle mapCircleStyle = new MapCircleStyle();
         // Establecemos el color del MapCircle
         mapCircleStyle.setFillColor(color, PixelFormat.RGBA_8888);
         // Establecemos el grosor del MapCircle
         mapCircleStyle.setDrawOrder(drawOrder);
         // Creamos y devolvemos el MapCircle
         return new MapCircle(geoCircle, mapCircleStyle);
     }*/

     public void addCameraObserver() {
         // Add the observer to the camera.
         mapView.getCamera().addListener(cameraObserver);
     }

     /*private GeoCoordinates getRandomGeoCoordinates() {
         // Get the current target of the camera.
         GeoCoordinates currentTarget = camera.getTarget();
         // Add a random amount to the current target.
         double amount = 0.05;
         // Get a random latitude and longitude.
         double latitude = getRandom(currentTarget.latitude - amount, currentTarget.latitude + amount);
         double longitude = getRandom(currentTarget.longitude - amount, currentTarget.longitude + amount);
         // Return the new GeoCoordinates.
         return new GeoCoordinates(latitude, longitude);
     }*/

     private double getRandom(double min, double max) {
         // Return a random double between min and max.
         return min + Math.random() * (max - min);
     }

     private void showDialog(String title, String message) {
         // Build the dialog.
         Builder builder = new Builder(context);
         // Set the dialog title and message.
         builder.setTitle(title);
         // Set the dialog message.
         builder.setMessage(message);
         // Mostrar el diálogo.
         builder.show();
     }
     private void showAddressDialog(String estado, String ciudad, String colonia, String codigo_postal, String calle, String numero) {
         // Crear el diálogo
         Dialog dialog = new Dialog(context);
         // Establecer el diseño del diálogo
         dialog.setContentView(R.layout.dialog_address);
         // Establecer el título del diálogo
         dialog.setTitle("Dirección");
         // Asignar los valores a los elementos del diálogo
         TextView textViewEstado = dialog.findViewById(R.id.textViewEstado);
         TextView textViewCiudad = dialog.findViewById(R.id.textViewCiudad);
         TextView textViewColonia = dialog.findViewById(R.id.textViewColonia);
         TextView textViewCodigoPostal = dialog.findViewById(R.id.textViewCodigoPostal);
         TextView textViewCalle = dialog.findViewById(R.id.textViewCalle);
         TextView textViewNumero = dialog.findViewById(R.id.textViewNumero);
         // Mostrar los datos en los elementos del diálogo
         textViewEstado.setText("Estado: " + estado);
         textViewCiudad.setText("Ciudad: " + ciudad);
         textViewColonia.setText("Colonia: " + colonia);
         textViewCodigoPostal.setText("Código Postal: " + codigo_postal);
         textViewCalle.setText("Calle: " + calle);
         textViewNumero.setText("Número: " + numero);
        // Mostrar el diálogo
         dialog.show();
     }
     public void removeCameraObserver() {
         // Remove the observer from the camera.
         mapView.getCamera().removeListener(cameraObserver);
     }
 }
