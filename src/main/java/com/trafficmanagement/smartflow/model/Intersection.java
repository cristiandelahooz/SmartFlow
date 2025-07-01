package com.trafficmanagement.smartflow.model;

import lombok.Getter;

import java.util.concurrent.PriorityBlockingQueue;

@Getter
public class Intersection {
    private final String id;
    private final boolean rightTurnAllowed;
    private final PriorityBlockingQueue<Vehicle> vehicleQueue;

    public Intersection(String id, boolean rightTurnAllowed) {
        this.id = id;
        this.rightTurnAllowed = rightTurnAllowed;
        this.vehicleQueue = new PriorityBlockingQueue<>();
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleQueue.put(vehicle);
    }

    public Vehicle getNextVehicle() {
        return vehicleQueue.poll(); // devuelve y remueve el vehículo con más prioridad
    }
}
