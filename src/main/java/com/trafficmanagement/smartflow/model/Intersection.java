package com.trafficmanagement.smartflow.model;

import java.util.concurrent.PriorityBlockingQueue;

public class Intersection {
    private String id;
    private boolean rightTurnAllowed;
    private PriorityBlockingQueue<Vehicle> vehicleQueue;

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

    public String getId() { return id; }
    public boolean isRightTurnAllowed() { return rightTurnAllowed; }
}
