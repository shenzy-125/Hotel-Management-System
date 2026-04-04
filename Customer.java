package com.raahul.hms.model;

import java.io.Serializable;

/**
 * Represents a hotel customer with booking information.
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String contact;
    private Integer roomNo;

    public Customer(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.roomNo = null;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public Integer getRoomNo() { return roomNo; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
    public void setRoomNo(Integer roomNo) { this.roomNo = roomNo; }

    @Override
    public String toString() {
        return "Customer #" + id + " [" + name + "] - Contact: " + contact
                + (roomNo != null ? " (Room " + roomNo + ")" : " (No room)");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer customer = (Customer) obj;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
