package com.trafficmanagement.smartflow.data.model;

import com.trafficmanagement.smartflow.data.enums.Locations;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class MotorwayIntersection implements TrafficManager {
  private final int id;
  private final ReentrantLock lock = new ReentrantLock(true);
  private final Map<Locations, Map<Locations, ConcurrentLinkedQueue<Vehicle>>> waitingLanes;
  private final LinkedList<Locations> laneQueue = new LinkedList<>();
  private final Set<Vehicle> crossingVehicles = ConcurrentHashMap.newKeySet();
  private volatile boolean emergencyActive = false;

  public MotorwayIntersection(int id) {
    this.id = id;
    this.waitingLanes = new EnumMap<>(Locations.class);
    for (Locations dir : Locations.getMotorwayDirections()) {
      Map<Locations, ConcurrentLinkedQueue<Vehicle>> lanes = new EnumMap<>(Locations.class);
      lanes.put(Locations.FIRST_RAIL, new ConcurrentLinkedQueue<>());
      lanes.put(Locations.SECOND_RAIL, new ConcurrentLinkedQueue<>());
      lanes.put(Locations.THIRD_RAIL, new ConcurrentLinkedQueue<>());
      waitingLanes.put(dir, lanes);
    }
  }

  @Override
  public void addToQueue(Vehicle vehicle) {
    waitingLanes.get(vehicle.getOrigin()).get(vehicle.getLane()).add(vehicle);
    log.info(
        "vehicle_queued vehicleId={} type={} origin={} lane={} intersectionId={} queuePosition={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        vehicle.getLane(),
        id,
        getPositionInQueue(vehicle));

    lock.lock();
    try {
      Locations originLane = vehicle.getOrigin();
      if (vehicle.getType() == VehicleType.EMERGENCY) {
        if (!this.emergencyActive) {
          this.emergencyActive = true;
          log.warn(
              "emergency_mode_activated intersectionId={} priorityLane={} vehicleId={}",
              id,
              originLane,
              vehicle.getId());
        }
        laneQueue.remove(originLane);
        laneQueue.addFirst(originLane);
      } else {
        if (!laneQueue.contains(originLane)) {
          laneQueue.addLast(originLane);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isMyTurn(Vehicle vehicle) {
    lock.lock();
    try {

      boolean otherDirectionIsCrossing =
          crossingVehicles.stream().anyMatch(v -> v.getOrigin() != vehicle.getOrigin());
      if (otherDirectionIsCrossing) {
        return false;
      }

      Locations activeLocations = laneQueue.peek();
      if (!vehicle.getOrigin().equals(activeLocations)) {
        return false;
      }

      boolean vehicleFromMyLaneIsCrossing =
          crossingVehicles.stream()
              .anyMatch(
                  v -> v.getOrigin() == vehicle.getOrigin() && v.getLane() == vehicle.getLane());
      if (vehicleFromMyLaneIsCrossing) {
        return false;
      }

      return Objects.equals(
          waitingLanes.get(vehicle.getOrigin()).get(vehicle.getLane()).peek(), vehicle);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void startCrossing(Vehicle vehicle) {
    lock.lock();
    try {
      if (waitingLanes.get(vehicle.getOrigin()).get(vehicle.getLane()).peek() == vehicle) {
        crossingVehicles.add(vehicle);
        log.info(
            "vehicle_crossing_started vehicleId={} type={} origin={} lane={} intersectionId={} emergencyActive={} crossingVehiclesCount={}",
            vehicle.getId(),
            vehicle.getType(),
            vehicle.getOrigin(),
            vehicle.getLane(),
            id,
            emergencyActive,
            crossingVehicles.size());
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void leaveIntersection(Vehicle vehicle) {
    lock.lock();
    try {
      crossingVehicles.remove(vehicle);
      waitingLanes.get(vehicle.getOrigin()).get(vehicle.getLane()).remove(vehicle);

      log.info(
          "vehicle_crossing_completed vehicleId={} type={} origin={} lane={} intersectionId={} crossingVehiclesRemaining={}",
          vehicle.getId(),
          vehicle.getType(),
          vehicle.getOrigin(),
          vehicle.getLane(),
          id,
          crossingVehicles.size());

      if (vehicle.getType() == VehicleType.EMERGENCY) {
        boolean anyOtherEmergency =
            crossingVehicles.stream().anyMatch(v -> v.getType() == VehicleType.EMERGENCY)
                || waitingLanes.values().stream()
                    .flatMap(lanes -> lanes.values().stream())
                    .flatMap(Queue::stream)
                    .anyMatch(v -> v.getType() == VehicleType.EMERGENCY);

        if (!anyOtherEmergency) {
          this.emergencyActive = false;
          log.info("emergency_mode_deactivated intersectionId={} emergencyCleared=true", id);
        }
      }

      if (isDirectionCompletelyClear(vehicle.getOrigin())) {
        laneQueue.remove(vehicle.getOrigin());
        log.debug(
            "direction_cleared intersectionId={} direction={} laneQueueSize={}",
            id,
            vehicle.getOrigin(),
            laneQueue.size());
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean hasEmergencyVehicleWaiting() {
    boolean westQueueHasEmergency =
        waitingLanes.get(Locations.WEST).get(Locations.FIRST_RAIL).stream()
            .anyMatch(v -> v.getType() == VehicleType.EMERGENCY);
    boolean eastQueueHasEmergency =
        waitingLanes.get(Locations.EAST).get(Locations.FIRST_RAIL).stream()
            .anyMatch(v -> v.getType() == VehicleType.EMERGENCY);
    return westQueueHasEmergency || eastQueueHasEmergency;
  }

  private boolean isDirectionCompletelyClear(Locations origin) {
    boolean crossingEmpty = crossingVehicles.stream().noneMatch(v -> v.getOrigin() == origin);
    boolean waitingEmpty = waitingLanes.get(origin).values().stream().allMatch(Queue::isEmpty);
    return crossingEmpty && waitingEmpty;
  }

  @Override
  public int getPositionInQueue(Vehicle vehicle) {
    return new ArrayList<>(waitingLanes.get(vehicle.getOrigin()).get(vehicle.getLane()))
        .indexOf(vehicle);
  }

  @Override
  public boolean isEmergencyActive() {
    return this.emergencyActive;
  }
}
