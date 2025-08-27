package com.trafficmanagement.smartflow.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.trafficmanagement.smartflow.controller.MotorwayViewController;
import com.trafficmanagement.smartflow.controller.IntersectionViewController;
import com.trafficmanagement.smartflow.controller.TrafficLightController;
import com.trafficmanagement.smartflow.data.enums.Direction;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.utils.MotorwayConstants;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Vehicle implements Runnable {
  private static final AtomicInteger idCounter = new AtomicInteger(0);
  private static final double SAFE_DISTANCE = 50.0;
  private final int id;
  private final VehicleType type;
  private final Direction origin;
  private final VehicleMovement movement;
  private final TrafficManager trafficManager;
  private final double normalSpeed = 2;
  private final double emergencyClearSpeed = 7.4;
  private volatile boolean running = true;
  private Direction lane;
  private MotorwayViewController motorwayViewController;
  private IntersectionViewController intersectionViewController;
  private long arrivalTime;
  private double x;
  private double y;
  private volatile boolean finished = false;
  private List<Integer> trafficLightPath;
  private int nextTrafficLightIndex = 0;
  private int lastKnownIntersectionId = -1;

  private TrafficLightController trafficLightController;
  private IntersectionStateManager intersectionStateManager;

  public Vehicle(
      VehicleType type, Direction origin, VehicleMovement movement, Intersection intersection) {
    this.id = idCounter.incrementAndGet();
    this.type = type;
    this.origin = origin;
    this.movement = movement;
    this.trafficManager = intersection;
    log.info("vehicle_created vehicleId={} type={} origin={} movement={} intersectionId={}",
        id, type, origin, movement, intersection.getId());
  }

  public Vehicle(
      VehicleType type,
      Direction origin,
      VehicleMovement movement,
      MotorwayIntersection targetIntersection) {
    this.id = idCounter.incrementAndGet();
    this.type = type;
    this.origin = origin;
    this.movement = movement;
    this.trafficManager = targetIntersection;
    log.info("vehicle_created vehicleId={} type={} origin={} movement={} targetIntersectionId={}",
        id, type, origin, movement, targetIntersection != null ? targetIntersection.getId(): -1);
  }

  @Override
  public void run() {
    if (isFinished()) return;
    
    log.info("vehicle_thread_started vehicleId={} type={} origin={} movement={}",
        id, type, origin, movement);

    try {
      if (intersectionViewController != null) {
        runSimpleIntersectionLogic();
      } else if (motorwayViewController != null) {
        runMotorwayLogic();
      } else {
        this.finished = true;
        log.warn("vehicle_no_controller vehicleId={} finished=true", id);
      }
    } finally {
      log.info("vehicle_thread_completed vehicleId={} finished={}", id, finished);
    }
  }

  private void runSimpleIntersectionLogic() {
    try {
      List<Point2D> path = getPathFromController();
      if (path.isEmpty()) {
        this.finished = true;
        return;
      }

      trafficManager.addToQueue(this);
      int currentPathSegment = 1;
      boolean crossingStarted = false;
      boolean hasLeftIntersection = false;

      while (running && currentPathSegment < path.size()) {
        Point2D target = path.get(currentPathSegment);

        if (!crossingStarted) {
          if (trafficManager.isMyTurn(this)) {
            crossingStarted = true;
            trafficManager.startCrossing(this);
            target = path.get(currentPathSegment);
            log.info("vehicle_crossing_started vehicleId={} type={} intersectionId={}", 
                id, type, trafficManager.getId());
          } else {
            target = getDynamicStopPoint(path.get(1));
          }
        }

        moveTo(target, this.type == VehicleType.EMERGENCY || trafficManager.isEmergencyActive());

        if (distanceTo(target) < 1.5) {
          if (crossingStarted) {
            currentPathSegment++;
          }
        }

        if (crossingStarted && !hasLeftIntersection && currentPathSegment == path.size() - 1) {
          trafficManager.leaveIntersection(this);
          hasLeftIntersection = true;
          log.info("vehicle_crossing_completed vehicleId={} type={} intersectionId={}", 
              id, type, trafficManager.getId());
        }

        Thread.sleep(16);
      }
    } catch (InterruptedException e) {
      log.info("vehicle_thread_interrupted vehicleId={} type={}", id, type);
      Thread.currentThread().interrupt();
    } finally {
      this.finished = true;
      log.debug("vehicle_simple_intersection_finished vehicleId={}", id);
    }
  }

  private void runMotorwayLogic() {
    try {
      List<Point2D> path = getPathFromController();
      if (path.isEmpty()) {
        this.finished = true;
        return;
      }

      calculateTrafficLightPath();
      int currentPathSegment = 1;

      while (running && currentPathSegment < path.size()) {

        Vehicle leader = motorwayViewController.findLeadingVehicle(this);
        if (leader != null
            && distanceTo(new Point2D(leader.getX(), leader.getY())) < SAFE_DISTANCE) {
          updateIntersectionState();
          Thread.sleep(16);
          continue;
        }

        if (isApproachingTrafficLight()) {
          int lightId = trafficLightPath.get(nextTrafficLightIndex);
          Point2D stopLine =
              motorwayViewController.getStopLineForLight(
                  lightId,
                  origin,
                  lane,
                  motorwayViewController.getSimulationPaneWidth(),
                  motorwayViewController.getSimulationPane().getHeight());
          boolean stopLineIsInFront =
              (origin == Direction.WEST && getX() < stopLine.getX())
                  || (origin == Direction.EAST && getX() > stopLine.getX());

          if (stopLineIsInFront && distanceTo(stopLine) > 2.0) {
            moveTo(stopLine, this.type == VehicleType.EMERGENCY);
            updateIntersectionState();
            Thread.sleep(16);
            continue;
          }

          if (stopLineIsInFront) {
            boolean canGo = false;
            if (this.type == VehicleType.EMERGENCY) {
              canGo = true;
              log.debug("emergency_vehicle_override vehicleId={} lightId={} canGo=true", id, lightId);
              if ((movement == VehicleMovement.TURN_LEFT || movement == VehicleMovement.U_TURN)
                  && isAtFinalTurn(lightId)) {
                if (intersectionStateManager.isOpposingTrafficCrossing(
                    getTargetIntersection().getId(), this)) {
                  canGo = false;
                  log.warn("emergency_blocked_by_opposing_traffic vehicleId={} lightId={} intersectionId={}", 
                      id, lightId, getTargetIntersection().getId());
                }
              }
            } else {
              boolean isLightGreen = trafficLightController.isGreen(lightId);
              canGo = isLightGreen;
              if (!isLightGreen) {
                if (motorwayViewController.findEmergencyFollower(this) != null) {
                  canGo = true;
                }
              }
              if (canGo
                  && (movement == VehicleMovement.TURN_LEFT || movement == VehicleMovement.U_TURN)
                  && isAtFinalTurn(lightId)) {
                if (intersectionStateManager.isOpposingTrafficCrossing(
                    getTargetIntersection().getId(), this)) {
                  canGo = false;
                }
              }
            }

            if (!canGo) {
              updateIntersectionState();
              Thread.sleep(16);
              continue;
            }
          }
          nextTrafficLightIndex++;
        }

        Point2D currentTarget = path.get(currentPathSegment);
        moveTo(currentTarget, this.type == VehicleType.EMERGENCY);

        if (distanceTo(currentTarget) < 2.0) {
          currentPathSegment++;
        }

        updateIntersectionState();
        Thread.sleep(16);
      }

      if (running && this.movement == VehicleMovement.U_TURN) {
        final Vehicle self = this;
        Platform.runLater(() -> motorwayViewController.spawnStraightVehicleFromUTurn(self));
      }

    } catch (InterruptedException e) {
      log.info("vehicle_thread_interrupted vehicleId={} type={}", id, type);
      Thread.currentThread().interrupt();
    } finally {
      if (intersectionStateManager != null) {
        if (lastKnownIntersectionId != -1) {
          intersectionStateManager.vehicleExitsStraightZone(lastKnownIntersectionId, this);
        }
      }
      if (trafficManager != null) trafficManager.leaveIntersection(this);
      this.finished = true;
      log.debug("vehicle_Motorway_finished vehicleId={}", id);
    }
  }

  private boolean isAtFinalTurn(int lightId) {
    if (getTargetIntersection() == null) return false;
    int targetIntersectionId = getTargetIntersection().getId();

    if ((lightId == 1 && targetIntersectionId == 1)
        || ((lightId == 2 || lightId == 3) && targetIntersectionId == 2)
        || ((lightId == 4 || lightId == 5) && targetIntersectionId == 3)
        || (lightId == 6 && targetIntersectionId == 4)) {
      return true;
    }
    return false;
  }

  private void updateIntersectionState() {
    int currentIntersectionId = getMyCurrentIntersectionId();

    if (lastKnownIntersectionId != -1 && lastKnownIntersectionId != currentIntersectionId) {
      intersectionStateManager.vehicleExitsStraightZone(lastKnownIntersectionId, this);
    }

    if (currentIntersectionId != -1) {
      intersectionStateManager.vehicleEntersStraightZone(currentIntersectionId, this);
    }

    lastKnownIntersectionId = currentIntersectionId;
  }

  private int getMyCurrentIntersectionId() {
    for (int ind = 1; ind <= 4; ind++) {
      double centerX =
          motorwayViewController.getIntersectionCenterX(ind, motorwayViewController.getSimulationPaneWidth());
      double width = MotorwayConstants.INTERSECTION_WIDTH;
      if (this.x > centerX - width / 2 && this.x < centerX + width / 2) {
        return ind;
      }
    }
    return -1;
  }

  private boolean isApproachingTrafficLight() {
    return trafficLightPath != null && nextTrafficLightIndex < trafficLightPath.size();
  }

  private void calculateTrafficLightPath() {
    trafficLightPath = new ArrayList<>();
    int finalIntersectionId =
        (getTargetIntersection() != null) ? getTargetIntersection().getId() : 0;

    if (origin == Direction.WEST) {
      if (movement == VehicleMovement.STRAIGHT || movement == VehicleMovement.STRAIGH_AFTER_U_TURN) {
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                2, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(3);
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                3, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(5);
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                4, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(6);
      } else {
        if (finalIntersectionId >= 2) trafficLightPath.add(3);
        if (finalIntersectionId >= 3) trafficLightPath.add(5);
        if (finalIntersectionId >= 4) trafficLightPath.add(6);
      }
    } else {
      if (movement == VehicleMovement.STRAIGHT  || movement == VehicleMovement.STRAIGH_AFTER_U_TURN) {
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                3, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(4);
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                2, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(2);
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                1, motorwayViewController.getSimulationPaneWidth())) trafficLightPath.add(1);
      } else {
        if (finalIntersectionId <= 3) trafficLightPath.add(4);
        if (finalIntersectionId <= 2) trafficLightPath.add(2);
        if (finalIntersectionId <= 1) trafficLightPath.add(1);
      }
    }
  }

  private List<Point2D> getPathFromController() {
    if (motorwayViewController != null) {
      return motorwayViewController.calculateVehiclePath(this);
    } else if (intersectionViewController != null) {
      return intersectionViewController.getPath(origin, movement);
    }
    return new ArrayList<>();
  }

  private void moveTo(Point2D target, boolean emergency) {
    double currentSpeed = emergency ? this.emergencyClearSpeed : this.normalSpeed;

    if (distanceTo(target) < currentSpeed) {
      this.x = target.getX();
      this.y = target.getY();
    } else {
      double angle = Math.atan2(target.getY() - y, target.getX() - x);
      x += currentSpeed * Math.cos(angle);
      y += currentSpeed * Math.sin(angle);
    }
  }

  private Point2D getDynamicStopPoint(Point2D baseStopLine) {
    if (trafficManager == null) return baseStopLine;
    int positionInQueue = trafficManager.getPositionInQueue(this);
    double vehicleSpacing = 30.0;
    double offset = positionInQueue * vehicleSpacing;

    return switch (origin) {
      case NORTH -> new Point2D(baseStopLine.getX(), baseStopLine.getY() - offset);
      case SOUTH -> new Point2D(baseStopLine.getX(), baseStopLine.getY() + offset);
      case EAST -> new Point2D(baseStopLine.getX() + offset, baseStopLine.getY());
      case WEST -> new Point2D(baseStopLine.getX() - offset, baseStopLine.getY());
      default -> baseStopLine;
    };
  }

  public double distanceTo(Point2D target) {
    return Math.sqrt(Math.pow(target.getX() - x, 2) + Math.pow(target.getY() - y, 2));
  }

  public void setController(IntersectionViewController controller) {
    this.intersectionViewController = controller;
  }

  public void setController(MotorwayViewController controller) {
    this.motorwayViewController = controller;
  }

  public void setPosition(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void stop() {
    this.running = false;
    log.info("vehicle_stopped vehicleId={} type={}", id, type);
  }

  public MotorwayIntersection getTargetIntersection() {
    if (trafficManager instanceof MotorwayIntersection motorwayIntersection)
      return motorwayIntersection;
    return null;
  }
}
