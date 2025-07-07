package org.example;

import java.util.concurrent.atomic.AtomicBoolean;

public class TrafficLight {
    private String id;
    private AtomicBoolean green;
    private long lastChange;
    private static final long CHANGE_INTERVAL = 5000; // 5 segundos

    public TrafficLight(String id) {
        this.id = id;
        this.green = new AtomicBoolean(true);
        this.lastChange = System.currentTimeMillis();
    }

    public void changeLight() {
        boolean currentState = green.get();
        green.set(!currentState);
        lastChange = System.currentTimeMillis();
        System.out.println("Traffic light " + id + " changed to " + (green.get() ? "GREEN" : "RED"));
    }

    public boolean isGreen() {
        return green.get();
    }

    public boolean shouldChange() {
        return System.currentTimeMillis() - lastChange > CHANGE_INTERVAL;
    }

    public String getId() { return id; }
}
