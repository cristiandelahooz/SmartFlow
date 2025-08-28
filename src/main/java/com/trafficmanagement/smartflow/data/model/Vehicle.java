package com.trafficmanagement.smartflow.data.model;

import static com.trafficmanagement.smartflow.utils.VehicleConstants.*;

import com.trafficmanagement.smartflow.controller.IntersectionViewController;
import com.trafficmanagement.smartflow.controller.MotorwayViewController;
import com.trafficmanagement.smartflow.controller.TrafficLightController;
import com.trafficmanagement.smartflow.data.enums.Locations;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.utils.MotorwayConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Vehicle implements Runnable {
  private static final AtomicInteger idCounter = new AtomicInteger(COUNTER_START);
  private static final double SAFE_DISTANCE = 50.0;
  private final int id;
  private final VehicleType type;
  private final Locations origin;
  private final VehicleMovement movement;
  private final TrafficManager trafficManager;
  private final double normalSpeed = NORMAL_SPEED;
  private final double emergencyClearSpeed = EMERGENCY_SPEED;
  private volatile boolean running = true;
  private Locations lane;
  private MotorwayViewController motorwayViewController;
  private IntersectionViewController intersectionViewController;
  private long arrivalTime;
  private double x;
  private double y;
  private volatile boolean finished = false;
  private List<Integer> trafficLightPath;
  private int nextTrafficLightIndex = INITIAL_TRAFFIC_LIGHT_INDEX;
  private int lastKnownIntersectionId = NO_INTERSECTION;

  private TrafficLightController trafficLightController;
  private IntersectionStateManager intersectionStateManager;

  public Vehicle(
      VehicleType type, Locations origin, VehicleMovement movement, Intersection intersection) {
    this.id = idCounter.incrementAndGet();
    this.type = type;
    this.origin = origin;
    this.movement = movement;
    this.trafficManager = intersection;
    log.info(
        "vehicle_created vehicleId={} type={} origin={} movement={} intersectionId={}",
        id,
        type,
        origin,
        movement,
        intersection.getId());
  }

  public Vehicle(
      VehicleType type,
      Locations origin,
      VehicleMovement movement,
      MotorwayIntersection targetIntersection) {
    this.id = idCounter.incrementAndGet();
    this.type = type;
    this.origin = origin;
    this.movement = movement;
    this.trafficManager = targetIntersection;
    log.info(
        "vehicle_created vehicleId={} type={} origin={} movement={} targetIntersectionId={}",
        id,
        type,
        origin,
        movement,
        targetIntersection != null ? targetIntersection.getId() : -1);
  }

  @Override
  public void run() {
    if (isFinished()) return;

    log.info(
        "vehicle_thread_started vehicleId={} type={} origin={} movement={}",
        id,
        type,
        origin,
        movement);

    try {
      if (intersectionViewController != null) {
        runIntersection();
      } else if (motorwayViewController != null) {
        runMotorway();
      } else {
        this.finished = true;
        log.warn("vehicle_no_controller vehicleId={} finished=true", id);
      }
    } finally {
      log.info("vehicle_thread_completed vehicleId={} finished={}", id, finished);
    }
  }

  private void runIntersection() {
    try {
      List<Point2D> path = getPathFromController();
      if (path.isEmpty()) {
        this.finished = true;
        return;
      }

      trafficManager.addToQueue(this);
      int currentPathSegment = INITIAL_PATH_SEGMENT;
      boolean crossingStarted = false;
      boolean hasLeftIntersection = false;

      while (running && currentPathSegment < path.size()) {
        Point2D target = path.get(currentPathSegment);

        if (!crossingStarted) {
          if (trafficManager.isMyTurn(this)) {
            crossingStarted = true;
            trafficManager.startCrossing(this);
            target = path.get(currentPathSegment);
            log.info(
                "vehicle_crossing_started vehicleId={} type={} intersectionId={}",
                id,
                type,
                trafficManager.getId());
          } else {
            target = getDynamicStopPoint(path.get(INITIAL_PATH_SEGMENT));
          }
        }

        moveTo(target, this.type == VehicleType.EMERGENCY || trafficManager.isEmergencyActive());

        if (distanceTo(target) < TARGET_REACHED_THRESHOLD) {
          if (crossingStarted) {
            currentPathSegment++;
          }
        }

        if (crossingStarted && !hasLeftIntersection && currentPathSegment == path.size() - 1) {
          trafficManager.leaveIntersection(this);
          hasLeftIntersection = true;
          log.info(
              "vehicle_crossing_completed vehicleId={} type={} intersectionId={}",
              id,
              type,
              trafficManager.getId());
        }

        Thread.sleep(MOVEMENT_SLEEP_DURATION_MS);
      }
    } catch (InterruptedException e) {
      log.info("vehicle_thread_interrupted vehicleId={} type={}", id, type);
      Thread.currentThread().interrupt();
    } finally {
      this.finished = true;
      log.debug("vehicle_simple_intersection_finished vehicleId={}", id);
    }
  }

  private void runMotorway() {
    try {
      List<Point2D> path = getPathFromController();
      if (path.isEmpty()) {
        this.finished = true;
        return;
      }

      calculateTrafficLightPath();
      int currentPathSegment = INITIAL_PATH_SEGMENT;

      while (running && currentPathSegment < path.size()) {

        Vehicle leader = motorwayViewController.findLeadingVehicle(this);
        if (leader != null
            && distanceTo(new Point2D(leader.getX(), leader.getY())) < SAFE_DISTANCE) {
          updateIntersectionState();
          Thread.sleep(MOVEMENT_SLEEP_DURATION_MS);
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
              (origin == Locations.WEST && getX() < stopLine.getX())
                  || (origin == Locations.EAST && getX() > stopLine.getX());

          if (stopLineIsInFront && distanceTo(stopLine) > STOP_LINE_PROXIMITY) {
            moveTo(stopLine, this.type == VehicleType.EMERGENCY);
            updateIntersectionState();
            Thread.sleep(MOVEMENT_SLEEP_DURATION_MS);
            continue;
          }

          if (stopLineIsInFront) {
            boolean canGo = false;
            if (this.type == VehicleType.EMERGENCY) {
              canGo = true;
              log.debug(
                  "emergency_vehicle_override vehicleId={} lightId={} canGo=true", id, lightId);
              if ((movement == VehicleMovement.TURN_LEFT || movement == VehicleMovement.U_TURN)
                  && isAtFinalTurn(lightId)) {
                if (intersectionStateManager.isOpposingTrafficCrossing(
                    getTargetIntersection().getId(), this)) {
                  canGo = false;
                  log.warn(
                      "emergency_blocked_by_opposing_traffic vehicleId={} lightId={} intersectionId={}",
                      id,
                      lightId,
                      getTargetIntersection().getId());
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
              Thread.sleep(MOVEMENT_SLEEP_DURATION_MS);
              continue;
            }
          }
          nextTrafficLightIndex++;
        }

        Point2D currentTarget = path.get(currentPathSegment);
        moveTo(currentTarget, this.type == VehicleType.EMERGENCY);

        if (distanceTo(currentTarget) < STOP_LINE_PROXIMITY) {
          currentPathSegment++;
        }

        updateIntersectionState();
        Thread.sleep(MOVEMENT_SLEEP_DURATION_MS);
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
        if (lastKnownIntersectionId != NO_INTERSECTION) {
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

    if ((lightId == MotorwayConstants.TRAFFIC_LIGHT_1
            && targetIntersectionId == MotorwayConstants.INTERSECTION_1)
        || ((lightId == MotorwayConstants.TRAFFIC_LIGHT_2
                || lightId == MotorwayConstants.TRAFFIC_LIGHT_3)
            && targetIntersectionId == MotorwayConstants.INTERSECTION_2)
        || ((lightId == MotorwayConstants.TRAFFIC_LIGHT_4
                || lightId == MotorwayConstants.TRAFFIC_LIGHT_5)
            && targetIntersectionId == MotorwayConstants.INTERSECTION_3)
        || (lightId == MotorwayConstants.TRAFFIC_LIGHT_6
            && targetIntersectionId == MotorwayConstants.INTERSECTION_4)) {
      return true;
    }
    return false;
  }

  private void updateIntersectionState() {
    int currentIntersectionId = getMyCurrentIntersectionId();

    if (lastKnownIntersectionId != NO_INTERSECTION
        && lastKnownIntersectionId != currentIntersectionId) {
      intersectionStateManager.vehicleExitsStraightZone(lastKnownIntersectionId, this);
    }

    if (currentIntersectionId != NO_INTERSECTION) {
      intersectionStateManager.vehicleEntersStraightZone(currentIntersectionId, this);
    }

    lastKnownIntersectionId = currentIntersectionId;
  }

  private int getMyCurrentIntersectionId() {
    for (int ind = MotorwayConstants.FIRST_INTERSECTION;
        ind <= MotorwayConstants.TOTAL_INTERSECTIONS;
        ind++) {
      double centerX =
          motorwayViewController.getIntersectionCenterX(
              ind, motorwayViewController.getSimulationPaneWidth());
      double width = MotorwayConstants.INTERSECTION_WIDTH;
      if (this.x > centerX - width / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR
          && this.x < centerX + width / MotorwayConstants.INTERSECTION_WIDTH_DIVISOR) {
        return ind;
      }
    }
    return NO_INTERSECTION;
  }

  private boolean isApproachingTrafficLight() {
    return trafficLightPath != null && nextTrafficLightIndex < trafficLightPath.size();
  }

  private void calculateTrafficLightPath() {
    trafficLightPath = new ArrayList<>();
    int finalIntersectionId =
        (getTargetIntersection() != null) ? getTargetIntersection().getId() : COUNTER_START;

    if (origin == Locations.WEST) {
      if (movement == VehicleMovement.STRAIGHT
          || movement == VehicleMovement.STRAIGH_AFTER_U_TURN) {
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_2, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_3);
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_3, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_5);
        if (getX()
            < motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_4, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_6);
      } else {
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_2)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_3);
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_3)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_5);
        if (finalIntersectionId >= MotorwayConstants.INTERSECTION_4)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_6);
      }
    } else {
      if (movement == VehicleMovement.STRAIGHT
          || movement == VehicleMovement.STRAIGH_AFTER_U_TURN) {
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_3, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_4);
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_2, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_2);
        if (getX()
            > motorwayViewController.getIntersectionCenterX(
                MotorwayConstants.INTERSECTION_1, motorwayViewController.getSimulationPaneWidth()))
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_1);
      } else {
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_3)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_4);
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_2)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_2);
        if (finalIntersectionId <= MotorwayConstants.INTERSECTION_1)
          trafficLightPath.add(MotorwayConstants.TRAFFIC_LIGHT_1);
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
    double offset = positionInQueue * VEHICLE_SPACING;

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
