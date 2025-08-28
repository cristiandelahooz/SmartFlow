package com.trafficmanagement.smartflow.controller;

import static com.trafficmanagement.smartflow.utils.IntersectionConstants.*;

import com.trafficmanagement.smartflow.data.enums.Locations;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.data.model.Intersection;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import com.trafficmanagement.smartflow.utils.MotorwayConstants;
import com.trafficmanagement.smartflow.utils.ViewsHandler;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

@Slf4j
public class IntersectionViewController {
  private final Intersection intersection = new Intersection();
  private final Map<Vehicle, Circle> vehicleMap = new ConcurrentHashMap<>();
  private final Group streetGroup = new Group();
  @FXML private Pane simulationPane;
  @FXML private ComboBox<VehicleType> typeComboBox;
  @FXML private ComboBox<Locations> originComboBox;
  @FXML private ComboBox<VehicleMovement> vehicleMovementComboBox;
  @FXML private Button addVehicleButton;
  @FXML private Button addMultipleButton;
  @FXML private Button backButton;
  private AnimationTimer animationTimer;

  @FXML
  private void goBackToMenu() {
    if (animationTimer != null) {
      animationTimer.stop();
      log.info("animation_timer_stopped simulationType=intersection");
    }
    log.info("simulation_stopping vehicleCount={} simulationType=intersection", vehicleMap.size());
    for (Vehicle vehicle : vehicleMap.keySet()) {
      vehicle.stop();

      vehicleMap.clear();
      simulationPane.getChildren().clear();
      log.info("simulation_cleaned simulationType=intersection");
    }
    ViewsHandler.changeView(ViewsHandler.MAIN_VIEW);
  }

  @FXML
  public void initialize() {
    simulationPane.getChildren().add(streetGroup);

    typeComboBox.getItems().setAll(VehicleType.values());
    originComboBox
        .getItems()
        .setAll(Locations.NORTH, Locations.SOUTH, Locations.EAST, Locations.WEST);
    vehicleMovementComboBox.getItems().setAll(VehicleMovement.values());
    typeComboBox.getSelectionModel().selectFirst();
    originComboBox.getSelectionModel().selectFirst();
    vehicleMovementComboBox.getSelectionModel().selectFirst();
    FontIcon backIcon = new FontIcon(FontAwesomeSolid.ARROW_LEFT);
    backButton.setGraphic(backIcon);

    simulationPane.widthProperty().addListener((obs, oldVal, newVal) -> redrawStreet());
    simulationPane.heightProperty().addListener((obs, oldVal, newVal) -> redrawStreet());

    startAnimationLoop();
  }

  private void redrawStreet() {
    streetGroup.getChildren().clear();

    double width = simulationPane.getWidth();
    double height = simulationPane.getHeight();
    if (width == 0 || height == 0) return;

    double streetWidth = Math.min(width, height) / STREET_WIDTH_DIVISOR;

    Rectangle hStreet = new Rectangle(0, height / 2 - streetWidth / 2, width, streetWidth);
    Rectangle vStreet = new Rectangle(width / 2 - streetWidth / 2, 0, streetWidth, height);
    hStreet.setFill(STREET_COLOR);
    vStreet.setFill(STREET_COLOR);
    hStreet.setStroke(STREET_STROKE_COLOR);
    vStreet.setStroke(STREET_STROKE_COLOR);
    streetGroup.getChildren().addAll(hStreet, vStreet);

    Line hLine = new Line(0, height / 2, width, height / 2);
    hLine.setStroke(LANE_DIVIDER_COLOR);
    hLine.getStrokeDashArray().addAll(DASH_LENGTH, DASH_SPACING);

    Line vLine = new Line(width / 2, 0, width / 2, height);
    vLine.setStroke(LANE_DIVIDER_COLOR);
    vLine.getStrokeDashArray().addAll(DASH_LENGTH, DASH_SPACING);
    streetGroup.getChildren().addAll(hLine, vLine);
  }

  @FXML
  private void addVehicle() {
    disableButtonsTemporarily();
    createAndStartVehicle(
        typeComboBox.getValue(), originComboBox.getValue(), vehicleMovementComboBox.getValue());
  }

