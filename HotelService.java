package com.raahul.hms.service;

import com.raahul.hms.model.Bill;
import com.raahul.hms.model.Customer;
import com.raahul.hms.model.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core business logic for hotel operations.
 * Uses file-based storage instead of in-memory collections.
 * All public methods are synchronized for thread safety.
 */
public class HotelService {

    private static final String DATA_DIR = "data";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.dat";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.dat";
    private static final String BILLS_FILE = DATA_DIR + "/bills.dat";

    private final FileStorageManager<Room> roomStorage;
    private final FileStorageManager<Customer> customerStorage;
    private final FileStorageManager<Bill> billStorage;

    public HotelService() {
        this.roomStorage = new FileStorageManager<>(ROOMS_FILE);
        this.customerStorage = new FileStorageManager<>(CUSTOMERS_FILE);
        this.billStorage = new FileStorageManager<>(BILLS_FILE);
    }

    // ═══════════════════════════════════════════
    // ROOM OPERATIONS
    // ═══════════════════════════════════════════

    /**
     * Adds a new room to the hotel.
     * @return true if added successfully, false if room number already exists
     */
    public synchronized boolean addRoom(Room room) {
        List<Room> rooms = roomStorage.loadAll();

        // Check for duplicate room number
        for (Room r : rooms) {
            if (r.getRoomNo() == room.getRoomNo()) {
                return false; // Duplicate
            }
        }

        rooms.add(room);
        roomStorage.saveAll(rooms);
        return true;
    }

    /**
     * Books a room for a customer.
     * @return true if booking was successful
     */
    public synchronized boolean bookRoom(int roomNo, Customer customer) {
        List<Room> rooms = roomStorage.loadAll();

        for (Room r : rooms) {
            if (r.getRoomNo() == roomNo && r.isAvailable()) {
                // Mark room as booked
                r.setAvailable(false);
                roomStorage.saveAll(rooms);

                // Save customer with room assignment
                customer.setRoomNo(roomNo);
                customerStorage.add(customer);

                return true;
            }
        }

        return false; // Room not found or not available
    }

    /**
     * Cancels a booking and releases the room.
     * @return true if cancellation was successful
     */
    public synchronized boolean cancelBooking(int roomNo) {
        // Release the room
        List<Room> rooms = roomStorage.loadAll();
        boolean roomFound = false;

        for (Room r : rooms) {
            if (r.getRoomNo() == roomNo && !r.isAvailable()) {
                r.setAvailable(true);
                roomFound = true;
                break;
            }
        }

        if (!roomFound) return false;

        roomStorage.saveAll(rooms);

        // Remove the customer booking
        List<Customer> customers = customerStorage.loadAll();
        customers.removeIf(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo);
        customerStorage.saveAll(customers);

        return true;
    }

    /**
     * Returns all rooms in the hotel.
     */
    public List<Room> getAllRooms() {
        return roomStorage.loadAll();
    }

    /**
     * Returns only available rooms.
     */
    public List<Room> getAvailableRooms() {
        return roomStorage.loadAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Returns only booked rooms.
     */
    public List<Room> getBookedRooms() {
        return roomStorage.loadAll().stream()
                .filter(r -> !r.isAvailable())
                .collect(Collectors.toList());
    }

    /**
     * Returns all customers with active bookings.
     */
    public List<Customer> getAllCustomers() {
        return customerStorage.loadAll();
    }

    /**
     * Gets the customer who booked a specific room.
     * @return the Customer or null if room is not booked
     */
    public Customer getCustomerByRoom(int roomNo) {
        return customerStorage.loadAll().stream()
                .filter(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a room by its number.
     * @return the Room or null if not found
     */
    public Room getRoomByNumber(int roomNo) {
        return roomStorage.loadAll().stream()
                .filter(r -> r.getRoomNo() == roomNo)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets summary statistics for the dashboard.
     */
    public int getTotalRooms() {
        return roomStorage.loadAll().size();
    }

    public int getAvailableCount() {
        return (int) roomStorage.loadAll().stream().filter(Room::isAvailable).count();
    }

    public int getBookedCount() {
        return (int) roomStorage.loadAll().stream().filter(r -> !r.isAvailable()).count();
    }

    /**
     * Removes a room (only if available/not booked).
     * @return true if removed
     */
    public synchronized boolean removeRoom(int roomNo) {
        List<Room> rooms = roomStorage.loadAll();
        boolean removed = rooms.removeIf(r -> r.getRoomNo() == roomNo && r.isAvailable());
        if (removed) {
            roomStorage.saveAll(rooms);
        }
        return removed;
    }

    // ═══════════════════════════════════════════
    // BILLING OPERATIONS
    // ═══════════════════════════════════════════

    /**
     * Performs checkout: generates a bill, releases the room, and removes the customer.
     *
     * @param roomNo   The room number to check out
     * @param checkIn  The check-in date
     * @param checkOut The check-out date
     * @return The generated Bill, or null if checkout failed
     */
    public synchronized Bill checkout(int roomNo, LocalDate checkIn, LocalDate checkOut) {
        // Find the room
        Room room = getRoomByNumber(roomNo);
        if (room == null || room.isAvailable()) {
            return null; // Room not booked
        }

        // Find the customer
        Customer customer = getCustomerByRoom(roomNo);
        if (customer == null) {
            return null;
        }

        // Generate bill
        int billId = getNextBillId();
        Bill bill = new Bill(billId, customer.getName(), customer.getContact(),
                roomNo, room.getType(), room.getPrice(), checkIn, checkOut);

        // Save bill
        billStorage.add(bill);

        // Release room
        List<Room> rooms = roomStorage.loadAll();
        for (Room r : rooms) {
            if (r.getRoomNo() == roomNo) {
                r.setAvailable(true);
                break;
            }
        }
        roomStorage.saveAll(rooms);

        // Remove customer
        List<Customer> customers = customerStorage.loadAll();
        customers.removeIf(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo);
        customerStorage.saveAll(customers);

        return bill;
    }

    /**
     * Returns all generated bills.
     */
    public List<Bill> getAllBills() {
        return billStorage.loadAll();
    }

    /**
     * Returns the total revenue from all bills.
     */
    public double getTotalRevenue() {
        return billStorage.loadAll().stream()
                .mapToDouble(Bill::getTotalAmount)
                .sum();
    }

    /**
     * Clears all generated bills.
     */
    public synchronized void clearAllBills() {
        billStorage.clear();
    }

    /**
     * Generates the next bill ID.
     */
    private int getNextBillId() {
        List<Bill> bills = billStorage.loadAll();
        return bills.stream()
                .mapToInt(Bill::getBillId)
                .max()
                .orElse(0) + 1;
    }
}

