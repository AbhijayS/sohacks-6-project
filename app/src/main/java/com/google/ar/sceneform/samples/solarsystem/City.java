package com.google.ar.sceneform.samples.solarsystem;

import android.util.Log;

public class City {
    String name;
    double longitude, latitude, population, x, y, z;

    City(String name, double latitude, double longitude, double population) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = -1*longitude-22;
        this.population = population;

        calculateXYZ();
    }

    void calculateXYZ() {
        this.x = calculateX();
        this.y = calculateY();
        this.z = calculateZ();
    }

    double calculateX() {
        double x = SolarActivity.EARTH_RADIUS * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(longitude));
        Log.i("SOHACKS", "x:" + x);
        return x;
    }

    double calculateY() {
        double y = SolarActivity.EARTH_RADIUS * Math.sin(Math.toRadians(latitude));
        Log.i("SOHACKS", "y:" + y);
        return y;
    }

    double calculateZ() {
        double z = SolarActivity.EARTH_RADIUS * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(longitude));
        Log.i("SOHACKS", "z:" + z);
        return z;
    }

}
