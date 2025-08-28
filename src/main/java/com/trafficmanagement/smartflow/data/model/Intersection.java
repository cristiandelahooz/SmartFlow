package com.trafficmanagement.smartflow.data.model;

import com.trafficmanagement.smartflow.data.enums.Direction;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Intersection implements TrafficManager {
  private final Map<Direction, ConcurrentLinkedQueue<Vehicle>> waitingQueues;
  private final ConcurrentLinkedQueue<Vehicle> globalArrivalQueue = new ConcurrentLinkedQueue<>();
  private final Set<Vehicle> crossingVehicles = ConcurrentHashMap.newKeySet();

  public Intersection() {
    waitingQueues = new EnumMap<>(Direction.class);
    for (Direction dir :
        new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
      waitingQueues.put(dir, new ConcurrentLinkedQueue<>());
    }
  }

  @Override
  public void addToQueue(Vehicle vehicle) {
    waitingQueues.get(vehicle.getOrigin()).add(vehicle);
    globalArrivalQueue.add(vehicle);
    log.info(
        "vehicle_queued vehicleId={} type={} origin={} queuePosition={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        getPositionInQueue(vehicle));
  }

  @Override
  public boolean isMyTurn(Vehicle vehicle) {
    Vehicle activeEmergency = findActiveEmergency();

    if (activeEmergency != null) {
      Direction emergencyLane = activeEmergency.getOrigin();

      Vehicle headOfEmergencyLane = waitingQueues.get(emergencyLane).peek();
      if (crossingVehicles.isEmpty()) {
        return vehicle == headOfEmergencyLane;
      } else {
        return false;
      }
    }

    if (!crossingVehicles.isEmpty()) {
      return false;
    }
    return globalArrivalQueue.peek() == vehicle;
  }

  private Vehicle findActiveEmergency() {
    for (ConcurrentLinkedQueue<Vehicle> queue : waitingQueues.values()) {
      for (Vehicle v : queue) {
        if (v.getType() == VehicleType.EMERGENCY) {
          return v;
        }
      }
    }
    return null;
  }

  @Override
  public void startCrossing(Vehicle vehicle) {
    globalArrivalQueue.remove(vehicle);
    waitingQueues.get(vehicle.getOrigin()).remove(vehicle);
    crossingVehicles.add(vehicle);
    log.info(
        "vehicle_crossing_started vehicleId={} type={} origin={} destination={} emergencyActive={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        vehicle.getMovement(),
        isEmergencyActive());
  }

  @Override
  public void leaveIntersection(Vehicle vehicle) {
    crossingVehicles.remove(vehicle);
    log.info(
        "vehicle_crossing_completed vehicleId={} type={} origin={} destination={} crossingVehiclesRemaining={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        vehicle.getMovement(),
        crossingVehicles.size());
  }

  @Override
  public int getPositionInQueue(Vehicle vehicle) {
    return new ArrayList<>(waitingQueues.get(vehicle.getOrigin())).indexOf(vehicle);
  }

  @Override
  public boolean isEmergencyActive() {
    return findActiveEmergency() != null;
  }

  @Override
  public int getId() {
    return 1;
  }
}