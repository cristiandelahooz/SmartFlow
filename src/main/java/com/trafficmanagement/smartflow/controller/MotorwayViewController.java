package com.trafficmanagement.smartflow.controller;

import static com.trafficmanagement.smartflow.utils.MotorwayConstants.*;

import com.trafficmanagement.smartflow.data.enums.Direction;
import com.trafficmanagement.smartflow.data.enums.VehicleMovement;
import com.trafficmanagement.smartflow.data.enums.VehicleType;
import com.trafficmanagement.smartflow.data.model.IntersectionStateManager;
import com.trafficmanagement.smartflow.data.model.MotorwayIntersection;
import com.trafficmanagement.smartflow.data.model.Vehicle;
import com.trafficmanagement.smartflow.ui.ComboBoxWrapper;
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
  @FXML private ComboBox<VehicleType> vehicleTypeComboBox;
  @FXML private ComboBox<Direction> startPosition;
  @FXML private ComboBox<VehicleMovement> movementComboBox;
  @FXML private ComboBox<Integer> intersectionComboBox;
  private ComboBoxWrapper<VehicleType> vehicleTypeWrapper;
  private ComboBoxWrapper<Direction> startPositionWrapper;
  private ComboBoxWrapper<VehicleMovement> movementWrapper;
  @FXML private Button addVehicleButton;
  @FXML private Button addMultipleButton;
  @FXML private Button backButton;
  @FXML private Label intersectionLabel;
  @Getter @FXML private Pane simulationPane;
  private AnimationTimer animationTimer;

  @FXML
  public void initialize() {
    for (int ind = 1; ind <= 4; ind++) intersections.add(new MotorwayIntersection(ind));
    log.info("motorway_controller_initialized intersectionCount=4 simulationType=motorway");

    simulationPane.getChildren().addAll(motorwayGroup, trafficLightsGroup);

    vehicleTypeWrapper = new ComboBoxWrapper<>(vehicleTypeComboBox);
    startPositionWrapper = new ComboBoxWrapper<>(startPosition);
    movementWrapper = new ComboBoxWrapper<>(movementComboBox);

    vehicleTypeWrapper.getItems().setAll(VehicleType.values());
    startPositionWrapper.getItems().setAll(Direction.getMotorwayDirections());
    movementWrapper.getItems().setAll(VehicleMovement.values());

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
    simulationPane.widthProperty().addListener((obs, o, n) -> redrawMotorway());
    simulationPane.heightProperty().addListener((obs, o, n) -> redrawMotorway());
    startVehicleAnimationLoop();
  }

  private void updateIntersectionSelectorVisibility(VehicleMovement movement) {
    boolean isTurn = movement.isTurn();
    intersectionLabel.setVisible(isTurn);
    intersectionLabel.setManaged(isTurn);
    intersectionComboBox.setVisible(isTurn);
    intersectionComboBox.setManaged(isTurn);
  }

  private void updateAvailableIntersections(Direction origin) {
    Integer previouslySelected = intersectionComboBox.getValue();
    intersectionComboBox.getItems().clear();
    if (origin == Direction.WEST) {
      intersectionComboBox.getItems().setAll(2, 3, 4);
    } else {
      intersectionComboBox.getItems().setAll(1, 2, 3);
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
      case INTERSECTION_1 -> INTERSECTION_WIDTH / 2.0;
      case INTERSECTION_2 -> totalMotorwayWidth / 2.0 - gapFromCenter;
      case INTERSECTION_3 -> totalMotorwayWidth / 2.0 + gapFromCenter;
      case INTERSECTION_4 -> totalMotorwayWidth - INTERSECTION_WIDTH / 2.0;
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

    for (int ind = 1; ind <= 4; ind++) {
      double centerX = getIntersectionCenterX(ind, width);
      Rectangle vStreet =
          new Rectangle(centerX - INTERSECTION_WIDTH / 2, 0, INTERSECTION_WIDTH, height);
      vStreet.setFill(MOTORWAY_COLOR);
      motorwayGroup.getChildren().add(vStreet);
    }

    for (int ind = 1; ind < 6; ind++) {
      if (ind == 3) continue;
      Line laneLine =
          new Line(0, motorwayY + ind * LANE_HEIGHT, width, motorwayY + ind * LANE_HEIGHT);
      laneLine.setStroke(LANE_LINE_COLOR);
      laneLine.getStrokeDashArray().addAll(LANE_DASH_LENGTH, LANE_DASH_SPACING);
      motorwayGroup.getChildren().add(laneLine);
    }

    double wallY = motorwayY + 3 * LANE_HEIGHT;
    double lastX = 0;
    for (int ind = 1; ind <= 4; ind++) {
      double centerX = getIntersectionCenterX(ind, width);
      double gapStart = centerX - INTERSECTION_WIDTH / 2;
      Rectangle wallSegment =
          new Rectangle(lastX, wallY - WALL_VERTICAL_OFFSET, gapStart - lastX, WALL_HEIGHT);
      wallSegment.setFill(WALL_COLOR);
      motorwayGroup.getChildren().add(wallSegment);
      lastX = centerX + INTERSECTION_WIDTH / 2;
    }
    Rectangle finalWallSegment =
        new Rectangle(lastX, wallY - WALL_VERTICAL_OFFSET, width - lastX, WALL_HEIGHT);
    finalWallSegment.setFill(WALL_COLOR);
    motorwayGroup.getChildren().add(finalWallSegment);

    // Semáforos vía superior (derecha a izquierda), IDs: 4, 2, 1
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_4,
                getIntersectionCenterX(INTERSECTION_3, width)
                    + INTERSECTION_WIDTH / 2
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * 1.5));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_2,
                getIntersectionCenterX(INTERSECTION_2, width)
                    + INTERSECTION_WIDTH / 2
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * 1.5));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_1,
                getIntersectionCenterX(INTERSECTION_1, width)
                    + INTERSECTION_WIDTH / 2
                    + TRAFFIC_LIGHT_OFFSET,
                motorwayY + LANE_HEIGHT * 1.5));

    // Semáforos vía inferior (izquierda a derecha), IDs: 3, 5, 6
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_3,
                getIntersectionCenterX(INTERSECTION_2, width)
                    - INTERSECTION_WIDTH / 2
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * 4.5));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_5,
                getIntersectionCenterX(INTERSECTION_3, width)
                    - INTERSECTION_WIDTH / 2
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * 4.5));
    trafficLightsGroup
        .getChildren()
        .add(
            createTrafficLight(
                TRAFFIC_LIGHT_6,
                getIntersectionCenterX(INTERSECTION_4, width)
                    - INTERSECTION_WIDTH / 2
                    - TRAFFIC_LIGHT_EXTENDED_OFFSET,
                motorwayY + LANE_HEIGHT * 4.5));

    motorwayGroup.toBack();
  }

  private Node createTrafficLight(int id, double x, double y) {
    Group lightGroup = new Group();
    lightGroup.setId(String.valueOf(id)); // ID del grupo para referencia

    Rectangle post = new Rectangle(x, y - 20, 14, 40);
    post.setFill(Color.BLACK);
    post.setArcWidth(5);
    post.setArcHeight(5);

    Circle red = new Circle(x + 7, y - 10, 6, Color.DARKRED);
    red.setId("red");
    Circle green = new Circle(x + 7, y + 10, 6, Color.DARKGREEN);
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
    Direction origin = startPositionWrapper.getValue();
    VehicleMovement movement = movementWrapper.getValue();
    Direction lane;
    if (movement.equals(VehicleMovement.TURN_LEFT) || movement.equals(VehicleMovement.U_TURN))
      lane = Direction.FIRST_RAIL;
    else if (movement.equals(VehicleMovement.TURN_RIGHT)) lane = Direction.THIRD_RAIL;
    else lane = Direction.SECOND_RAIL;
    Integer intersectionId =
        intersectionComboBox.isVisible() ? intersectionComboBox.getValue() : null;
    createAndStartVehicle(type, origin, lane, movement, intersectionId);
  }

  @FXML
  private void addMultipleVehicles() {
    disableVehicleCreationButtonsTemporarily();
    final int numVehicles = 15;
    final Random random = new Random();
    VehicleMovement[] movements = VehicleMovement.getAllMovements();
    log.info("batch_vehicle_creation_started count={} simulationType=motorway", numVehicles);
    new Thread(
            () -> {
              try {
                for (int ind = 0; ind < numVehicles; ind++) {
                  Direction origin = random.nextBoolean() ? Direction.WEST : Direction.EAST;
                  VehicleMovement movement = movements[random.nextInt(movements.length)];
                  VehicleType type =
                      (random.nextInt(1000) == 0) ? VehicleType.EMERGENCY : VehicleType.NORMAL;

                  Direction lane;
                  if (movement.equals(VehicleMovement.U_TURN)
                      || movement.equals(VehicleMovement.TURN_LEFT)) lane = Direction.FIRST_RAIL;
                  else if (movement.equals(VehicleMovement.TURN_RIGHT)) lane = Direction.THIRD_RAIL;
                  else lane = Direction.SECOND_RAIL;

                  Integer intersectionId = null;
                  if (movement != VehicleMovement.STRAIGHT) {
                    List<Integer> possibleIntersections = new ArrayList<>();
                    if (origin == Direction.WEST)
                      possibleIntersections.addAll(Arrays.asList(2, 3, 4));
                    else possibleIntersections.addAll(Arrays.asList(1, 2, 3));
                    intersectionId =
                        possibleIntersections.get(random.nextInt(possibleIntersections.size()));
                  }

                  final Integer finalIntersectionId = intersectionId;
                  final Direction finalLane = lane;
                  Platform.runLater(
                      () ->
                          createAndStartVehicle(
                              type, origin, finalLane, movement, finalIntersectionId));
                  Thread.sleep(1000);
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
      Direction origin,
      Direction lane,
      VehicleMovement movement,
      Integer intersectionId) {
    MotorwayIntersection targetIntersection =
        (intersectionId != null) ? intersections.get(intersectionId - 1) : null;
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
    Direction origin = vehicle.getOrigin();
    VehicleMovement movement = vehicle.getMovement();
    Direction lane = vehicle.getLane();
    double startY = getLaneY(origin, lane, motorwayY);

    if (movement.equals(VehicleMovement.STRAIGH_AFTER_U_TURN)) {
      Point2D start = new Point2D(vehicle.getX(), vehicle.getY());
      Point2D end =
          new Point2D(origin == Direction.WEST ? width + VEHICLE_OFFSET : -VEHICLE_OFFSET, startY);
      return List.of(start, end);
    }

    if (movement.equals(VehicleMovement.STRAIGHT)) {
      Point2D start = new Point2D(origin == Direction.WEST ? -50 : width + 50, startY);
      Point2D end =
          new Point2D(origin == Direction.WEST ? width + VEHICLE_OFFSET : -VEHICLE_OFFSET, startY);
      return List.of(start, end);
    }

    MotorwayIntersection intersection = vehicle.getTargetIntersection();
    if (intersection == null) return List.of();

    double intersectionCenterX = getIntersectionCenterX(intersection.getId(), width);
    List<Point2D> path = new ArrayList<>();
    Point2D startPoint, stopPoint, turnPoint;

    if (origin == Direction.WEST) {
      startPoint = new Point2D(-50, startY);
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
                  intersectionCenterX, getLaneY(Direction.EAST, Direction.SECOND_RAIL, motorwayY)));
          path.add(
              new Point2D(
                  intersectionCenterX - U_TURN_OFFSET,
                  getLaneY(Direction.EAST, Direction.SECOND_RAIL, motorwayY)));
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
                  intersectionCenterX, getLaneY(Direction.WEST, Direction.SECOND_RAIL, motorwayY)));
          path.add(
              new Point2D(
                  intersectionCenterX + U_TURN_OFFSET,
                  getLaneY(Direction.WEST, Direction.SECOND_RAIL, motorwayY)));
        }
        default -> {}
      }
    }
    return path;
  }

  private int getLightIdForIntersection(int intersectionId, Direction origin) {
    if (origin == Direction.WEST) { // Vía inferior
      if (intersectionId == 2) return 3;
      if (intersectionId == 3) return 5;
      if (intersectionId == 4) return 6;
    } else { // Vía superior
      if (intersectionId == 1) return 1;
      if (intersectionId == 2) return 2;
      if (intersectionId == 3) return 4;
    }
    return -1; // No debería ocurrir
  }

  public Point2D getStopLineForLight(
      int lightId, Direction origin, Direction lane, double width, double height) {
    double motorwayY = (height - (LANE_HEIGHT * TOTAL_LANES)) / 2;
    double yPos = getLaneY(origin, lane, motorwayY);
    double xPos =
        switch (lightId) {
          case TRAFFIC_LIGHT_1 ->
              getIntersectionCenterX(INTERSECTION_1, width) + INTERSECTION_WIDTH / 2;
          case TRAFFIC_LIGHT_2 ->
              getIntersectionCenterX(INTERSECTION_2, width) + INTERSECTION_WIDTH / 2;
          case TRAFFIC_LIGHT_4 ->
              getIntersectionCenterX(INTERSECTION_3, width) + INTERSECTION_WIDTH / 2;
          case TRAFFIC_LIGHT_3 ->
              getIntersectionCenterX(INTERSECTION_2, width) - INTERSECTION_WIDTH / 2;
          case TRAFFIC_LIGHT_5 ->
              getIntersectionCenterX(INTERSECTION_3, width) - INTERSECTION_WIDTH / 2;
          case TRAFFIC_LIGHT_6 ->
              getIntersectionCenterX(INTERSECTION_4, width) - INTERSECTION_WIDTH / 2;
          default -> 0;
        };

    return new Point2D(
        xPos - (origin == Direction.WEST ? STOP_LINE_OFFSET : -STOP_LINE_OFFSET), yPos);
  }

  private double getLaneY(Direction origin, Direction lane, double motorwayY) {
    double laneOffset = LANE_1_OFFSET;
    if (lane == Direction.SECOND_RAIL) laneOffset = LANE_2_OFFSET;
    if (lane == Direction.THIRD_RAIL) laneOffset = LANE_3_OFFSET;
    return origin == Direction.EAST
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
        if (followerVehicle.getOrigin() == Direction.WEST) {
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
        if (leader.getOrigin() == Direction.WEST) { // Moviéndose a la derecha
          isBehind = potentialFollower.getX() < leader.getX();
        } else { // Moviéndose a la izquierda
          isBehind = potentialFollower.getX() > leader.getX();
        }

        if (isBehind
            && leader.distanceTo(new Point2D(potentialFollower.getX(), potentialFollower.getY()))
                < SAFE_DISTANCE * 1.5) {
          return potentialFollower;
        }
      }
    }
    return null;
  }

  public void spawnStraightVehicleFromUTurn(Vehicle uTurnVehicle) {
    Direction newOrigin =
        (uTurnVehicle.getOrigin() == Direction.WEST) ? Direction.EAST : Direction.WEST;
    VehicleType type = uTurnVehicle.getType();
    Direction lane = Direction.SECOND_RAIL;

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
                entry.getValue().relocate(entry.getKey().getX() - 10, entry.getKey().getY() - 10);
              }
            }
          }
        };
    animationTimer.start();
  }

  private void disableVehicleCreationButtonsTemporarily() {
    addVehicleButton.setDisable(true);
    addMultipleButton.setDisable(true);
    PauseTransition pause = new PauseTransition(Duration.seconds(1));
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
    log.info("simulation_cleaned simulationType=motorway");
    ViewsHandler.changeView(ViewsHandler.MAIN_VIEW);
  }
}
