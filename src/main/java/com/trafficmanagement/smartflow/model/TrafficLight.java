package com.trafficmanagement.smartflow.model;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clase que representa un semáforo en una intersección.
 * Cada semáforo tiene un identificador y un estado (verde o rojo).
 */
@Getter
public class TrafficLight {
    private final String id;
    private final AtomicBoolean green;

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
}
