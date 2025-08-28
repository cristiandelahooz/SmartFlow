package com.trafficmanagement.smartflow.controller;

import static com.trafficmanagement.smartflow.utils.MotorwayConstants.*;

import com.trafficmanagement.smartflow.data.enums.Locations;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.data.model.IntersectionStateManager;
import com.trafficmanagement.smartflow.data.model.MotorwayIntersection;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import com.trafficmanagement.smartflow.ui.ComboBoxWrapper;
import com.trafficmanagement.smartflow.utils.CompassUtils;
import com.trafficmanagement.smartflow.utils.ViewsHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

@Slf4j
public class MotorwayViewController {
  private final List<MotorwayIntersection> intersections = new ArrayList<>();
  private final Map<Vehicle, Circle> vehicleMap = new ConcurrentHashMap<>();
  private final Group motorwayGroup = new Group();
  private final Group trafficLightsGroup = new Group();
  private final TrafficLightController trafficLightController = new TrafficLightController();
  private final IntersectionStateManager intersectionStateManager = new IntersectionStateManager();
  private Group compass;
  @FXML private ComboBox<VehicleType> vehicleTypeComboBox;
  @FXML private ComboBox<Locations> startPosition;
  @FXML private ComboBox<VehicleMovement> movementComboBox;
  @FXML private ComboBox<Integer> intersectionComboBox;
  private ComboBoxWrapper<VehicleType> vehicleTypeWrapper;
  private ComboBoxWrapper<Locations> startPositionWrapper;
  private ComboBoxWrapper<VehicleMovement> movementWrapper;
  @FXML private Button addVehicleButton;
  @FXML private Button addMultipleButton;
  @FXML private Button backButton;
  @FXML private Label intersectionLabel;
  @Getter @FXML private Pane simulationPane;
  private AnimationTimer animationTimer;

