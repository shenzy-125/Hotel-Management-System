package com.raahul.hms.ui;

import com.raahul.hms.model.Bill;
import com.raahul.hms.model.Customer;
import com.raahul.hms.model.Room;
import com.raahul.hms.service.HotelService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.beans.property.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main application view with sidebar navigation and content area.
 * Features: Dashboard, Rooms table, Bookings management, Billing.
 */
public class MainView {

    private final HotelService hotelService;
    private final BorderPane root;
    private final StackPane contentArea;

    // Sidebar buttons
    private Button dashboardBtn;
    private Button roomsBtn;
    private Button bookingsBtn;
    private Button billingBtn;

    public MainView(HotelService hotelService) {
        this.hotelService = hotelService;
        this.root = new BorderPane();
        this.contentArea = new StackPane();

        root.getStyleClass().add("root-pane");
        buildUI();
        showDashboard(); // Show dashboard by default
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildUI() {
        // Header
        HBox header = buildHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Content area
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(30));
        root.setCenter(contentArea);
    }

    // ═══════════════════════════════════════════
    // HEADER
    // ═══════════════════════════════════════════

    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Label title = new Label("Hotel Management System");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label("● Online");
        status.getStyleClass().add("header-status");

        Label profile = new Label("Admin");
        profile.getStyleClass().add("header-profile");

