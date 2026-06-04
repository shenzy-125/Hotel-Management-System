package com.raahul.hms.controller;

import java.util.List;

import com.raahul.hms.model.Customer;
import com.raahul.hms.service.HotelService;
import com.raahul.hms.ui.AddRoomDialog;
import com.raahul.hms.ui.BookRoomDialog;
import com.raahul.hms.ui.CheckoutDialog;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardController {

    @FXML private HBox statsRow;
    @FXML private VBox recentList;

    private HotelService hotelService;
    private Runnable onNavigateDashboard;

    public void init(HotelService hotelService, Runnable onNavigateDashboard) {
        this.hotelService = hotelService;
        this.onNavigateDashboard = onNavigateDashboard;
        refreshDashboard();
    }

    @FXML
    public void initialize() {
    }

    public void refreshDashboard() {
        if (hotelService == null) return;

        statsRow.getChildren().clear();
        statsRow.getChildren().addAll(
                createStatCard("Total Rooms", String.valueOf(hotelService.getTotalRooms()), "stat-card-total"),
                createStatCard("Available", String.valueOf(hotelService.getAvailableCount()), "stat-card-available"),
                createStatCard("Booked", String.valueOf(hotelService.getBookedCount()), "stat-card-booked"),
                createStatCard("Guests", String.valueOf(hotelService.getAllCustomers().size()), "stat-card-guests"),
                createStatCard("Revenue", "₹" + String.format("%.0f", hotelService.getTotalRevenue()), "stat-card-revenue")
        );

        recentList.getChildren().clear();
        List<Customer> customers = hotelService.getAllCustomers();

        if (customers.isEmpty()) {
            Label emptyMsg = new Label("No bookings yet. Start by adding rooms and booking them!");
            emptyMsg.getStyleClass().add("empty-message");
            recentList.getChildren().add(emptyMsg);
        } else {
            int limit = Math.min(customers.size(), 5);
            for (int i = customers.size() - 1; i >= customers.size() - limit; i--) {
                Customer c = customers.get(i);
                HBox row = new HBox(15);
                row.getStyleClass().add("recent-row");
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 15, 10, 15));

                Label nameLabel = new Label("👤 " + c.getName());
                nameLabel.getStyleClass().add("recent-name");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label roomLabel = new Label("Room " + c.getRoomNo());
                roomLabel.getStyleClass().add("recent-room");

                Label contactLabel = new Label("📞 " + c.getContact());
                contactLabel.getStyleClass().add("recent-contact");

                row.getChildren().addAll(nameLabel, spacer, roomLabel, contactLabel);
                recentList.getChildren().add(row);
            }
        }
    }

    private VBox createStatCard(String title, String value, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.setPrefWidth(170);
        card.setPrefHeight(110);
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    @FXML
    private void handleAddRoom() {
        AddRoomDialog dialog = new AddRoomDialog(hotelService);
        dialog.show();
        refreshDashboard();
    }

    @FXML
    private void handleBookRoom() {
        BookRoomDialog dialog = new BookRoomDialog(hotelService);
        dialog.show();
        refreshDashboard();
    }

    @FXML
    private void handleCheckout() {
        CheckoutDialog dialog = new CheckoutDialog(hotelService);
        dialog.show();
        refreshDashboard();
    }

    @FXML
    private void handleRefresh() {
        refreshDashboard();
    }
}
