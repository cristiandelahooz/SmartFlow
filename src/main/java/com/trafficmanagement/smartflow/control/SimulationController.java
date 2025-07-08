package com.trafficmanagement.smartflow.control;

import com.trafficmanagement.smartflow.model.Intersection;
import com.trafficmanagement.smartflow.model.TrafficLight;
import com.trafficmanagement.smartflow.model.Vehicle;
import com.trafficmanagement.smartflow.model.VehicleType;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimulationController {

    @FXML
    private AnchorPane simulationPane;

    private Intersection intersection;

    // Instance variables for road dimensions
    private double roadWidth = 160.0;
    private double paneWidth;
    private double paneHeight;
    private double center_x;
    private double center_y;
    private double road_half_width;
    private double lane_center_offset;
    private double x_inner_left_lane;
    private double x_inner_right_lane;
    private double y_inner_top_lane;
    private double y_inner_bottom_lane;
    private double x_intersection_left;
    private double x_intersection_right;
    private double y_intersection_top;
    private double y_intersection_bottom;
    private double y_east_left_lane;
    private double y_west_right_lane;

    @FXML
    public void initialize() {
        List<TrafficLight> trafficLights;
        TrafficController trafficController;
        intersection = new Intersection("intersection1", true, 4);
        trafficLights = new ArrayList<>();
        trafficLights.add(new TrafficLight("northWest"));
        trafficLights.add(new TrafficLight("northEast"));
        trafficLights.add(new TrafficLight("southWest"));
        trafficLights.add(new TrafficLight("southEast"));

        trafficController = new TrafficController(intersection, trafficLights);
        trafficController.startControl();

        Platform.runLater(() -> drawIntersection(trafficLights));
    }

    private void drawIntersection(List<TrafficLight> trafficLights) {
        simulationPane.getChildren().clear();
        paneWidth = simulationPane.getWidth();
        paneHeight = simulationPane.getHeight();

        center_x = paneWidth / 2;
        center_y = paneHeight / 2;
        road_half_width = roadWidth / 2; // 80
        lane_center_offset = roadWidth / 4; // 40

        // Lane center coordinates within the intersection
        x_inner_left_lane = center_x - lane_center_offset;
        x_inner_right_lane = center_x + lane_center_offset;
        y_inner_top_lane = center_y - lane_center_offset;
        y_inner_bottom_lane = center_y + lane_center_offset;

        // Intersection boundaries (for turns)
        x_intersection_left = center_x - road_half_width;
        x_intersection_right = center_x + road_half_width;
        y_intersection_top = center_y - road_half_width;
        y_intersection_bottom = center_y + road_half_width;

        y_east_left_lane = center_y - lane_center_offset; // Left lane for E->W, E->N, E->S (exit lane for N-E, S-E)
        y_west_right_lane = center_y + lane_center_offset; // Right lane for W->E, W->N, W->S (exit lane for N-W, S-W)

        Rectangle hRoad = new Rectangle(0, (paneHeight / 2) - (roadWidth / 2), paneWidth, roadWidth);
        hRoad.setFill(Color.DARKGRAY);
        simulationPane.getChildren().add(hRoad);

        Rectangle vRoad = new Rectangle((paneWidth / 2) - (roadWidth / 2), 0, roadWidth, paneHeight);
        vRoad.setFill(Color.DARKGRAY);
        simulationPane.getChildren().add(vRoad);

        for (int i = 0; i < paneWidth / 20; i++) {
            Rectangle dash = new Rectangle(i * 40 + 10, paneHeight / 2 - 2, 20, 4);
            dash.setFill(Color.YELLOW);
            simulationPane.getChildren().add(dash);
        }

        for (int i = 0; i < paneHeight / 20; i++) {
            Rectangle dash = new Rectangle(paneWidth / 2 - 2, i * 40 + 10, 4, 20);
            dash.setFill(Color.YELLOW);
            simulationPane.getChildren().add(dash);
        }

        drawTrafficLight(paneWidth / 2 - roadWidth / 2 - 40, paneHeight / 2 - roadWidth / 2 - 40, "northWest", trafficLights.get(0));
        drawTrafficLight(paneWidth / 2 + roadWidth / 2 + 20, paneHeight / 2 - roadWidth / 2 - 40, "northEast", trafficLights.get(1));
        drawTrafficLight(paneWidth / 2 - roadWidth / 2 - 40, paneHeight / 2 + roadWidth / 2 + 20, "southWest", trafficLights.get(2));
        drawTrafficLight(paneWidth / 2 + roadWidth / 2 + 20, paneHeight / 2 + roadWidth / 2 + 20, "southEast", trafficLights.get(3));
    }

    private void drawTrafficLight(double x, double y, String id, TrafficLight trafficLight) {
        Group trafficLightGroup = new Group();
        trafficLightGroup.setLayoutX(x);
        trafficLightGroup.setLayoutY(y);
        trafficLightGroup.setId(id);

        Rectangle casing = new Rectangle(0, 0, 20, 60);
        casing.setFill(Color.BLACK);
        trafficLightGroup.getChildren().add(casing);

        Circle redLight = new Circle(10, 10, 8);
        redLight.setFill(Color.RED);
        redLight.setId(id + "Red");
        trafficLightGroup.getChildren().add(redLight);

        Circle yellowLight = new Circle(10, 30, 8);
        yellowLight.setFill(Color.GRAY);
        yellowLight.setId(id + "Yellow");
        trafficLightGroup.getChildren().add(yellowLight);

        Circle greenLight = new Circle(10, 50, 8);
        greenLight.setFill(Color.GRAY);
        greenLight.setId(id + "Green");
        trafficLightGroup.getChildren().add(greenLight);

        trafficLight.greenProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                redLight.setFill(newVal ? Color.GRAY : Color.RED);
                greenLight.setFill(newVal ? Color.GREEN : Color.GRAY);
            });
        });

        simulationPane.getChildren().add(trafficLightGroup);
    }

    private void createVehicle(String direction, VehicleType type) {
        String id = UUID.randomUUID().toString();
        Vehicle vehicle = new Vehicle(id, type, direction, intersection);
        intersection.addVehicle(vehicle);
        drawVehicle(vehicle);
    }

    private void drawVehicle(Vehicle vehicle) {
        Circle vehicleShape = new Circle(10, vehicle.getType() == VehicleType.EMERGENCY ? Color.RED : Color.BLUE);
        vehicleShape.setId(vehicle.getId());

        Path path = new Path();

        // Lane center coordinates at intersection edges
        final double x_north_entry = center_x + lane_center_offset; // Right lane for N->S, N->E, N->W
        final double x_south_entry = center_x - lane_center_offset; // Right lane for S->N, S->W, S->E
        final double y_east_entry = center_y + lane_center_offset; // Right lane for E->W, E->N, E->S
        final double y_west_entry = center_y - lane_center_offset; // Right lane for W->E, W->N, W->S

        switch (vehicle.getDirection()) {
            case "N-S": // North to South (Straight)
                path.getElements().add(new MoveTo(x_north_entry, 0));
                path.getElements().add(new LineTo(x_north_entry, paneHeight));
                break;
            case "N-E": // North to East (Right Turn)
                path.getElements().add(new MoveTo(x_north_entry, 0));
                path.getElements().add(new LineTo(x_north_entry, y_intersection_top + lane_center_offset)); // To turn point
                path.getElements().add(new LineTo(x_intersection_right - lane_center_offset, y_intersection_top + lane_center_offset)); // Turn
                path.getElements().add(new LineTo(paneWidth, y_east_left_lane)); // Exit
                break;
            case "N-W": // North to West (Left Turn)
                path.getElements().add(new MoveTo(x_north_entry, 0));
                path.getElements().add(new LineTo(x_north_entry, y_intersection_top + lane_center_offset)); // To turn point
                path.getElements().add(new LineTo(x_inner_left_lane, y_inner_top_lane)); // Move to center of intersection
                path.getElements().add(new LineTo(x_inner_left_lane, y_intersection_bottom - lane_center_offset)); // Continue through intersection
                path.getElements().add(new LineTo(0, y_west_right_lane)); // Exit
                break;

            case "S-N": // South to North (Straight)
                path.getElements().add(new MoveTo(x_south_entry, paneHeight));
                path.getElements().add(new LineTo(x_south_entry, 0));
                break;
            case "S-E": // South to East (Left Turn)
                path.getElements().add(new MoveTo(x_south_entry, paneHeight));
                path.getElements().add(new LineTo(x_south_entry, y_intersection_bottom - lane_center_offset)); // To turn point
                path.getElements().add(new LineTo(x_inner_right_lane, y_inner_bottom_lane)); // Move to center of intersection
                path.getElements().add(new LineTo(x_inner_right_lane, y_intersection_top + lane_center_offset)); // Continue through intersection
                path.getElements().add(new LineTo(paneWidth, y_east_left_lane)); // Exit
                break;
            case "S-W": // South to West (Right Turn)
                path.getElements().add(new MoveTo(x_south_entry, paneHeight));
                path.getElements().add(new LineTo(x_south_entry, y_intersection_bottom - lane_center_offset)); // To turn point
                path.getElements().add(new LineTo(x_intersection_left + lane_center_offset, y_intersection_bottom - lane_center_offset)); // Turn
                path.getElements().add(new LineTo(0, y_west_right_lane)); // Exit
                break;

            case "E-W": // East to West (Straight)
                path.getElements().add(new MoveTo(paneWidth, y_east_entry));
                path.getElements().add(new LineTo(0, y_east_entry));
                break;
            case "E-N": // East to North (Left Turn)
                path.getElements().add(new MoveTo(paneWidth, y_east_entry));
                path.getElements().add(new LineTo(x_intersection_right - lane_center_offset, y_east_entry)); // To turn point
                path.getElements().add(new LineTo(x_inner_right_lane, y_inner_top_lane)); // Move to center of intersection
                path.getElements().add(new LineTo(x_inner_left_lane, y_inner_top_lane)); // Continue through intersection
                path.getElements().add(new LineTo(x_north_entry, 0)); // Exit
                break;
            case "E-S": // East to South (Right Turn)
                path.getElements().add(new MoveTo(paneWidth, y_east_entry));
                path.getElements().add(new LineTo(x_intersection_right - lane_center_offset, y_east_entry)); // To turn point
                path.getElements().add(new LineTo(x_intersection_right - lane_center_offset, y_intersection_bottom - lane_center_offset)); // Turn
                path.getElements().add(new LineTo(x_south_entry, paneHeight)); // Exit
                break;

            case "W-E": // West to East (Straight)
                path.getElements().add(new MoveTo(0, y_west_entry));
                path.getElements().add(new LineTo(paneWidth, y_west_entry));
                break;
            case "W-N": // West to North (Right Turn)
                path.getElements().add(new MoveTo(0, y_west_entry));
                path.getElements().add(new LineTo(x_intersection_left + lane_center_offset, y_west_entry)); // To turn point
                path.getElements().add(new LineTo(x_intersection_left + lane_center_offset, y_intersection_top + lane_center_offset)); // Turn
                path.getElements().add(new LineTo(x_north_entry, 0)); // Exit
                break;
            case "W-S": // West to South (Left Turn)
                path.getElements().add(new MoveTo(0, y_west_entry));
                path.getElements().add(new LineTo(x_intersection_left + lane_center_offset, y_west_entry)); // To turn point
                path.getElements().add(new LineTo(x_inner_left_lane, y_inner_bottom_lane)); // Move to center of intersection
                path.getElements().add(new LineTo(x_inner_right_lane, y_inner_bottom_lane)); // Continue through intersection
                path.getElements().add(new LineTo(x_south_entry, paneHeight)); // Exit
                break;
        }

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.seconds(5));
        pathTransition.setPath(path);
        pathTransition.setNode(vehicleShape);
        pathTransition.setOnFinished(event -> Platform.runLater(() -> simulationPane.getChildren().remove(vehicleShape)));

        Platform.runLater(() -> {
            simulationPane.getChildren().add(vehicleShape);
            pathTransition.play();
        });
    }

    @FXML
    private void onNorthSouth() {
        createVehicle("N-S", VehicleType.NORMAL);
    }

    @FXML
    private void onNorthEast() {
        createVehicle("N-E", VehicleType.NORMAL);
    }

    @FXML
    private void onNorthWest() {
        createVehicle("N-W", VehicleType.NORMAL);
    }

    @FXML
    private void onSouthNorth() {
        createVehicle("S-N", VehicleType.NORMAL);
    }

    @FXML
    private void onSouthEast() {
        createVehicle("S-E", VehicleType.NORMAL);
    }

    @FXML
    private void onSouthWest() {
        createVehicle("S-W", VehicleType.NORMAL);
    }

    @FXML
    private void onEastNorth() {
        createVehicle("E-N", VehicleType.NORMAL);
    }

    @FXML
    private void onEastSouth() {
        createVehicle("E-S", VehicleType.NORMAL);
    }

    @FXML
    private void onEastWest() {
        createVehicle("E-W", VehicleType.NORMAL);
    }

    @FXML
    private void onWestNorth() {
        createVehicle("W-N", VehicleType.NORMAL);
    }

    @FXML
    private void onWestSouth() {
        createVehicle("W-S", VehicleType.NORMAL);
    }

    @FXML
    private void onWestEast() {
        createVehicle("W-E", VehicleType.NORMAL);
    }

    @FXML
    private void onCreateEmergencyVehicle() {
        createVehicle("N-S", VehicleType.EMERGENCY);
    }
}