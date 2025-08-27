package com.trafficmanagement.smartflow.data;

import com.trafficmanagement.smartflow.data.enums.VehicleType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class Vehicle implements Comparable<Vehicle>, Runnable {
    private final String id;
    private final VehicleType type;
    private final String direction; // "right", "straight", "left", "u-turn"
    private final Intersection intersection;

    @Override
    public int compareTo(Vehicle other) {
        if (this.type == VehicleType.EMERGENCY && other.type != VehicleType.EMERGENCY) {
            return -1;
        } else if (this.type != VehicleType.EMERGENCY && other.type == VehicleType.EMERGENCY) {
            return 1;
        } else {
            return 0; // Same priority
        }
    }

    @Override
    public void run() {
        try {
            log.info("Vehicle {} is approaching intersection {}", id, intersection.getId());
            intersection.getBarrier().await(); // Wait for other vehicles
            log.info("Vehicle {} is crossing intersection {}", id, intersection.getId());
        } catch (Exception e) {
            log.error("Vehicle {} was interrupted", id, e);
            Thread.currentThread().interrupt();
        }
    }
}

