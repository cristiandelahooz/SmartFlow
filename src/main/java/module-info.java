module com.trafficmanagement.smartflow {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.slf4j;
    requires static lombok;
    requires javafx.base;
    requires javafx.graphics;

    opens com.trafficmanagement.smartflow.controller to javafx.fxml;
    exports com.trafficmanagement.smartflow;
}