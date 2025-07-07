package org.example;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrafficLightController {
    private Intersection intersection;
    private ScheduledExecutorService scheduler;
    private boolean northSouthGreen;
    private volatile boolean running;

    public TrafficLightController(Intersection intersection) {
        this.intersection = intersection;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.northSouthGreen = true;
        this.running = false;
    }

    public void startControl() {
        if (running) return;
        running = true;

        // Configurar luces iniciales
        updateTrafficLights();

        // Cambiar luces cada 8 segundos
        scheduler.scheduleAtFixedRate(this::switchLights, 8, 8, TimeUnit.SECONDS);
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
