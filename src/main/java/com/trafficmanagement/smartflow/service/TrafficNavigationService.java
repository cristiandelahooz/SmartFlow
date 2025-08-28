package com.trafficmanagement.smartflow.service;

import com.trafficmanagement.smartflow.controller.MotorwayViewController;
import com.trafficmanagement.smartflow.controller.TrafficLightController;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import com.trafficmanagement.smartflow.utils.MotorwayConstants;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;

import static com.trafficmanagement.smartflow.utils.VehicleConstants.*;

@Slf4j
public class TrafficNavigationService {

    private final TrafficLightController trafficLightController;

    public TrafficNavigationService(TrafficLightController trafficLightController) {
        this.trafficLightController = trafficLightController;
    }

    public boolean canProceedThroughLight(Vehicle vehicle, int lightId) {
        int targetIntersectionId = (vehicle.getTargetIntersection() != null) 
            ? vehicle.getTargetIntersection().getId() 
            : 0;

        if (isLightForTargetIntersection(lightId, targetIntersectionId)) {
            if (vehicle.getType() == VehicleType.EMERGENCY) {
                return canEmergencyVehicleProceed(vehicle, lightId);
            }
            return trafficLightController.isGreen(lightId);
        }
        return true;
    }

    private boolean isLightForTargetIntersection(int lightId, int targetIntersectionId) {
        return (lightId == MotorwayConstants.TRAFFIC_LIGHT_1 && targetIntersectionId == MotorwayConstants.INTERSECTION_1)
            || ((lightId == MotorwayConstants.TRAFFIC_LIGHT_2 || lightId == MotorwayConstants.TRAFFIC_LIGHT_3) && targetIntersectionId == MotorwayConstants.INTERSECTION_2)
            || ((lightId == MotorwayConstants.TRAFFIC_LIGHT_4 || lightId == MotorwayConstants.TRAFFIC_LIGHT_5) && targetIntersectionId == MotorwayConstants.INTERSECTION_3)
            || (lightId == MotorwayConstants.TRAFFIC_LIGHT_6 && targetIntersectionId == MotorwayConstants.INTERSECTION_4);
    }

    private boolean canEmergencyVehicleProceed(Vehicle emergencyVehicle, int lightId) {
        log.info("emergency_vehicle_light_check vehicleId={} lightId={}", emergencyVehicle.getId(), lightId);
        return true;
    }

    public Optional<Vehicle> findLeadingVehicle(Vehicle follower, Collection<Vehicle> allVehicles) {
        Point2D followerPos = new Point2D(follower.getX(), follower.getY());
        
        return allVehicles.stream()
            .filter(vehicle -> !vehicle.equals(follower))
            .filter(vehicle -> !vehicle.isFinished())
            .filter(vehicle -> isInFrontOf(vehicle, follower))
            .filter(vehicle -> {
                Point2D leaderPos = new Point2D(vehicle.getX(), vehicle.getY());
                return calculateDistance(followerPos, leaderPos) < SAFE_DISTANCE * PROXIMITY_THRESHOLD;
            })
            .findFirst();
    }

    private boolean isInFrontOf(Vehicle leader, Vehicle follower) {
        switch (follower.getOrigin()) {
            case WEST:
                return leader.getX() > follower.getX();
            case EAST:
                return leader.getX() < follower.getX();
            case NORTH:
                return leader.getY() > follower.getY();
            case SOUTH:
                return leader.getY() < follower.getY();
            default:
                return false;
        }
    }

    public boolean isAtSafeDistance(Vehicle vehicle, Vehicle other) {
        Point2D vehiclePos = new Point2D(vehicle.getX(), vehicle.getY());
        Point2D otherPos = new Point2D(other.getX(), other.getY());
        
        double distance = calculateDistance(vehiclePos, otherPos);
        double safeDistance = (vehicle.getType() == VehicleType.EMERGENCY) 
            ? SAFE_DISTANCE * EMERGENCY_CLEARANCE_MULTIPLIER
            : SAFE_DISTANCE;
        
        return distance >= safeDistance;
    }

