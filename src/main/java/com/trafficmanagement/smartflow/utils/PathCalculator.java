package com.trafficmanagement.smartflow.utils;

import com.trafficmanagement.smartflow.controller.MotorwayViewController;
import com.trafficmanagement.smartflow.data.enums.Direction;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

import static com.trafficmanagement.smartflow.utils.IntersectionConstants.*;
import static com.trafficmanagement.smartflow.utils.MotorwayConstants.*;

public class PathCalculator {

    public static List<Point2D> calculateIntersectionPath(Direction origin, VehicleMovement movement, 
                                                         double width, double height) {
        if (width == 0 || height == 0) {
            return List.of();
        }

        double streetW = Math.min(width, height) / STREET_WIDTH_DIVISOR;
        final double STOP_GAP = STOP_LINE_GAP;
        
        double N_IN_X = width / 2 - streetW / LANE_OFFSET_DIVISOR;
        double N_OUT_X = width / 2 + streetW / LANE_OFFSET_DIVISOR;
        double S_IN_X = width / 2 + streetW / LANE_OFFSET_DIVISOR;
        double S_OUT_X = width / 2 - streetW / LANE_OFFSET_DIVISOR;
        double E_IN_Y = height / 2 - streetW / LANE_OFFSET_DIVISOR;
        double E_OUT_Y = height / 2 + streetW / LANE_OFFSET_DIVISOR;
        double W_IN_Y = height / 2 + streetW / LANE_OFFSET_DIVISOR;
        double W_OUT_Y = height / 2 - streetW / LANE_OFFSET_DIVISOR;

        Point2D stopN = new Point2D(N_IN_X, height / 2 - streetW / 2 - STOP_GAP);
        Point2D stopS = new Point2D(S_IN_X, height / 2 + streetW / 2 + STOP_GAP);
        Point2D stopE = new Point2D(width / 2 + streetW / 2 + STOP_GAP, E_IN_Y);
        Point2D stopW = new Point2D(width / 2 - streetW / 2 - STOP_GAP, W_IN_Y);

        Point2D exitN = new Point2D(N_OUT_X, -ENTRY_EXIT_OFFSET);
        Point2D exitS = new Point2D(S_OUT_X, height + ENTRY_EXIT_OFFSET);
        Point2D exitE = new Point2D(width + ENTRY_EXIT_OFFSET, E_OUT_Y);
        Point2D exitW = new Point2D(-ENTRY_EXIT_OFFSET, W_OUT_Y);
        
        if (movement.equals(VehicleMovement.U_TURN)) {
            return calculateUTurnPath(origin, stopN, stopS, stopE, stopW, exitN, exitS, exitE, exitW, width, height, STOP_GAP, N_IN_X, N_OUT_X, S_IN_X, S_OUT_X, E_IN_Y, E_OUT_Y, W_IN_Y, W_OUT_Y);
        }

        return calculateRegularPath(origin, movement, stopN, stopS, stopE, stopW, exitN, exitS, exitE, exitW, width, height, STOP_GAP, N_IN_X, S_IN_X, E_OUT_Y, W_OUT_Y);
    }

    private static List<Point2D> calculateUTurnPath(Direction origin, Point2D stopN, Point2D stopS, Point2D stopE, Point2D stopW, 
                                                   Point2D exitN, Point2D exitS, Point2D exitE, Point2D exitW, 
                                                   double width, double height, double STOP_GAP, 
                                                   double N_IN_X, double N_OUT_X, double S_IN_X, double S_OUT_X, 
                                                   double E_IN_Y, double E_OUT_Y, double W_IN_Y, double W_OUT_Y) {
        return switch (origin) {
            case NORTH -> List.of(
                new Point2D(N_IN_X, -ENTRY_EXIT_OFFSET), 
                stopN, 
                new Point2D(N_OUT_X, stopN.getY() + STOP_GAP),
                exitN
            );
            case SOUTH -> List.of(
                new Point2D(S_IN_X, height + ENTRY_EXIT_OFFSET), 
                stopS,
                new Point2D(S_OUT_X, stopS.getY() - STOP_GAP), 
                exitS
            );
            case EAST -> List.of(
                new Point2D(width + ENTRY_EXIT_OFFSET, E_IN_Y), 
                stopE,
                new Point2D(stopE.getX() - STOP_GAP, E_OUT_Y), 
                exitE
            );
            case WEST -> List.of(
                new Point2D(-ENTRY_EXIT_OFFSET, W_IN_Y), 
                stopW, 
                new Point2D(stopW.getX() + STOP_GAP, W_OUT_Y),
                exitW
            );
            default -> List.of();
        };
    }

