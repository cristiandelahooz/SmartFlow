package org.example;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;

public class TrafficController {
    private Intersection intersection;
    private TrafficLightController lightController;
    private ScheduledExecutorService scheduler;
    private ExecutorService vehicleExecutor;
    private volatile boolean running;

    public TrafficController(Intersection intersection) {
        this.intersection = intersection;
        this.lightController = new TrafficLightController(intersection);
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.vehicleExecutor = Executors.newCachedThreadPool();
        this.running = false;
    }

    public void startControl() {
        if (running) return;
        running = true;

        System.out.println("Traffic Controller started");
        lightController.startControl();

        // Procesar vehículos cada 500ms
        scheduler.scheduleAtFixedRate(this::processVehicles, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stopControl() {
        running = false;
        scheduler.shutdown();
        vehicleExecutor.shutdown();
        lightController.stopControl();
        System.out.println("Traffic Controller stopped");
    }

    private void processVehicles() {
        for (String direction : new String[]{"NORTH", "SOUTH", "EAST", "WEST"}) {
            Street street = intersection.getStreets().get(direction);
            if (street.canVehiclePass()) {
                Vehicle vehicle = intersection.getNextVehicleFromStreet(direction);
                if (vehicle != null) {
                    vehicleExecutor.submit(() -> processVehicle(vehicle));
                }
            }
        }
    }

    private void processVehicle(Vehicle vehicle) {
        try {
            System.out.println("Processing vehicle " + vehicle.getId() + " from " + vehicle.getFromDirection());

            // Prioridad para emergencias
            if (!vehicle.getType().equals("EMERGENCY")) {
                Thread.sleep(100);
            }

            intersection.enterIntersection(vehicle);

            // Simular tiempo de cruce (varia según el tipo de giro)
            int crossingTime = switch (vehicle.getTurnType()) {
                case "STRAIGHT" -> 2000;
                case "RIGHT" -> 1500;
                case "LEFT" -> 3000;
                default -> 2000;
            };

            Thread.sleep(crossingTime);

            intersection.exitIntersection(vehicle);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Vehicle processing interrupted: " + vehicle.getId());
        }
    }

    public Intersection getIntersection() { return intersection; }
}