  @FXML
  public void initialize() {
    for (int ind = FIRST_INTERSECTION; ind <= TOTAL_INTERSECTIONS; ind++) intersections.add(new MotorwayIntersection(ind));
    log.info("motorway_controller_initialized intersectionCount=4 simulationType=motorway");

    simulationPane.getChildren().addAll(motorwayGroup, trafficLightsGroup);

    vehicleTypeWrapper = new ComboBoxWrapper<>(vehicleTypeComboBox);
    startPositionWrapper = new ComboBoxWrapper<>(startPosition);
    movementWrapper = new ComboBoxWrapper<>(movementComboBox);

    vehicleTypeWrapper.getItems().setAll(VehicleType.values());
    startPositionWrapper.getItems().setAll(Locations.getMotorwayDirections());
    movementWrapper.getItems().setAll(VehicleMovement.getAllMovements());

    vehicleTypeComboBox.getSelectionModel().selectFirst();
    startPosition.getSelectionModel().selectFirst();
    movementComboBox.getSelectionModel().selectFirst();
    backButton.setGraphic(new FontIcon(FontAwesomeSolid.ARROW_CIRCLE_LEFT));
    movementComboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, o, newAction) -> updateIntersectionSelectorVisibility(newAction));
    startPosition
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, o, newOrigin) -> updateAvailableIntersections(newOrigin));
    updateAvailableIntersections(startPosition.getValue());
    updateIntersectionSelectorVisibility(movementComboBox.getValue());

    compass = CompassUtils.createCompass();
    simulationPane.getChildren().add(compass);

    simulationPane.widthProperty().addListener((obs, o, n) -> {
      redrawMotorway();
      repositionCompass();
    });
    simulationPane.heightProperty().addListener((obs, o, n) -> {
      redrawMotorway();
      repositionCompass();
    });
    startVehicleAnimationLoop();
  }

  private void updateIntersectionSelectorVisibility(VehicleMovement movement) {
    boolean isTurn = movement.isTurn();
    intersectionLabel.setVisible(isTurn);
    intersectionLabel.setManaged(isTurn);
    intersectionComboBox.setVisible(isTurn);
    intersectionComboBox.setManaged(isTurn);
  }

  private void updateAvailableIntersections(Locations origin) {
    Integer previouslySelected = intersectionComboBox.getValue();
    intersectionComboBox.getItems().clear();
    if (origin == Locations.WEST) {
      intersectionComboBox.getItems().setAll(INTERSECTION_2, INTERSECTION_3, INTERSECTION_4);
    } else {
      intersectionComboBox.getItems().setAll(INTERSECTION_1, INTERSECTION_2, INTERSECTION_3);
    }
    if (previouslySelected != null
        && intersectionComboBox.getItems().contains(previouslySelected)) {
      intersectionComboBox.setValue(previouslySelected);
    } else {
      intersectionComboBox.getSelectionModel().selectFirst();
    }
  }

  public double getIntersectionCenterX(int intersectionId, double totalMotorwayWidth) {
    double gapFromCenter = totalMotorwayWidth / MOTORWAY_GAP_FROM_CENTER_DIVISOR;
    return switch (intersectionId) {
      case INTERSECTION_1 -> INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
      case INTERSECTION_2 -> totalMotorwayWidth / INTERSECTION_WIDTH_DIVISOR - gapFromCenter;
      case INTERSECTION_3 -> totalMotorwayWidth / INTERSECTION_WIDTH_DIVISOR + gapFromCenter;
      case INTERSECTION_4 -> totalMotorwayWidth - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
      default -> 0;
    };
  }

  private void redrawMotorway() {
    motorwayGroup.getChildren().clear();
    trafficLightsGroup.getChildren().clear();

    double width = simulationPane.getWidth();
    double height = simulationPane.getHeight();
    if (width == 0 || height == 0) return;

    double totalMotorwayHeight = LANE_HEIGHT * TOTAL_LANES;
    double motorwayY = (height - totalMotorwayHeight) / 2;

    Rectangle motorwayBackground = new Rectangle(0, motorwayY, width, totalMotorwayHeight);
    motorwayBackground.setFill(MOTORWAY_COLOR);
    motorwayGroup.getChildren().add(motorwayBackground);

    for (int ind = FIRST_INTERSECTION; ind <= TOTAL_INTERSECTIONS; ind++) {
      double centerX = getIntersectionCenterX(ind, width);
      Rectangle vStreet =
          new Rectangle(centerX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR, 0, INTERSECTION_WIDTH, height);
      vStreet.setFill(MOTORWAY_COLOR);
      motorwayGroup.getChildren().add(vStreet);
    }

    for (int ind = FIRST_INTERSECTION; ind < TOTAL_LANES; ind++) {
      if (ind == INTERSECTION_3) continue;
      Line laneLine =
          new Line(0, motorwayY + ind * LANE_HEIGHT, width, motorwayY + ind * LANE_HEIGHT);
      laneLine.setStroke(LANE_LINE_COLOR);
      laneLine.getStrokeDashArray().addAll(LANE_DASH_LENGTH, LANE_DASH_SPACING);
      motorwayGroup.getChildren().add(laneLine);
    }

    double wallY = motorwayY + 3 * LANE_HEIGHT;
    double lastX = 0;
    for (int ind = FIRST_INTERSECTION; ind <= TOTAL_INTERSECTIONS; ind++) {
      double centerX = getIntersectionCenterX(ind, width);
      double gapStart = centerX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
      Rectangle wallSegment =
          new Rectangle(lastX, wallY - WALL_VERTICAL_OFFSET, gapStart - lastX, WALL_HEIGHT);
      wallSegment.setFill(WALL_COLOR);
      motorwayGroup.getChildren().add(wallSegment);
      lastX = centerX + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
    }
    Rectangle finalWallSegment =
        new Rectangle(lastX, wallY - WALL_VERTICAL_OFFSET, width - lastX, WALL_HEIGHT);
    finalWallSegment.setFill(WALL_COLOR);
    motorwayGroup.getChildren().add(finalWallSegment);

    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_4,
                getIntersectionCenterX(INTERSECTION_3, width)
                    + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_UPPER));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_2,
                getIntersectionCenterX(INTERSECTION_2, width)
                    + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_UPPER));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_1,
                getIntersectionCenterX(INTERSECTION_1, width)
                    + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_UPPER));

    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_3,
                getIntersectionCenterX(INTERSECTION_2, width)
                    - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_LOWER));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_5,
                getIntersectionCenterX(INTERSECTION_3, width)
                    - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_LOWER));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_6,
                getIntersectionCenterX(INTERSECTION_4, width)
                    - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * LANE_Y_MULTIPLIER_LOWER));

    motorwayGroup.toBack();
  }

  private void repositionCompass() {
    if (compass != null) {
      CompassUtils.positionCompass(compass, simulationPane.getWidth(), simulationPane.getHeight());
    }
  }

  private Node createTrafficLight(int id, double x, double y) {
    Group lightGroup = new Group();
    lightGroup.setId(String.valueOf(id));

    Rectangle post = new Rectangle(x, y - TRAFFIC_LIGHT_Y_OFFSET, TRAFFIC_LIGHT_POST_WIDTH, TRAFFIC_LIGHT_POST_HEIGHT);
    post.setFill(Color.BLACK);
    post.setArcWidth(5);
    post.setArcHeight(5);

    Circle red = new Circle(x + TRAFFIC_LIGHT_X_CENTER_OFFSET, y + TRAFFIC_LIGHT_Y_UPPER_OFFSET, TRAFFIC_LIGHT_RADIUS, TRAFFIC_LIGHT_RED_OFF);
    red.setId("red");
    Circle green = new Circle(x + TRAFFIC_LIGHT_X_CENTER_OFFSET, y + TRAFFIC_LIGHT_Y_LOWER_OFFSET, TRAFFIC_LIGHT_RADIUS, TRAFFIC_LIGHT_GREEN_OFF);
    green.setId("green");

    lightGroup.getChildren().addAll(post, red, green);
    return lightGroup;
  }

  private void updateTrafficLights() {
    for (Node node : trafficLightsGroup.getChildren()) {
      if (node instanceof Group lightGroup) {
        int id = Integer.parseInt(lightGroup.getId());
        Circle red = (Circle) lightGroup.lookup("#red");
        Circle green = (Circle) lightGroup.lookup("#green");

        if (red != null && green != null) {
          if (trafficLightController.isGreen(id)) {
            green.setFill(Color.LIME);
            red.setFill(Color.DARKRED);
          } else {
            green.setFill(Color.DARKGREEN);
            red.setFill(Color.RED);
          }
        }
      }
    }
  }

  @FXML
  private void addVehicle() {
    disableVehicleCreationButtonsTemporarily();
    VehicleType type = vehicleTypeWrapper.getValue();
    Locations origin = startPositionWrapper.getValue();
    VehicleMovement movement = movementWrapper.getValue();
    Locations lane;
    if (movement.equals(VehicleMovement.TURN_LEFT) || movement.equals(VehicleMovement.U_TURN))
      lane = Locations.FIRST_RAIL;
    else if (movement.equals(VehicleMovement.TURN_RIGHT)) lane = Locations.THIRD_RAIL;
    else lane = Locations.SECOND_RAIL;
    Integer intersectionId =
        intersectionComboBox.isVisible() ? intersectionComboBox.getValue() : null;
    createAndStartVehicle(type, origin, lane, movement, intersectionId);
  }

  @FXML
  private void addMultipleVehicles() {
    disableVehicleCreationButtonsTemporarily();
    final int numVehicles = MULTIPLE_VEHICLES_COUNT;
    final Random random = new Random();
    VehicleMovement[] movements = VehicleMovement.getAllMovements();
    log.info("batch_vehicle_creation_started count={} simulationType=motorway", numVehicles);
    new Thread(
            () -> {
              try {
                for (int ind = 0; ind < numVehicles; ind++) {
                  Locations origin = random.nextBoolean() ? Locations.WEST : Locations.EAST;
                  VehicleMovement movement = movements[random.nextInt(movements.length)];
                  VehicleType type =
                      (random.nextInt(EMERGENCY_VEHICLE_PROBABILITY) == 0) ? VehicleType.EMERGENCY : VehicleType.NORMAL;

                  Locations lane;
                  if (movement.equals(VehicleMovement.U_TURN)
                      || movement.equals(VehicleMovement.TURN_LEFT)) lane = Locations.FIRST_RAIL;
                  else if (movement.equals(VehicleMovement.TURN_RIGHT)) lane = Locations.THIRD_RAIL;
                  else lane = Locations.SECOND_RAIL;

                  Integer intersectionId = null;
                  if (movement != VehicleMovement.STRAIGHT) {
                    List<Integer> possibleIntersections = new ArrayList<>();
                    if (origin == Locations.WEST)
                      possibleIntersections.addAll(Arrays.asList(INTERSECTION_2, INTERSECTION_3, INTERSECTION_4));
                    else possibleIntersections.addAll(Arrays.asList(INTERSECTION_1, INTERSECTION_2, INTERSECTION_3));
                    intersectionId =
                        possibleIntersections.get(random.nextInt(possibleIntersections.size()));
                  }

                  final Integer finalIntersectionId = intersectionId;
                  final Locations finalLane = lane;
                  Platform.runLater(
                      () ->
                          createAndStartVehicle(
                              type, origin, finalLane, movement, finalIntersectionId));
                  Thread.sleep(VEHICLE_SPAWN_DELAY_MS);
                }
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              log.info(
                  "batch_vehicle_creation_completed count={} simulationType=motorway", numVehicles);
            })
        .start();
  }

  private void createAndStartVehicle(
      VehicleType type,
      Locations origin,
      Locations lane,
      VehicleMovement movement,
      Integer intersectionId) {
    MotorwayIntersection targetIntersection =
        (intersectionId != null) ? intersections.get(intersectionId - FIRST_INTERSECTION) : null;
    Vehicle vehicle = new Vehicle(type, origin, movement, targetIntersection);
    vehicle.setLane(lane);
    vehicle.setController(this);

    log.info(
        "vehicle_created vehicleId={} type={} origin={} lane={} destination={} targetIntersectionId={} simulationType=motorway",
        vehicle.getId(),
        type,
        origin,
        lane,
        movement,
        intersectionId);
    vehicle.setTrafficLightController(trafficLightController);
    vehicle.setIntersectionStateManager(intersectionStateManager);

    Circle vehicleCircle =
        new Circle(
            VEHICLE_RADIUS,
            type == VehicleType.EMERGENCY
                ? Color.web(EMERGENCY_VEHICLE_COLOR)
                : Color.web(NORMAL_VEHICLE_COLOR));
    vehicleCircle.setStroke(VEHICLE_STROKE_COLOR);

    List<Point2D> path = calculateVehiclePath(vehicle);
    if (path.isEmpty()) return;

    Point2D startPos = path.getFirst();
    vehicle.setPosition(startPos.getX(), startPos.getY());
    vehicleMap.put(vehicle, vehicleCircle);
    simulationPane.getChildren().add(vehicleCircle);
    new Thread(vehicle).start();
  }

  public List<Point2D> calculateVehiclePath(Vehicle vehicle) {
    double width = simulationPane.getWidth();
    double height = simulationPane.getHeight();
    if (width == 0 || height == 0) return List.of();

    double motorwayY = (height - (LANE_HEIGHT * TOTAL_LANES)) / 2;
    Locations origin = vehicle.getOrigin();
    VehicleMovement movement = vehicle.getMovement();
    Locations lane = vehicle.getLane();
    double startY = getLaneY(origin, lane, motorwayY);

    if (movement.equals(VehicleMovement.STRAIGH_AFTER_U_TURN)) {
      Point2D start = new Point2D(vehicle.getX(), vehicle.getY());
      Point2D end =
          new Point2D(origin == Locations.WEST ? width + VEHICLE_OFFSET : -VEHICLE_OFFSET, startY);
      return List.of(start, end);
    }

    if (movement.equals(VehicleMovement.STRAIGHT)) {
      Point2D start = new Point2D(origin == Locations.WEST ? VEHICLE_PATH_OFFSET : width + VEHICLE_START_OFFSET, startY);
      Point2D end =
          new Point2D(origin == Locations.WEST ? width + VEHICLE_OFFSET : -VEHICLE_OFFSET, startY);
      return List.of(start, end);
    }

    MotorwayIntersection intersection = vehicle.getTargetIntersection();
    if (intersection == null) return List.of();

    double intersectionCenterX = getIntersectionCenterX(intersection.getId(), width);
    List<Point2D> path = new ArrayList<>();
    Point2D startPoint, stopPoint, turnPoint;

    if (origin == Locations.WEST) {
      startPoint = new Point2D(VEHICLE_PATH_OFFSET, startY);
      stopPoint =
          new Point2D(
              getStopLineForLight(
                      getLightIdForIntersection(intersection.getId(), origin),
                      origin,
                      lane,
                      width,
                      height)
                  .getX(),
              startY);
      turnPoint = new Point2D(intersectionCenterX, startY);
      path.addAll(Arrays.asList(startPoint, stopPoint, turnPoint));

      switch (movement) {
        case TURN_RIGHT:
          path.add(new Point2D(intersectionCenterX, height + VEHICLE_OFFSET));
          break;
        case TURN_LEFT:
          path.add(new Point2D(intersectionCenterX, -VEHICLE_OFFSET));
          break;
        case U_TURN:
          path.add(
              new Point2D(
                  intersectionCenterX, getLaneY(Locations.EAST, Locations.SECOND_RAIL, motorwayY)));
          path.add(
              new Point2D(
                  intersectionCenterX - U_TURN_OFFSET,
                  getLaneY(Locations.EAST, Locations.SECOND_RAIL, motorwayY)));
          break;
        default:
          break;
      }
    } else {
      startPoint = new Point2D(width + VEHICLE_OFFSET, startY);
      stopPoint =
          new Point2D(
              getStopLineForLight(
                      getLightIdForIntersection(intersection.getId(), origin),
                      origin,
                      lane,
                      width,
                      height)
                  .getX(),
              startY);
      turnPoint = new Point2D(intersectionCenterX, startY);
      path.addAll(Arrays.asList(startPoint, stopPoint, turnPoint));

      switch (movement) {
        case TURN_RIGHT -> path.add(new Point2D(intersectionCenterX, -VEHICLE_OFFSET));
        case TURN_LEFT -> path.add(new Point2D(intersectionCenterX, height + VEHICLE_OFFSET));
        case U_TURN -> {
          path.add(
              new Point2D(
                  intersectionCenterX, getLaneY(Locations.WEST, Locations.SECOND_RAIL, motorwayY)));
          path.add(
              new Point2D(
                  intersectionCenterX + U_TURN_OFFSET,
                  getLaneY(Locations.WEST, Locations.SECOND_RAIL, motorwayY)));
        }
        default -> {}
      }
    }
    return path;
  }

  private int getLightIdForIntersection(int intersectionId, Locations origin) {
    if (origin == Locations.WEST) {
      if (intersectionId == INTERSECTION_2) return TRAFFIC_LIGHT_3;
      if (intersectionId == INTERSECTION_3) return TRAFFIC_LIGHT_5;
      if (intersectionId == INTERSECTION_4) return TRAFFIC_LIGHT_6;
    } else {
      if (intersectionId == INTERSECTION_1) return TRAFFIC_LIGHT_1;
      if (intersectionId == INTERSECTION_2) return TRAFFIC_LIGHT_2;
      if (intersectionId == INTERSECTION_3) return TRAFFIC_LIGHT_4;
    }
    return -1;
  }

  public Point2D getStopLineForLight(
      int lightId, Locations origin, Locations lane, double width, double height) {
    double motorwayY = (height - (LANE_HEIGHT * TOTAL_LANES)) / 2;
    double yPos = getLaneY(origin, lane, motorwayY);
    double xPos =
        switch (lightId) {
          case TRAFFIC_LIGHT_1 ->
              getIntersectionCenterX(INTERSECTION_1, width) + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          case TRAFFIC_LIGHT_2 ->
              getIntersectionCenterX(INTERSECTION_2, width) + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          case TRAFFIC_LIGHT_4 ->
              getIntersectionCenterX(INTERSECTION_3, width) + INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          case TRAFFIC_LIGHT_3 ->
              getIntersectionCenterX(INTERSECTION_2, width) - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          case TRAFFIC_LIGHT_5 ->
              getIntersectionCenterX(INTERSECTION_3, width) - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          case TRAFFIC_LIGHT_6 ->
              getIntersectionCenterX(INTERSECTION_4, width) - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR;
          default -> 0;
        };

    return new Point2D(
        xPos - (origin == Locations.WEST ? STOP_LINE_OFFSET : -STOP_LINE_OFFSET), yPos);
  }

  private double getLaneY(Locations origin, Locations lane, double motorwayY) {
    double laneOffset = LANE_1_OFFSET;
    if (lane == Locations.SECOND_RAIL) laneOffset = LANE_2_OFFSET;
    if (lane == Locations.THIRD_RAIL) laneOffset = LANE_3_OFFSET;
    return origin == Locations.EAST
        ? motorwayY + (3 - laneOffset) * LANE_HEIGHT
        : motorwayY + (3 * LANE_HEIGHT) + (laneOffset * LANE_HEIGHT);
  }

  public Vehicle findLeadingVehicle(Vehicle followerVehicle) {
    Vehicle leader = null;
    double minDistance = Double.MAX_VALUE;

    for (Vehicle potentialLeader : vehicleMap.keySet()) {
      if (followerVehicle.equals(potentialLeader)) continue;

      if (followerVehicle.getOrigin() == potentialLeader.getOrigin()
          && followerVehicle.getLane() == potentialLeader.getLane()) {
        double distance;
        boolean isInFront;
        if (followerVehicle.getOrigin() == Locations.WEST) {
          isInFront = potentialLeader.getX() > followerVehicle.getX();
          distance = potentialLeader.getX() - followerVehicle.getX();
        } else {
          isInFront = potentialLeader.getX() < followerVehicle.getX();
          distance = followerVehicle.getX() - potentialLeader.getX();
        }
        if (isInFront && distance < minDistance) {
          minDistance = distance;
          leader = potentialLeader;
        }
      }
    }
    return leader;
  }

  public Vehicle findEmergencyFollower(Vehicle leader) {
    for (Vehicle potentialFollower : vehicleMap.keySet()) {
      if (leader.equals(potentialFollower)
          || potentialFollower.getType() != VehicleType.EMERGENCY) {
        continue;
      }

      if (leader.getOrigin() == potentialFollower.getOrigin()
          && leader.getLane() == potentialFollower.getLane()) {
        boolean isBehind;
        if (leader.getOrigin() == Locations.WEST) {
          isBehind = potentialFollower.getX() < leader.getX();
        } else {
          isBehind = potentialFollower.getX() > leader.getX();
        }

        if (isBehind
            && leader.distanceTo(new Point2D(potentialFollower.getX(), potentialFollower.getY()))
                < SAFE_DISTANCE * SAFE_DISTANCE_MULTIPLIER) {
          return potentialFollower;
        }
      }
    }
    return null;
  }

  public void spawnStraightVehicleFromUTurn(Vehicle uTurnVehicle) {
    Locations newOrigin =
        (uTurnVehicle.getOrigin() == Locations.WEST) ? Locations.EAST : Locations.WEST;
    VehicleType type = uTurnVehicle.getType();
    Locations lane = Locations.SECOND_RAIL;

    Vehicle straightVehicle =
        new Vehicle(
            type, newOrigin, VehicleMovement.STRAIGH_AFTER_U_TURN, (MotorwayIntersection) null);

    straightVehicle.setLane(lane);
    straightVehicle.setController(this);
    straightVehicle.setTrafficLightController(trafficLightController);
    straightVehicle.setIntersectionStateManager(intersectionStateManager);

    straightVehicle.setPosition(uTurnVehicle.getX(), uTurnVehicle.getY());

    Circle vehicleCircle =
        new Circle(
            VEHICLE_RADIUS,
            type == VehicleType.EMERGENCY
                ? Color.web(EMERGENCY_VEHICLE_COLOR)
                : Color.web(NORMAL_VEHICLE_COLOR));
    vehicleCircle.setStroke(VEHICLE_STROKE_COLOR);

    vehicleMap.put(straightVehicle, vehicleCircle);
    simulationPane.getChildren().add(vehicleCircle);
    new Thread(straightVehicle).start();
  }

  public double getSimulationPaneWidth() {
    return simulationPane.getWidth();
  }

  private void startVehicleAnimationLoop() {
    animationTimer =
        new AnimationTimer() {
          @Override
          public void handle(long now) {
            updateTrafficLights();

            Iterator<Map.Entry<Vehicle, Circle>> iterator = vehicleMap.entrySet().iterator();
            while (iterator.hasNext()) {
              Map.Entry<Vehicle, Circle> entry = iterator.next();
              if (entry.getKey().isFinished()) {
                simulationPane.getChildren().remove(entry.getValue());
                iterator.remove();
              } else {
                entry.getValue().relocate(entry.getKey().getX() - VEHICLE_RELOCATE_OFFSET, entry.getKey().getY() - VEHICLE_RELOCATE_OFFSET);
              }
            }
          }
        };
    animationTimer.start();
  }

  private void disableVehicleCreationButtonsTemporarily() {
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

  @FXML
  private void goBackToMenu() {
    if (animationTimer != null) animationTimer.stop();
    trafficLightController.shutdown();
    log.info("simulation_stopping vehicleCount={} simulationType=motorway", vehicleMap.size());
    for (Vehicle vehicle : vehicleMap.keySet()) vehicle.stop();
    vehicleMap.clear();
    simulationPane.getChildren().clear();
    compass = null;
    log.info("simulation_cleaned simulationType=motorway");
    ViewsHandler.changeView(ViewsHandler.MAIN_VIEW);
  }
}
