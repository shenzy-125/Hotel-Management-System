package com.raahul.hms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a billing invoice generated during checkout.
 * Contains all charges, tax breakdown, and total amount.
 */
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double TAX_RATE = 0.18; 

    private int billId;
    private String customerName;
    private String contact;
    private int roomNo;
    private String roomType;
    private double pricePerNight;
    private String checkInDate;   // Stored as String for serialization compatibility
    private String checkOutDate;
    private long nights;
    private double roomCharges;
    private double taxAmount;
    private double totalAmount;

    /**
     * Creates a new bill with automatic charge calculations.
     */
    public Bill(int billId, String customerName, String contact,
                int roomNo, String roomType, double pricePerNight,
                LocalDate checkIn, LocalDate checkOut) {
        this.billId = billId;
        this.customerName = customerName;
        this.contact = contact;
        this.roomNo = roomNo;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.checkInDate = checkIn.toString();
        this.checkOutDate = checkOut.toString();

        this.nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (this.nights <= 0) this.nights = 1; 
        this.roomCharges = pricePerNight * nights;
        this.taxAmount = roomCharges * TAX_RATE;
        this.totalAmount = roomCharges + taxAmount;
    }

    public int getBillId() { return billId; }
    public String getCustomerName() { return customerName; }
    public String getContact() { return contact; }
    public int getRoomNo() { return roomNo; }
    public String getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public String getCheckInDate() { return checkInDate; }
    public String getCheckOutDate() { return checkOutDate; }
    public long getNights() { return nights; }
    public double getRoomCharges() { return roomCharges; }
    public double getTaxRate() { return TAX_RATE; }
    public double getTaxAmount() { return taxAmount; }
    public double getTotalAmount() { return totalAmount; }

    public void setBillId(int billId) { this.billId = billId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setContact(String contact) { this.contact = contact; }
    public void setRoomNo(int roomNo) { this.roomNo = roomNo; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

    @Override
    public String toString() {
        return "Bill #" + billId + " | " + customerName + " | Room " + roomNo
                + " | ₹" + String.format("%.2f", totalAmount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bill bill = (Bill) obj;
        return billId == bill.billId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(billId);
    }
}
