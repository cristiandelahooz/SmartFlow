package org.example;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Street {
    private String direction; // NORTH, SOUTH, EAST, WEST
    private PriorityBlockingQueue<Vehicle> waitingVehicles;
    private AtomicInteger vehicleCount;
    private boolean hasTrafficLight;
    private boolean greenLight;

    public Street(String direction) {
        this.direction = direction;
        this.waitingVehicles = new PriorityBlockingQueue<>();
        this.vehicleCount = new AtomicInteger(0);
        this.hasTrafficLight = true;
        this.greenLight = true; // Inicia en verde
    }

    public Vehicle peekNextVehicle() {
        return waitingVehicles.peek();
    }

    public void addVehicle(Vehicle vehicle) {
        waitingVehicles.offer(vehicle);
        vehicleCount.incrementAndGet();
        System.out.println("Vehicle " + vehicle.getId() + " waiting on " + direction + " street (Queue: " + vehicleCount.get() + ")");
    }

    public Vehicle getNextVehicle() {
        Vehicle vehicle = waitingVehicles.poll();
        if (vehicle != null) {
            vehicleCount.decrementAndGet();
        }
        return vehicle;
    }

    public boolean hasWaitingVehicles() {
        return !waitingVehicles.isEmpty();
    }

    public void setTrafficLight(boolean green) {
        this.greenLight = green;
        System.out.println(direction + " street light: " + (green ? "GREEN" : "RED"));
    }

    public boolean canVehiclePass() {
        return greenLight && hasWaitingVehicles();
    }

    // Getters
    public String getDirection() { return direction; }
    public int getVehicleCount() { return vehicleCount.get(); }
    public boolean isGreenLight() { return greenLight; }
}
