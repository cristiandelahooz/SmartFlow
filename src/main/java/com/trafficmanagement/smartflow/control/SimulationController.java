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

    @FXML
    public void initialize() {
        List<TrafficLight> trafficLights;
        TrafficController trafficController;
        intersection = new Intersection("intersection1", true, 4);
        trafficLights = new ArrayList<>();
        trafficLights.add(new TrafficLight("north"));
        trafficLights.add(new TrafficLight("south"));
        trafficLights.add(new TrafficLight("east"));
        trafficLights.add(new TrafficLight("west"));

        trafficController = new TrafficController(intersection, trafficLights);
        trafficController.startControl();

        Platform.runLater(() -> drawIntersection(trafficLights));
    }

    private void drawIntersection(List<TrafficLight> trafficLights) {
        simulationPane.getChildren().clear();
        final double roadWidth = 160.0;
        final double paneWidth = simulationPane.getWidth();
        final double paneHeight = simulationPane.getHeight();

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
        String[] directions = vehicle.getDirection().split("-");
        double startX = 0;
        double startY = 0;
        double endX = 0;
        double endY = 0;
        double paneWidth = simulationPane.getWidth();
        double paneHeight = simulationPane.getHeight();

        switch (directions[0]) {
            case "N":
                startX = paneWidth / 2 - 40;
                startY = 0;
                break;
            case "S":
                startX = paneWidth / 2 + 40;
                startY = paneHeight;
                break;
            case "E":
                startX = paneWidth;
                startY = paneHeight / 2 - 40;
                break;
            case "W":
                startX = 0;
                startY = paneHeight / 2 + 40;
                break;
        }

        switch (directions[1]) {
            case "N":
                endX = paneWidth / 2 - 40;
                endY = 0;
                break;
            case "S":
                endX = paneWidth / 2 + 40;
                endY = paneHeight;
                break;
            case "E":
                endX = paneWidth;
                endY = paneHeight / 2 - 40;
                break;
            case "W":
                endX = 0;
                endY = paneHeight / 2 + 40;
                break;
        }

        path.getElements().add(new MoveTo(startX, startY));
        path.getElements().add(new LineTo(endX, endY));

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