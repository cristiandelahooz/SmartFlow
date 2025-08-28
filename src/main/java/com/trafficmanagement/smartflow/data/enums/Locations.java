package com.trafficmanagement.smartflow.data.enums;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Locations {
  NORTH("Norte"),
  SOUTH("Sur"),
  EAST("Este"),
  WEST("Oeste"),

  FIRST_RAIL("Carril Derecho"),
  SECOND_RAIL("Carril Central"),
  THIRD_RAIL("Carril Izquierdo");

  private final String displayName;

  public static List<Locations> getMotorwayDirections() {
    return List.of(WEST, EAST);
  }
}
