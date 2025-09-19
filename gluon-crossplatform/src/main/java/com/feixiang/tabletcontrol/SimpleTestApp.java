package com.feixiang.tabletcontrol;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 简化的测试应用程序
 * 用于验证GluonFX构建是否正常工作
 */
public class SimpleTestApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 创建简单的UI
        Label titleLabel = new Label("平板中控系统 - 测试版");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label platformLabel = new Label("平台: " + System.getProperty("os.name"));
        
        Button testButton = new Button("测试按钮");
        testButton.setOnAction(e -> {
            System.out.println("按钮被点击了！");
            titleLabel.setText("按钮已点击 - " + System.currentTimeMillis());
            platformLabel.setText("Java版本: " + System.getProperty("java.version"));
        });
        
        // 创建布局
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(titleLabel, platformLabel, testButton);
        root.setStyle("-fx-padding: 50px;");
        
        // 创建场景
        Scene scene = new Scene(root, 400, 300);
        
        // 设置舞台
        primaryStage.setTitle("平板中控 - 简化测试版");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("简化测试应用启动成功！");
    }
}
