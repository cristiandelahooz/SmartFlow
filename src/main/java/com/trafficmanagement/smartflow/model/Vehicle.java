package com.trafficmanagement.smartflow.model;

public class Vehicle implements Comparable<Vehicle> {
    private String id;
    private String type; // "normal" o "emergency"
    private String direction; // "right", "straight", "left", "u-turn"
    private boolean inIntersection;

    public Vehicle(String id, String type, String direction) {
        this.id = id;
        this.type = type;
        this.direction = direction;
        this.inIntersection = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public boolean isInIntersection() { return inIntersection; }
    public void setInIntersection(boolean inIntersection) { this.inIntersection = inIntersection; }

    // Para que PriorityBlockingQueue dé prioridad a los vehículos de emergencia
    @Override
    public int compareTo(Vehicle other) {
        if (this.type.equals("emergency") && !other.type.equals("emergency")) return -1;
        if (!this.type.equals("emergency") && other.type.equals("emergency")) return 1;
        return 0; // igual prioridad entre mismos tipos
    }
}

