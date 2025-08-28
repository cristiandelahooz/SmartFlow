package com.trafficmanagement.smartflow.utils;

import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cristiandelahoz
 * @created 27/8/25 - 01:43
 */
@Slf4j
public class MotorwayConstants {

  public static final double SAFE_DISTANCE = 50.0;
  public static final double SAFE_DISTANCE_MULTIPLIER = 1.5;

  public static final int EMERGENCY_VEHICLE_PROBABILITY = 1000;
  public static final int MULTIPLE_VEHICLES_COUNT = 15;
  public static final int VEHICLE_SPAWN_DELAY_MS = 1000;

  public static final double LANE_HEIGHT = 60.0;
  public static final double INTERSECTION_WIDTH = 120.0;
  public static final int TOTAL_LANES = 6;
  public static final double VEHICLE_RADIUS = 10.0;

  public static final double TRAFFIC_LIGHT_POST_WIDTH = 14.0;
  public static final double TRAFFIC_LIGHT_POST_HEIGHT = 40.0;
  public static final double TRAFFIC_LIGHT_RADIUS = 6.0;

  public static final double VEHICLE_OFFSET = 50.0;
  public static final double TRAFFIC_LIGHT_OFFSET = 15.0;
  public static final double TRAFFIC_LIGHT_EXTENDED_OFFSET = 25.0;
  public static final double U_TURN_OFFSET = 30.0;
  public static final double STOP_LINE_OFFSET = 50.0;
  public static final double WALL_HEIGHT = 10.0;
  public static final double WALL_VERTICAL_OFFSET = 5.0;

  public static final double LANE_1_OFFSET = 0.5;
  public static final double LANE_2_OFFSET = 1.5;
  public static final double LANE_3_OFFSET = 2.5;

  public static final String EMERGENCY_VEHICLE_COLOR = "#e74c3c";
  public static final String NORMAL_VEHICLE_COLOR = "#4a9a9621";
  public static final Color MOTORWAY_COLOR = Color.GRAY;
  public static final Color LANE_LINE_COLOR = Color.WHITE;
  public static final Color WALL_COLOR = Color.DARKSLATEGRAY;
  public static final Color TRAFFIC_LIGHT_RED_OFF = Color.DARKRED;
  public static final Color TRAFFIC_LIGHT_GREEN_OFF = Color.DARKGREEN;
  public static final Color VEHICLE_STROKE_COLOR = Color.BLACK;

  public static final double LANE_DASH_LENGTH = 25.0;
  public static final double LANE_DASH_SPACING = 20.0;

  public static final double MOTORWAY_GAP_FROM_CENTER_DIVISOR = 8.0;

  public static final int INTERSECTION_1 = 1;
  public static final int INTERSECTION_2 = 2;
  public static final int INTERSECTION_3 = 3;
  public static final int INTERSECTION_4 = 4;

  public static final int TRAFFIC_LIGHT_1 = 1;
  public static final int TRAFFIC_LIGHT_2 = 2;
  public static final int TRAFFIC_LIGHT_3 = 3;
  public static final int TRAFFIC_LIGHT_4 = 4;
  public static final int TRAFFIC_LIGHT_5 = 5;
  public static final int TRAFFIC_LIGHT_6 = 6;

  public static final int TOTAL_INTERSECTIONS = 4;
  public static final int FIRST_INTERSECTION = 1;

  public static final double TRAFFIC_LIGHT_Y_OFFSET = 20.0;
  public static final double TRAFFIC_LIGHT_X_CENTER_OFFSET = 7.0;
  public static final double TRAFFIC_LIGHT_Y_UPPER_OFFSET = -10.0;
  public static final double TRAFFIC_LIGHT_Y_LOWER_OFFSET = 10.0;

  public static final double VEHICLE_START_OFFSET = 50.0;
  public static final double VEHICLE_RELOCATE_OFFSET = 10.0;
  public static final double VEHICLE_PATH_OFFSET = -50.0;

  public static final double LANE_Y_MULTIPLIER_UPPER = 1.5;
  public static final double LANE_Y_MULTIPLIER_LOWER = 4.5;

  public static final int BUTTON_DISABLE_DURATION_SECONDS = 1;

  public static final double INTERSECTION_WIDTH_DIVISOR = 2.0;

  private MotorwayConstants() {
    // it's not required
  }
}
