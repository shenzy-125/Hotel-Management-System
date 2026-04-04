package com.raahul.hms.ui;

import com.raahul.hms.model.Bill;
import com.raahul.hms.model.Room;
import com.raahul.hms.model.Customer;
import com.raahul.hms.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.util.List;

/**
 * Modal dialog for guest checkout and bill generation.
 * Allows selecting a booked room, entering dates, and generates an invoice.
 */
public class CheckoutDialog {

    private final HotelService hotelService;
    private final Stage dialog;
    private Bill generatedBill = null;

    public CheckoutDialog(HotelService hotelService) {
        this.hotelService = hotelService;
        this.dialog = new Stage();
        buildDialog();
    }

    private void buildDialog() {
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Checkout & Generate Bill");

        VBox container = new VBox(20);
        container.getStyleClass().add("dialog-container");
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);
        container.setPrefWidth(520);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setSpacing(10);

        Label title = new Label("💰  Checkout & Generate Bill");
        title.getStyleClass().add("dialog-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> dialog.close());

        titleBar.getChildren().addAll(title, spacer, closeBtn);

        // Booked rooms list
        Label roomsLabel = new Label("Select Booked Room");
        roomsLabel.getStyleClass().add("form-label");

        ListView<Room> roomListView = new ListView<>();
        roomListView.getStyleClass().add("room-list");
        roomListView.setPrefHeight(140);

        List<Room> bookedRooms = hotelService.getBookedRooms();
        roomListView.getItems().addAll(bookedRooms);

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

                    // Show guest name
                    Customer guest = hotelService.getCustomerByRoom(room.getRoomNo());
                    Label guestLabel = new Label(guest != null ? "👤 " + guest.getName() : "");
                    guestLabel.getStyleClass().add("room-cell-price");

                    cell.getChildren().addAll(roomNo, type, sp, guestLabel);
                    setGraphic(cell);
                    setText(null);
                }
            }
        });

        if (bookedRooms.isEmpty()) {
            roomListView.setPlaceholder(new Label("No booked rooms to checkout"));
        }

        // Date fields
        Label datesLabel = new Label("Stay Dates");
        datesLabel.getStyleClass().add("form-label");

        HBox datesRow = new HBox(15);
        datesRow.setAlignment(Pos.CENTER_LEFT);

        VBox checkInBox = new VBox(5);
        Label checkInLabel = new Label("Check-in Date");
        checkInLabel.getStyleClass().add("form-label");
        checkInLabel.setStyle("-fx-font-size: 11px;");
        DatePicker checkInPicker = new DatePicker(LocalDate.now().minusDays(1));
        checkInPicker.getStyleClass().add("form-field");
        checkInPicker.setPrefWidth(200);
        checkInBox.getChildren().addAll(checkInLabel, checkInPicker);

        VBox checkOutBox = new VBox(5);
        Label checkOutLabel = new Label("Check-out Date");
        checkOutLabel.getStyleClass().add("form-label");
        checkOutLabel.setStyle("-fx-font-size: 11px;");
        DatePicker checkOutPicker = new DatePicker(LocalDate.now());
        checkOutPicker.getStyleClass().add("form-field");
        checkOutPicker.setPrefWidth(200);
        checkOutBox.getChildren().addAll(checkOutLabel, checkOutPicker);

        HBox.setHgrow(checkInBox, Priority.ALWAYS);
        HBox.setHgrow(checkOutBox, Priority.ALWAYS);
        datesRow.getChildren().addAll(checkInBox, checkOutBox);

        // Status
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("form-status");

        // Checkout button
        Button checkoutBtn = new Button("💰  Generate Bill & Checkout");
        checkoutBtn.getStyleClass().addAll("action-btn", "action-btn-success");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setPrefHeight(42);

        checkoutBtn.setOnAction(e -> {
            statusLabel.getStyleClass().removeAll("form-status-success", "form-status-error");

            Room selectedRoom = roomListView.getSelectionModel().getSelectedItem();
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if (selectedRoom == null) {
                statusLabel.setText("⚠ Please select a booked room");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            if (checkIn == null || checkOut == null) {
                statusLabel.setText("⚠ Please select both check-in and check-out dates");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            if (!checkOut.isAfter(checkIn)) {
                statusLabel.setText("⚠ Check-out date must be after check-in date");
                statusLabel.getStyleClass().add("form-status-error");
                return;
            }

            // Perform checkout
            Bill bill = hotelService.checkout(selectedRoom.getRoomNo(), checkIn, checkOut);

            if (bill != null) {
                generatedBill = bill;
                statusLabel.setText("✓ Bill #" + bill.getBillId() + " generated! Total: ₹"
                        + String.format("%.2f", bill.getTotalAmount()));
                statusLabel.getStyleClass().add("form-status-success");

                // Refresh list
                roomListView.getItems().clear();
                roomListView.getItems().addAll(hotelService.getBookedRooms());

                // Show invoice popup
                showInvoice(bill);
            } else {
                statusLabel.setText("⚠ Checkout failed");
                statusLabel.getStyleClass().add("form-status-error");
            }
        });

        container.getChildren().addAll(
                titleBar,
                roomsLabel, roomListView,
                datesLabel, datesRow,
                statusLabel,
                checkoutBtn
        );

        Scene scene = new Scene(container);
        String css = getClass().getResource("/com/raahul/hms/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        dialog.setScene(scene);
    }

    /**
     * Displays a formatted invoice for the generated bill.
     */
    private void showInvoice(Bill bill) {
        Stage invoiceStage = new Stage();
        invoiceStage.initModality(Modality.APPLICATION_MODAL);
        invoiceStage.initStyle(StageStyle.UNDECORATED);
        invoiceStage.setTitle("Invoice #" + bill.getBillId());

        VBox invoice = new VBox(12);
        invoice.getStyleClass().add("dialog-container");
        invoice.getStyleClass().add("invoice-container");
        invoice.setPadding(new Insets(30));
        invoice.setPrefWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label invoiceTitle = new Label("Invoice #" + bill.getBillId());
        invoiceTitle.getStyleClass().add("dialog-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button closeInv = new Button("✕");
        closeInv.getStyleClass().add("dialog-close-btn");
        closeInv.setOnAction(e -> invoiceStage.close());

        header.getChildren().addAll(invoiceTitle, sp, closeInv);

        // Separator
        Separator sep1 = new Separator();
        sep1.getStyleClass().add("invoice-separator");

        // Guest Info
        Label guestHeader = new Label("GUEST DETAILS");
        guestHeader.getStyleClass().add("invoice-section-header");

        HBox guestRow = createInvoiceRow("Guest Name", bill.getCustomerName());
        HBox contactRow = createInvoiceRow("Contact", bill.getContact());

        // Room Info
        Separator sep2 = new Separator();
        sep2.getStyleClass().add("invoice-separator");

        Label roomHeader = new Label("ROOM DETAILS");
        roomHeader.getStyleClass().add("invoice-section-header");

        HBox roomRow = createInvoiceRow("Room Number", String.valueOf(bill.getRoomNo()));
        HBox typeRow = createInvoiceRow("Room Type", bill.getRoomType());
        HBox rateRow = createInvoiceRow("Rate / Night", "₹ " + String.format("%.2f", bill.getPricePerNight()));

        // Stay Info
        Separator sep3 = new Separator();
        sep3.getStyleClass().add("invoice-separator");

        Label stayHeader = new Label("STAY DETAILS");
        stayHeader.getStyleClass().add("invoice-section-header");

        HBox checkInRow = createInvoiceRow("Check-in", bill.getCheckInDate());
        HBox checkOutRow = createInvoiceRow("Check-out", bill.getCheckOutDate());
        HBox nightsRow = createInvoiceRow("Nights", String.valueOf(bill.getNights()));

        // Charges
        Separator sep4 = new Separator();
        sep4.getStyleClass().add("invoice-separator");

        Label chargesHeader = new Label("CHARGES");
        chargesHeader.getStyleClass().add("invoice-section-header");

        HBox roomChargeRow = createInvoiceRow("Room Charges",
                "₹ " + String.format("%.2f", bill.getRoomCharges()));
        HBox taxRow = createInvoiceRow("GST (18%)",
                "₹ " + String.format("%.2f", bill.getTaxAmount()));

        Separator sep5 = new Separator();
        sep5.getStyleClass().add("invoice-separator");

        // Total
        HBox totalRow = createInvoiceRow("TOTAL AMOUNT",
                "₹ " + String.format("%.2f", bill.getTotalAmount()));
        totalRow.getStyleClass().add("invoice-total-row");

        // Close button
        Button doneBtn = new Button("Done");
        doneBtn.getStyleClass().addAll("action-btn", "action-btn-primary");
        doneBtn.setMaxWidth(Double.MAX_VALUE);
        doneBtn.setPrefHeight(38);
        doneBtn.setOnAction(e -> invoiceStage.close());

        invoice.getChildren().addAll(
                header, sep1,
                guestHeader, guestRow, contactRow,
                sep2, roomHeader, roomRow, typeRow, rateRow,
                sep3, stayHeader, checkInRow, checkOutRow, nightsRow,
                sep4, chargesHeader, roomChargeRow, taxRow,
                sep5, totalRow,
                doneBtn
        );

        ScrollPane scrollPane = new ScrollPane(invoice);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-scroll");
        scrollPane.setPrefHeight(580);

        Scene scene = new Scene(scrollPane);
        String css = getClass().getResource("/com/raahul/hms/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        invoiceStage.setScene(scene);
        invoiceStage.showAndWait();
    }

    private HBox createInvoiceRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("invoice-row");

        Label l = new Label(label);
        l.getStyleClass().add("invoice-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label v = new Label(value);
        v.getStyleClass().add("invoice-value");

        row.getChildren().addAll(l, spacer, v);
        return row;
    }

    public void show() {
        dialog.showAndWait();
    }

    public Bill getGeneratedBill() {
        return generatedBill;
    }
}
