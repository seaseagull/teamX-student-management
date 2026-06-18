module com.teach.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires javafx.web;
    requires java.net.http;
    requires com.google.gson;
    requires org.json;

    opens com.teach.javafx to javafx.fxml;
    opens com.teach.javafx.controller to javafx.fxml;
    opens com.teach.javafx.controller.base to javafx.fxml;
    opens com.teach.javafx.request to javafx.fxml, com.google.gson;
    opens com.teach.javafx.models to javafx.fxml;

    exports com.teach.javafx;
    exports com.teach.javafx.controller;
    exports com.teach.javafx.controller.base;
    exports com.teach.javafx.request;
    exports com.teach.javafx.models;
}
