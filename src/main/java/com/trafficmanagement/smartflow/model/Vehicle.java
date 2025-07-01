package com.trafficmanagement.smartflow.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Vehicle implements Comparable<Vehicle> {
    private final String id;
    private final VehicleType type;
    private final String direction; // "right", "straight", "left", "u-turn"
    private final boolean inIntersection;

    // Para que PriorityBlockingQueue dé prioridad a los vehículos de emergencia
    @Override
    public int compareTo(Vehicle other) {
        if (VehicleType.EMERGENCY.equals(type) && !VehicleType.EMERGENCY.equals(other)) return -1;
        if (!VehicleType.EMERGENCY.equals(type) && VehicleType.EMERGENCY.equals(other)) return 1;
        return 0; // igual prioridad entre mismos tipos
    }
}

