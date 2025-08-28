package com.trafficmanagement.smartflow.data.model;

import com.trafficmanagement.smartflow.data.enums.Locations;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntersectionStateManager {

  private final Map<Integer, Set<Vehicle>> crossingStraightVehicles = new ConcurrentHashMap<>();

  public IntersectionStateManager() {
    for (int ind = 1; ind <= 4; ind++) crossingStraightVehicles.put(ind, ConcurrentHashMap.newKeySet());
  }

  public void vehicleEntersStraightZone(int intersectionId, Vehicle vehicle) {
    crossingStraightVehicles.get(intersectionId).add(vehicle);
    log.info(
        "vehicle_entered_straight_zone vehicleId={} type={} origin={} intersectionId={} vehiclesInZone={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        intersectionId,
        crossingStraightVehicles.get(intersectionId).size());
  }

  public void vehicleExitsStraightZone(int intersectionId, Vehicle vehicle) {
    crossingStraightVehicles.get(intersectionId).remove(vehicle);
    log.info(
        "vehicle_exited_straight_zone vehicleId={} type={} origin={} intersectionId={} vehiclesRemaining={}",
        vehicle.getId(),
        vehicle.getType(),
        vehicle.getOrigin(),
        intersectionId,
        crossingStraightVehicles.get(intersectionId).size());
  }

  public boolean isOpposingTrafficCrossing(int intersectionId, Vehicle turningVehicle) {
    Locations opposingLocations =
            turningVehicle.getOrigin() == Locations.WEST ? Locations.EAST : Locations.WEST;

    boolean hasOpposingTraffic =
        crossingStraightVehicles.get(intersectionId).stream()
            .anyMatch(v -> v.getOrigin() == opposingLocations);

    if (hasOpposingTraffic)
      log.info(
          "opposing_traffic_detected vehicleId={} type={} origin={} intersectionId={} opposingLocations={}",
          turningVehicle.getId(),
          turningVehicle.getType(),
          turningVehicle.getOrigin(),
          intersectionId, opposingLocations);

    return hasOpposingTraffic;
  }
}
