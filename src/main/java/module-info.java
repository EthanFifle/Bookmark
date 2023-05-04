module com.home{
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires json.simple;
    requires java.sql;
    requires java.desktop;
    requires org.testng;
    requires org.junit.jupiter.api;
    requires org.mariadb.jdbc;

    exports home.model;
    exports home.view;
    exports home.controller;
    opens home.controller to javafx.fxml;
}