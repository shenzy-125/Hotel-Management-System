package com.raahul.hms.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.raahul.hms.model.Bill;
import com.raahul.hms.service.HotelService;
import com.raahul.hms.ui.CheckoutDialog;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXML Controller for the Billing view.
 * Loaded and initialized via FXMLLoader from MainView.
 * Uses @FXML annotated fields injected by Scene Builder / FXMLLoader.
 */
public class BillingController {

    @FXML private HBox revenueRow;
    @FXML private TextField searchField;
    @FXML private TableView<Bill> billsTable;
    @FXML private TableColumn<Bill, Integer> billIdCol;
    @FXML private TableColumn<Bill, String> customerCol;
    @FXML private TableColumn<Bill, Integer> roomCol;
    @FXML private TableColumn<Bill, String> roomTypeCol;
    @FXML private TableColumn<Bill, String> checkInCol;
    @FXML private TableColumn<Bill, String> checkOutCol;
    @FXML private TableColumn<Bill, Long> nightsCol;
    @FXML private TableColumn<Bill, Double> totalCol;

    private HotelService hotelService;

    /**
     * Initializes the controller with the HotelService dependency.
     */
    public void init(HotelService hotelService) {
        this.hotelService = hotelService;
        setupTableColumns();
        setupSearch();
        refreshView();
    }

    /**
     * Called by FXMLLoader after @FXML injection.
     */
    @FXML
    public void initialize() {
        // Dependencies set later via init()
    }

    /**
     * Configures table column cell value factories.
     */
    private void setupTableColumns() {
        billsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        billIdCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getBillId()).asObject());

        customerCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCustomerName()));

        roomCol.setCellValueFactory(cd ->
                new SimpleIntegerProperty(cd.getValue().getRoomNo()).asObject());

        roomTypeCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getRoomType()));

        checkInCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCheckInDate()));

        checkOutCol.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCheckOutDate()));

        nightsCol.setCellValueFactory(cd ->
                new SimpleLongProperty(cd.getValue().getNights()).asObject());

        totalCol.setCellValueFactory(cd ->
                new SimpleDoubleProperty(cd.getValue().getTotalAmount()).asObject());

        // Format total column with currency
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                setText(empty || total == null ? null : "₹ " + String.format("%.2f", total));
            }
        });

        billsTable.setPlaceholder(new Label("No bills generated yet. Checkout a guest to create a bill."));
    }

    /**
     * Sets up search field listener for filtering bills.
     */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> refreshTable(val));
    }

    /**
     * Refreshes revenue stats and bill table.
     */
    public void refreshView() {
        if (hotelService == null) return;

        // Revenue stats
        revenueRow.getChildren().clear();
        List<Bill> bills = hotelService.getAllBills();
        double totalRevenue = bills.stream().mapToDouble(Bill::getTotalAmount).sum();
        int totalBills = bills.size();

        revenueRow.getChildren().addAll(
                createStatCard("Total Revenue", "₹ " + String.format("%.0f", totalRevenue), "stat-card-revenue"),
                createStatCard("Bills Generated", String.valueOf(totalBills), "stat-card-total"),
                createStatCard("Avg. Bill Value",
                        totalBills > 0 ? "₹ " + String.format("%.0f", totalRevenue / totalBills) : "₹ 0",
                        "stat-card-available")
        );

        // Refresh table
        refreshTable(searchField != null ? searchField.getText() : null);
    }

    private void refreshTable(String searchText) {
        billsTable.getItems().clear();
        List<Bill> bills = hotelService.getAllBills();

        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase().trim();
            bills = bills.stream().filter(b ->
                    b.getCustomerName().toLowerCase().contains(lower) ||
                            String.valueOf(b.getRoomNo()).contains(lower) ||
                            String.valueOf(b.getBillId()).contains(lower) ||
                            b.getRoomType().toLowerCase().contains(lower)
            ).collect(Collectors.toList());
        }

        billsTable.getItems().addAll(bills);
    }

    private VBox createStatCard(String title, String value, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.setPrefWidth(200);
        card.setPrefHeight(110);
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    // ═══════════════════════════════════════════
    // FXML EVENT HANDLERS
    // ═══════════════════════════════════════════

    @FXML
    private void handleCheckout() {
        CheckoutDialog dialog = new CheckoutDialog(hotelService);
        dialog.show();
        refreshView();
    }

    @FXML
    private void handleViewInvoice() {
        Bill selectedBill = billsTable.getSelectionModel().getSelectedItem();
        if (selectedBill == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText("Please select a bill from the table first.");
            alert.showAndWait();
            return;
        }
        showInvoice(selectedBill);
    }

    @FXML
    private void handleResetRevenue() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Revenue");
        alert.setHeaderText("Are you sure you want to reset all revenue data?");
        alert.setContentText("This will permanently delete ALL billing history. This action cannot be undone.");

        // Apply dark theme styling to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/raahul/hms/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                hotelService.clearAllBills();
                refreshView();
            }
        });
    }

    /**
     * Displays a detailed invoice for a bill.
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

        // Guest
        Label guestHeader = new Label("GUEST DETAILS");
        guestHeader.getStyleClass().add("invoice-section-header");
        HBox guestRow = createRow("Guest Name", bill.getCustomerName());
        HBox contactRow = createRow("Contact", bill.getContact());

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("invoice-separator");

        // Room
        Label roomHeader = new Label("ROOM DETAILS");
        roomHeader.getStyleClass().add("invoice-section-header");
        HBox roomRow = createRow("Room Number", String.valueOf(bill.getRoomNo()));
        HBox typeRow = createRow("Room Type", bill.getRoomType());
        HBox rateRow = createRow("Rate / Night", "₹ " + String.format("%.2f", bill.getPricePerNight()));

        Separator sep3 = new Separator();
        sep3.getStyleClass().add("invoice-separator");

        // Stay
        Label stayHeader = new Label("STAY DETAILS");
        stayHeader.getStyleClass().add("invoice-section-header");
        HBox checkInRow = createRow("Check-in", bill.getCheckInDate());
        HBox checkOutRow = createRow("Check-out", bill.getCheckOutDate());
        HBox nightsRow = createRow("Nights", String.valueOf(bill.getNights()));

        Separator sep4 = new Separator();
        sep4.getStyleClass().add("invoice-separator");

        // Charges
        Label chargesHeader = new Label("CHARGES");
        chargesHeader.getStyleClass().add("invoice-section-header");
        HBox roomChargeRow = createRow("Room Charges", "₹ " + String.format("%.2f", bill.getRoomCharges()));
        HBox taxRow = createRow("GST (18%)", "₹ " + String.format("%.2f", bill.getTaxAmount()));

        Separator sep5 = new Separator();
        sep5.getStyleClass().add("invoice-separator");

        HBox totalRow = createRow("TOTAL AMOUNT", "₹ " + String.format("%.2f", bill.getTotalAmount()));
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

    private HBox createRow(String label, String value) {
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
