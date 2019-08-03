//package com.google.ar.sceneform.samples.solarsystem;
//
//public class City {
//    String name;
//    double longitude, latitude, population, x, y, z;
//
//    City(String name, double longitude, double latitude, double population, double radius) {
//        this.name = name;
//        this.longitude = -longitude;
//        this.latitude = latitude;
//        this.population = population;
//
//        calculateXYZ(radius);
//    }
//
//    void calculateXYZ(double radius) {
//        x = radius * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(longitude));
//
//        z = radius * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(longitude));
//
//        y = radius * Math.sin(Math.toRadians(latitude));
//    }
//
//}

/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.solarsystem;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Node that represents a planet.
 *
 * <p>The planet creates two child nodes when it is activated:
 *
 * <ul>
 *   <li>The visual of the planet, rotates along it's own axis and renders the planet.
 *   <li>An info card, renders an Android View that displays the name of the planerendt. This can be
 *       toggled on and off.
 * </ul>
 *
 * The planet is rendered by a child instead of this node so that the spinning of the planet doesn't
 * make the info card spin as well.
 */
public class City extends Node implements Node.OnTapListener {
    private final String cityName;
//    private final float planetScale;
//    private final float orbitDegreesPerSecond;
//    private final float axisTilt;
    private final ModelRenderable cityRenderable;
    private final Snackbar cityInfoSnackbar;
    private double longitude, latitude, radius, x, y, z;
    private long population;
    private float scale;
//    private final SolarSettings solarSettings;

    private Node infoCard;
    private Node cityVisual;
    private final Context context;

//    private static final float INFO_CARD_Y_POS_COEFF = 0.55f;

    public City(
            Context context,
            ModelRenderable cityRenderable,
            Snackbar infoSnackbar,
            String cityName,
            Double latitude,
            Double longitude,
            long population,
            Double radius,
            float scale
//            float planetScale,
//            float orbitDegreesPerSecond,
//            float axisTilt,
//            SolarSettings solarSettings
    ) {
        this.context = context;
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = -1*longitude-22;
        this.population = population;
        this.radius = radius;
        this.scale = scale;
//        this.planetScale = planetScale;
//        this.orbitDegreesPerSecond = orbitDegreesPerSecond;
//        this.axisTilt = axisTilt;
        this.cityRenderable = cityRenderable;
        this.cityInfoSnackbar = infoSnackbar;
//        this.solarSettings = solarSettings;
        setOnTapListener(this);

        calculateXYZ();
    }

    void calculateXYZ() {
        this.x = calculateX();
        this.y = calculateY();
        this.z = calculateZ();
    }

    double calculateX() {
        double x = radius * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(longitude));
//        Log.i("SOHACKS", "x:" + x);
        return x;
    }

    double calculateY() {
        double y = radius * Math.sin(Math.toRadians(latitude));
//        Log.i("SOHACKS", "y:" + y);
        return y;
    }

    double calculateZ() {
        double z = radius * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(longitude));
//        Log.i("SOHACKS", "z:" + z);
        return z;
    }

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void onActivate() {

        if (getScene() == null) {
            throw new IllegalStateException("Scene is null!");
        }

        if (infoCard == null) {
            infoCard = new Node();
            infoCard.setParent(this);
            infoCard.setEnabled(false);
            infoCard.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));
            infoCard.setLocalScale(new Vector3(5f, 5f, 5f));

            ViewRenderable.builder()
                    .setView(context, R.layout.planet_card_view)
                    .build()
                    .thenAccept(
                            (renderable) -> {
                                infoCard.setRenderable(renderable);
                                TextView textView = (TextView) renderable.getView();
                                textView.setText(cityName);
                            })
                    .exceptionally(
                            (throwable) -> {
                                throw new AssertionError("Could not load plane card view.", throwable);
                            });
        }

        if (cityVisual == null) {
            // Put a rotator to counter the effects of orbit, and allow the planet orientation to remain
            // of planets like Uranus (which has high tilt) to keep tilted towards the same direction
            // wherever it is in its orbit.
//            RotatingNode counterOrbit = new RotatingNode(solarSettings, true, true, 0f);
//            counterOrbit.setDegreesPerSecond(orbitDegreesPerSecond);
//            counterOrbit.setParent(this);

            cityVisual = new Node();
            cityVisual.setParent(this);
            cityVisual.setRenderable(cityRenderable);
            cityVisual.setLocalScale(new Vector3(scale, scale, scale));
        }
    }

    @Override
    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Log.i("SOHACKS", "TAPPED ME");
//        if (infoCard == null) {
//            return;
//        }
//
//        infoCard.setEnabled(!infoCard.isEnabled());
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String populationAsString = numberFormat.format(population);
        String info = cityName.toUpperCase() + "\n" + "Population: " + populationAsString;
        SolarActivity.updateInfoWindow(info);
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        if (infoCard == null) {
            return;
        }

        // Typically, getScene() will never return null because onUpdate() is only called when the node
        // is in the scene.
        // However, if onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (getScene() == null) {
            return;
        }
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = infoCard.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        infoCard.setWorldRotation(lookRotation);
    }





    public double getX() {return x;}
    public double getY() {return y;}
    public double getZ() {return z;}
}
