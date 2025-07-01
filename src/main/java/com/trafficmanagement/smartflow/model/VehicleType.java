package com.trafficmanagement.smartflow.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum VehicleType {
    NORMAL("normal", "Normal Vehicle"),
    EMERGENCY("emergency", "Emergency Vehicle");

    private final String type;
    private final String description;
}
