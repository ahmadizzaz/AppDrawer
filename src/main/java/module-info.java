module com.izzaz.appdrawer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.sun.jna.platform;
    requires com.sun.jna;
    requires java.desktop;
    requires org.apache.commons.io;
    requires lombok;
    requires jdk.jsobject;

    opens com.izzaz.appdrawer to javafx.fxml;
    exports com.izzaz.appdrawer;
}