    private static List<Point2D> calculateRegularPath(Direction origin, VehicleMovement movement,
                                                     Point2D stopN, Point2D stopS, Point2D stopE, Point2D stopW,
                                                     Point2D exitN, Point2D exitS, Point2D exitE, Point2D exitW,
                                                     double width, double height, double STOP_GAP,
                                                     double N_IN_X, double S_IN_X, double E_OUT_Y, double W_OUT_Y) {
        return switch (origin) {
            case NORTH -> calculateNorthPath(movement, stopN, exitS, exitW, exitE, N_IN_X, stopW, stopE, STOP_GAP, E_OUT_Y);
            case SOUTH -> calculateSouthPath(movement, stopS, exitN, exitE, exitW, S_IN_X, stopE, stopW, STOP_GAP, W_OUT_Y);
            case EAST -> calculateEastPath(movement, stopE, exitW, exitN, exitS, width, height, STOP_GAP, E_OUT_Y);
            case WEST -> calculateWestPath(movement, stopW, exitE, exitS, exitN, width, height, STOP_GAP, W_OUT_Y);
            default -> List.of();
        };
    }

    private static List<Point2D> calculateNorthPath(VehicleMovement movement, Point2D stopN, Point2D exitS, Point2D exitW, Point2D exitE, 
                                                   double N_IN_X, Point2D stopW, Point2D stopE, double STOP_GAP, double E_OUT_Y) {
        Point2D startN = new Point2D(N_IN_X, -ENTRY_EXIT_OFFSET);
        Point2D enterN = new Point2D(N_IN_X, stopN.getY() + STOP_GAP);
        
        return switch (movement) {
            case STRAIGHT -> List.of(startN, stopN, new Point2D(N_IN_X, exitS.getY() - ENTRY_EXIT_OFFSET), exitS);
            case TURN_RIGHT -> List.of(startN, stopN, enterN, new Point2D(stopW.getX(), exitW.getY()), exitW);
            case TURN_LEFT -> List.of(startN, stopN, new Point2D(N_IN_X, E_OUT_Y), new Point2D(stopE.getX(), E_OUT_Y), exitE);
            default -> List.of();
        };
    }

    private static List<Point2D> calculateSouthPath(VehicleMovement movement, Point2D stopS, Point2D exitN, Point2D exitE, Point2D exitW,
                                                   double S_IN_X, Point2D stopE, Point2D stopW, double STOP_GAP, double W_OUT_Y) {
        Point2D startS = new Point2D(S_IN_X, exitN.getY() + ENTRY_EXIT_OFFSET + exitN.getY());
        Point2D enterS = new Point2D(S_IN_X, stopS.getY() - STOP_GAP);
        
        return switch (movement) {
            case STRAIGHT -> List.of(startS, stopS, new Point2D(S_IN_X, exitN.getY() + ENTRY_EXIT_OFFSET), exitN);
            case TURN_RIGHT -> List.of(startS, stopS, enterS, new Point2D(stopE.getX(), exitE.getY()), exitE);
            case TURN_LEFT -> List.of(startS, stopS, new Point2D(S_IN_X, W_OUT_Y), new Point2D(stopW.getX(), W_OUT_Y), exitW);
            default -> List.of();
        };
    }

    private static List<Point2D> calculateEastPath(VehicleMovement movement, Point2D stopE, Point2D exitW, Point2D exitN, Point2D exitS,
                                                  double width, double height, double STOP_GAP, double E_IN_Y) {
        Point2D startE = new Point2D(width + ENTRY_EXIT_OFFSET, E_IN_Y);
        Point2D enterE = new Point2D(stopE.getX() - STOP_GAP, E_IN_Y);
        
        return switch (movement) {
            case STRAIGHT -> List.of(startE, stopE, new Point2D(exitW.getX() + ENTRY_EXIT_OFFSET, E_IN_Y), exitW);
            case TURN_RIGHT -> List.of(startE, stopE, new Point2D(exitN.getX(), E_IN_Y), new Point2D(exitN.getX(), exitN.getY() + ENTRY_EXIT_OFFSET), exitN);
            case TURN_LEFT -> List.of(startE, stopE, enterE, new Point2D(exitS.getX(), exitS.getY() - ENTRY_EXIT_OFFSET), exitS);
            default -> List.of();
        };
    }

    private static List<Point2D> calculateWestPath(VehicleMovement movement, Point2D stopW, Point2D exitE, Point2D exitS, Point2D exitN,
                                                  double width, double height, double STOP_GAP, double W_IN_Y) {
        Point2D startW = new Point2D(-ENTRY_EXIT_OFFSET, W_IN_Y);
        Point2D enterW = new Point2D(stopW.getX() + STOP_GAP, W_IN_Y);
        
        return switch (movement) {
            case STRAIGHT -> List.of(startW, stopW, new Point2D(exitE.getX() - ENTRY_EXIT_OFFSET, W_IN_Y), exitE);
            case TURN_RIGHT -> List.of(startW, stopW, new Point2D(exitS.getX(), W_IN_Y), new Point2D(exitS.getX(), exitS.getY() - ENTRY_EXIT_OFFSET), exitS);
            case TURN_LEFT -> List.of(startW, stopW, enterW, new Point2D(exitN.getX(), exitN.getY() + ENTRY_EXIT_OFFSET), exitN);
            default -> List.of();
        };
    }

