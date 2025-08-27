package com.trafficmanagement.smartflow;

import com.trafficmanagement.smartflow.utils.ViewsHandler;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("SmartFlow Traffic Simulation");
        stage.setScene(ViewsHandler.getScene());

        stage.setMaximized(true);
        stage.setResizable(false);

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }
}