package org.example;

import java.util.concurrent.atomic.AtomicBoolean;

public class Vehicle implements Comparable<Vehicle> {
    private String id;
    private String type;
    private String fromDirection; // De donde viene: NORTH, SOUTH, EAST, WEST
    private String toDirection;   // Hacia donde va: NORTH, SOUTH, EAST, WEST
    private String turnType;      // STRAIGHT, LEFT, RIGHT
    private AtomicBoolean inIntersection;
    private long arrivalTime;
    private int priority;

    public Vehicle(String id, String type, String fromDirection, String toDirection) {
        this.id = id;
        this.type = type;
        this.fromDirection = fromDirection;
        this.toDirection = toDirection;
        this.inIntersection = new AtomicBoolean(false);
        this.arrivalTime = System.currentTimeMillis();
        this.priority = type.equals("EMERGENCY") ? 1 : 2;
        this.turnType = calculateTurnType(fromDirection, toDirection);
    }

    private String calculateTurnType(String from, String to) {
        if (from.equals("NORTH") && to.equals("SOUTH")) return "STRAIGHT";
        if (from.equals("SOUTH") && to.equals("NORTH")) return "STRAIGHT";
        if (from.equals("EAST") && to.equals("WEST")) return "STRAIGHT";
        if (from.equals("WEST") && to.equals("EAST")) return "STRAIGHT";

        // Determinar si es LEFT o RIGHT basado en la dirección
        if (from.equals("NORTH")) {
            if (to.equals("WEST")) return "LEFT";
            if (to.equals("EAST")) return "RIGHT";
        } else if (from.equals("SOUTH")) {
            if (to.equals("EAST")) return "LEFT";
            if (to.equals("WEST")) return "RIGHT";
        } else if (from.equals("EAST")) {
            if (to.equals("NORTH")) return "LEFT";
            if (to.equals("SOUTH")) return "RIGHT";
        } else if (from.equals("WEST")) {
            if (to.equals("SOUTH")) return "LEFT";
            if (to.equals("NORTH")) return "RIGHT";
        }

        return "STRAIGHT"; // default
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;
        this.priority = type.equals("EMERGENCY") ? 1 : 2;
    }

    public String getFromDirection() { return fromDirection; }
    public void setFromDirection(String fromDirection) { this.fromDirection = fromDirection; }

    public String getToDirection() { return toDirection; }
    public void setToDirection(String toDirection) { this.toDirection = toDirection; }

    public String getTurnType() { return turnType; }

    public boolean isInIntersection() { return inIntersection.get(); }
    public void setInIntersection(boolean inIntersection) { this.inIntersection.set(inIntersection); }

    public long getArrivalTime() { return arrivalTime; }
    public int getPriority() { return priority; }

    @Override
    public int compareTo(Vehicle other) {
        if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        }
        return Long.compare(this.arrivalTime, other.arrivalTime);
    }

    @Override
    public String toString() {
        return String.format("Vehicle[id=%s, type=%s, %s->%s (%s)]",
                id, type, fromDirection, toDirection, turnType);
    }
}
