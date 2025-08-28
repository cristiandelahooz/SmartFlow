package com.trafficmanagement.smartflow.utils;

public class VehicleConstants {
    
    public static final double NORMAL_SPEED = 2.0;
    public static final double EMERGENCY_SPEED = 7.4;
    
    public static final double SAFE_DISTANCE = 50.0;
    public static final double PROXIMITY_THRESHOLD = 1.5;
    public static final double INTERSECTION_PROXIMITY = 2.0;
    public static final double VEHICLE_SPACING = 30.0;
    
    public static final int ANIMATION_FRAME_DELAY_MS = 16;
    public static final int MOVEMENT_SLEEP_DURATION_MS = 16;
    
    public static final double LANE_CENTER_OFFSET = 0.5;
    public static final double LANE_1_POSITION = 1.5;
    public static final double LANE_2_POSITION = 2.5;
    public static final double LANE_3_POSITION = 3.5;
    public static final double LANE_3_MULTIPLIER = 3.0;
    
    public static final double STOP_THRESHOLD = 1.0;
    public static final double DIRECTION_CHANGE_THRESHOLD = 5.0;
    
    public static final double EMERGENCY_CLEARANCE_MULTIPLIER = 2.0;
    public static final double NORMAL_FOLLOWING_DISTANCE_MULTIPLIER = 1.0;
    
    public static final double COORDINATE_PRECISION = 0.01;
    
    public static final double TARGET_REACHED_THRESHOLD = 1.5;
    public static final double STOP_LINE_PROXIMITY = 2.0;
    public static final int INITIAL_PATH_SEGMENT = 1;
    public static final int INITIAL_TRAFFIC_LIGHT_INDEX = 0;
    public static final int NO_INTERSECTION = -1;
    public static final int COUNTER_START = 0;
    
    private VehicleConstants() {
    }
}