package com.trafficmanagement.smartflow.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

/**
 * Clase que representa un semáforo en una intersección.
 * Cada semáforo tiene un identificador y un estado (verde o rojo).
 */
@Getter
public class TrafficLight {
    public static final int MAX_GREEN_TIME = 2;
    private final String id;
    private final BooleanProperty green;

    public TrafficLight(String id) {
        this.id = id;
        this.green = new SimpleBooleanProperty(false); // rojo por defecto
    }

    public void changeLight() {
        green.set(!green.get());
    }

    public boolean isGreen() {
        return green.get();
    }

    public BooleanProperty greenProperty() {
        return green;
    }
}
