package com.raahul.hms.thread;

import java.util.function.Consumer;

import com.raahul.hms.model.Customer;
import com.raahul.hms.service.HotelService;

public class BookingThread extends Thread {

    private final HotelService service;
    private final Customer customer;
    private final int roomNo;
    private final Consumer<Boolean> callback;

    public BookingThread(HotelService service, Customer customer,
                         int roomNo, Consumer<Boolean> callback) {
        this.service = service;
        this.customer = customer;
        this.roomNo = roomNo;
        this.callback = callback;
        this.setDaemon(true);
        this.setName("BookingThread-Room" + roomNo);
    }

    @Override
    public void run() {
        boolean result = service.bookRoom(roomNo, customer);

        if (result) {
            System.out.println("[" + getName() + "] " + customer.getName()
                    + " successfully booked room " + roomNo);
        } else {
            System.out.println("[" + getName() + "] Booking failed for room " + roomNo);
        }

        if (callback != null) {
            callback.accept(result);
        }
    }
}