        header.getChildren().addAll(title, spacer, status, profile);
        return header;
    }

    // ═══════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setSpacing(8);
        sidebar.setPrefWidth(220);

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("sidebar-section-label");

        dashboardBtn = createSidebarButton("Dashboard");
        roomsBtn = createSidebarButton("Rooms");
        bookingsBtn = createSidebarButton("Bookings");
        billingBtn = createSidebarButton("Billing");

        dashboardBtn.setOnAction(e -> showDashboard());
        roomsBtn.setOnAction(e -> showRooms());
        bookingsBtn.setOnAction(e -> showBookings());
        billingBtn.setOnAction(e -> showBilling());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label version = new Label("v1.0.0");
        version.getStyleClass().add("sidebar-version");

        sidebar.getChildren().addAll(navLabel, dashboardBtn, roomsBtn, bookingsBtn, billingBtn, spacer, version);
        return sidebar;
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        return btn;
    }

    private void setActiveButton(Button active) {
        dashboardBtn.getStyleClass().remove("sidebar-btn-active");
        roomsBtn.getStyleClass().remove("sidebar-btn-active");
        bookingsBtn.getStyleClass().remove("sidebar-btn-active");
        billingBtn.getStyleClass().remove("sidebar-btn-active");
        active.getStyleClass().add("sidebar-btn-active");
    }

    private void showNotImplemented(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(null);
        alert.setContentText(feature + " module will be implemented in a future update.");
        alert.show();
    }

    // ═══════════════════════════════════════════
    // DASHBOARD VIEW
    // ═══════════════════════════════════════════

    private void showDashboard() {
        setActiveButton(dashboardBtn);
        contentArea.getChildren().clear();

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("content-scroll");

        VBox dashboard = new VBox(25);
        dashboard.setAlignment(Pos.TOP_LEFT);
        dashboard.setPadding(new Insets(5));

        // ── Page Heading ──
        Label heading = new Label("Dashboard Overview");
        heading.getStyleClass().add("view-heading");

        // ── Statistics Cards Grid ──
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        statsGrid.add(createStatCard("Total Rooms", String.valueOf(hotelService.getTotalRooms()), "stat-card-total"), 0, 0);
        statsGrid.add(createStatCard("Available", String.valueOf(hotelService.getAvailableCount()), "stat-card-available"), 1, 0);
        statsGrid.add(createStatCard("Booked", String.valueOf(hotelService.getBookedCount()), "stat-card-booked"), 2, 0);
        statsGrid.add(createStatCard("Revenue", "₹" + String.format("%.0f", hotelService.getTotalRevenue()), "stat-card-revenue"), 3, 0);

        // Occupancy section removed as requested

        // ── Recent Guests Section (Table) ──
        Label recentLabel = new Label("Recent Guests");
        recentLabel.getStyleClass().add("section-label");

        TableView<Customer> guestsTable = new TableView<>();
        guestsTable.getStyleClass().add("data-table");
        guestsTable.setPrefHeight(200);
        guestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getName()));

        TableColumn<Customer, Integer> roomCol = new TableColumn<>("Room No.");
        roomCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getRoomNo()).asObject());

        TableColumn<Customer, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContact()));

        guestsTable.getColumns().addAll(nameCol, roomCol, contactCol);

        List<Customer> customers = hotelService.getAllCustomers();
        if (customers.isEmpty()) {
            guestsTable.setPlaceholder(new Label("No recent guests found."));
        } else {
            int limit = Math.min(customers.size(), 5);
            for (int i = customers.size() - 1; i >= customers.size() - limit; i--) {
                guestsTable.getItems().add(customers.get(i));
            }
        }

        dashboard.getChildren().addAll(heading, statsGrid, recentLabel, guestsTable);

        scroll.setContent(dashboard);
        contentArea.getChildren().add(scroll);
    }

    // ═══════════════════════════════════════════
    // ROOMS VIEW
    // ═══════════════════════════════════════════

    private void showRooms() {
        setActiveButton(roomsBtn);
        contentArea.getChildren().clear();

        ViewRoomsView roomsView = new ViewRoomsView(hotelService, this::showRooms, this::showDashboard);
        contentArea.getChildren().add(roomsView.getView());
    }

    // ═══════════════════════════════════════════
    // BOOKINGS VIEW
    // ═══════════════════════════════════════════

    private void showBookings() {
        setActiveButton(bookingsBtn);
        contentArea.getChildren().clear();

        VBox bookingsView = new VBox(20);
        bookingsView.setAlignment(Pos.TOP_LEFT);

        Label heading = new Label("Bookings & Guests");
        heading.getStyleClass().add("view-heading");

        // Actions bar
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);

        Button newBookingBtn = new Button("📝  New Booking");
        newBookingBtn.getStyleClass().addAll("action-btn", "action-btn-success");
        newBookingBtn.setOnAction(e -> {
            BookRoomDialog dialog = new BookRoomDialog(hotelService);
            dialog.show();
            showBookings(); // Refresh
        });

        Button checkoutBtn = new Button("💰  Checkout");
        checkoutBtn.getStyleClass().addAll("action-btn", "action-btn-info");
        checkoutBtn.setOnAction(e -> {
            CheckoutDialog dialog = new CheckoutDialog(hotelService);
            dialog.show();
            showBookings(); // Refresh
        });

        actionsBar.getChildren().addAll(newBookingBtn, checkoutBtn);

        // Bookings table
        TableView<Customer> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getId()).asObject());
        idCol.setPrefWidth(60);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Customer, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getContact()));
        contactCol.setPrefWidth(150);

        TableColumn<Customer, Integer> roomCol = new TableColumn<>("Room No.");
        roomCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(
                        cd.getValue().getRoomNo() != null ? cd.getValue().getRoomNo() : 0).asObject());
        roomCol.setPrefWidth(100);

        TableColumn<Customer, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            {
                cancelBtn.getStyleClass().addAll("table-action-btn", "table-action-danger");
                cancelBtn.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer.getRoomNo() != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Cancel Booking");
                        confirm.setHeaderText("Cancel booking for " + customer.getName() + "?");
                        confirm.setContentText("Room " + customer.getRoomNo() + " will be released.");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                hotelService.cancelBooking(customer.getRoomNo());
                                showBookings();
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cancelBtn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, contactCol, roomCol, actionCol);

        List<Customer> customers = hotelService.getAllCustomers();
        table.getItems().addAll(customers);

        if (customers.isEmpty()) {
            table.setPlaceholder(new Label("No bookings found. Book a room to see guests here."));
        }

        VBox.setVgrow(table, Priority.ALWAYS);

        bookingsView.getChildren().addAll(heading, actionsBar, table);
        contentArea.getChildren().add(bookingsView);
    }

    // ═══════════════════════════════════════════
    // BILLING VIEW
    // ═══════════════════════════════════════════

    private void showBilling() {
        setActiveButton(billingBtn);
        contentArea.getChildren().clear();

        VBox billingView = new VBox(20);
        billingView.setAlignment(Pos.TOP_LEFT);

        // ── Page Heading ──
        Label heading = new Label("Billing & Invoices");
        heading.getStyleClass().add("view-heading");

        // ── Revenue Stats Row ──
        List<Bill> allBills = hotelService.getAllBills();
        double totalRevenue = allBills.stream().mapToDouble(Bill::getTotalAmount).sum();
        int totalBillCount = allBills.size();

        HBox revenueRow = new HBox(20);
        revenueRow.setAlignment(Pos.CENTER_LEFT);
        revenueRow.getChildren().addAll(
                createStatCard("Total Revenue", "₹ " + String.format("%.0f", totalRevenue), "stat-card-revenue"),
                createStatCard("Bills Generated", String.valueOf(totalBillCount), "stat-card-total"),
                createStatCard("Avg. Bill Value",
                        totalBillCount > 0
                                ? "₹ " + String.format("%.0f", totalRevenue / totalBillCount)
                                : "₹ 0",
                        "stat-card-available"),
                createStatCard("Tax Collected",
                        "₹ " + String.format("%.0f", allBills.stream().mapToDouble(Bill::getTaxAmount).sum()),
                        "stat-card-guests")
        );

        // ── Toolbar ──
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button checkoutBtn = new Button("💰  Checkout & Generate Bill");
        checkoutBtn.getStyleClass().addAll("action-btn", "action-btn-success");
        checkoutBtn.setOnAction(e -> {
            CheckoutDialog dialog = new CheckoutDialog(hotelService);
            dialog.show();
            showBilling(); // Refresh
        });

        Button viewInvoiceBtn = new Button("View Invoice");
        viewInvoiceBtn.getStyleClass().addAll("action-btn", "action-btn-info");

        Button resetBtn = new Button("🗑  Reset Revenue");
        resetBtn.getStyleClass().addAll("action-btn", "action-btn-danger");
        resetBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Reset Revenue");
            confirm.setHeaderText("Are you sure you want to reset all revenue data?");
            confirm.setContentText("This will permanently delete ALL billing history. This action cannot be undone.");

            DialogPane dialogPane = confirm.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/com/raahul/hms/styles.css").toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    hotelService.clearAllBills();
                    showBilling();
                }
            });
        });

        Region toolSpacer = new Region();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("🔍 Search bills...");
        searchField.setPrefWidth(220);

        toolbar.getChildren().addAll(checkoutBtn, viewInvoiceBtn, resetBtn, toolSpacer, searchField);

        // ── Bills Table ──
        TableView<Bill> billsTable = new TableView<>();
        billsTable.getStyleClass().add("data-table");
        billsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Bill, Integer> billIdCol = new TableColumn<>("Bill #");
        billIdCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getBillId()).asObject());
        billIdCol.setPrefWidth(70);

        TableColumn<Bill, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCustomerName()));
        customerCol.setPrefWidth(160);

        TableColumn<Bill, Integer> roomNoCol = new TableColumn<>("Room");
        roomNoCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getRoomNo()).asObject());
        roomNoCol.setPrefWidth(80);

        TableColumn<Bill, String> roomTypeCol = new TableColumn<>("Type");
        roomTypeCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomType()));
        roomTypeCol.setPrefWidth(100);

        TableColumn<Bill, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckInDate()));
        checkInCol.setPrefWidth(110);

        TableColumn<Bill, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckOutDate()));
        checkOutCol.setPrefWidth(110);

        TableColumn<Bill, Long> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(cd -> new SimpleLongProperty(cd.getValue().getNights()).asObject());
        nightsCol.setPrefWidth(70);

        TableColumn<Bill, Double> totalCol = new TableColumn<>("Total (₹)");
        totalCol.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getTotalAmount()).asObject());
        totalCol.setPrefWidth(120);
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? null : "₹ " + String.format("%.2f", total));
            }
        });

        billsTable.getColumns().addAll(billIdCol, customerCol, roomNoCol, roomTypeCol,
                checkInCol, checkOutCol, nightsCol, totalCol);

        billsTable.getItems().addAll(allBills);
        billsTable.setPlaceholder(new Label("No bills generated yet. Checkout a guest to create a bill."));

        // ── Search filtering ──
        searchField.textProperty().addListener((obs, old, val) -> {
            billsTable.getItems().clear();
            List<Bill> filtered = hotelService.getAllBills();
            if (val != null && !val.trim().isEmpty()) {
                String lower = val.toLowerCase().trim();
                filtered = filtered.stream().filter(b ->
                        b.getCustomerName().toLowerCase().contains(lower) ||
                                String.valueOf(b.getRoomNo()).contains(lower) ||
                                String.valueOf(b.getBillId()).contains(lower) ||
                                b.getRoomType().toLowerCase().contains(lower)
                ).collect(Collectors.toList());
            }
            billsTable.getItems().addAll(filtered);
        });

        // ── View Invoice button logic ──
        viewInvoiceBtn.setOnAction(e -> {
            Bill selectedBill = billsTable.getSelectionModel().getSelectedItem();
            if (selectedBill == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Selection");
                alert.setHeaderText("Please select a bill from the table first.");
                alert.showAndWait();
                return;
            }
            showInvoice(selectedBill);
        });

        VBox.setVgrow(billsTable, Priority.ALWAYS);

        billingView.getChildren().addAll(heading, revenueRow, toolbar, billsTable);
        contentArea.getChildren().add(billingView);
    }

    // ═══════════════════════════════════════════
    // SHARED HELPERS
    // ═══════════════════════════════════════════

    private VBox createStatCard(String title, String value, String styleClass) {
        VBox card = new VBox(5);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefWidth(140);
        card.setPrefHeight(75);
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Displays a detailed invoice dialog for a bill.
     */
    private void showInvoice(Bill bill) {
        Stage invoiceStage = new Stage();
        invoiceStage.initModality(Modality.APPLICATION_MODAL);
        invoiceStage.initStyle(StageStyle.UNDECORATED);

        VBox invoice = new VBox(12);
        invoice.getStyleClass().addAll("dialog-container", "invoice-container");
        invoice.setPadding(new Insets(30));
        invoice.setPrefWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label invoiceTitle = new Label("Invoice #" + bill.getBillId());
        invoiceTitle.getStyleClass().add("dialog-title");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.getStyleClass().add("dialog-close-btn");
        closeBtn.setOnAction(e -> invoiceStage.close());

        header.getChildren().addAll(invoiceTitle, sp, closeBtn);

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("invoice-separator");

        // Guest Details
        Label guestHeader = new Label("GUEST DETAILS");
        guestHeader.getStyleClass().add("invoice-section-header");
        HBox guestRow = createInvoiceRow("Guest Name", bill.getCustomerName());
        HBox contactRow = createInvoiceRow("Contact", bill.getContact());

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("invoice-separator");

        // Room Details
        Label roomHeader = new Label("ROOM DETAILS");
        roomHeader.getStyleClass().add("invoice-section-header");
        HBox roomRow = createInvoiceRow("Room Number", String.valueOf(bill.getRoomNo()));
        HBox typeRow = createInvoiceRow("Room Type", bill.getRoomType());
        HBox rateRow = createInvoiceRow("Rate / Night", "₹ " + String.format("%.2f", bill.getPricePerNight()));

        Separator sep3 = new Separator();
        sep3.getStyleClass().add("invoice-separator");

        // Stay Details
        Label stayHeader = new Label("STAY DETAILS");
        stayHeader.getStyleClass().add("invoice-section-header");
        HBox checkInRow = createInvoiceRow("Check-in", bill.getCheckInDate());
        HBox checkOutRow = createInvoiceRow("Check-out", bill.getCheckOutDate());
        HBox nightsRow = createInvoiceRow("Nights", String.valueOf(bill.getNights()));

        Separator sep4 = new Separator();
        sep4.getStyleClass().add("invoice-separator");

        // Charges
        Label chargesHeader = new Label("CHARGES");
        chargesHeader.getStyleClass().add("invoice-section-header");
        HBox roomChargeRow = createInvoiceRow("Room Charges", "₹ " + String.format("%.2f", bill.getRoomCharges()));
        HBox taxRow = createInvoiceRow("GST (18%)", "₹ " + String.format("%.2f", bill.getTaxAmount()));

        Separator sep5 = new Separator();
        sep5.getStyleClass().add("invoice-separator");

        // Total
        HBox totalRow = createInvoiceRow("TOTAL AMOUNT", "₹ " + String.format("%.2f", bill.getTotalAmount()));
        totalRow.getStyleClass().add("invoice-total-row");

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
}
