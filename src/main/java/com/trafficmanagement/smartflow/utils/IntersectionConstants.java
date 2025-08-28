package com.trafficmanagement.smartflow.utils;

import javafx.scene.paint.Color;

public class IntersectionConstants {

  public static final double STREET_WIDTH_DIVISOR = 4.0;

  public static final double VEHICLE_RADIUS = 8.0;
  public static final double VEHICLE_VISUAL_OFFSET = 8.0;

  public static final double STOP_LINE_GAP = 20.0;
  public static final double ENTRY_EXIT_OFFSET = 50.0;

  public static final int MULTIPLE_VEHICLES_COUNT = 15;
  public static final int EMERGENCY_VEHICLE_PROBABILITY = 200;
  public static final int VEHICLE_SPAWN_DELAY_MS = 1000;
  public static final int BUTTON_DISABLE_DURATION_SECONDS = 1;

  public static final double DASH_LENGTH = 25.0;
  public static final double DASH_SPACING = 20.0;

  public static final Color STREET_COLOR = Color.GRAY;
  public static final Color STREET_STROKE_COLOR = Color.WHITE;
  public static final Color LANE_DIVIDER_COLOR = Color.WHITE;

  private IntersectionConstants() {}
}
