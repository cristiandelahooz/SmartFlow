package com.trafficmanagement.smartflow.controller;

import com.trafficmanagement.smartflow.utils.ViewsHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cristiandelahoz
 */
@Slf4j
public class MainViewController {
  @FXML
  private void startIntersectionView(ActionEvent event) {
    ViewsHandler.changeView(ViewsHandler.INTERSECTION_VIEW);
  }

  @FXML
  private void startMotorwayView(ActionEvent event) {
    ViewsHandler.changeView(ViewsHandler.MOTORWAY_VIEW);
  }
}