    private double calculateDistance(Point2D point1, Point2D point2) {
        return Math.sqrt(
            Math.pow(point2.getX() - point1.getX(), 2) + 
            Math.pow(point2.getY() - point1.getY(), 2)
        );
    }

    public int getCurrentIntersectionId(Vehicle vehicle, MotorwayViewController controller) {
        double width = controller.getSimulationPane().getWidth();
        
        for (int ind = MotorwayConstants.FIRST_INTERSECTION; ind <= MotorwayConstants.TOTAL_INTERSECTIONS; ind++) {
            double centerX = controller.getIntersectionCenterX(ind, width);
            
            if (vehicle.getX() > centerX - width / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR && 
                vehicle.getX() < centerX + width / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR) {
                return ind;
            }
        }
        return NO_INTERSECTION;
    }

    public int getLightIdForIntersection(int intersectionId, boolean isUpperLane) {
        if (isUpperLane) {
            return switch (intersectionId) {
                case MotorwayConstants.INTERSECTION_2 -> MotorwayConstants.TRAFFIC_LIGHT_3;
                case MotorwayConstants.INTERSECTION_3 -> MotorwayConstants.TRAFFIC_LIGHT_5;
                case MotorwayConstants.INTERSECTION_4 -> MotorwayConstants.TRAFFIC_LIGHT_6;
                default -> NO_INTERSECTION;
            };
        } else {
            return switch (intersectionId) {
                case MotorwayConstants.INTERSECTION_1 -> MotorwayConstants.TRAFFIC_LIGHT_1;
                case MotorwayConstants.INTERSECTION_2 -> MotorwayConstants.TRAFFIC_LIGHT_2;
                case MotorwayConstants.INTERSECTION_3 -> MotorwayConstants.TRAFFIC_LIGHT_4;
                default -> NO_INTERSECTION;
            };
        }
    }

    public boolean hasEmergencyFollower(Vehicle vehicle, Collection<Vehicle> allVehicles) {
        return allVehicles.stream()
            .filter(v -> v.getType() == VehicleType.EMERGENCY)
            .filter(v -> !v.equals(vehicle))
            .filter(v -> !v.isFinished())
            .anyMatch(emergencyVehicle -> isFollowingBehind(emergencyVehicle, vehicle));
    }

    private boolean isFollowingBehind(Vehicle follower, Vehicle leader) {
        Point2D followerPos = new Point2D(follower.getX(), follower.getY());
        Point2D leaderPos = new Point2D(leader.getX(), leader.getY());
        
        double distance = calculateDistance(followerPos, leaderPos);
        
        if (distance > SAFE_DISTANCE) {
            return false;
        }
        
        switch (leader.getOrigin()) {
            case WEST:
                return follower.getX() < leader.getX();
            case EAST:
                return follower.getX() > leader.getX();
            case NORTH:
                return follower.getY() < leader.getY();
            case SOUTH:
                return follower.getY() > leader.getY();
            default:
                return false;
        }
    }

    public void activateEmergencyProtocol(Vehicle emergencyVehicle) {
        log.info("emergency_protocol_activated vehicleId={} type={}", 
            emergencyVehicle.getId(), emergencyVehicle.getType());
        
        int currentIntersection = (emergencyVehicle.getTargetIntersection() != null) 
            ? emergencyVehicle.getTargetIntersection().getId()
            : NO_INTERSECTION;
            
        if (currentIntersection != NO_INTERSECTION) {
            int lightId = getLightIdForIntersection(currentIntersection, emergencyVehicle.getOrigin().name().contains("WEST"));
            if (lightId != NO_INTERSECTION) {
                trafficLightController.setEmergencyGreen(lightId, true);
                log.info("emergency_light_override lightId={} vehicleId={}", lightId, emergencyVehicle.getId());
            }
        }
    }
}