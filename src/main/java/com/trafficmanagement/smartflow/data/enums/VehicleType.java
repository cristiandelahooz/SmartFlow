package com.trafficmanagement.smartflow.data.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum VehicleType {
    NORMAL("Normal"),
    EMERGENCY("Emergencia");

    private final String displayName;

    /**
     * Returns the display name of the enum constant.
     *
     * @return The translated string representation.
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Finds a VehicleType enum constant by its display name.
     *
     * @param displayName The display name to search for.
     * @return The matching VehicleType constant.
     * @throws IllegalArgumentException if no constant matches the given display name.
     */
    public static VehicleType fromDisplayName(String displayName) {
        return Arrays.stream(VehicleType.values())
                .filter(type -> type.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No vehicle type found with display name: " + displayName));
    }
}