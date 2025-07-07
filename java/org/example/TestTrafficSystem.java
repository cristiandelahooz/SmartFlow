package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestTrafficSystem {
    private static ScheduledExecutorService vehicleGenerator = Executors.newScheduledThreadPool(2);
    private static int vehicleCounter = 1;
    private static TrafficManager manager;

    public static void main(String[] args) {
        System.out.println("=== TRAFFIC INTERSECTION SYSTEM ===\n");

        // Crear intersección única
        Intersection intersection = new Intersection("MAIN_INTERSECTION");

        // Crear controller y manager
        TrafficController controller = new TrafficController(intersection);
        manager = new TrafficManager(controller);

        // Iniciar sistema
        manager.startManagement();

        // Generar vehículos
        startVehicleGeneration();

        // Ejecutar por 40 segundos
        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Detener sistema
        stopVehicleGeneration();
        manager.stopManagement();

        System.out.println("\n=== TEST COMPLETED ===");
    }

    private static void startVehicleGeneration() {
        String[] directions = {"NORTH", "SOUTH", "EAST", "WEST"};
        String[] types = {"NORMAL", "NORMAL", "NORMAL", "EMERGENCY"}; // 25% emergencia

        // Generar vehículos cada 1.5 segundos
        vehicleGenerator.scheduleAtFixedRate(() -> {
            String from = directions[(int)(Math.random() * directions.length)];
            String to;
            do {
                to = directions[(int)(Math.random() * directions.length)];
            } while (from.equals(to)); // Asegurar que no sea la misma dirección

            String type = types[(int)(Math.random() * types.length)];

            Vehicle vehicle = new Vehicle("V" + vehicleCounter++, type, from, to);
            manager.addVehicle(vehicle);

            System.out.println("Generated: " + vehicle);
        }, 0, 1500, TimeUnit.MILLISECONDS);

        // Generar vehículos de emergencia ocasionales
        vehicleGenerator.scheduleAtFixedRate(() -> {
            if (Math.random() < 0.4) { // 40% probabilidad
                String from = directions[(int)(Math.random() * directions.length)];
                String to;
                do {
                    to = directions[(int)(Math.random() * directions.length)];
                } while (from.equals(to));

                Vehicle emergencyVehicle = new Vehicle("E" + vehicleCounter++, "EMERGENCY", from, to);
                manager.addVehicle(emergencyVehicle);

                System.out.println("EMERGENCY: " + emergencyVehicle);
            }
        }, 5, 10, TimeUnit.SECONDS);
    }

    private static void stopVehicleGeneration() {
        vehicleGenerator.shutdown();
        System.out.println("Vehicle generation stopped");
    }
}
