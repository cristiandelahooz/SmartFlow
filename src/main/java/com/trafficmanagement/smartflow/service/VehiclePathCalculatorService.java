package com.trafficmanagement.smartflow.service;

import com.trafficmanagement.smartflow.data.enums.Locations;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import com.trafficmanagement.smartflow.utils.MotorwayConstants;
import javafx.geometry.Point2D;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.trafficmanagement.smartflow.utils.VehicleConstants.*;

@Slf4j
public class VehiclePathCalculatorService {

    public List<Point2D> calculateTrafficLightPath(Vehicle vehicle, double width, double height) {
        int finalIntersectionId = (vehicle.getTargetIntersection() != null) 
            ? vehicle.getTargetIntersection().getId() 
            : COUNTER_START;

        List<Integer> trafficLightPath = new ArrayList<>();
        
        if (vehicle.getOrigin() == Locations.WEST) {
            addWestboundTrafficLights(trafficLightPath, finalIntersectionId, width);
        } else {
            addEastboundTrafficLights(trafficLightPath, finalIntersectionId, width);
        }
        
        log.debug("calculated_traffic_light_path vehicleId={} origin={} path={}", 
            vehicle.getId(), vehicle.getOrigin(), trafficLightPath);
            
        return convertToPoints(trafficLightPath, width, height);
    }

    private void addWestboundTrafficLights(List<Integer> trafficLightPath, int finalIntersectionId, double width) {
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_2) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_3);
        }
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_3) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_5);
        }
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_4) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_6);
        }
    }

    private void addEastboundTrafficLights(List<Integer> trafficLightPath, int finalIntersectionId, double width) {
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_3) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_4);
        }
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_2) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_2);
        }
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_1) {
            trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_1);
        }
    }

    private List<Point2D> convertToPoints(List<Integer> trafficLightIds, double width, double height) {
        List<Point2D> points = new ArrayList<>();
        
        for (Integer lightId : trafficLightIds) {
            Point2D lightPosition = calculateTrafficLightPosition(lightId, width, height);
            points.add(lightPosition);
        }
        
        return points;
    }

    private Point2D calculateTrafficLightPosition(int lightId, double width, double height) {
        double motorwayY = (height - (MotorwayConstants.LANE_HEIGHT * MotorwayConstants.TOTAL_LANES)) / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
        
        double x = switch (lightId) {
            case MotorwayConstants.TRAFFIC_LIGHT_1 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_1, width) + MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.TRAFFIC_LIGHT_2 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_2, width) + MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.TRAFFIC_LIGHT_3 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_2, width) - MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.TRAFFIC_LIGHT_4 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_3, width) + MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.TRAFFIC_LIGHT_5 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_3, width) - MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.TRAFFIC_LIGHT_6 -> getIntersectionCenterX(MotorwayConstants.INTERSECTION_4, width) - MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            default -> 0;
        };
        
        return new Point2D(x, motorwayY);
    }

    private double getIntersectionCenterX(int intersectionId, double totalMotorwayWidth) {
        double gapFromCenter = totalMotorwayWidth / MotorwayConstants.MOTORWAY_GAP_FROM_CENTER_DIVISOR;
        
        return switch (intersectionId) {
            case MotorwayConstants.INTERSECTION_1 -> MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            case MotorwayConstants.INTERSECTION_2 -> totalMotorwayWidth / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR - gapFromCenter;
            case MotorwayConstants.INTERSECTION_3 -> totalMotorwayWidth / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR + gapFromCenter;
            case MotorwayConstants.INTERSECTION_4 -> totalMotorwayWidth - MotorwayConstants.INTERSECTION_WIDTH / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR;
            default -> 0;
        };
    }

    public Point2D getDynamicStopPoint(Point2D baseStopLine, Locations origin, int queuePosition) {
        double offset = queuePosition * VEHICLE_SPACING;
        
        return switch (origin) {
            case NORTH -> new Point2D(baseStopLine.getX(), baseStopLine.getY() - offset);
            case SOUTH -> new Point2D(baseStopLine.getX(), baseStopLine.getY() + offset);
            case EAST -> new Point2D(baseStopLine.getX() + offset, baseStopLine.getY());
            case WEST -> new Point2D(baseStopLine.getX() - offset, baseStopLine.getY());
            default -> baseStopLine;
        };
    }

    public boolean isApproachingIntersection(Vehicle vehicle, Point2D intersectionCenter, double threshold) {
        Point2D vehiclePos = new Point2D(vehicle.getX(), vehicle.getY());
        double distance = Math.sqrt(
            Math.pow(intersectionCenter.getX() - vehiclePos.getX(), 2) + 
            Math.pow(intersectionCenter.getY() - vehiclePos.getY(), 2)
        );
        return distance <= threshold;
    }

    public List<Point2D> optimizePathForVehicle(List<Point2D> originalPath, Vehicle vehicle) {
        if (originalPath.isEmpty()) {
            return originalPath;
        }
        
        List<Point2D> optimizedPath = new ArrayList<>(originalPath);
        
        if (vehicle.getType() == com.trafficmanagement.smartflow.data.enums.VehicleType.EMERGENCY) {
            optimizedPath = createEmergencyOptimizedPath(optimizedPath);
        }
        
        return optimizedPath;
    }

    private List<Point2D> createEmergencyOptimizedPath(List<Point2D> path) {
        return path;
    }
}