package org.example;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class Intersection {
    private String id;
    private Map<String, Street> streets;
    private ReentrantLock intersectionLock;
    private Condition canPass;
    private Set<String> activeRoutes; // Rutas actualmente en uso
    private final Object routeLock = new Object();

    public Intersection(String id) {
        this.id = id;
        this.streets = new HashMap<>();
        this.intersectionLock = new ReentrantLock();
        this.canPass = intersectionLock.newCondition();
        this.activeRoutes = new HashSet<>();

        // Crear las 4 calles
        streets.put("NORTH", new Street("NORTH"));
        streets.put("SOUTH", new Street("SOUTH"));
        streets.put("EAST", new Street("EAST"));
        streets.put("WEST", new Street("WEST"));
    }

    public String getEmergencyDirection() {
        for (Map.Entry<String, Street> entry : streets.entrySet()) {
            Vehicle next = entry.getValue().peekNextVehicle();
            if (next != null && "EMERGENCY".equals(next.getType())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void addVehicle(Vehicle vehicle) {
        Street street = streets.get(vehicle.getFromDirection());
        if (street != null) {
            street.addVehicle(vehicle);
        }
    }

    public boolean canVehicleEnter(Vehicle vehicle) {
        String route = vehicle.getFromDirection() + "->" + vehicle.getToDirection();

        synchronized (routeLock) {
            // Verificar si hay conflicto con rutas activas
            for (String activeRoute : activeRoutes) {
                if (hasRouteConflict(route, activeRoute)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean hasRouteConflict(String route1, String route2) {
        // Definir rutas que no pueden coexistir
        Set<String> conflictRoutes = new HashSet<>();

        // Rutas que se cruzan (no pueden ir simultáneamente)
        if (route1.contains("NORTH") && route1.contains("SOUTH")) {
            conflictRoutes.add("EAST->WEST");
            conflictRoutes.add("WEST->EAST");
            conflictRoutes.add("EAST->NORTH");
            conflictRoutes.add("EAST->SOUTH");
            conflictRoutes.add("WEST->NORTH");
            conflictRoutes.add("WEST->SOUTH");
        }

        if (route1.contains("EAST") && route1.contains("WEST")) {
            conflictRoutes.add("NORTH->SOUTH");
            conflictRoutes.add("SOUTH->NORTH");
            conflictRoutes.add("NORTH->EAST");
            conflictRoutes.add("NORTH->WEST");
            conflictRoutes.add("SOUTH->EAST");
            conflictRoutes.add("SOUTH->WEST");
        }

        // Giros a la izquierda conflictivos
        if (route1.equals("NORTH->WEST")) {
            conflictRoutes.add("EAST->SOUTH");
            conflictRoutes.add("SOUTH->EAST");
            conflictRoutes.add("WEST->NORTH");
        }

        return conflictRoutes.contains(route2);
    }

    public void enterIntersection(Vehicle vehicle) throws InterruptedException {
        intersectionLock.lock();
        try {
            while (!canVehicleEnter(vehicle)) {
                canPass.await();
            }

            String route = vehicle.getFromDirection() + "->" + vehicle.getToDirection();
            synchronized (routeLock) {
                activeRoutes.add(route);
            }

            vehicle.setInIntersection(true);
            System.out.println("Vehicle " + vehicle.getId() + " entered intersection (" + route + ")");
            System.out.println("Active routes: " + activeRoutes);
        } finally {
            intersectionLock.unlock();
        }
    }

    public void exitIntersection(Vehicle vehicle) {
        intersectionLock.lock();
        try {
            String route = vehicle.getFromDirection() + "->" + vehicle.getToDirection();
            synchronized (routeLock) {
                activeRoutes.remove(route);
            }

            vehicle.setInIntersection(false);
            canPass.signalAll();
            System.out.println("Vehicle " + vehicle.getId() + " exited intersection");
            System.out.println("Active routes: " + activeRoutes);
        } finally {
            intersectionLock.unlock();
        }
    }

    public Vehicle getNextVehicleFromStreet(String direction) {
        Street street = streets.get(direction);
        return street != null ? street.getNextVehicle() : null;
    }

    public boolean hasWaitingVehicles() {
        return streets.values().stream().anyMatch(Street::hasWaitingVehicles);
    }

    public void setTrafficLights(String direction, boolean green) {
        Street street = streets.get(direction);
        if (street != null) {
            street.setTrafficLight(green);
        }
    }

    public Map<String, Street> getStreets() { return streets; }
    public String getId() { return id; }
}
