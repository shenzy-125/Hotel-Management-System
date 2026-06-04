package com.raahul.hms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.raahul.hms.model.Bill;
import com.raahul.hms.model.Customer;
import com.raahul.hms.model.Room;

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

    public synchronized boolean addRoom(Room room) {
        List<Room> rooms = roomStorage.loadAll();

        for (Room r : rooms) {
            if (r.getRoomNo() == room.getRoomNo()) {
                return false; 
            }
        }

        rooms.add(room);
        roomStorage.saveAll(rooms);
        return true;
    }

    public synchronized boolean bookRoom(int roomNo, Customer customer) {
        List<Room> rooms = roomStorage.loadAll();

        for (Room r : rooms) {
            if (r.getRoomNo() == roomNo && r.isAvailable()) {
                r.setAvailable(false);
                roomStorage.saveAll(rooms);

                customer.setRoomNo(roomNo);
                customerStorage.add(customer);

                return true;
            }
        }

        return false; 
    }

    public synchronized boolean cancelBooking(int roomNo) {
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

        List<Customer> customers = customerStorage.loadAll();
        customers.removeIf(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo);
        customerStorage.saveAll(customers);

        return true;
    }

    public List<Room> getAllRooms() {
        return roomStorage.loadAll();
    }

    public List<Room> getAvailableRooms() {
        return roomStorage.loadAll().stream()
                .filter(Room::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Room> getBookedRooms() {
        return roomStorage.loadAll().stream()
                .filter(r -> !r.isAvailable())
                .collect(Collectors.toList());
    }

    public List<Customer> getAllCustomers() {
        return customerStorage.loadAll();
    }

    public Customer getCustomerByRoom(int roomNo) {
        return customerStorage.loadAll().stream()
                .filter(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo)
                .findFirst()
                .orElse(null);
    }

    public Room getRoomByNumber(int roomNo) {
        return roomStorage.loadAll().stream()
                .filter(r -> r.getRoomNo() == roomNo)
                .findFirst()
                .orElse(null);
    }

    public int getTotalRooms() {
        return roomStorage.loadAll().size();
    }

    public int getAvailableCount() {
        return (int) roomStorage.loadAll().stream().filter(Room::isAvailable).count();
    }

    public int getBookedCount() {
        return (int) roomStorage.loadAll().stream().filter(r -> !r.isAvailable()).count();
    }

    public synchronized boolean removeRoom(int roomNo) {
        List<Room> rooms = roomStorage.loadAll();
        boolean removed = rooms.removeIf(r -> r.getRoomNo() == roomNo && r.isAvailable());
        if (removed) {
            roomStorage.saveAll(rooms);
        }
        return removed;
    }

    public synchronized Bill checkout(int roomNo, LocalDate checkIn, LocalDate checkOut) {
        Room room = getRoomByNumber(roomNo);
        if (room == null || room.isAvailable()) {
            return null; 
        }

        Customer customer = getCustomerByRoom(roomNo);
        if (customer == null) {
            return null;
        }

        int billId = getNextBillId();
        Bill bill = new Bill(billId, customer.getName(), customer.getContact(),
                roomNo, room.getType(), room.getPrice(), checkIn, checkOut);

        billStorage.add(bill);

        List<Room> rooms = roomStorage.loadAll();
        for (Room r : rooms) {
            if (r.getRoomNo() == roomNo) {
                r.setAvailable(true);
                break;
            }
        }
        roomStorage.saveAll(rooms);

        List<Customer> customers = customerStorage.loadAll();
        customers.removeIf(c -> c.getRoomNo() != null && c.getRoomNo() == roomNo);
        customerStorage.saveAll(customers);

        return bill;
    }

    public List<Bill> getAllBills() {
        return billStorage.loadAll();
    }

    public double getTotalRevenue() {
        return billStorage.loadAll().stream()
                .mapToDouble(Bill::getTotalAmount)
                .sum();
    }

    public synchronized void clearAllBills() {
        billStorage.clear();
    }

    private int getNextBillId() {
        List<Bill> bills = billStorage.loadAll();
        return bills.stream()
                .mapToInt(Bill::getBillId)
                .max()
                .orElse(0) + 1;
    }
}

