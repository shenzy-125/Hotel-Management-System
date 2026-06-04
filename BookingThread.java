package com.raahul.hms.thread;

import java.util.function.Consumer;

import com.raahul.hms.model.Customer;
import com.raahul.hms.service.HotelService;

/**
 * Thread for handling room bookings concurrently.
 */
public class BookingThread extends Thread {

    private final HotelService service;
    private final Customer customer;
    private final int roomNo;
    private final Consumer<Boolean> callback;

    /**
     * Creates a new booking thread.
     *
     * @param service  The hotel service instance
     * @param customer The customer making the booking
     * @param roomNo   The room number to book
     * @param callback Callback to execute with the result (true = success)
     */
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
