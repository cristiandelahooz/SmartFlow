package com.trafficmanagement.smartflow.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Direction {
  NORTH("Norte"),
  SOUTH("Sur"),
  EAST("Este"),
  WEST("Oeste"),

  FIRST_RAIL("Carril Derecho"),
  SECOND_RAIL("Carril Central"),
  THIRD_RAIL("Carril Izquierdo");

  private final String displayName;

  public static List<Direction> getMotorwayDirections() {
    return List.of(WEST, EAST);
  }
}
