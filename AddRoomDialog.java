package com.raahul.hms.ui;

import com.raahul.hms.model.Room;
import com.raahul.hms.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Modal dialog for adding a new room to the hotel.
 * Features styled form fields with validation.
 */
public class AddRoomDialog {

    private final HotelService hotelService;
    private final Stage dialog;

    public AddRoomDialog(HotelService hotelService) {
        this.hotelService = hotelService;
        this.dialog = new Stage();
        buildDialog();
    }

    private void buildDialog() {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Add New Room");

        VBox container = new VBox(20);
        container.getStyleClass().add("dialog-container");
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setPrefWidth(420);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setSpacing(10);

        Label title = new Label("➕  Add New Room");
        title.getStyleClass().add("dialog-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(title, spacer, closeBtn);

        // Form fields
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER_LEFT);

        // Room Number
        Label roomNoLabel = new Label("Room Number");
        roomNoLabel.getStyleClass().add("form-label");
        TextField roomNoField = new TextField();
        roomNoField.getStyleClass().add("form-field");
        roomNoField.setPromptText("e.g. 101");

        // Room Type
        Label typeLabel = new Label("Room Type");
        typeLabel.getStyleClass().add("form-label");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getStyleClass().add("form-field");
        typeCombo.getItems().addAll("Single", "Double", "Suite", "Deluxe");
        typeCombo.setPromptText("Select room type");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        // Price
        Label priceLabel = new Label("Price per Night (₹)");
        priceLabel.getStyleClass().add("form-label");
        TextField priceField = new TextField();
        priceField.getStyleClass().add("form-field");
        priceField.setPromptText("e.g. 2500.00");

        // Status message
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("form-status");

        // Submit button
        Button submitBtn = new Button("Add Room");
        submitBtn.getStyleClass().addAll("action-btn", "action-btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setPrefHeight(42);

        submitBtn.setOnAction(e -> {
            statusLabel.getStyleClass().removeAll("form-status-success", "form-status-error");

            // Validate
            String roomNoText = roomNoField.getText().trim();
            String type = typeCombo.getValue();
            String priceText = priceField.getText().trim();

            if (roomNoText.isEmpty() || type == null || priceText.isEmpty()) {
                statusLabel.setText("⚠ Please fill in all fields");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            int roomNo;
            double price;
            try {
                roomNo = Integer.parseInt(roomNoText);
            } catch (NumberFormatException ex) {
                statusLabel.setText("⚠ Room number must be a valid integer");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException ex) {
                statusLabel.setText("⚠ Price must be a valid number");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            if (roomNo <= 0) {
                statusLabel.setText("⚠ Room number must be positive");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            if (price <= 0) {
                statusLabel.setText("⚠ Price must be positive");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            // Add room
            Room room = new Room(roomNo, type, price);
            boolean success = hotelService.addRoom(room);

            if (success) {
                statusLabel.setText("✓ Room " + roomNo + " added successfully!");
                statusLabel.getStyleClass().add("form-status-success");
                roomNoField.clear();
                typeCombo.setValue(null);
                priceField.clear();
            } else {
                statusLabel.setText("⚠ Room " + roomNo + " already exists");
                statusLabel.getStyleClass().add("form-status-error");
            }
        });

        form.getChildren().addAll(
                roomNoLabel, roomNoField,
                typeLabel, typeCombo,
                priceLabel, priceField,
                statusLabel,
                submitBtn
        );

        container.getChildren().addAll(titleBar, form);

        Scene scene = new Scene(container);
        String css = getClass().getResource("/com/raahul/hms/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }
}
