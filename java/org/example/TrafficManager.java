package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficManager {
    private TrafficController controller;
    private ScheduledExecutorService managerScheduler;
    private volatile boolean managing;

    public TrafficManager(TrafficController controller) {
        this.controller = controller;
        this.managerScheduler = Executors.newScheduledThreadPool(1);
        this.managing = false;
    }

    public void startManagement() {
        if (managing) return;
        managing = true;

        System.out.println("Traffic Manager started");
        controller.startControl();

        // Monitorear sistema cada 5 segundos
        managerScheduler.scheduleAtFixedRate(this::monitorSystem, 5, 5, TimeUnit.SECONDS);
    }

    public void stopManagement() {
        managing = false;
        managerScheduler.shutdown();
        controller.stopControl();
        System.out.println("Traffic Manager stopped");
    }

    private void monitorSystem() {
        System.out.println("\n=== INTERSECTION STATUS ===");
        Intersection intersection = controller.getIntersection();

        for (String direction : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            Street street = intersection.getStreets().get(direction);
            System.out.println(direction + " street: " + street.getVehicleCount() + " vehicles waiting, " +
                    "Light: " + (street.isGreenLight() ? "GREEN" : "RED"));
        }
        System.out.println("===========================\n");
    }

    public void addVehicle(Vehicle vehicle) {
        controller.getIntersection().addVehicle(vehicle);
    }
}
