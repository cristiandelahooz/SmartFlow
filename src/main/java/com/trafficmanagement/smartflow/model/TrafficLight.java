package com.trafficmanagement.smartflow.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class TrafficLight {
    private String id;
    private AtomicBoolean green;

    public TrafficLight(String id) {
        this.id = id;
        this.green = new AtomicBoolean(false); // rojo por defecto
    }

    public void changeLight() {
        green.set(!green.get());
    }

    public boolean isGreen() {
        return green.get();
    }

    public String getId() { return id; }
}
