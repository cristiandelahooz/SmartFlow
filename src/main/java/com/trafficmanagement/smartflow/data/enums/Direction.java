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

    STRAIGHT("Recto"),
    RIGHT("Derecha"),
    LEFT("Izquierda"),
    U_TURN("Giro en U"),
    U_TURN_CONTINUATION("Continuación Giro en U"),

    FIRST_RAIL("Carril Derecho"),
    SECOND_RAIL("Carril Central"),
    THIRD_RAIL("Carril Izquierdo");

    private final String displayName;

    public static List<Direction> getMotorwayDirections() {
        return List.of(EAST, WEST);
    }
}