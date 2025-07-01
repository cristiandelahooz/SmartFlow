package com.trafficmanagement.smartflow.model;

import lombok.Getter;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.PriorityBlockingQueue;

@Getter
public class Intersection {
    private final String id;
    private final boolean rightTurnAllowed;
    private final PriorityBlockingQueue<Vehicle> vehicleQueue;
    private final CyclicBarrier barrier;

    public Intersection(String id, boolean rightTurnAllowed, int parties) {
        this.id = id;
        this.rightTurnAllowed = rightTurnAllowed;
        this.vehicleQueue = new PriorityBlockingQueue<>();
        this.barrier = new CyclicBarrier(parties);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicleQueue.put(vehicle);
    }

    public Vehicle getNextVehicle() {
        return vehicleQueue.poll();
    }
}
