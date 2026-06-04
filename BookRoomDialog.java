package com.raahul.hms.ui;

import com.raahul.hms.model.Customer;
import com.raahul.hms.model.Room;
import com.raahul.hms.service.HotelService;
import com.raahul.hms.thread.BookingThread;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

/**
 * Modal dialog for booking a room.
 * Shows available rooms and collects customer details.
 * Uses BookingThread for concurrent booking.
 */
public class BookRoomDialog {

    private final HotelService hotelService;
    private final Stage dialog;

    public BookRoomDialog(HotelService hotelService) {
        this.hotelService = hotelService;
        this.dialog = new Stage();
        buildDialog();
    }

    private void buildDialog() {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Book a Room");

        VBox container = new VBox(20);
        container.getStyleClass().add("dialog-container");
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setPrefWidth(480);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setSpacing(10);

        Label title = new Label("📝  Book a Room");
        title.getStyleClass().add("dialog-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(title, spacer, closeBtn);

        // Available rooms section
        Label roomsLabel = new Label("Available Rooms");
        roomsLabel.getStyleClass().add("form-label");

        ListView<Room> roomListView = new ListView<>();
        roomListView.getStyleClass().add("room-list");
        roomListView.setPrefHeight(150);

        List<Room> availableRooms = hotelService.getAvailableRooms();
        roomListView.getItems().addAll(availableRooms);

        roomListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                if (empty || room == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cell = new HBox(15);
                    cell.setAlignment(Pos.CENTER_LEFT);
                    cell.setPadding(new Insets(5));

                    Label roomNo = new Label("🔑 Room " + room.getRoomNo());
                    roomNo.getStyleClass().add("room-cell-no");

                    Label type = new Label(room.getType());
                    type.getStyleClass().add("room-cell-type");

                    Region sp = new Region();
                    HBox.setHgrow(sp, Priority.ALWAYS);

                    Label price = new Label("₹" + String.format("%.0f", room.getPrice()));
                    price.getStyleClass().add("room-cell-price");

                    cell.getChildren().addAll(roomNo, type, sp, price);
                    setGraphic(cell);
                    setText(null);
                }
            }
        });

        if (availableRooms.isEmpty()) {
            roomListView.setPlaceholder(new Label("No rooms available. Add rooms first!"));
        }

        // Customer details section
        Label custLabel = new Label("Customer Details");
        custLabel.getStyleClass().add("form-label");

        HBox idNameRow = new HBox(10);
        TextField idField = new TextField();
        idField.getStyleClass().add("form-field");
        idField.setPromptText("Customer ID");
        idField.setPrefWidth(120);

        TextField nameField = new TextField();
        nameField.getStyleClass().add("form-field");
        nameField.setPromptText("Full Name");
        HBox.setHgrow(nameField, Priority.ALWAYS);

        idNameRow.getChildren().addAll(idField, nameField);

        TextField contactField = new TextField();
        contactField.getStyleClass().add("form-field");
        contactField.setPromptText("Contact Number");

        // Status
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("form-status");

        // Progress indicator
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);
        progress.setPrefSize(30, 30);

        // Book button
        Button bookBtn = new Button("Book Room");
        bookBtn.getStyleClass().addAll("action-btn", "action-btn-success");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setPrefHeight(42);

        bookBtn.setOnAction(e -> {
            statusLabel.getStyleClass().removeAll("form-status-success", "form-status-error");

            Room selectedRoom = roomListView.getSelectionModel().getSelectedItem();
            String idText = idField.getText().trim();
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();

            if (selectedRoom == null) {
                statusLabel.setText("⚠ Please select a room");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            if (idText.isEmpty() || name.isEmpty() || contact.isEmpty()) {
                statusLabel.setText("⚠ Please fill in all customer details");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            int custId;
            try {
                custId = Integer.parseInt(idText);
            } catch (NumberFormatException ex) {
                statusLabel.setText("⚠ Customer ID must be a number");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            // Use BookingThread for concurrent booking
            Customer customer = new Customer(custId, name, contact);
            progress.setVisible(true);
            bookBtn.setDisable(true);

            BookingThread bookingThread = new BookingThread(
                    hotelService, customer, selectedRoom.getRoomNo(),
                    success -> Platform.runLater(() -> {
                        progress.setVisible(false);
                        bookBtn.setDisable(false);
                        statusLabel.getStyleClass().removeAll("form-status-success", "form-status-error");

                        if (success) {
                            statusLabel.setText("✓ Room " + selectedRoom.getRoomNo()
                                    + " booked for " + name + "!");
                            statusLabel.getStyleClass().add("form-status-success");

                            // Refresh available rooms
                            roomListView.getItems().clear();
                            roomListView.getItems().addAll(hotelService.getAvailableRooms());

                            // Clear fields
                            idField.clear();
                            nameField.clear();
                            contactField.clear();
                        } else {
                            statusLabel.setText("⚠ Booking failed. Room may already be booked.");
                            statusLabel.getStyleClass().add("form-status-error");
                        }
                    })
            );
            bookingThread.start();
        });

        HBox bookRow = new HBox(10);
        bookRow.setAlignment(Pos.CENTER);
        bookRow.getChildren().addAll(bookBtn, progress);
        HBox.setHgrow(bookBtn, Priority.ALWAYS);

        container.getChildren().addAll(
                titleBar,
                roomsLabel, roomListView,
                custLabel, idNameRow, contactField,
                statusLabel,
                bookRow
        );

        Scene scene = new Scene(container);
        String css = getClass().getResource("/com/raahul/hms/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }
}
