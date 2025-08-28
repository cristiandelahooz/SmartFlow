package com.trafficmanagement.smartflow.utils;

import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class CompassUtils {
    
    private static final double COMPASS_SIZE = 60.0;
    private static final double COMPASS_RADIUS = COMPASS_SIZE / 2;
    private static final Color GOLD_DARK = Color.web("#92400E");
    private static final Color GOLD_MEDIUM = Color.web("#F59E0B");
    private static final Color GOLD_LIGHT = Color.web("#FCD34D");
    
    public static Group createCompass() {
        Group compass = new Group();
        
        Circle outerCircle = new Circle(COMPASS_RADIUS);
        outerCircle.setFill(createGoldGradient());
        outerCircle.setStroke(GOLD_DARK);
        outerCircle.setStrokeWidth(2.0);
        outerCircle.setStrokeType(StrokeType.OUTSIDE);
        
        Circle innerCircle = new Circle(COMPASS_RADIUS - 8);
        innerCircle.setFill(Color.TRANSPARENT);
        innerCircle.setStroke(GOLD_MEDIUM);
        innerCircle.setStrokeWidth(1.0);
        
        Text northText = createCardinalText("N", 0, -18);
        Text southText = createCardinalText("S", 0, 22);
        Text eastText = createCardinalText("E", 18, 6);
        Text westText = createCardinalText("W", -18, 6);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(4.0);
        shadow.setOffsetX(2.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.GRAY);
        
        compass.getChildren().addAll(outerCircle, innerCircle, northText, southText, eastText, westText);
        compass.setEffect(shadow);
        
        return compass;
    }
    
    private static LinearGradient createGoldGradient() {
        return LinearGradient.valueOf("linear-gradient(from 0% 0% to 100% 100%, " +
                "#FCD34D 0%, #F59E0B 50%, #92400E 100%)");
    }
    
    private static Text createCardinalText(String cardinal, double x, double y) {
        Text text = new Text(cardinal);
        text.setFont(Font.font("System", FontWeight.BOLD, 14));
        text.setFill(GOLD_DARK);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setX(x - text.getBoundsInLocal().getWidth() / 2);
        text.setY(y);
        
        DropShadow textShadow = new DropShadow();
        textShadow.setRadius(2.0);
        textShadow.setOffsetX(1.0);
        textShadow.setOffsetY(1.0);
        textShadow.setColor(Color.WHITE);
        text.setEffect(textShadow);
        
        return text;
    }
    
    public static void positionCompass(Group compass, double paneWidth, double paneHeight) {
        double margin = 20.0;
        compass.setLayoutX(paneWidth - COMPASS_SIZE - margin);
        compass.setLayoutY(margin + COMPASS_RADIUS);
    }
}