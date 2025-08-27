package com.trafficmanagement.smartflow.utils;

import com.trafficmanagement.smartflow.data.enums.Direction;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

import static com.trafficmanagement.smartflow.utils.IntersectionConstants.*;

public class GeometryUtils {

    public static double calculateDistance(Point2D point1, Point2D point2) {
        return Math.sqrt(Math.pow(point2.getX() - point1.getX(), 2) + Math.pow(point2.getY() - point1.getY(), 2));
    }

    public static Point2D calculateMidpoint(Point2D start, Point2D end) {
        return new Point2D(
            (start.getX() + end.getX()) / 2.0,
            (start.getY() + end.getY()) / 2.0
        );
    }

    public static boolean isPointInFront(Point2D vehicle, Point2D target, Direction direction) {
        return switch (direction) {
            case NORTH -> vehicle.getY() > target.getY();
            case SOUTH -> vehicle.getY() < target.getY();
            case EAST -> vehicle.getX() < target.getX();
            case WEST -> vehicle.getX() > target.getX();
            default -> false;
        };
    }

    public static Point2D calculateStopPosition(Direction origin, Point2D baseStop, int queuePosition) {
        double vehicleSpacing = VehicleConstants.VEHICLE_SPACING;
        double offset = queuePosition * vehicleSpacing;
        
        return switch (origin) {
            case NORTH -> new Point2D(baseStop.getX(), baseStop.getY() - offset);
            case SOUTH -> new Point2D(baseStop.getX(), baseStop.getY() + offset);
            case EAST -> new Point2D(baseStop.getX() + offset, baseStop.getY());
            case WEST -> new Point2D(baseStop.getX() - offset, baseStop.getY());
            default -> baseStop;
        };
    }

    public static List<Point2D> createStraightPath(Direction origin, double width, double height) {
        double streetWidth = Math.min(width, height) / STREET_WIDTH_DIVISOR;
        
        return switch (origin) {
            case NORTH -> List.of(
                new Point2D(width / 2 - streetWidth / LANE_OFFSET_DIVISOR, -ENTRY_EXIT_OFFSET),
                new Point2D(width / 2 - streetWidth / LANE_OFFSET_DIVISOR, height + ENTRY_EXIT_OFFSET)
            );
            case SOUTH -> List.of(
                new Point2D(width / 2 + streetWidth / LANE_OFFSET_DIVISOR, height + ENTRY_EXIT_OFFSET),
                new Point2D(width / 2 + streetWidth / LANE_OFFSET_DIVISOR, -ENTRY_EXIT_OFFSET)
            );
            case EAST -> List.of(
                new Point2D(width + ENTRY_EXIT_OFFSET, height / 2 - streetWidth / LANE_OFFSET_DIVISOR),
                new Point2D(-ENTRY_EXIT_OFFSET, height / 2 - streetWidth / LANE_OFFSET_DIVISOR)
            );
            case WEST -> List.of(
                new Point2D(-ENTRY_EXIT_OFFSET, height / 2 + streetWidth / LANE_OFFSET_DIVISOR),
                new Point2D(width + ENTRY_EXIT_OFFSET, height / 2 + streetWidth / LANE_OFFSET_DIVISOR)
            );
            default -> new ArrayList<>();
        };
    }

    public static List<Point2D> createTurnPath(Direction origin, VehicleMovement movement, 
                                               double width, double height) {
        double streetWidth = Math.min(width, height) / STREET_WIDTH_DIVISOR;
        List<Point2D> path = new ArrayList<>();
        
        Point2D center = new Point2D(width / 2, height / 2);
        
        switch (origin) {
            case NORTH:
                path.add(new Point2D(width / 2 - streetWidth / LANE_OFFSET_DIVISOR, -ENTRY_EXIT_OFFSET));
                path.add(center);
                if (movement == VehicleMovement.TURN_RIGHT) {
                    path.add(new Point2D(-ENTRY_EXIT_OFFSET, height / 2 - streetWidth / LANE_OFFSET_DIVISOR));
                } else if (movement == VehicleMovement.TURN_LEFT) {
                    path.add(new Point2D(width + ENTRY_EXIT_OFFSET, height / 2 + streetWidth / LANE_OFFSET_DIVISOR));
                }
                break;
            case SOUTH:
                path.add(new Point2D(width / 2 + streetWidth / LANE_OFFSET_DIVISOR, height + ENTRY_EXIT_OFFSET));
                path.add(center);
                if (movement == VehicleMovement.TURN_RIGHT) {
                    path.add(new Point2D(width + ENTRY_EXIT_OFFSET, height / 2 + streetWidth / LANE_OFFSET_DIVISOR));
                } else if (movement == VehicleMovement.TURN_LEFT) {
                    path.add(new Point2D(-ENTRY_EXIT_OFFSET, height / 2 - streetWidth / LANE_OFFSET_DIVISOR));
                }
                break;
            case EAST:
                path.add(new Point2D(width + ENTRY_EXIT_OFFSET, height / 2 - streetWidth / LANE_OFFSET_DIVISOR));
                path.add(center);
                if (movement == VehicleMovement.TURN_RIGHT) {
                    path.add(new Point2D(width / 2 + streetWidth / LANE_OFFSET_DIVISOR, -ENTRY_EXIT_OFFSET));
                } else if (movement == VehicleMovement.TURN_LEFT) {
                    path.add(new Point2D(width / 2 - streetWidth / LANE_OFFSET_DIVISOR, height + ENTRY_EXIT_OFFSET));
                }
                break;
            case WEST:
                path.add(new Point2D(-ENTRY_EXIT_OFFSET, height / 2 + streetWidth / LANE_OFFSET_DIVISOR));
                path.add(center);
                if (movement == VehicleMovement.TURN_RIGHT) {
                    path.add(new Point2D(width / 2 - streetWidth / LANE_OFFSET_DIVISOR, height + ENTRY_EXIT_OFFSET));
                } else if (movement == VehicleMovement.TURN_LEFT) {
                    path.add(new Point2D(width / 2 + streetWidth / LANE_OFFSET_DIVISOR, -ENTRY_EXIT_OFFSET));
                }
                break;
        }
        
        return path;
    }

    public static boolean isWithinBounds(Point2D point, double width, double height) {
        return point.getX() >= 0 && point.getX() <= width && 
               point.getY() >= 0 && point.getY() <= height;
    }

    public static Point2D normalizeVector(Point2D vector) {
        double magnitude = Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
        if (magnitude == 0) {
            return new Point2D(0, 0);
        }
        return new Point2D(vector.getX() / magnitude, vector.getY() / magnitude);
    }

    private GeometryUtils() {
    }
}