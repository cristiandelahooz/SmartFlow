package com.trafficmanagement.smartflow.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cristiandelahoz
 * @created 27/8/25 - 01:43
 */
@Slf4j
public class ViewsHandler {
  public static final String MAIN_VIEW = "MainView";
  public static final String INTERSECTION_VIEW = "IntersectionView";
  public static final String MOTORWAY_VIEW = "MotorwayView";

  private static final Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getBounds();

  @Getter
  private static final Scene scene =
      new Scene(loadView(MAIN_VIEW), screenBounds.getWidth(), screenBounds.getHeight());

  private ViewsHandler() {
    // it's not required
  }

  public static void changeView(String viewName) {
    Parent view = loadView(viewName);

    if (view != null) scene.setRoot(view);
    else log.error("Scene is not initialized or view is null: {}", viewName);
  }

  private static Parent loadView(String viewName) {
    FXMLLoader loader =
        new FXMLLoader(ViewsHandler.class.getResource("/fxml/" + viewName + ".fxml"));
    try {
      return loader.load();
    } catch (Exception ex) {
      log.error("Error loading view: {}", viewName, ex);
      return null;
    }
  }
}
