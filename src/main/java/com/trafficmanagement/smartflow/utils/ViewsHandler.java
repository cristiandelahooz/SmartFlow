package com.trafficmanagement.smartflow.utils;

import com.trafficmanagement.smartflow.Main;
import javafx.fxml.FXMLLoader;
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
  public static final String MAIN_LAYOUT = "MainView";
  public static final String INTERSECTION_VIEW = "IntersectionView";
  public static final String MOTORWAY_VIEW = "MotorwayView";

  @Getter
  private static final Scene scene = new Scene(loadView(MAIN_LAYOUT), 800, 600);

  private ViewsHandler() {
    // it's not required
  }

  public static void changeView(String viewName) {
    Parent view = loadView(viewName);

    if (view != null) scene.setRoot(view);
    else log.error("Scene is not initialized or view is null: {}", viewName);
  }

  private static Parent loadView(String viewName) {
    FXMLLoader loader = new FXMLLoader(ViewsHandler.class.getResource("/fxml/" + viewName + ".fxml"));
    try {
      return loader.load();
    } catch (Exception ex) {
      log.error("Error loading view: {}", viewName, ex);
      return null;
    }
  }
}
