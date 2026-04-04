package com.raahul.hms.ui;

import com.raahul.hms.model.Customer;
import com.raahul.hms.model.Room;
import com.raahul.hms.service.HotelService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comprehensive rooms view with table, filters, and actions.
 */
public class ViewRoomsView {

    private final HotelService hotelService;
    private final Runnable refreshCallback;
    private final Runnable dashboardCallback;
    private final VBox view;
    private TableView<Room> table;
    private TextField searchField;
    private ComboBox<String> filterCombo;

    public ViewRoomsView(HotelService hotelService, Runnable refreshCallback, Runnable dashboardCallback) {
        this.hotelService = hotelService;
        this.refreshCallback = refreshCallback;
        this.dashboardCallback = dashboardCallback;
        this.view = new VBox(20);
        buildView();
    }

    public VBox getView() {
        return view;
    }

    private void buildView() {
        view.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Room Management");
        heading.getStyleClass().add("view-heading");

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().addAll("action-btn", "action-btn-primary");
        addBtn.setOnAction(e -> {
            AddRoomDialog dialog = new AddRoomDialog(hotelService);
            dialog.show();
            refreshTable(null);
        });

        Button bookBtn = new Button("Book Room");
        bookBtn.getStyleClass().addAll("action-btn", "action-btn-success");
        bookBtn.setOnAction(e -> {
            BookRoomDialog dialog = new BookRoomDialog(hotelService);
            dialog.show();
            refreshTable(null);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Search / filter
        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search rooms...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, val) -> refreshTable(val));

        // Filter combo
        filterCombo = new ComboBox<>();
        filterCombo.getStyleClass().add("filter-combo");
        filterCombo.getItems().addAll("All Rooms", "Available", "Booked");
        filterCombo.setValue("All Rooms");
        filterCombo.setOnAction(e -> refreshTable(searchField.getText()));

        toolbar.getChildren().addAll(addBtn, bookBtn, spacer, searchField, filterCombo);

        // Table
        table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Room, Integer> roomNoCol = new TableColumn<>("Room No.");
        roomNoCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getRoomNo()).asObject());
        roomNoCol.setPrefWidth(100);

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getType()));
        typeCol.setPrefWidth(150);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price (Rs.)");
        priceCol.setCellValueFactory(cd ->
                new SimpleDoubleProperty(cd.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<Room, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("Rs. %.2f", price));
            }
        });

        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isAvailable() ? "Available" : "Booked"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Room, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add(
                            "Available".equals(status) ? "status-badge-available" : "status-badge-booked"
                    );
                    badge.setPadding(new Insets(3, 12, 3, 12));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<Room, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(cd -> {
            Room room = cd.getValue();
            if (!room.isAvailable()) {
                Customer c = hotelService.getCustomerByRoom(room.getRoomNo());
                if (c != null) {
                    return new SimpleStringProperty(c.getName());
                }
            }
            return new SimpleStringProperty("—");
        });
        guestCol.setPrefWidth(150);

        TableColumn<Room, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<Room, Void>() {
            private final Button cancelBtn = new Button("Cancel Booking");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, cancelBtn, deleteBtn);

            {
                cancelBtn.getStyleClass().addAll("table-action-btn", "table-action-warning");
                deleteBtn.getStyleClass().addAll("table-action-btn", "table-action-danger");
                box.setAlignment(Pos.CENTER);

                cancelBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cancel Booking");
                    confirm.setHeaderText("Cancel booking for Room " + room.getRoomNo() + "?");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            hotelService.cancelBooking(room.getRoomNo());
                            refreshTable(searchField.getText());
                        }
                    });
                });

                deleteBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    if (!room.isAvailable()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Cannot Delete");
                        alert.setHeaderText("Room " + room.getRoomNo() + " is currently booked.");
                        alert.setContentText("Cancel the booking first before deleting.");
                        alert.showAndWait();
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Room");
                    confirm.setHeaderText("Delete Room " + room.getRoomNo() + "?");
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            hotelService.removeRoom(room.getRoomNo());
                            refreshTable(searchField.getText());
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Room room = getTableView().getItems().get(getIndex());
                    cancelBtn.setVisible(!room.isAvailable());
                    cancelBtn.setManaged(!room.isAvailable());
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(roomNoCol, typeCol, priceCol, statusCol, guestCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        refreshTable(null);

        view.getChildren().addAll(heading, toolbar, table);
    }

    private void refreshTable(String searchText) {
        table.getItems().clear();
        
        List<Room> rooms;
        String filterValue = filterCombo != null ? filterCombo.getValue() : "All Rooms";
        
        if ("Available".equals(filterValue)) {
            rooms = hotelService.getAvailableRooms();
        } else if ("Booked".equals(filterValue)) {
            rooms = hotelService.getBookedRooms();
        } else {
            rooms = hotelService.getAllRooms();
        }

        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase().trim();
            rooms = rooms.stream().filter(r ->
                    String.valueOf(r.getRoomNo()).contains(lower) ||
                            r.getType().toLowerCase().contains(lower)
            ).collect(Collectors.toList());
        }

        table.getItems().addAll(rooms);

        if (rooms.isEmpty()) {
            table.setPlaceholder(new Label("No rooms found. Add a room to get started!"));
        }
    }
}
