package com.raahul.hms.model;

import java.io.Serializable;

/**
 * Represents a hotel room with its details and availability status.
 */
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private int roomNo;
    private String type;
    private double price;
    private boolean available;

    public Room(int roomNo, String type, double price) {
        this.roomNo = roomNo;
        this.type = type;
        this.price = price;
        this.available = true;
    }

    public int getRoomNo() { return roomNo; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setRoomNo(int roomNo) { this.roomNo = roomNo; }
    public void setType(String type) { this.type = type; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Room " + roomNo + " [" + type + "] - ₹" + String.format("%.2f", price)
                + " (" + (available ? "Available" : "Booked") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return roomNo == room.roomNo;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(roomNo);
    }
}
