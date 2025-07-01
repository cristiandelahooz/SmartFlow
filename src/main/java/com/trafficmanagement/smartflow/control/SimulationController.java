package com.trafficmanagement.smartflow.control;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.Group;

/**
 * Controller for the traffic simulation view.
 * Manages the drawing of the intersection and traffic lights.
 */
public class SimulationController {

    @FXML
    private AnchorPane simulationPane;

    /**
     * Initializes the controller. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Draw initially
        drawIntersection();

        // Add listeners for responsive resizing
        simulationPane.widthProperty().addListener((obs, oldVal, newVal) -> drawIntersection());
        simulationPane.heightProperty().addListener((obs, oldVal, newVal) -> drawIntersection());
    }

    /**
     * Draws the intersection layout on the simulation pane.
     * This method creates the road segments for a 4-way intersection.
     */
    private void drawIntersection() {
        // Clear existing drawings before redrawing
        simulationPane.getChildren().clear();
        // Define road width
        final double roadWidth = 160.0; // Increased for two lanes
        final double laneWidth = roadWidth / 2;
        final double paneWidth = simulationPane.getWidth();
        final double paneHeight = simulationPane.getHeight();

        // Horizontal road
        Rectangle hRoad = new Rectangle(0, (paneHeight / 2) - (roadWidth / 2), paneWidth, roadWidth);
        hRoad.setFill(Color.DARKGRAY);
        simulationPane.getChildren().add(hRoad);

        // Vertical road
        Rectangle vRoad = new Rectangle((paneWidth / 2) - (roadWidth / 2), 0, roadWidth, paneHeight);
        vRoad.setFill(Color.DARKGRAY);
        simulationPane.getChildren().add(vRoad);

        // Lane markings for horizontal road
        for (int i = 0; i < paneWidth / 20; i++) { // Draw dashed line
            Rectangle dash = new Rectangle(i * 40 + 10, paneHeight / 2 - 2, 20, 4);
            dash.setFill(Color.YELLOW);
            simulationPane.getChildren().add(dash);
        }

        // Lane markings for vertical road
        for (int i = 0; i < paneHeight / 20; i++) { // Draw dashed line
            Rectangle dash = new Rectangle(paneWidth / 2 - 2, i * 40 + 10, 4, 20);
            dash.setFill(Color.YELLOW);
            simulationPane.getChildren().add(dash);
        }

        // Draw traffic lights at each corner of the intersection
        // Adjust positions for wider roads
        drawTrafficLight(paneWidth / 2 - roadWidth / 2 - 40, paneHeight / 2 - roadWidth / 2 - 40, "northWest");
        drawTrafficLight(paneWidth / 2 + roadWidth / 2 + 20, paneHeight / 2 - roadWidth / 2 - 40, "northEast");
        drawTrafficLight(paneWidth / 2 - roadWidth / 2 - 40, paneHeight / 2 + roadWidth / 2 + 20, "southWest");
        drawTrafficLight(paneWidth / 2 + roadWidth / 2 + 20, paneHeight / 2 + roadWidth / 2 + 20, "southEast");
    }

    /**
     * Draws a traffic light at the specified coordinates.
     *
     * @param x The x-coordinate for the traffic light.
     * @param y The y-coordinate for the traffic light.
     * @param id An identifier for the traffic light (e.g., "northWest").
     */
    private void drawTrafficLight(double x, double y, String id) {
        Group trafficLightGroup = new Group();
        trafficLightGroup.setLayoutX(x);
        trafficLightGroup.setLayoutY(y);
        trafficLightGroup.setId(id); // Set an ID for easy retrieval if needed

        // Traffic light casing
        Rectangle casing = new Rectangle(0, 0, 20, 60);
        casing.setFill(Color.BLACK);
        trafficLightGroup.getChildren().add(casing);

        // Red light
        Circle redLight = new Circle(10, 10, 8);
        redLight.setFill(Color.RED); // Default to red
        redLight.setId(id + "Red");
        trafficLightGroup.getChildren().add(redLight);

        // Yellow light
        Circle yellowLight = new Circle(10, 30, 8);
        yellowLight.setFill(Color.GRAY); // Off state
        yellowLight.setId(id + "Yellow");
        trafficLightGroup.getChildren().add(yellowLight);

        // Green light
        Circle greenLight = new Circle(10, 50, 8);
        greenLight.setFill(Color.GRAY); // Off state
        greenLight.setId(id + "Green");
        trafficLightGroup.getChildren().add(greenLight);

        simulationPane.getChildren().add(trafficLightGroup);
    }

    // TODO: Add methods to update traffic light states (e.g., setGreen, setRed)
    // TODO: Add methods to draw and move vehicles
}