  private void createAndStartVehicle(VehicleType type, Locations origin, VehicleMovement movement) {
    Vehicle vehicle = new Vehicle(type, origin, movement, intersection);
    vehicle.setController(this);

    Circle vehicleCircle =
        new Circle(
            VEHICLE_RADIUS,
            type == VehicleType.EMERGENCY
                ? Color.web(MotorwayConstants.EMERGENCY_VEHICLE_COLOR)
                : Color.web(MotorwayConstants.NORMAL_VEHICLE_COLOR));
    vehicleCircle.setStroke(MotorwayConstants.VEHICLE_STROKE_COLOR);

    List<Point2D> path = getPath(origin, movement);
    if (path.isEmpty()) return;

    Point2D startPos = path.getFirst();
    vehicle.setPosition(startPos.getX(), startPos.getY());

    vehicleMap.put(vehicle, vehicleCircle);
    simulationPane.getChildren().add(vehicleCircle);
    vehicleCircle.toFront();

    new Thread(vehicle).start();
  }

  @FXML
  private void addMultipleVehicles() {
    disableButtonsTemporarily();
    final int numberOfVehiclesToAdd = MULTIPLE_VEHICLES_COUNT;
    log.info(
        "batch_vehicle_creation_started count={} simulationType=intersection",
        numberOfVehiclesToAdd);
    final Random random = new Random();

    new Thread(
            () -> {
              try {
                Locations[] origins = {
                  Locations.NORTH, Locations.SOUTH, Locations.EAST, Locations.WEST
                };
                VehicleMovement[] movements = VehicleMovement.values();

                for (int ind = 0; ind < numberOfVehiclesToAdd; ind++) {
                  Locations randomOrigin = origins[random.nextInt(origins.length)];
                  VehicleMovement randomDestination = movements[random.nextInt(movements.length)];

                  VehicleType randomType =
                      (random.nextInt(EMERGENCY_VEHICLE_PROBABILITY) == 0)
                          ? VehicleType.EMERGENCY
                          : VehicleType.NORMAL;

                  Platform.runLater(
                      () -> createAndStartVehicle(randomType, randomOrigin, randomDestination));

                  Thread.sleep(VEHICLE_SPAWN_DELAY_MS);
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              log.info(
                  "batch_vehicle_creation_completed count={} simulationType=intersection",
                  numberOfVehiclesToAdd);
            })
        .start();
  }

  private void startAnimationLoop() {
    this.animationTimer =
        new AnimationTimer() {
          @Override
          public void handle(long now) {
            Iterator<Map.Entry<Vehicle, Circle>> iterator = vehicleMap.entrySet().iterator();
            while (iterator.hasNext()) {
              Map.Entry<Vehicle, Circle> entry = iterator.next();
              if (entry.getKey().isFinished()) {
                simulationPane.getChildren().remove(entry.getValue());
                iterator.remove();
              } else {
                entry
                    .getValue()
                    .relocate(
                        entry.getKey().getX() - VEHICLE_VISUAL_OFFSET,
                        entry.getKey().getY() - VEHICLE_VISUAL_OFFSET);
              }
            }
          }
        };
    this.animationTimer.start();
  }

  private void disableButtonsTemporarily() {
    addVehicleButton.setDisable(true);
    addMultipleButton.setDisable(true);

    PauseTransition pause = new PauseTransition(Duration.seconds(BUTTON_DISABLE_DURATION_SECONDS));

    pause.setOnFinished(
        event -> {
          addVehicleButton.setDisable(false);
          addMultipleButton.setDisable(false);
        });

    pause.play();
  }

  public List<Point2D> getPath(Locations origin, VehicleMovement movement) {
    double width = simulationPane.getWidth();
    double height = simulationPane.getHeight();
    if (width == 0 || height == 0) return List.of();

    double streetW = Math.min(width, height) / STREET_WIDTH_DIVISOR;
    final double STOP_GAP = STOP_LINE_GAP;
    double N_IN_X = width / 2 - streetW / 4;
    double N_OUT_X = width / 2 + streetW / 4;
    double S_IN_X = width / 2 + streetW / 4;
    double S_OUT_X = width / 2 - streetW / 4;
    double E_IN_Y = height / 2 - streetW / 4;
    double E_OUT_Y = height / 2 + streetW / 4;
    double W_IN_Y = height / 2 + streetW / 4;
    double W_OUT_Y = height / 2 - streetW / 4;

    Point2D stopN = new Point2D(N_IN_X, height / 2 - streetW / 2 - STOP_GAP);
    Point2D stopS = new Point2D(S_IN_X, height / 2 + streetW / 2 + STOP_GAP);
    Point2D stopE = new Point2D(width / 2 + streetW / 2 + STOP_GAP, E_IN_Y);
    Point2D stopW = new Point2D(width / 2 - streetW / 2 - STOP_GAP, W_IN_Y);

    Point2D exitN = new Point2D(N_OUT_X, -ENTRY_EXIT_OFFSET);
    Point2D exitS = new Point2D(S_OUT_X, height + ENTRY_EXIT_OFFSET);
    Point2D exitE = new Point2D(width + ENTRY_EXIT_OFFSET, E_OUT_Y);
    Point2D exitW = new Point2D(-ENTRY_EXIT_OFFSET, W_OUT_Y);
    if (movement.equals(VehicleMovement.U_TURN)) {
      switch (origin) {
        case NORTH:
          return List.of(
              new Point2D(N_IN_X, -ENTRY_EXIT_OFFSET),
              stopN,
              new Point2D(N_OUT_X, stopN.getY() + STOP_GAP),
              exitN);
        case SOUTH:
          return List.of(
              new Point2D(S_IN_X, height + ENTRY_EXIT_OFFSET),
              stopS,
              new Point2D(S_OUT_X, stopS.getY() - STOP_GAP),
              exitS);
        case EAST:
          return List.of(
              new Point2D(width + ENTRY_EXIT_OFFSET, E_IN_Y),
              stopE,
              new Point2D(stopE.getX() - STOP_GAP, E_OUT_Y),
              exitE);
        case WEST:
          return List.of(
              new Point2D(-ENTRY_EXIT_OFFSET, W_IN_Y),
              stopW,
              new Point2D(stopW.getX() + STOP_GAP, W_OUT_Y),
              exitW);
        default:
          break;
      }
    }

    switch (origin) {
      case NORTH:
        Point2D startN = new Point2D(N_IN_X, -ENTRY_EXIT_OFFSET);
        Point2D enterN = new Point2D(N_IN_X, stopN.getY() + STOP_GAP);
        switch (movement) {
          case STRAIGHT:
            return List.of(startN, stopN, new Point2D(N_IN_X, stopS.getY()), exitS);
          case TURN_RIGHT:
            return List.of(startN, stopN, enterN, new Point2D(stopW.getX(), W_OUT_Y), exitW);
          case TURN_LEFT:
            return List.of(
                startN,
                stopN,
                new Point2D(N_IN_X, E_OUT_Y),
                new Point2D(stopE.getX(), E_OUT_Y),
                exitE);
          default:
            break;
        }
        break;
      case SOUTH:
        Point2D startS = new Point2D(S_IN_X, height + ENTRY_EXIT_OFFSET);
        Point2D enterS = new Point2D(S_IN_X, stopS.getY() - STOP_GAP);
        switch (movement) {
          case STRAIGHT:
            return List.of(startS, stopS, new Point2D(S_IN_X, stopN.getY()), exitN);
          case TURN_RIGHT:
            return List.of(startS, stopS, enterS, new Point2D(stopE.getX(), E_OUT_Y), exitE);
          case TURN_LEFT:
            return List.of(
                startS,
                stopS,
                new Point2D(S_IN_X, W_OUT_Y),
                new Point2D(stopW.getX(), W_OUT_Y),
                exitW);
          default:
            break;
        }
        break;
      case EAST:
        Point2D startE = new Point2D(width + ENTRY_EXIT_OFFSET, E_IN_Y);
        Point2D enterE = new Point2D(stopE.getX() - STOP_GAP, E_IN_Y);
        switch (movement) {
          case STRAIGHT:
            return List.of(startE, stopE, new Point2D(stopW.getX(), E_IN_Y), exitW);
          case TURN_RIGHT:
            return List.of(
                startE,
                stopE,
                new Point2D(N_OUT_X, E_IN_Y),
                new Point2D(N_OUT_X, stopN.getY()),
                exitN);
          case TURN_LEFT:
            return List.of(startE, stopE, enterE, new Point2D(S_OUT_X, stopS.getY()), exitS);
          default:
            break;
        }
        break;
      case WEST:
        Point2D startW = new Point2D(-ENTRY_EXIT_OFFSET, W_IN_Y);
        Point2D enterW = new Point2D(stopW.getX() + STOP_GAP, W_IN_Y);
        switch (movement) {
          case STRAIGHT:
            return List.of(startW, stopW, new Point2D(stopE.getX(), W_IN_Y), exitE);
          case TURN_RIGHT:
            return List.of(
                startW,
                stopW,
                new Point2D(S_OUT_X, W_IN_Y),
                new Point2D(S_OUT_X, stopS.getY()),
                exitS);
          case TURN_LEFT:
            return List.of(startW, stopW, enterW, new Point2D(N_OUT_X, stopN.getY()), exitN);
          default:
            break;
        }
        break;
      default:
        break;
    }
    return List.of(new Point2D(0, 0));
  }
}
