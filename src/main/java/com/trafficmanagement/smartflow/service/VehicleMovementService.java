package com.trafficmanagement.smartflow.service;

import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;

import static com.trafficmanagement.smartflow.utils.VehicleConstants.*;

@Slf4j
public class VehicleMovementService {

    public void moveToTarget(Vehicle vehicle, Point2D target, boolean isEmergency) {
        if (vehicle.isFinished()) {
            return;
        }

        double currentX = vehicle.getX();
        double currentY = vehicle.getY();
        
        if (hasReachedTarget(currentX, currentY, target)) {
            return;
        }

        Point2D direction = calculateMovementDirection(currentX, currentY, target);
        double speed = determineSpeed(vehicle.getType(), isEmergency);
        
        double newX = currentX + direction.getX() * speed;
        double newY = currentY + direction.getY() * speed;
        
        vehicle.setPosition(newX, newY);
        
        log.debug("vehicle_moved vehicleId={} from=({},{}) to=({},{}) speed={}", 
            vehicle.getId(), currentX, currentY, newX, newY, speed);
    }

    public Point2D calculateNextPosition(Vehicle vehicle, Point2D target) {
        double currentX = vehicle.getX();
        double currentY = vehicle.getY();
        
        if (hasReachedTarget(currentX, currentY, target)) {
            return new Point2D(currentX, currentY);
        }

        Point2D direction = calculateMovementDirection(currentX, currentY, target);
        double speed = determineSpeed(vehicle.getType(), false);
        
        return new Point2D(
            currentX + direction.getX() * speed,
            currentY + direction.getY() * speed
        );
    }

    private boolean hasReachedTarget(double currentX, double currentY, Point2D target) {
        double distance = Math.sqrt(
            Math.pow(target.getX() - currentX, 2) + 
            Math.pow(target.getY() - currentY, 2)
        );
        return distance < TARGET_REACHED_THRESHOLD;
    }

    private Point2D calculateMovementDirection(double currentX, double currentY, Point2D target) {
        double angle = Math.atan2(target.getY() - currentY, target.getX() - currentX);
        return new Point2D(Math.cos(angle), Math.sin(angle));
    }

    private double determineSpeed(VehicleType type, boolean isEmergency) {
        if (isEmergency || type == VehicleType.EMERGENCY) {
            return EMERGENCY_SPEED;
        }
        return NORMAL_SPEED;
    }

    public double calculateDistanceToTarget(Vehicle vehicle, Point2D target) {
        return Math.sqrt(
            Math.pow(target.getX() - vehicle.getX(), 2) + 
            Math.pow(target.getY() - vehicle.getY(), 2)
        );
    }

    public boolean isVehicleAtPosition(Vehicle vehicle, Point2D position, double tolerance) {
        return calculateDistanceToTarget(vehicle, position) <= tolerance;
    }

    public Point2D getCurrentPosition(Vehicle vehicle) {
        return new Point2D(vehicle.getX(), vehicle.getY());
    }
}