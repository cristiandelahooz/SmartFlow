package com.trafficmanagement.smartflow.control;

import com.trafficmanagement.smartflow.model.Intersection;
import com.trafficmanagement.smartflow.model.TrafficLight;
import com.trafficmanagement.smartflow.model.Vehicle;
import com.trafficmanagement.smartflow.model.VehicleType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TrafficController {
    private final Intersection intersection;
    private final List<TrafficLight> trafficLights;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService vehicleExecutor;
    private volatile boolean diagonalOneGreen = true;

    public TrafficController(Intersection intersection, List<TrafficLight> trafficLights) {
        this.intersection = intersection;
        this.trafficLights = trafficLights;
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.vehicleExecutor = Executors.newFixedThreadPool(10);
        // Set initial state
        synchronizeLights();
    }

    public void startControl() {
        scheduler.scheduleAtFixedRate(this::synchronizeLights, 15, 15, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::manageIntersection, 0, 1, TimeUnit.SECONDS);
    }

    private void synchronizeLights() {
        diagonalOneGreen = !diagonalOneGreen;

        for (TrafficLight light : trafficLights) {
            boolean isDiagonalOne = light.getId().equals("northWest") || light.getId().equals("southEast");
            boolean isDiagonalTwo = light.getId().equals("northEast") || light.getId().equals("southWest");

            if (isDiagonalOne) {
                light.greenProperty().set(diagonalOneGreen);
            } else if (isDiagonalTwo) {
                light.greenProperty().set(!diagonalOneGreen);
            }
        }
    }

    private void manageIntersection() {
        Vehicle nextVehicle = intersection.getVehicleQueue().peek();
        if (nextVehicle != null && nextVehicle.getType() == VehicleType.EMERGENCY) {
            handleEmergencyVehicle(nextVehicle);
        } else {
            if (trafficLights.stream().anyMatch(TrafficLight::isGreen)) {
                Vehicle vehicle = intersection.getNextVehicle();
                if (vehicle != null) {
                    vehicleExecutor.submit(vehicle);
                }
            }
        }
    }

    private void handleEmergencyVehicle(Vehicle emergencyVehicle) {
        log.info("Emergency vehicle {} detected. Clearing intersection.", emergencyVehicle.getId());
        // Clear the intersection by stopping other traffic
        for (TrafficLight light : trafficLights) {
            if (!light.getId().equals(getDirection(emergencyVehicle.getDirection()))) {
                light.getGreen().set(false);
            }
        }
        // Allow the emergency vehicle to proceed
        trafficLights.stream()
                .filter(l -> l.getId().equals(getDirection(emergencyVehicle.getDirection())))
                .findFirst()
                .ifPresent(l -> l.getGreen().set(true));

        Vehicle vehicle = intersection.getNextVehicle();
        if (vehicle != null) {
            vehicleExecutor.submit(vehicle);
        }
    }

    private String getDirection(String route) {
        return route.split("-")[0];
    }

    public void stopControl() {
        scheduler.shutdown();
        vehicleExecutor.shutdown();
    }
}
