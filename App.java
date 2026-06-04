package com.raahul.hms;

import com.raahul.hms.service.HotelService;
import com.raahul.hms.ui.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the Hotel Management System application.
 */
public class App extends Application {

    private static final double WINDOW_WIDTH = 1100;
    private static final double WINDOW_HEIGHT = 700;

    @Override
    public void start(Stage stage) {
        HotelService hotelService = new HotelService();
        MainView mainView = new MainView(hotelService);

        Scene scene = new Scene(mainView.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);

        String css = getClass().getResource("/com/raahul/hms/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Hotel Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
