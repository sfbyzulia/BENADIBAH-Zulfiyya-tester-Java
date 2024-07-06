package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FareCalculatorService {

    private static final Logger logger = LogManager.getLogger("FareCalculatorService");

    /**
     * Calculates the fare for a given ticket without applying any discounts.
     * The fare is calculated based on the duration the vehicle was parked.
     * If the parking duration is 30 minutes or less, the parking is free.
     *
     * @param ticket the ticket for which to calculate the fare.
     */
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    /**
     * Calculates the fare for a given ticket, optionally applying a discount.
     * The fare is calculated based on the type of vehicle (car or bike) and the duration of the stay.
     * The first 30 minutes are free, and a 5% discount is applied if the discount parameter is true.
     *
     * @param ticket the ticket for which to calculate the fare
     * @param discount true if a discount should be applied, false otherwise
     * @throws IllegalArgumentException if the out time of the ticket is before the in time or if it is null.
     */
    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            logger.error("Out time provided is incorrect: " + ticket.getOutTime());
            throw new IllegalArgumentException("Out time provided is incorrect: " + ticket.getOutTime());
        }

        long inTimeMillis = ticket.getInTime().getTime();
        long outTimeMillis = ticket.getOutTime().getTime();
        long durationInMinutes = (outTimeMillis - inTimeMillis) / 60000; // Convert milliseconds to minutes

        if (durationInMinutes <= 30) {
            ticket.setPrice(0); // First 30 minutes are free
            logger.info("Parking duration is less than or equal to 30 minutes. No charge.");
            return;
        }

        // Calculate price considering free 30 minutes
        long chargeableMinutes = durationInMinutes - 30;
        BigDecimal price = BigDecimal.ZERO;
        if (ticket.getParkingSpot() != null && ticket.getParkingSpot().getParkingType() != null) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR:
                    price = BigDecimal.valueOf(chargeableMinutes)
                            .multiply(BigDecimal.valueOf(Fare.CAR_RATE_PER_HOUR))
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    break;
                case BIKE:
                    price = BigDecimal.valueOf(chargeableMinutes)
                            .multiply(BigDecimal.valueOf(Fare.BIKE_RATE_PER_HOUR))
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    break;
                default:
                    logger.error("Unknown Parking Type");
                    throw new IllegalArgumentException("Unknown Parking Type");
            }

            if (discount) {
                price = price.multiply(BigDecimal.valueOf(0.95)); // Apply 5% discount
            }

            // Use BigDecimal for rounding to 2 decimal places
            price = price.setScale(2, RoundingMode.HALF_UP);
            ticket.setPrice(price.doubleValue());
            ticket.setPriceText(String.format("%.2f EUR", price.doubleValue())); // Set the formatted price with EUR
            logger.info("Calculated price: " + price.doubleValue());
        } else {
            logger.error("Parking spot or type is null");
            throw new IllegalArgumentException("Parking spot or type is null");
        }
    }
}
