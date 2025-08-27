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
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;

    opens com.trafficmanagement.smartflow.controller to javafx.fxml;
    exports com.trafficmanagement.smartflow;
}