    public static List<Point2D> calculateMotorwayPath(Vehicle vehicle, MotorwayViewController controller) {
        double width = controller.getSimulationPane().getWidth();
        double height = controller.getSimulationPane().getHeight();
        
        if (width == 0 || height == 0) return List.of();

        double motorwayY = (height - (LANE_HEIGHT * TOTAL_LANES)) / INTERSECTION_WIDTH_DIVISOR;
        double startY = getMotorwayLaneY(vehicle.getLane(), motorwayY);

        if (vehicle.getMovement() == VehicleMovement.STRAIGHT) {
            return calculateStraightMotorwayPath(vehicle.getOrigin(), startY, width);
        } else {
            return calculateTurnMotorwayPath(vehicle, startY, width, height, controller);
        }
    }

    private static List<Point2D> calculateStraightMotorwayPath(Direction origin, double startY, double width) {
        Point2D start = new Point2D(
            origin == Direction.WEST ? VEHICLE_PATH_OFFSET : width + VEHICLE_START_OFFSET, 
            startY
        );
        Point2D end = new Point2D(
            origin == Direction.WEST ? width + VEHICLE_OFFSET : -VEHICLE_OFFSET, 
            startY
        );
        return List.of(start, end);
    }

    private static List<Point2D> calculateTurnMotorwayPath(Vehicle vehicle, double startY, double width, double height, MotorwayViewController controller) {
        List<Point2D> path = new ArrayList<>();
        Point2D startPoint, stopPoint, turnPoint;
        double intersectionCenterX = controller.getIntersectionCenterX(
            vehicle.getTargetIntersection().getId(), 
            width
        );

        if (vehicle.getOrigin() == Direction.WEST) {
            startPoint = new Point2D(VEHICLE_PATH_OFFSET, startY);
            stopPoint = new Point2D(
                intersectionCenterX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR - STOP_LINE_OFFSET,
                startY
            );
            path.addAll(List.of(startPoint, stopPoint));
            turnPoint = new Point2D(intersectionCenterX, startY);
            path.add(turnPoint);

            switch (vehicle.getMovement()) {
                case TURN_RIGHT -> path.add(new Point2D(intersectionCenterX, height + VEHICLE_OFFSET));
                case TURN_LEFT -> path.add(new Point2D(intersectionCenterX, -VEHICLE_OFFSET));
                case U_TURN -> {
                    path.add(new Point2D(
                        intersectionCenterX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR - U_TURN_OFFSET,
                        startY
                    ));
                    path.add(new Point2D(
                        intersectionCenterX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR - U_TURN_OFFSET,
                        getMotorwayLaneY(Direction.SECOND_RAIL, (height - (LANE_HEIGHT * TOTAL_LANES)) / INTERSECTION_WIDTH_DIVISOR)
                    ));
                }
            }
        } else {
            startPoint = new Point2D(width + VEHICLE_OFFSET, startY);
            stopPoint = new Point2D(
                intersectionCenterX + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR + STOP_LINE_OFFSET,
                startY
            );
            path.addAll(List.of(startPoint, stopPoint));
            turnPoint = new Point2D(intersectionCenterX, startY);
            path.add(turnPoint);

            switch (vehicle.getMovement()) {
                case TURN_RIGHT -> path.add(new Point2D(intersectionCenterX, -VEHICLE_OFFSET));
                case TURN_LEFT -> path.add(new Point2D(intersectionCenterX, height + VEHICLE_OFFSET));
                case U_TURN -> {
                    path.add(new Point2D(
                        intersectionCenterX + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR + U_TURN_OFFSET,
                        startY
                    ));
                    path.add(new Point2D(
                        intersectionCenterX + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR + U_TURN_OFFSET,
                        getMotorwayLaneY(Direction.SECOND_RAIL, (height - (LANE_HEIGHT * TOTAL_LANES)) / INTERSECTION_WIDTH_DIVISOR)
                    ));
                }
            }
        }
        return path;
    }

    private static double getMotorwayLaneY(Direction lane, double motorwayY) {
        double laneOffset = LANE_1_OFFSET;
        if (lane == Direction.SECOND_RAIL) laneOffset = LANE_2_OFFSET;
        if (lane == Direction.THIRD_RAIL) laneOffset = LANE_3_OFFSET;
        
        return (lane == Direction.WEST || lane == Direction.EAST) 
            ? motorwayY + (VehicleConstants.LANE_3_MULTIPLIER - laneOffset) * LANE_HEIGHT
            : motorwayY + (VehicleConstants.LANE_3_MULTIPLIER * LANE_HEIGHT) + (laneOffset * LANE_HEIGHT);
    }

    private PathCalculator() {
    }
}