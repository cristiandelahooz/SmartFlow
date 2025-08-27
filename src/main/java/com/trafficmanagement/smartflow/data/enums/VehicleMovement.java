package com.trafficmanagement.smartflow.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author cristiandelahoz
 * @created 27/8/25 - 15:56
 */
@Getter
@RequiredArgsConstructor
public enum VehicleMovement {
  STRAIGHT("Seguir derecho"),
  TURN_LEFT("Doblar a la izquierda"),
  TURN_RIGHT("Doblar a la derecha"),
  U_TURN("Giro en U"),
  STRAIGH_AFTER_U_TURN("Continuación Giro en U");

  private final String displayName;

  public boolean isTurn() {
    return this == TURN_LEFT || this == TURN_RIGHT || this == U_TURN;
  }

  public static VehicleMovement[] getAllMovements() {
    return new VehicleMovement[] {STRAIGHT, TURN_LEFT, TURN_RIGHT, U_TURN};
  }

  @Override
  public String toString() {
    return displayName;
  }
}
