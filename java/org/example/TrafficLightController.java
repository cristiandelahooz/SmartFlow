package org.example;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrafficLightController {
    private Intersection intersection;
    private ScheduledExecutorService scheduler;
    private boolean northSouthGreen = true;
    private volatile boolean running;

    public TrafficLightController(Intersection intersection) {
        this.intersection = intersection;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.northSouthGreen = true;
        this.running = false;
        updateTrafficLights();
    }

    public void startControl() {
        if (running) return;
        running = true;
        scheduler.scheduleAtFixedRate(this::controlCycle, 0, 1, TimeUnit.SECONDS);
        System.out.println("Traffic Light Controller started");
    }

    public void stopControl() {
        running = false;
        scheduler.shutdown();
        System.out.println("Traffic Light Controller stopped");
    }

    private void switchLights() {
        northSouthGreen = !northSouthGreen;
        updateTrafficLights();
    }

    private void controlCycle() {
        long now = System.currentTimeMillis();
        // Usamos el mismo CHANGE_INTERVAL de antes (8 000 ms)
        if (now - lastSwitchTime >= 8000) {
            northSouthGreen = !northSouthGreen;
            lastSwitchTime = now;
            updateTrafficLights();
        }
    }

    private long lastSwitchTime = System.currentTimeMillis();

    private void updateTrafficLights() {
        if (northSouthGreen) {
            intersection.setTrafficLights("NORTH", true);
            intersection.setTrafficLights("SOUTH", true);
            intersection.setTrafficLights("EAST", false);
            intersection.setTrafficLights("WEST", false);
        } else {
            intersection.setTrafficLights("NORTH", false);
            intersection.setTrafficLights("SOUTH", false);
            intersection.setTrafficLights("EAST", true);
            intersection.setTrafficLights("WEST", true);
        }
    }
}
