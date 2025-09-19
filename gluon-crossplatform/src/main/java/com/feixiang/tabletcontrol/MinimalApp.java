package com.feixiang.tabletcontrol;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 最小化的GluonFX测试应用
 * 只包含最基本的JavaFX功能，用于测试GluonFX构建
 */
public class MinimalApp extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        
        Label label = new Label("Hello GluonFX!\nJava: " + javaVersion + "\nJavaFX: " + javafxVersion);
        StackPane root = new StackPane();
        root.getChildren().add(label);

        Scene scene = new Scene(root, 300, 200);

        stage.setTitle("Minimal GluonFX App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
