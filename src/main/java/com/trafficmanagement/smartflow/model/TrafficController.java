package com.trafficmanagement.smartflow.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class TrafficController {
    private final List<Intersection> intersections;
    private final List<TrafficLight> trafficLights;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService vehicleExecutor;

    public TrafficController(List<Intersection> intersections, List<TrafficLight> trafficLights) {
        this.intersections = intersections;
        this.trafficLights = trafficLights;
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.vehicleExecutor = Executors.newFixedThreadPool(10);
    }

    public void startControl() {
        for (TrafficLight light : trafficLights) {
            scheduler.scheduleAtFixedRate(light::changeLight, 0, 60, TimeUnit.SECONDS);
        }
        scheduler.scheduleAtFixedRate(this::manageIntersections, 0, 1, TimeUnit.SECONDS);
    }

    private void manageIntersections() {
        for (Intersection intersection : intersections) {
            if (trafficLights.stream().anyMatch(TrafficLight::isGreen)) {
                Vehicle vehicle = intersection.getNextVehicle();
                if (vehicle != null) {
                    vehicleExecutor.submit(vehicle);
                }
            }
        }
    }

    public void stopControl() {
        scheduler.shutdown();
        vehicleExecutor.shutdown();
    }
}
