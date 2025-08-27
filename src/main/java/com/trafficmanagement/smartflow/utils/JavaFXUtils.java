package com.trafficmanagement.smartflow.utils;

import com.trafficmanagement.smartflow.data.enums.VehicleType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static com.trafficmanagement.smartflow.utils.IntersectionConstants.*;
import static com.trafficmanagement.smartflow.utils.MotorwayConstants.*;

public class JavaFXUtils {

    public static Circle createVehicleCircle(VehicleType type, double radius) {
        Color vehicleColor = (type == VehicleType.EMERGENCY) ? 
            Color.web(MotorwayConstants.EMERGENCY_VEHICLE_COLOR) : Color.web(MotorwayConstants.NORMAL_VEHICLE_COLOR);
        Circle circle = new Circle(radius, vehicleColor);
        circle.setStroke(MotorwayConstants.VEHICLE_STROKE_COLOR);
        return circle;
    }

    public static Group createStopSign(double x, double y, double angle) {
        double scale = STOP_SIGN_SCALE;
        
        Polygon octagon = new Polygon(
            STOP_SIGN_OCTAGON_SIZE * scale, 0,
            STOP_SIGN_OCTAGON_OFFSET * scale, 0,
            STOP_SIGN_OCTAGON_CORNER * scale, STOP_SIGN_OCTAGON_SIZE * scale,
            STOP_SIGN_OCTAGON_CORNER * scale, STOP_SIGN_OCTAGON_OFFSET * scale,
            STOP_SIGN_OCTAGON_OFFSET * scale, STOP_SIGN_OCTAGON_CORNER * scale,
            STOP_SIGN_OCTAGON_SIZE * scale, STOP_SIGN_OCTAGON_CORNER * scale,
            0, STOP_SIGN_OCTAGON_OFFSET * scale,
            0, STOP_SIGN_OCTAGON_SIZE * scale
        );
        octagon.setFill(STOP_SIGN_COLOR);
        octagon.setStroke(STOP_SIGN_STROKE_COLOR);
        octagon.setStrokeWidth(STOP_SIGN_STROKE_WIDTH);

        Text text = new Text(STOP_SIGN_TEXT);
        text.setFont(Font.font(STOP_SIGN_FONT_FAMILY, STOP_SIGN_TEXT_SIZE * scale));
        text.setFill(STOP_SIGN_TEXT_COLOR);
        text.setX(STOP_SIGN_TEXT_X_OFFSET * scale);
        text.setY(STOP_SIGN_TEXT_Y_OFFSET * scale);

        Rectangle pole = new Rectangle();
        pole.setX(STOP_SIGN_POLE_X_OFFSET * scale);
        pole.setY(STOP_SIGN_POLE_Y_OFFSET * scale);
        pole.setWidth(STOP_SIGN_POLE_WIDTH * scale);
        pole.setHeight(STOP_SIGN_POLE_HEIGHT * scale);
        pole.setFill(STOP_SIGN_POLE_COLOR);

        Group sign = new Group(octagon, text, pole);
        sign.relocate(x - STOP_SIGN_CENTER_OFFSET * scale, y - STOP_SIGN_CENTER_OFFSET * scale);
        sign.getTransforms().add(new Rotate(angle, STOP_SIGN_CENTER_OFFSET * scale, STOP_SIGN_CENTER_OFFSET * scale));
        
        return sign;
    }

    public static Group createTrafficLight(int id, double x, double y) {
        Rectangle post = new Rectangle(
            x, 
            y - TRAFFIC_LIGHT_Y_OFFSET, 
            TRAFFIC_LIGHT_POST_WIDTH, 
            TRAFFIC_LIGHT_POST_HEIGHT
        );
        post.setFill(TRAFFIC_LIGHT_POST_COLOR);
        post.setArcWidth(TRAFFIC_LIGHT_POST_ARC_SIZE);
        post.setArcHeight(TRAFFIC_LIGHT_POST_ARC_SIZE);

        Circle redLight = new Circle(
            x + TRAFFIC_LIGHT_X_CENTER_OFFSET, 
            y + TRAFFIC_LIGHT_Y_UPPER_OFFSET, 
            TRAFFIC_LIGHT_RADIUS, 
            TRAFFIC_LIGHT_RED_OFF
        );
        redLight.setId("red-" + id);

        Circle greenLight = new Circle(
            x + TRAFFIC_LIGHT_X_CENTER_OFFSET, 
            y + TRAFFIC_LIGHT_Y_LOWER_OFFSET, 
            TRAFFIC_LIGHT_RADIUS, 
            TRAFFIC_LIGHT_GREEN_OFF
        );
        greenLight.setId("green-" + id);

        return new Group(post, redLight, greenLight);
    }

    public static void updateTrafficLightColor(Node lightGroup, int lightId, boolean isGreen) {
        if (!(lightGroup instanceof Group group)) {
            return;
        }

        Circle redLight = (Circle) group.lookup("#red-" + lightId);
        Circle greenLight = (Circle) group.lookup("#green-" + lightId);

        if (redLight != null && greenLight != null) {
            if (isGreen) {
                redLight.setFill(TRAFFIC_LIGHT_RED_OFF);
                greenLight.setFill(TRAFFIC_LIGHT_GREEN_ON);
            } else {
                redLight.setFill(TRAFFIC_LIGHT_RED_ON);
                greenLight.setFill(TRAFFIC_LIGHT_GREEN_OFF);
            }
        }
    }

    public static void enableButtonsAfterDelay(Duration delay, Button... buttons) {
        for (Button button : buttons) {
            button.setDisable(true);
        }
        
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(delay);
        pause.setOnFinished(event -> {
            for (Button button : buttons) {
                button.setDisable(false);
            }
        });
        pause.play();
    }

    public static Rectangle createStreetRectangle(double x, double y, double width, double height) {
        Rectangle street = new Rectangle(x, y, width, height);
        street.setFill(STREET_COLOR);
        street.setStroke(STREET_STROKE_COLOR);
        return street;
    }

    public static Rectangle createMotorwayBackground(double x, double y, double width, double height) {
        Rectangle motorway = new Rectangle(x, y, width, height);
        motorway.setFill(MOTORWAY_COLOR);
        return motorway;
    }

    public static Rectangle createIntersectionArea(double centerX, double width, double height) {
        return new Rectangle(
            centerX - INTERSECTION_WIDTH / INTERSECTION_WIDTH_DIVISOR,
            0,
            INTERSECTION_WIDTH,
            height
        );
    }

    private JavaFXUtils() {
    }
}