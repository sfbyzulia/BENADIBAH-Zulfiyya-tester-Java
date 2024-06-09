package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect: " + ticket.getOutTime());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        double duration = (outTimeMillis - inTimeMillis) / 3600000.0;

        if (duration <= 0.5) {
            ticket.setPrice(0);
            return;
        }

        // Initialize price calculation variable
        double price = 0;
        if (ticket.getParkingSpot() != null && ticket.getParkingSpot().getParkingType() != null) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR:
                    price = duration * Fare.CAR_RATE_PER_HOUR;
                    break;
                case BIKE:
                    price = duration * Fare.BIKE_RATE_PER_HOUR;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }

            // Apply discount if applicable
            if (discount) {
                price *= 0.95; // Apply 5% discount
            }
            
 // Round the price to 2 decimal places and format with EUR
 price = Math.round(price * 100) / 100.0;
 ticket.setPriceText(String.format("%.2f EUR", price)); // Set the formatted price with EUR
        } else {
            throw new IllegalArgumentException("Parking spot or type is null");
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}
