package com.google.ar.sceneform.samples.solarsystem;

public class City {
    String name;
    double longitude, latitude, population, x, y, z;

    City(String name, double longitude, double latitude, double population, double radius) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.population = population;

        calculateXYZ(radius);
    }

    void calculateXYZ(double radius) {
        x = radius * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(longitude));

        z = radius * Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(longitude));

        y = radius * Math.sin(Math.toRadians(latitude));
    }

}
