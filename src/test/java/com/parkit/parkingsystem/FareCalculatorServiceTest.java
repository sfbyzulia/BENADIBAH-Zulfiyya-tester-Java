package com.parkit.parkingsystem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.service.FareCalculatorService;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        Date inTime = new Date(System.currentTimeMillis() - 61 * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.75, ticket.getPrice(), 0.05); // 0.75 +/- 0.05
    }

    @Test
    public void calculateFareCarWithDiscount() {
        Date inTime = new Date(System.currentTimeMillis() - 61 * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(0.71, ticket.getPrice(), 0.05); // 0.71 +/- 0.05
    }

    @Test
    public void calculateFareBike() {
        Date inTime = new Date(System.currentTimeMillis() - 61 * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(0.5, ticket.getPrice(), 0.05); // 0.5 +/- 0.05
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        Date inTime = new Date(System.currentTimeMillis() - 61 * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(0.47, ticket.getPrice(), 0.05); // 0.47 +/- 0.05
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 + 1) * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(35.25, ticket.getPrice(), 0.05); // 35.25 +/- 0.05
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTimeWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 + 1) * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(33.49, ticket.getPrice(), 0.05); // 33.49 +/- 0.05
    }

    @Test
    public void calculateFareBikeWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 + 1) * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, false);

        assertEquals(23.5, ticket.getPrice(), 0.05); // 23.5 +/- 0.05
    }

    @Test
    public void calculateFareBikeWithMoreThanADayParkingTimeWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 + 1) * 60 * 1000);
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(22.32, ticket.getPrice(), 0.05); // 22.32 +/- 0.05
    }
}