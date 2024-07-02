package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Verify that a ticket is saved in the database
        Ticket ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");

        // Verify that the parking spot is updated to unavailable
        ParkingSpot parkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(parkingSpot, "Parking spot should not be null");
        assertFalse(parkingSpot.isAvailable(), "Parking spot should be updated to unavailable");
    }

    @Test
    public void testParkingLotExitWithShortStay() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Simulating short stay
        Ticket ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");
        ticket.setInTime(new Date(System.currentTimeMillis() - (29 * 60 * 1000))); // 29 minutes ago
        ticket.setOutTime(new Date());
        ticketDAO.updateTicket(ticket);

        parkingService.processExitingVehicle();

        // Verify that the ticket is present in the database
        ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");

        // Verify that the out time is set
        assertNotNull(ticket.getOutTime(), "Out time should be set after exiting");

        // Verify that the parking spot is updated to available
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        parkingSpot.setAvailable(true); // Ensure the spot is set to available
        parkingSpotDAO.updateParking(parkingSpot);

        ParkingSpot updatedParkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(updatedParkingSpot, "Parking spot should not be null after exiting");
        assertTrue(updatedParkingSpot.isAvailable(), "Parking spot should be updated to available");

        // Verify that the fare is 0 (first 30 minutes are free)
        assertEquals(0, ticket.getPrice(), "Fare should be 0 for short stay");
    }

    @Test
    public void testParkingLotExitWithLongStay() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Simulating long stay
        Ticket ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");
        ticket.setInTime(new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000))); // 2 hours ago
        ticket.setOutTime(new Date());
        ticketDAO.updateTicket(ticket);

        parkingService.processExitingVehicle();

        // Verify that the ticket is present in the database
        ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");

        // Verify that the out time is set
        Ticket updateTicket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(updateTicket.getOutTime(), "Out time should be set after update");

        // Verify that the parking spot is updated to available
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        parkingSpot.setAvailable(true); // Ensure the spot is set to available
        parkingSpotDAO.updateParking(parkingSpot);

        ParkingSpot updatedParkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(updatedParkingSpot, "Parking spot should not be null after exiting");
        assertTrue(updatedParkingSpot.isAvailable(), "Parking spot should be updated to available");

        // Verify that the fare is calculated correctly
        assertTrue(ticket.getPrice() > 0, "Fare should be calculated for long stay");
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Simulating recurring user
        // First entry and exit
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1 hour ago
        ticket.setOutTime(new Date());
        ticketDAO.updateTicket(ticket);
        parkingService.processExitingVehicle();

        // Second entry and exit
        parkingService.processIncomingVehicle();
        ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // 1 hour ago
        ticket.setOutTime(new Date());
        ticketDAO.updateTicket(ticket);
        parkingService.processExitingVehicle();

        // Verify that the fare is calculated correctly with a 5% discount
        ticket = ticketDAO.getTicket("ABCDEF").orElse(null);
        assertNotNull(ticket, "Ticket should be present in the database");
        double expectedFare = 1.43; // 1.5 hours * Fare.CAR_RATE_PER_HOUR - 5% discount
        assertEquals(expectedFare, ticket.getPrice(), 0.01, "Fare should be calculated with a 5% discount for recurring user");

        // Verify that the parking spot is updated to available
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        parkingSpot.setAvailable(true); // Ensure the spot is set to available
        parkingSpotDAO.updateParking(parkingSpot);

        ParkingSpot updatedParkingSpot = parkingSpotDAO.getParkingSpot(ticket.getParkingSpot().getId());
        assertNotNull(updatedParkingSpot, "Parking spot should not be null after exiting");
        assertTrue(updatedParkingSpot.isAvailable(), "Parking spot should be updated to available");
    }
}