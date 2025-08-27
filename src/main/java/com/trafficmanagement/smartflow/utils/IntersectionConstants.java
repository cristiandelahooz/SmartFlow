package com.trafficmanagement.smartflow.utils;

import javafx.scene.paint.Color;

public class IntersectionConstants {
    
    public static final double STREET_WIDTH_DIVISOR = 4.0;
    
    public static final double STOP_SIGN_OFFSET = 45.0;
    public static final double STOP_SIGN_SCALE = 0.5;
    public static final double STOP_SIGN_ROTATION_EAST = -90.0;
    public static final double STOP_SIGN_ROTATION_WEST = 90.0;
    public static final double STOP_SIGN_ROTATION_NORTH = 180.0;
    public static final double STOP_SIGN_ROTATION_SOUTH = 0.0;
    
    public static final double STOP_SIGN_OCTAGON_SIZE = 20.0;
    public static final double STOP_SIGN_OCTAGON_OFFSET = 40.0;
    public static final double STOP_SIGN_OCTAGON_CORNER = 60.0;
    public static final double STOP_SIGN_POLE_X_OFFSET = 28.0;
    public static final double STOP_SIGN_POLE_Y_OFFSET = 60.0;
    public static final double STOP_SIGN_POLE_WIDTH = 4.0;
    public static final double STOP_SIGN_POLE_HEIGHT = 40.0;
    public static final double STOP_SIGN_TEXT_X_OFFSET = 10.0;
    public static final double STOP_SIGN_TEXT_Y_OFFSET = 37.0;
    public static final double STOP_SIGN_TEXT_SIZE = 16.0;
    public static final double STOP_SIGN_CENTER_OFFSET = 30.0;
    public static final double STOP_SIGN_STROKE_WIDTH = 2.0;
    
    public static final double VEHICLE_RADIUS = 8.0;
    public static final double VEHICLE_VISUAL_OFFSET = 8.0;
    
    public static final double STOP_LINE_GAP = 20.0;
    public static final double ENTRY_EXIT_OFFSET = 50.0;
    public static final double LANE_OFFSET_DIVISOR = 4.0;
    
    public static final int MULTIPLE_VEHICLES_COUNT = 15;
    public static final int EMERGENCY_VEHICLE_PROBABILITY = 200;
    public static final int VEHICLE_SPAWN_DELAY_MS = 1000;
    public static final int BUTTON_DISABLE_DURATION_SECONDS = 1;
    
    public static final double DASH_LENGTH = 25.0;
    public static final double DASH_SPACING = 20.0;
    
    public static final Color STREET_COLOR = Color.GRAY;
    public static final Color STREET_STROKE_COLOR = Color.DARKGRAY;
    public static final Color LANE_DIVIDER_COLOR = Color.YELLOW;
    public static final Color STOP_SIGN_COLOR = Color.RED;
    public static final Color STOP_SIGN_TEXT_COLOR = Color.WHITE;
    public static final Color STOP_SIGN_STROKE_COLOR = Color.WHITE;
    public static final Color STOP_SIGN_POLE_COLOR = Color.LIGHTGRAY;
    
    public static final String STOP_SIGN_TEXT = "STOP";
    public static final String STOP_SIGN_FONT_FAMILY = "Arial BOLD";
    
    private IntersectionConstants() {
    }
}